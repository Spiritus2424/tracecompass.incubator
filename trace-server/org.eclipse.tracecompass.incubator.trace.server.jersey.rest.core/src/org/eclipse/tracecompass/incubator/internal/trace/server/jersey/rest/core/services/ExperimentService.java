package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.Activator;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.model.annotations.TraceAnnotationProvider;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

/**
 * Service to manage experiments.
 *
 * @author Ahmad Faour
 */
public class ExperimentService {

    private static final String EXPERIMENTS_FOLDER = "Experiments"; //$NON-NLS-1$
    private static final String TRACES_FOLDER = "Traces"; //$NON-NLS-1$

    private static ExperimentService experimentService;

    private final Map<UUID, IResource> resources;
    private final Map<UUID, List<UUID>> traceUuids;
    private final Map<UUID, TmfExperiment> experiments;
    private final Map<UUID, TraceAnnotationProvider> traceAnnotationProviders;

    private ExperimentService() {
        this.resources = Collections.synchronizedMap(this.initExperimentResources());
        this.traceUuids = Collections.synchronizedMap(new HashMap<>());
        this.experiments = Collections.synchronizedMap(new HashMap<>());
        this.traceAnnotationProviders = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * @return an instance of ExperimentService
     */
    public static ExperimentService getInstance() {
        if (experimentService == null) {
            experimentService = new ExperimentService();
        }

        return experimentService;
    }

    /**
     * Getter for the list of experiments from the trace manager
     *
     * @return The set of opened experiments
     */
    public List<Experiment> getExperiments() {
        synchronized (this.resources) {
            return Lists.transform(new ArrayList<>(this.resources.entrySet()), resourceEntry -> {
                UUID expUuid = resourceEntry.getKey();
                TmfExperiment tmfExperiment = this.experiments.get(expUuid);
                return (tmfExperiment != null) ? Experiment.from(tmfExperiment, expUuid) : Experiment.from(resourceEntry.getValue(), expUuid);
            });
        }
    }

    /**
     * Try and find an experiment with the queried UUID in the experiment
     * manager.
     *
     * @param expUuid
     *            queried {@link UUID}
     * @return the experiment or null if none match.
     */
    public synchronized @Nullable Experiment getExperiment(UUID expUuid) {
        TmfExperiment experiment = this.experiments.get(expUuid);
        if (experiment == null) {
            experiment = createExperimentInstance(expUuid);
        }

        return Experiment.from(experiment, expUuid);
    }

    /**
     * Gets the Eclipse resource from the experiment name.
     *
     * @param name
     *            the experiment name
     * @return The Eclipse resource
     *
     * @throws CoreException
     *             if an error occurs
     */
    public IFolder getExperimentResource(String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        IFolder experimentsFolder = project.getFolder(EXPERIMENTS_FOLDER);
        return experimentsFolder.getFolder(name);
    }

    /**
     * @param name of the experiment
     * @param traceResources of the experiment
     * @return an experiment
     * @throws InternalServerErrorException
     *              if an error occurs
     */
    public Experiment createExperiment(String name, List<IResource> traceResources) throws InternalServerErrorException {
        UUID expUUID = UUID.nameUUIDFromBytes(Objects.requireNonNull(name.getBytes(Charset.defaultCharset())));
        IFolder folderResource = null;

        try {
            folderResource = this.getExperimentResource(name);

        if (folderResource.exists()) {
            // An experiment with that name has already been created
            Multiset<IResource> oldTraceResources = HashMultiset.create(this.getTraceResources(folderResource));
            Multiset<IResource> newTraceResources = HashMultiset.create(traceResources);
            if (!oldTraceResources.equals(newTraceResources)) {
                // It's a different experiment, return a conflict
                TmfExperiment oldExperiment = new TmfExperiment(ITmfEvent.class, folderResource.getLocation().toOSString(), new ITmfTrace[0], TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, folderResource);
                Experiment entity = Experiment.from(oldExperiment, expUUID);
                oldExperiment.dispose();
                throw new ClientErrorException(Response.status(Status.CONFLICT).entity(entity).build());
            }
            // It's the same experiment, check if it is opened already
            TmfExperiment experiment = this.experiments.get(expUUID);
            if (experiment != null) {
                // It's already opened, return it
                return Experiment.from(experiment, expUUID);
            }
        } else {
            // It's not opened, continue below to instantiate it
            // It's a new experiment, create the experiment resources
            // create the experiment folder resource
            createFolder(folderResource);
            // add the traces
            for (IResource traceResource : traceResources) {
                addTrace(folderResource, traceResource);
            }
        }

        } catch (CoreException e) {
            throw new InternalServerErrorException(e.getMessage());
        }

        this.traceUuids.put(expUUID, traceResources.stream().map((traceResource) -> TraceService.getInstance().getTraceUUID(traceResource)).collect(Collectors.toList()));
        this.resources.put(expUUID, folderResource);

        TmfExperiment tmfExperiment = createExperimentInstance(expUUID);

        return Experiment.from(tmfExperiment, expUUID);
    }

    /**
     * @param experimentUuid of the experiment
     * @return deleted experiment
     * @throws NotFoundException if an error occurs
     */
    public Experiment deleteExperiment(UUID experimentUuid) throws NotFoundException {
        IResource resource = this.resources.remove(experimentUuid);
        if (resource == null) {
            throw new NotFoundException();
        }
        Experiment experimentModel = Experiment.from(resource, experimentUuid);
        TmfExperiment experiment = this.experiments.remove(experimentUuid);
        if (experiment != null) {
            TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, experiment));
            experiment.dispose();
        }

        this.traceAnnotationProviders.remove(experimentUuid);
        this.traceUuids.remove(experimentUuid);
        boolean deleteResources = true;
        synchronized (this.experiments) {
            for (TmfExperiment e : this.experiments.values()) {
                if (resource.equals(e.getResource())) {
                    deleteResources = false;
                    break;
                }
            }
        }
        if (deleteResources) {
            try {
                // Delete supplementary files and folders
                File supplFolder = new File(resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER));
                FileUtils.cleanDirectory(supplFolder);
                supplFolder.delete();
                // Delete experiment resource
                resource.delete(true, null);
                // Refresh the workspace
                resource.getProject().refreshLocal(Integer.MAX_VALUE, null);
            } catch (CoreException | IOException e) {
                Activator.getInstance().logError("Failed to delete experiment", e); //$NON-NLS-1$
            }
        }

        return experimentModel;
    }


    /**
     * @param folder resource
     * @return List of trace resources
     */
    public List<IResource> getTraceResources(IFolder folder) {
        final List<IResource> list = new ArrayList<>();
        final IFolder tracesFolder = folder.getProject().getFolder(TRACES_FOLDER);
        try {
            folder.accept(new IResourceProxyVisitor() {
                @Override
                public boolean visit(IResourceProxy resource) throws CoreException {
                    if (resource.getType() == IResource.FILE) {
                        IResource traceResourceUnderExperiment = resource.requestResource();
                        IPath relativePath = traceResourceUnderExperiment.getProjectRelativePath().makeRelativeTo(folder.getProjectRelativePath());
                        IResource traceResource = tracesFolder.findMember(relativePath);
                        if (traceResource != null) {
                            list.add(traceResource);
                        }
                        return false;
                    }
                    return true;
                }
            }, IResource.NONE);
        } catch (CoreException e) {
        }
        list.sort(Comparator.comparing(resource -> resource.getFullPath().toString()));
        return list;
    }

    /**
     * Get the list of trace UUIDs of an experiment from the experiment manager.
     *
     * @param expUUID
     *            queried {@link UUID}
     * @return the list of trace UUIDs.
     */
    public List<UUID> getTraces(UUID expUUID) {
        return this.traceUuids.getOrDefault(expUUID, Collections.emptyList());
    }

    /**
     * Returns true if the given trace is in use by any experiment
     *
     * @param uuid
     *            the trace UUID
     * @return true if the given trace is in use by any experiment
     */
    public boolean isTraceInUse(UUID uuid) {
        synchronized (this.traceUuids) {
            return this.traceUuids.values().stream().anyMatch(traceUUIDs -> traceUUIDs.contains(uuid));
        }
    }

    /**
     * Returns a {@link TraceAnnotationProvider} for a given experiment
     *
     * @param uuid
     *  the trace UUID
     * @return {@link TraceAnnotationProvider}
     */
    public TraceAnnotationProvider getTraceAnnotationProvider(UUID uuid) {
        return this.traceAnnotationProviders.get(uuid);
    }

    /**
     * Dispose method to be only called at server shutdown. It disposes experiments, traces etc.
     */
    public void dispose() {
        for (TmfExperiment experiment : this.experiments.values()) {
            if (experiment != null) {
                TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(experiment, experiment));
                // Experiment dispose() will dispose its traces as well.
                experiment.dispose();
            }
        }
        this.resources.clear();
        this.traceUuids.clear();
        this.experiments.clear();
        this.traceAnnotationProviders.clear();
    }

    private Map<UUID, IResource> initExperimentResources() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
        Map<UUID, IResource> initialExperimentResources = new HashMap<>();
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            IFolder experimentsFolder = project.getFolder(EXPERIMENTS_FOLDER);
            experimentsFolder.accept((IResourceVisitor) resource -> {
                boolean isAccepted = resource.equals(experimentsFolder);
                if (!isAccepted && resource instanceof IFolder) {
                    UUID expUUID = UUID.nameUUIDFromBytes(Objects.requireNonNull(resource.getName().getBytes(Charset.defaultCharset())));
                    initialExperimentResources.put(expUUID, resource);
                    List<UUID> traceUUIDs = getTraceUUIDs((IFolder) resource);
                    this.traceUuids.put(expUUID, traceUUIDs);
                }
                return isAccepted;
            }, IResource.DEPTH_ONE, IResource.NONE);
        } catch (CoreException e) {
        }
        return initialExperimentResources;
    }

    private static List<UUID> getTraceUUIDs(IFolder experimentResource) throws CoreException {
        List<UUID> traceUUIDs = new ArrayList<>();
        experimentResource.accept(resource -> {
            boolean isAccepted = true;
            if (resource instanceof IFile) {
                IPath path = resource.getProjectRelativePath().makeRelativeTo(experimentResource.getProjectRelativePath());
                IResource traceResource = experimentResource.getProject().getFolder(TRACES_FOLDER).findMember(path);
                if (traceResource != null) {
                    traceUUIDs.add(TraceService.getInstance().getTraceUUID(traceResource));
                }
                isAccepted = false;
            }
            return isAccepted;
        });
        return traceUUIDs;
    }

    private @Nullable TmfExperiment createExperimentInstance(UUID expUUID) throws InternalServerErrorException {
        List<UUID> traceUUIDs = this.traceUuids.get(expUUID);
        IResource resource = this.resources.get(expUUID);
        if (traceUUIDs == null || resource == null) {
            return null;
        }
        // Create and set the supplementary folder
        createSupplementaryFolder(resource);

        // Instantiate the experiment and return it
        ITmfTrace[] traces = Lists.transform(traceUUIDs, uuid -> TraceService.getInstance().createTraceInstance(uuid)).toArray(new ITmfTrace[0]);
        // Determine cache size for experiments
        int cacheSize = Integer.MAX_VALUE;
        for (ITmfTrace trace : traces) {
            cacheSize = Math.min(cacheSize, trace.getCacheSize());
        }
        TmfExperiment experiment = null;
        String experimentTypeId = null;


        try {
            experimentTypeId = getOrDetectExerimentType(resource, traces);
            experiment = TmfTraceType.instantiateExperiment(experimentTypeId);
        } catch (CoreException e) {
            Activator.getInstance().logWarning("Error instantiating experiment"); //$NON-NLS-1$
            throw new InternalServerErrorException("Failed to instantiate experiment"); //$NON-NLS-1$
        }
        if (experiment != null) {
            experiment.initExperiment(ITmfEvent.class, resource.getLocation().toOSString(), traces, cacheSize, resource, experimentTypeId);
            experiment.indexTrace(false);
            // read first event to make sure start time is initialized
            ITmfContext ctx = experiment.seekEvent(0);
            experiment.getNext(ctx);
            ctx.dispose();

            TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(ExperimentManagerService.class, experiment, null));

            this.experiments.put(expUUID, experiment);
            this.traceAnnotationProviders.put(expUUID, new TraceAnnotationProvider(experiment));
            return experiment;
        }

        return experiment;
    }

    /**
     * Get experiment type from experiment resource or auto-detect if it has not
     * been detected. It will fall-back to the default experiment if experiment
     * type can't be retrieved or detected.
     *
     * @param resource
     *            the experiment resource
     * @param traces
     *            array for traces for the experiment
     * @return experiment type ID
     * @throws CoreException
     *             in case of error handling Eclipse resource
     */
    private static @NonNull String getOrDetectExerimentType(IResource resource, ITmfTrace[] traces) throws CoreException {
        String experimentTypeId = TmfTraceType.getTraceTypeId(resource);
        if (experimentTypeId == null) {
            // Fall-back experiment type.
            experimentTypeId = TmfTraceType.DEFAULT_EXPERIMENT_TYPE;
            // Auto-detect experiment type
            List<TraceTypeHelper> helpers = TmfTraceType.selectExperimentType(Arrays.asList(traces), null);
            if (!helpers.isEmpty()) {
                experimentTypeId = helpers.get(0).getTraceTypeId();
            }
            resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, experimentTypeId);
        } else if (TmfTraceType.getTraceAttributes(experimentTypeId) == null) {
            // Plug-in for experiment type not available. Fall-back to default
            // experiment type.
            Activator.getInstance().logWarning("Extension for experiment type (" + experimentTypeId + ") not installed. Fall-back to generic experiment."); //$NON-NLS-1$ //$NON-NLS-2$
            experimentTypeId = TmfTraceType.DEFAULT_EXPERIMENT_TYPE;
        }
        return experimentTypeId;
    }

    private static void addTrace(IFolder folder, IResource traceResource) throws CoreException {
        /*
         * Create an empty file to represent the experiment trace. The file's element
         * path relative to the experiment resource corresponds to the trace's element
         * path relative to the Traces folder.
         */
        IPath relativePath = traceResource.getProjectRelativePath().removeFirstSegments(1);
        IFile file = folder.getFile(relativePath);
        createFolder((IFolder) file.getParent());
        file.create(new ByteArrayInputStream(new byte[0]), false, new NullProgressMonitor());
        file.setPersistentProperty(TmfCommonConstants.TRACETYPE, TmfTraceType.getTraceTypeId(traceResource));
    }

    private static void createSupplementaryFolder(IResource experimentResource) {
        try {
            final String SUFFIX = "_exp"; //$NON-NLS-1$
            IFolder supplRootFolder = experimentResource.getProject().getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
            IFolder supplFolder = supplRootFolder.getFolder(experimentResource.getName() + SUFFIX);
            createFolder(supplFolder);
            experimentResource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocation().toOSString());
        } catch (CoreException e) {
        }
    }

    private static void createFolder(IFolder folder) throws CoreException {
        if (!folder.exists()) {
            if (folder.getParent() instanceof IFolder) {
                createFolder((IFolder) folder.getParent());
            }
            folder.create(true, true, null);
        }
    }

}

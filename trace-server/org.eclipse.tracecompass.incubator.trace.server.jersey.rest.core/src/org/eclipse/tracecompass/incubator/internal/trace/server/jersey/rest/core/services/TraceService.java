/*******************************************************************************
 * Copyright (c) 2017, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NOT_SUPPORTED;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TRACE_CREATION_FAILED;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.Activator;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Service to manage traces.
 *
 * @author Ahmad Faour
 */
public class TraceService {
    private static final String TRACES_FOLDER = "Traces"; //$NON-NLS-1$
    private static TraceService traceService;

    private final @NonNull Logger logger;

    private final Map<UUID, IResource> resources;



    private TraceService() {
        this.logger = TraceCompassLog.getLogger(TraceService.class);
        this.resources = Collections.synchronizedMap(initTraces());
    }


    public static TraceService getInstance() {
        if (traceService == null) {
            traceService = new TraceService();
        }

        return traceService;
    }

    public List<Trace> getTraces() {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#getTraces").build() ) { //$NON-NLS-1$
            List<Trace> traces = null;
            synchronized (this.resources) {
                traces = this.resources.keySet().stream()
                        .map((UUID uuid) -> createTraceModel(uuid))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            return traces;
        }
    }


    public Trace getTrace(@NotNull UUID uuid) throws NotFoundException {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#getTrace").build() ) { //$NON-NLS-1$
            Trace trace = createTraceModel(uuid);
            if (trace == null) {
                throw new NotFoundException(NO_SUCH_TRACE);
            }
            return trace;
        }
    }


    /**
     * @param name of the trace
     * @param uri path of the trace
     * @param typeID of the trace
     * @param maxDepth max depth
     * @param regexFilter filter the paths
     * @return List of Trace
     */
    public List<Trace> openTraces(String uri, String name, String typeID, int maxDepth ,Optional<String> regexFilter) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#openTraces").build() ) { //$NON-NLS-1$
            List<String> paths;
            try(Stream<java.nio.file.Path> stream = Files.walk(Paths.get(uri), maxDepth)) {
                paths = stream.parallel()
                        .filter((Path path) -> Files.isDirectory(path))
                        .map(path -> path.toString())
                        .collect(Collectors.toList());
            } catch (IOException e) {
                paths = new ArrayList<>();
            }

            if (regexFilter.isPresent()) {
                paths.removeIf(Pattern.compile(regexFilter.get()).asPredicate().negate());
            }

            List<Trace> traces = new ArrayList<>();
            for(String path : paths) {
                String traceName = name != null ? String.format("%s%s", name,Paths.get(path).getFileName().toString()) : null; //$NON-NLS-1$
                try {
                    traces.add(this.openTrace(path, traceName, typeID));
                } catch (Exception e) { }
            }


            return traces;
        }
    }


    public Trace openTrace(String path, String name, String typeID) throws TmfTraceImportException, CoreException, IllegalArgumentException, SecurityException, NotFoundException, InternalServerErrorException, ClientErrorException {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#openTrace").build() ) { //$NON-NLS-1$
            if (!Paths.get(path).toFile().exists()) {
                throw new NotFoundException(String.format("%s at %s", NO_SUCH_TRACE, path)); //$NON-NLS-1$
            }
            List<TraceTypeHelper> traceTypes = null;

            try (FlowScopeLog scopeSelectTraceType = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#openTrace:selectTraceType").build() ) { //$NON-NLS-1$
             traceTypes = TmfTraceType.selectTraceType(path, typeID);
            }

            if (traceTypes.isEmpty()) {
                throw new ClientErrorException(NOT_SUPPORTED, Status.NOT_IMPLEMENTED);
            }
            String traceType = traceTypes.get(0).getTraceTypeId();
            String traceName = name == null ? Paths.get(path).getFileName().toString() : name;

            IResource resource = getResource(path, traceName);
            if (!resource.exists()) {
                if (!createResource(path, resource)) {
                    throw new InternalServerErrorException(TRACE_CREATION_FAILED);
                }
                resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
            } else if(resource.exists()) {
                IPath targetLocation = getTargetLocation(path);
                IPath oldLocation = ResourceUtil.getLocation(resource);
                if (oldLocation == null || !targetLocation.equals(oldLocation.removeTrailingSeparator()) ||
                        !traceType.equals(resource.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                    synchronized (this.resources) {
                        Optional<@NonNull Entry<UUID, IResource>> oldEntry = this.resources.entrySet().stream()
                                .filter(entry -> resource.equals(entry.getValue()))
                                .findFirst();
                        if(oldEntry.isPresent()) {
                            UUID oldUUID = oldEntry.get().getKey();
                            throw new ClientErrorException(Response.status(Status.CONFLICT).entity(createTraceModel(oldUUID)).build());
                        }
                        throw new InternalServerErrorException("Failed to find conflicting trace"); //$NON-NLS-1$
                    }
                }
            }
            UUID uuid = getTraceUUID(resource);
            this.resources.put(uuid, resource);
            return createTraceModel(uuid);
        }

    }

    public Trace deleteTrace(@NotNull UUID uuid) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#deleteTrace").build() ) { //$NON-NLS-1$
            Trace trace = createTraceModel(uuid);
            if (trace == null) {
                throw new NotFoundException(NO_SUCH_TRACE);
            }
            if (ExperimentService.getInstance().isTraceInUse(uuid)) {
                throw new ClientErrorException(Response.status(Status.CONFLICT).entity(trace).build());
            }
            IResource resource = this.resources.remove(uuid);
            if (resource != null) {
                try {
                    // Delete supplementary files and folders
                    File supplFolder = new File(resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER));
                    FileUtils.cleanDirectory(supplFolder);
                    cleanupFolders(supplFolder,
                            resource.getProject().getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME).getLocation().toFile());
                    // Delete trace resource
                    resource.delete(IResource.FORCE, null);
                    cleanupFolders(resource.getParent().getLocation().toFile(),
                            resource.getProject().getFolder(TRACES_FOLDER).getLocation().toFile());
                    // Refresh the workspace
                    resource.getProject().refreshLocal(Integer.MAX_VALUE, null);
                } catch (CoreException | IOException e) {
                    Activator.getInstance().logError("Failed to delete trace", e); //$NON-NLS-1$
                }
            }
            return trace;
        }
    }

    /**
     * Get the UUID of a trace by its resource
     *
     * @param resource
     *            the trace resource
     * @return the trace UUID
     */
    public UUID getTraceUUID(IResource resource) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#getTraceUUID").build() ) { //$NON-NLS-1$
            IPath location = ResourceUtil.getLocation(resource);
            IPath path = location != null ? location.append(resource.getName()) : resource.getProjectRelativePath();
            UUID uuid = UUID.nameUUIDFromBytes(Objects.requireNonNull(path.toString().getBytes(Charset.defaultCharset())));
            return uuid;
        }
    }

    /**
     * Get the resource of a trace by its UUID.
     * @param uuid
     *            the trace UUID
     * @return the trace resource, or null if it could not be found
     */
    public @Nullable IResource getTraceResource(UUID uuid) {
        return this.resources.get(uuid);
    }

    /**
     * Create an instance of a trace by its UUID. The caller is responsible to
     * dispose the instance when it is no longer needed.
     *
     * @param uuid
     *            the trace UUID
     * @return the trace instance, or null if it could not be created
     */
    public @Nullable ITmfTrace createTraceInstance(UUID uuid) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#createTraceInstance").build() ) { //$NON-NLS-1$
            try {
                IResource resource = this.resources.get(uuid);
                if (resource == null) {
                    return null;
                }
                String typeID = TmfTraceType.getTraceTypeId(resource);
                if (typeID == null) {
                    return null;
                }
                ITmfTrace trace = TmfTraceType.instantiateTrace(typeID);
                if (trace != null) {
                    String path = Objects.requireNonNull(ResourceUtil.getLocation(resource)).removeTrailingSeparator().toOSString();
                    String name = resource.getName();
                    trace.initTrace(resource, path, ITmfEvent.class, name, typeID);
                    trace.indexTrace(false);
                    // read first event to make sure start time is initialized
                    ITmfContext ctx = trace.seekEvent(0);
                    trace.getNext(ctx);
                    ctx.dispose();
                }
                return trace;
            } catch (CoreException | TmfTraceException e) {
                Activator.getInstance().logError("Failed to create trace instance for " + uuid, e); //$NON-NLS-1$
                return null;
            }
        }
    }



    /**
     * Dispose method to be only called at server shutdown.
     */
    public void dispose() {
        this.resources.clear();
    }


    private Trace createTraceModel(UUID uuid) {
        IResource resource = this.resources.get(uuid);
        return (resource != null) ? Trace.from(resource, uuid) : null;
    }

    private Map<UUID, IResource> initTraces() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
        Map<UUID, IResource> initialResources = new HashMap<>();
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            IFolder tracesFolder = project.getFolder(TRACES_FOLDER);
            tracesFolder.accept(resource -> {
                boolean isSymbolcLink = ResourceUtil.isSymbolicLink(resource);
                if (isSymbolcLink) {
                    initialResources.put(getTraceUUID(resource), resource);
                }
                return isSymbolcLink;
            });
        } catch (CoreException e) {
        }
        return initialResources;
    }

    /**
     * Create the Eclipse resource from the target location and prepare the
     * supplementary directory for this trace.
     *
     * @param path
     *            the absolute path string to the trace
     * @param name
     *            the trace name
     * @return true if creation was successful
     *
     * @throws CoreException
     *             if an error occurs
     */
    private synchronized boolean createResource(String path, IResource resource) throws CoreException {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#createResource").build() ) { //$NON-NLS-1$
            // create the resource hierarchy.
            IPath targetLocation = new org.eclipse.core.runtime.Path(path);
            createFolder((IFolder) resource.getParent(), null);
            if (!ResourceUtil.createSymbolicLink(resource, targetLocation, true, null)) {
                return false;
            }

            // create supplementary folder on file system:
            IFolder supplRootFolder = resource.getProject().getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
            IFolder supplFolder = supplRootFolder.getFolder(resource.getProjectRelativePath().removeFirstSegments(1));
            createFolder(supplFolder, null);
            resource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocation().toOSString());

            return true;
        }
    }

    private void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TraceService#createFolder").build() ) { //$NON-NLS-1$
            // Taken from: org.eclipse.tracecompass.tmf.ui.project.model.TraceUtil.java
            // TODO: have a tmf.core util for that.
            if (!folder.exists()) {
                if (folder.getParent() instanceof IFolder) {
                    createFolder((IFolder) folder.getParent(), monitor);
                }
                folder.create(true, true, monitor);
            }
        }
    }

    private static synchronized void cleanupFolders(File folder, File root) {
        File current = folder;
        while (current.isDirectory() && !current.equals(root)) {
            File[] listFiles = current.listFiles();
            if (listFiles == null || listFiles.length != 0) {
                break;
            }
            current.delete();
            current = current.getParentFile();
        }
    }

    /**
     * Gets the Eclipse resource from the path and prepares the supplementary
     * directory for this trace.
     *
     * @param path
     *            the absolute path string to the trace
     * @param name
     *            the trace name
     * @return The Eclipse resources
     *
     * @throws CoreException
     *             if an error occurs
     */
    private static IResource getResource(String path, String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        IFolder tracesFolder = project.getFolder(TRACES_FOLDER);
        IPath targetLocation = getTargetLocation(path);
        IPath resourcePath = targetLocation.removeLastSegments(1).append(name);

        IResource resource = null;
        // create the resource hierarchy.
        if (new File(path).isFile()) {
            resource = tracesFolder.getFile(resourcePath);
        } else {
            resource = tracesFolder.getFolder(resourcePath);
        }
        return resource;
    }

    /**
     * Get the location in the workspace that will represent this path on disk.
     *
     * @param path
     *            The full path to the trace on disk
     * @return The path in the workspace where the trace is represented
     */
    private static IPath getTargetLocation(String path) {
        IPath p = new org.eclipse.core.runtime.Path(path);
        if (p.getDevice() != null) {
            // We need to make a path that is a valid file/folder location within the Eclipse
            // workspace, this means if there is a device involved we need to drop the :
            // or else later we'll fail to make the path
            p = new org.eclipse.core.runtime.Path(p.toString().replace(":", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return p.removeTrailingSeparator();
    }

}

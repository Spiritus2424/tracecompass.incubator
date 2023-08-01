package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;


import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider.CriticalPathDataProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

@SuppressWarnings({"restriction", "javadoc"})
public class ActionService {
    private static ActionService actionService;


    private final DataProviderManager dataProviderManager;

    private ActionService() {
        this.dataProviderManager = DataProviderManager.getInstance();
    }

//TmfThreadSelectedSignal

    public static ActionService getInstance() {
        if (actionService == null) {
            actionService = new ActionService();
        }

        return actionService;
    }

//    public void getAction(UUID experimentUuid, String outputId, UUID actionUuid) {
//        TmfExperiment tmfExperiement = this.experimentService.getTmfExperiment(experimentUuid);
////        this.dataProviderManager.getOrCreateDataProvider(tmfExperiement, outputId, OS);
//    }

    public void getActions() {

    }

    public void createAction(@NonNull TmfExperiment tmfExperiment, @NonNull String outputId, int entryId) {
        ThreadStatusDataProvider threadStatusDataProvider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment, outputId, ThreadStatusDataProvider.class);
        if (threadStatusDataProvider != null) {
            int threadId = threadStatusDataProvider.findThreadId(entryId);
            TmfSignalManager.dispatchSignal(new TmfThreadSelectedSignal(this, new HostThread(tmfExperiment.getHostId(), threadId)));
            CriticalPathDataProvider criticalPathDataProvider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment, CriticalPathDataProvider.ID, CriticalPathDataProvider.class);

        }
    }

    public void deleteAction() {

    }


    public void testCriticalPath(@NonNull TmfExperiment tmfExperiment, int entryId) {
        TmfModelResponse<?> modelResponse = null;
        List<ITmfTrace> tracesForHost = tmfExperiment.getTraces();

        for (ITmfTrace tmfTrace : tracesForHost) {
            ThreadStatusDataProvider threadStatusDataProvider = this.dataProviderManager.getOrCreateDataProvider(tmfTrace, ThreadStatusDataProvider.ID, ThreadStatusDataProvider.class);
            if (threadStatusDataProvider != null) {
                Integer threadId = threadStatusDataProvider.findThreadId(entryId);
                if (threadId != null) {
                    TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, tmfExperiment));
                    TmfSignalManager.dispatchSignal(new TmfThreadSelectedSignal(this, new HostThread(tmfTrace.getHostId(), threadId)));
                }
            }
        }
    }



}

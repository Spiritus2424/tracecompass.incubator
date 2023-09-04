package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;



import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;
import org.eclipse.tracecompass.analysis.graph.core.graph.ITmfVertex;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraph;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.OsHistoryTreeGraph;
import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.TmfVertex;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerExecutionGraph;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;



public class GraphService {

    private static GraphService graphService;

    private GraphService() { }

    @SuppressWarnings("null")
    public static @NonNull GraphService getInstance() {
        if (graphService == null) {
            graphService = new GraphService();
        }
        return graphService;
    }


    public @Nullable IGraphWorker getWorker(TmfExperiment tmfExperiment, Integer workerId) {
        IGraphWorker graphWorker = null;
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
                OsHistoryTreeGraph tmfGraph = (OsHistoryTreeGraph) osExecutionGraph.getTmfGraph();
                if (tmfGraph != null) {
                    graphWorker = tmfGraph.getWorker(workerId);
                }
            }
        }

        return graphWorker;
    }


    public Map<ITmfVertex, IEventMatchingKey> getVertexIndexes(TmfExperiment tmfExperiment, Direction direction) {
        Map<ITmfVertex, IEventMatchingKey> indexes = null;
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
//                OsHistoryTreeGraph tmfGraph = (OsHistoryTreeGraph) osExecutionGraph.getTmfGraph();
                OsExecutionGraphProvider provider = osExecutionGraph.getOsExecutionGraphProvider();
                for (ITraceEventHandler traceEventHandler: provider.getHandlers()) {
                    if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                        TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                        indexes = traceEventHandlerExecutionGraph.getUnmatchedTmfVertex(direction == null ? Optional.empty() : Optional.of(direction));
                        break;
                    }
                }
            }
        }
        return indexes;
    }

    public IEventMatchingKey getEventKey(TmfExperiment tmfExperiment, TmfVertex tmfVertex, Direction direction) {
        IEventMatchingKey eventKey = null;
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
//                OsHistoryTreeGraph tmfGraph = (OsHistoryTreeGraph) osExecutionGraph.getTmfGraph();
                OsExecutionGraphProvider provider = osExecutionGraph.getOsExecutionGraphProvider();
                for (ITraceEventHandler traceEventHandler: provider.getHandlers()) {
                    if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                        TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                        eventKey = traceEventHandlerExecutionGraph.getUnmatchedTmfVertex(direction == null ? Optional.empty() : Optional.of(direction)).get(tmfVertex);
                        break;
                    }
                }
            }
        }

        return eventKey;
    }

    public List<@NonNull ITmfVertex> getUnmatechedTmfVertex(TmfExperiment tmfExperiment, TimeRange timeRange, Direction direction) {
        List<@NonNull ITmfVertex> tmfVertexes = null;

        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
//                OsHistoryTreeGraph tmfGraph = (OsHistoryTreeGraph) osExecutionGraph.getTmfGraph();
                OsExecutionGraphProvider provider = osExecutionGraph.getOsExecutionGraphProvider();
                for (ITraceEventHandler traceEventHandler: provider.getHandlers()) {
                    if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                        TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                        tmfVertexes = traceEventHandlerExecutionGraph.findUnmatchedTmfVertex(timeRange.start, timeRange.end, direction == null ? Optional.empty() : Optional.of(direction));
                        break;
                    }
                }
            }
        }

        return tmfVertexes;
    }

    public void test1(TmfExperiment tmfExperiment) {
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);

        System.out.println(tmfExperiment.getHostId());
        tmfExperiment.getTraces().forEach(trace -> System.out.println(trace.getHostId()));
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
                osExecutionGraph.getCriticalPathGraph();
            }
        }



    }



//    public void test2(TmfExperiment tmfExperiment) {
//        @NonNull OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
//        osExecutionGraph.schedule();
//        if (osExecutionGraph.waitForCompletion()) {
//            OsExecutionGraphProvider provider = osExecutionGraph.getOsExecutionGraphProvider();
//            provider.getHandlers().forEach(traceEventHandler -> {
//                if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
//                    TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
//
//
//                    traceEventHandlerExecutionGraph.getUnmatchedTmfVertex().forEach((tmfVertex, eventKey) -> {
//                        System.out.print(tmfVertex + " "); //$NON-NLS-1$
//                        System.out.println(eventKey);
//                    });
//
//
//
////                    TmfEventMatching tcpEventMatching = traceEventHandlerExecutionGraph.getTcpMatching();
////                    System.out.println(tcpEventMatching);
//
//                }
//            });
//        }
//    }
}

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathAlgorithmException;
import org.eclipse.tracecompass.analysis.graph.core.graph.ITmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.graph.ITmfVertex;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraph;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath.OSCriticalPathAlgorithm;
import org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider.OsCriticalPathVisitor;
import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.OsHistoryTreeGraph;
import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.TmfVertex;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerExecutionGraph;
import org.eclipse.tracecompass.internal.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;


@SuppressWarnings({ "restriction", "javadoc", "null" })
public class GraphService {

    private static GraphService graphService;

    private GraphService() { }

    public static @NonNull GraphService getInstance() {
        if (graphService == null) {
            graphService = new GraphService();
        }
        return graphService;
    }

    public @Nullable IGraphWorker getWorker(TmfExperiment tmfExperiment, Integer workerId) {
        IGraphWorker graphWorker = null;
        OsHistoryTreeGraph osHistoryTreeGraph = (OsHistoryTreeGraph) getTmfGraph(tmfExperiment);
        if (osHistoryTreeGraph != null) {
            graphWorker = osHistoryTreeGraph.getWorker(workerId);
        }

        return graphWorker;
    }

    public @Nullable ITmfVertex getTmfVertex(TmfExperiment tmfExperiment, Integer workerId, long timestamp) {
        ITmfVertex tmfVertex = null;
        OsHistoryTreeGraph osHistoryTreeGraph = (OsHistoryTreeGraph) getTmfGraph(tmfExperiment);
        if (osHistoryTreeGraph != null) {
            IGraphWorker graphWorker = osHistoryTreeGraph.getWorker(workerId);
            tmfVertex = osHistoryTreeGraph.getVertexAt(new TmfNanoTimestamp(timestamp), graphWorker);
        }

        return tmfVertex;
    }

    // public @Nullable IGraphWorker getPreviousWorker(TmfExperiment tmfExperiment, Integer workerId, long timestamp) {
    //     IGraphWorker previousGraphWorker = null;
    //     OsHistoryTreeGraph osHistoryTreeGraph = (OsHistoryTreeGraph) getTmfGraph(tmfExperiment);
    //     if (osHistoryTreeGraph != null) {
    //         IGraphWorker graphWorker = osHistoryTreeGraph.getWorker(workerId);
    //         ITmfVertex tmfVertex = osHistoryTreeGraph.getVertexAt(new TmfNanoTimestamp(timestamp), graphWorker);
    //         previousGraphWorker = osHistoryTreeGraph.getPreviousWorker(tmfVertex);
    //     }

    //     return previousGraphWorker;
    // }

    public Map<ITmfVertex, IEventMatchingKey> getVertexIndexes(TmfExperiment tmfExperiment, Direction direction) {
        Map<ITmfVertex, IEventMatchingKey> indexes = null;

        OsExecutionGraphProvider osExecutionGraphProvider = getOsExecutionGraphProvider(tmfExperiment);

        if (osExecutionGraphProvider != null) {
            for (ITraceEventHandler traceEventHandler: osExecutionGraphProvider.getHandlers()) {
                if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                    TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                    indexes = traceEventHandlerExecutionGraph.getUnmatchedTmfVertex(direction == null ? Optional.empty() : Optional.of(direction));
                    break;
                }
            }
        }

        return indexes;
    }

    public List<@NonNull ITmfVertex> getUnmatchedTmfVertex(TmfExperiment tmfExperiment, TimeRange timeRange, Direction direction) {
        List<@NonNull ITmfVertex> tmfVertexes = null;
        OsExecutionGraphProvider osExecutionGraphProvider = getOsExecutionGraphProvider(tmfExperiment);
        if (osExecutionGraphProvider != null) {
            for (ITraceEventHandler traceEventHandler: osExecutionGraphProvider.getHandlers()) {
                if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                    TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                    tmfVertexes = traceEventHandlerExecutionGraph.findUnmatchedTmfVertex(timeRange.start, timeRange.end, direction == null ? Optional.empty() : Optional.of(direction));
                    break;
                }
            }

        }

        return tmfVertexes;
    }

//    public void getEventMatchingTmfVertex(TmfExperiment tmfExperiment, long start, long end, Direction direction) {
//        List<@NonNull ITmfVertex> tmfVertexes = null;
//
//        OsExecutionGraphProvider osExecutionGraphProvider = getOsExecutionGraphProvider(tmfExperiment);
//
//        if (osExecutionGraphProvider != null) {
//            for (ITraceEventHandler traceEventHandler: osExecutionGraphProvider.getHandlers()) {
//                if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
//                    TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
//                    tmfVertexes = traceEventHandlerExecutionGraph.findUnmatchedTmfVertex(start, end, direction == null ? Optional.empty() : Optional.of(direction));
//                    break;
//                }
//            }
//
//        }
//
//        return tmfVertexes;
//    }


    public IEventMatchingKey getEventKey(TmfExperiment tmfExperiment, TmfVertex tmfVertex, Direction direction) {
        IEventMatchingKey eventKey = null;
        OsExecutionGraphProvider osExecutionGraphProvider = getOsExecutionGraphProvider(tmfExperiment);

        if (osExecutionGraphProvider != null) {
            for (ITraceEventHandler traceEventHandler: osExecutionGraphProvider.getHandlers()) {
                if (traceEventHandler instanceof TraceEventHandlerExecutionGraph) {
                    TraceEventHandlerExecutionGraph traceEventHandlerExecutionGraph = (TraceEventHandlerExecutionGraph) traceEventHandler;
                    eventKey = traceEventHandlerExecutionGraph.getUnmatchedTmfVertex(direction == null ? Optional.empty() : Optional.of(direction)).get(tmfVertex);
                    break;
                }
            }
        }

        return eventKey;
    }


    // public ITmfVertex getPreviousTmfVertex(TmfExperiment tmfExperiment, Integer workerId, long timestamp) {
    //     ITmfVertex previousTmfVertex = null;
    //     OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
    //     OsHistoryTreeGraph osHistoryTreeGraph = (OsHistoryTreeGraph) getTmfGraph(tmfExperiment);
    //     if (osHistoryTreeGraph != null) {
    //         IGraphWorker graphWorker = osHistoryTreeGraph.getWorker(workerId);
    //         ITmfVertex tmfVertex = osHistoryTreeGraph.getVertexAt(new TmfNanoTimestamp(timestamp), graphWorker);
    //         ITmfEdge tmfEdge = osHistoryTreeGraph.getEdgeFrom(tmfVertex, EdgeDirection.INCOMING_HORIZONTAL_EDGE);
    //         if (tmfEdge != null) {
    //             previousTmfVertex = tmfEdge.getVertexFrom();
    //         }
    //     }

    //     return previousTmfVertex;
    // }

    public GraphDto createCriticalPath(TmfExperiment tmfExperiment, TmfVertex startVertex, TmfVertex endVertex) {
        OSCriticalPathAlgorithm osCriticalPathAlgorithm = getOsCriticalPathAlgorithm(tmfExperiment);
        ITmfGraph tmfGraph;
        List<@NonNull ITimeGraphRowModel> rowModels = new ArrayList<>();
        List<@NonNull ITimeGraphArrow> arrows = new ArrayList<>();
        if (osCriticalPathAlgorithm != null) {
            try {
                tmfGraph = osCriticalPathAlgorithm.computeCriticalPath(osCriticalPathAlgorithm.getGraph(), startVertex, endVertex);
                ITmfVertex headVertex = tmfGraph.getHead();
                if (headVertex != null) {
                    IGraphWorker graphWorker = tmfGraph.getParentOf(headVertex);
                    if (graphWorker != null) {
                        OsCriticalPathVisitor osCriticalPathVisitor = new OsCriticalPathVisitor(tmfExperiment, tmfGraph, graphWorker);

                        arrows = osCriticalPathVisitor.getGraphLinks();
                        for (Long key: osCriticalPathVisitor.getEntryIdToStates().keys()) {
                            Collection<ITimeGraphState> states = osCriticalPathVisitor.getEntryIdToStates().asMap().get(key);
                            if (states != null) {
                                rowModels.add(new TimeGraphRowModel(key, states.stream().collect(Collectors.toList())));
                            }
                        }
                    }

                }
            } catch (CriticalPathAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        return new GraphDto(rowModels, arrows);
    }

    private static @Nullable ITmfGraph getTmfGraph(TmfExperiment tmfExperiment) {
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        OsHistoryTreeGraph osHistoryTreeGraph = null;
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
                osHistoryTreeGraph = (OsHistoryTreeGraph) osExecutionGraph.getTmfGraph();
            }
        }

        return osHistoryTreeGraph;
    }

    private static @Nullable OsExecutionGraphProvider getOsExecutionGraphProvider(TmfExperiment tmfExperiment) {
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        OsExecutionGraphProvider osExecutionGraphProvider = null;
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
                osExecutionGraphProvider = osExecutionGraph.getOsExecutionGraphProvider();
            }
        }
        return osExecutionGraphProvider;
    }

    private static @Nullable OSCriticalPathAlgorithm getOsCriticalPathAlgorithm(TmfExperiment tmfExperiment) {
        OsExecutionGraph osExecutionGraph = (OsExecutionGraph) tmfExperiment.getAnalysisModule(OsExecutionGraph.ANALYSIS_ID);
        OSCriticalPathAlgorithm osCriticalPathAlgorithm = null;
        if (osExecutionGraph != null) {
            osExecutionGraph.schedule();
            if (osExecutionGraph.waitForCompletion()) {
                ITmfGraph tmfGraph = osExecutionGraph.getCriticalPathGraph();
                if (tmfGraph != null) {
                    osCriticalPathAlgorithm = new OSCriticalPathAlgorithm(tmfGraph);
                }
            }
        }
        return osCriticalPathAlgorithm;
    }
}

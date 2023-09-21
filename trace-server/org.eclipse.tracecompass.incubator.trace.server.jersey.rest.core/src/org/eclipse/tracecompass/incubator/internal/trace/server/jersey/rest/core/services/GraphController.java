package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;


import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.graph.ITmfVertex;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import io.swagger.v3.oas.annotations.tags.Tag;
/*
 * TODO
 * - Add Get Previous Worker in case that we have a ThreadId == -1
 * - Think about the API (Resource, hierarchy, DataProvider)
 */

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Graph")
@Path("/experiments/{expUUID}/graph")
@SuppressWarnings("javadoc")
public class GraphController {




    private final ExperimentService experimentService;
    private final GraphService graphService;

    public GraphController() {
        this.experimentService = ExperimentService.getInstance();
        this.graphService = GraphService.getInstance();
    }

    @Path("workers/{workerId}")
    @GET
    public Response getWorker(@PathParam("expUUID") UUID expUUID, @PathParam("workerId") Integer workerId) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        IGraphWorker graphWorker = this.graphService.getWorker(tmfExperiment, workerId);
        return Response.ok(graphWorker).build();
    }

    // @Path("workers/{workerId}/vertexes/{timestamp}")
    // @GET
    // public Response getPreviousWorker(@PathParam("expUUID") UUID expUUID, @PathParam("workerId") Integer workerId, @PathParam("timestamp") long timestamp) {
    //     TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
    //     if (tmfExperiment == null) {
    //         return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
    //     }
    //     TimeRange timeRange = new TimeRange();
    //     timeRange.start = 1539975457980191124L;
    //     timeRange.end = 1539975466296685583L;

    //     List<@NonNull ITmfVertex> tmfVertexes = this.graphService.getUnmatechedTmfVertex(tmfExperiment, timeRange, null);
    //     IGraphWorker previousGraphWorker = this.graphService.getPreviousWorker(tmfExperiment, workerId, timestamp);

    //     return Response.ok(previousGraphWorker).build();
    // }

    @Path("vertexes")
    @POST
    public Response getUnmatchedTmfVertex(@PathParam("expUUID") UUID expUuid, @QueryParam("direction") Direction direction, Body<TimeRange> body) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUuid);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        List<ITmfVertex> vertexes = this.graphService.getUnmatchedTmfVertex(tmfExperiment, body.getParameters(), direction);
        return Response.ok(vertexes).build();
    }

    @Path("indexes")
    @GET
    public Response getVertexIndexes(@PathParam("expUUID") UUID expUuid, @QueryParam("direction") Direction direction) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUuid);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        Map<ITmfVertex, IEventMatchingKey> indexes = this.graphService.getVertexIndexes(tmfExperiment, direction);
        return Response.ok(indexes).build();
    }

    @Path("critical-path")
    @POST
    public Response createCriticalPath(@PathParam("expUUID") UUID expUUID, Body<CreateCriticalPathDto> body) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        return Response.ok(this.graphService.createCriticalPath(tmfExperiment, body.getParameters().startVertex, body.getParameters().endVertex)).build();
    }

//    @Path("event-matching-vertexes")
//    @POST
//    public Response getEventMatchingTmfVertex(@PathParam("expUUID") UUID expUuid, @QueryParam("direction") Direction direction, @QueryParam("start") long start, @QueryParam("end") long end) {
//        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUuid);
//        if (tmfExperiment == null) {
//            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
//        }
//
//        List<ITmfVertex> vertexes = this.graphService.getEventMatchingTmfVertex(tmfExperiment, start, end, direction);
//        return Response.ok(vertexes).build();
//    }
}

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


    @Path("vertexes")
    @POST
    public Response getUnmatchedTmfVertex(@PathParam("expUUID") UUID expUuid, @QueryParam("direction") Direction direction, Body<TimeRange> body) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUuid);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        List<ITmfVertex> vertexes = this.graphService.getUnmatechedTmfVertex(tmfExperiment, body.getParameters(), direction);
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

//    @POST
//    public Response graphTest(@PathParam("expUUID") UUID expUuid, Body<TimeRange> body) {
//        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUuid);
//        if (tmfExperiment == null) {
//            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
//        }
////        this.graphService.test1(tmfExperiment, timestamp.getParameters());
////        this.graphService.test2(tmfExperiment);
//
//        List<ITmfVertex> vertexes = this.graphService.getUnmatechedTmfVertex(tmfExperiment, body.getParameters());
//        return Response.ok(vertexes).build();
//    }



}

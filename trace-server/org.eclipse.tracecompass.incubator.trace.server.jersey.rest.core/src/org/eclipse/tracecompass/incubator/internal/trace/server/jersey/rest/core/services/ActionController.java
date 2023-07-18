package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Controller to manage experiments endpoints
 *
 * @author Ahmad Faour
 */
@Path("/experiments/{expUUID}/outputs/{outputId}/actions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = EndpointConstants.ACT)
public class ActionController {
    private final ExperimentService experimentService;
    private final ActionService actionService;


    public ActionController() {
        this.experimentService = ExperimentService.getInstance();
        this.actionService = ActionService.getInstance();
    }

    @POST
    @Path("{actionId}")
    public Response createAction(@NonNull @PathParam("expUUID") UUID experiementUuid, @NonNull @PathParam("outputId") String outputId, Map<String, @NonNull Object> parameters) {


        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(experiementUuid);

        ResponseBuilder responseBuilder = null;
        try {
            if (tmfExperiment != null) {
                this.actionService.testCriticalPath(tmfExperiment,(Integer) parameters.get("entryId"));
            }
            responseBuilder = Response.ok();
        } catch (ServerErrorException e) {
            responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
        }

        return responseBuilder.build();
    }

}

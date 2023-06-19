package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.EXP_UUID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.INVALID_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_EXPERIMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.core.resources.IResource;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.ExperimentQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller to manage experiments endpoints
 *
 * @author Ahmad Faour
 */
@Path("experiments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = EndpointConstants.EXP)
public class ExperimentController {

    private final TraceService traceService;
    private final ExperimentService experimentService;

    public ExperimentController() {
        this.traceService = TraceService.getInstance();
        this.experimentService = ExperimentService.getInstance();
    }


    /**
     * Getter for the list of experiments from the trace manager
     *
     * @return The set of opened experiments
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the list of experiments on the server", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of experiments", content = @Content(array = @ArraySchema(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Experiment.class))))
    })
    public Response getExperiments() {
        return Response.ok(this.experimentService.getExperiments()).build();

    }

    /**
     * Getter for an experiment by {@link UUID}.
     *
     * @param expUUID
     *            UUID of the experiment to search for
     *
     * @return The experiment with the queried {@link UUID} if it exists.
     */
    @GET
    @Path("/{expUUID}")
    @Operation(summary = "Get the model object for an experiment", responses = {
            @ApiResponse(responseCode = "200", description = "Return the experiment model", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Experiment.class))),
            @ApiResponse(responseCode = "404", description = NO_SUCH_EXPERIMENT, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getExperiment(@Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID) {
        ResponseBuilder responseBuilder = null;
        Experiment experiment = this.experimentService.getExperiment(expUUID);
        if (experiment != null) {
            responseBuilder = Response.ok(experiment);
        } else {
            responseBuilder = Response.status(Status.NOT_FOUND);
        }
        return responseBuilder.build();
    }

    /**
     * Post a new experiment encapsulating the traces from the list of
     * {@link UUID}s.
     *
     *  @param body
     *            Parameters to post a experiment as described by
     *            {@link QueryParameters}
     *            - name -> name for the experiment.
     *            - traces -> List of UUID strings of the traces to add to the experiment
     *
     * @return no content response if one of the trace {@link UUID}s does not map to
     *         any trace.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new experiment on the server", responses = {
            @ApiResponse(responseCode = "200", description = "The experiment was successfully created", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Experiment.class))),
            @ApiResponse(responseCode = "204", description = "The experiment has at least one trace which hasn't been created yet", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = INVALID_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "The experiment (name) already exists and both differ", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal trace-server error while trying to post experiment", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response createExperiment(@RequestBody(content = {
            @Content(schema = @Schema(implementation = ExperimentQueryParameters.class))
    }, required = true) @NotNull @Valid final Body<CreateExperimentRequestDto> body) {
        List<IResource> traceResources = new ArrayList<>();
        for (UUID traceUuid : body.getParameters().traces) {
            IResource traceResource = this.traceService.getTraceResource(traceUuid);
            if (traceResource == null) {
                // The trace should have been created first
                return Response.noContent().build();
            }
            traceResources.add(traceResource);
        }

        ResponseBuilder responseBuilder = null;
        try {
            responseBuilder = Response.ok(this.experimentService.createExperiment(body.getParameters().experimentName, traceResources));
        } catch (ServerErrorException e) {
            responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
        }

        return responseBuilder.build();
    }

    /**
     * Delete an experiment by {@link UUID}.
     *
     * @param expUUID
     *            UUID of the experiment to delete
     *
     * @return The experiment with the queried {@link UUID} if it exists.
     */
    @DELETE
    @Path("/{expUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove an experiment from the server", responses = {
            @ApiResponse(responseCode = "200", description = "The trace was successfully deleted, return the deleted experiment.", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Experiment.class))),
            @ApiResponse(responseCode = "404", description = NO_SUCH_EXPERIMENT, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response deleteExperiment(@Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID) {
        ResponseBuilder responseBuilder = null;
        try {

            responseBuilder = Response.ok(this.experimentService.deleteExperiment(expUUID));
        } catch (ClientErrorException e) {
            responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
        }

        return responseBuilder.build();
    }

}

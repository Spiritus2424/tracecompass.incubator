package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.CONSISTENT_PARENT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ELEMENT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ELEMENT_EX;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.EXP_UUID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.INVALID_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS_EX;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS_EX_TT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS_TT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.MISSING_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_PROVIDER;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.OUTPUT_ID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.PROVIDER_NOT_FOUND;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TGR;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_EX;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_EX_TREE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_TREE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMES_EX_TT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMES_TT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TREE_ENTRIES;

import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.ArrowsQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.RequestedQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TimeGraphArrowsResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TimeGraphStatesResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TimeGraphTooltipResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TimeGraphTreeResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TooltipQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TreeQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.GenericView;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;



@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = TGR)
@Path("/experiments/{expUUID}/outputs/timeGraph/{outputId}")
@SuppressWarnings("javadoc")
public class TimeGraphController {

    private final TimeGraphService timeGraphService;
    private final TreeService treeService;
    private final ExperimentService experimentService;


    public TimeGraphController() {
        this.timeGraphService = TimeGraphService.getInstance();
        this.treeService = TreeService.getInstance();
        this.experimentService = ExperimentService.getInstance();
    }

    /**
     * Query the provider for the time graph tree
     *
     * @param expUUID
     *            {@link UUID} of the experiment to query
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph tree as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("tree")
    @Operation(summary = "API to get the Time Graph tree", description = TREE_ENTRIES, responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of Time Graph entries. " +
                    CONSISTENT_PARENT, content = @Content(schema = @Schema(implementation = TimeGraphTreeResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getTree(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the timegraph tree. " + TIMERANGE_TREE, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMERANGE_EX_TREE +
                            "}}"), schema = @Schema(implementation = TreeQueryParameters.class))
            }, required = true) Body<GetTreeRequestDto> body) {

        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }
        return Response.ok(this.treeService.getTree(tmfExperiment, outputId, body.getParameters())).build();
    }

    /**
     * Query the provider for the time graph states
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph states as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("states")
    @Tag(name = TGR)
    @Operation(summary = "API to get the Time Graph states", description = "Unique entry point for all TimeGraph states, ensures that the same template is followed for all views", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of time graph rows", content = @Content(schema = @Schema(implementation = TimeGraphStatesResponse.class))),
            @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getStates(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the timegraph states. " + TIMERANGE + " " + ITEMS, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMERANGE_EX + "," + ITEMS_EX +
                            "}}"), schema = @Schema(implementation = RequestedQueryParameters.class))
            }, required = true) @NotNull @Valid final Body<GetTimeGraphStatesRequestDto> body) {

        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        return Response.ok(this.timeGraphService.getStates(tmfExperiment, outputId, body.getParameters())).build();
    }


    /**
     * Query the provider for the time graph arrows
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph arrows as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("arrows")
    @Operation(summary = "API to get the Time Graph arrows", description = "Unique entry point for all TimeGraph models, " +
            "ensures that the same template is followed for all models", responses = {
                    @ApiResponse(responseCode = "200", description = "Returns a sampled list of TimeGraph arrows", content = @Content(schema = @Schema(implementation = TimeGraphArrowsResponse.class))),
                    @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response getArrows(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the timegraph arrows. " + TIMERANGE, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMERANGE_EX +
                            "}}"), schema = @Schema(implementation = ArrowsQueryParameters.class))
            }, required = true) @NotNull @Valid final Body<GetTimeGraphArrowsRequestDto> body) {
        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        return Response.ok(this.timeGraphService.getArrows(tmfExperiment, outputId, body.getParameters())).build();
    }

    /**
     * Query the provider for the time graph tooltips
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph tooltip as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("tooltip")
    @Tag(name = TGR)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "API to get a Time Graph tooltip", description = "Endpoint to retrieve tooltips for time graph", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of tooltip keys to values", content = @Content(schema = @Schema(implementation = TimeGraphTooltipResponse.class))),
            @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getTimeGraphTooltip(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the timegraph tooltip. " + TIMES_TT + ITEMS_TT + ELEMENT, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMES_EX_TT + ITEMS_EX_TT + ELEMENT_EX +
                            "}}"), schema = @Schema(implementation = TooltipQueryParameters.class))
            }, required = true) Body<GetTimeGraphTooltipsDto> body) {


        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        return Response.ok(this.timeGraphService.getTooltips(tmfExperiment, outputId, body.getParameters())).build();
    }

    /**
     * Query the provider for the time graph tooltips Action
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph tooltip as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("tooltip/actions")
    @Tag(name = TGR)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "API to get a Time Graph Action tooltip", description = "Endpoint to retrieve tooltips for time graph", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of tooltip keys to values", content = @Content(schema = @Schema(implementation = TimeGraphTooltipResponse.class))),
            @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getTimeGraphActionTooltips(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the timegraph tooltip. " + TIMES_TT + ITEMS_TT + ELEMENT, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMES_EX_TT + ITEMS_EX_TT + ELEMENT_EX +
                            "}}"), schema = @Schema(implementation = TooltipQueryParameters.class))
            }, required = true) Body<GetTimeGraphTooltipsDto> body) {


        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }


        return Response.ok(this.timeGraphService.getActionTooltips(tmfExperiment, outputId, body.getParameters())).build();
    }

    /**
     * Query the provider for the time graph tooltips Action
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch time graph tooltip as described by
     *            {@link QueryParameters}
     * @return {@link GenericView} with the results
     */
    @POST
    @Path("tooltip/actions/{actionId}")
    @Tag(name = TGR)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "API to get a Time Graph tooltip", description = "Endpoint to retrieve tooltips for time graph", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of tooltip keys to values", content = @Content(schema = @Schema(implementation = TimeGraphTooltipResponse.class))),
            @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response applyTimeGraphAction(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @PathParam("actionId") String actionId,
            @RequestBody(description = "Query parameters to fetch the timegraph tooltip. " + TIMES_TT + ITEMS_TT + ELEMENT, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMES_EX_TT + ITEMS_EX_TT + ELEMENT_EX +
                            "}}"), schema = @Schema(implementation = TooltipQueryParameters.class))
            }, required = true) Body<Map<String,Object>> body) {


        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        this.timeGraphService.applyAction(tmfExperiment, outputId, actionId, body.getParameters());
        return Response.ok().build();
    }

}

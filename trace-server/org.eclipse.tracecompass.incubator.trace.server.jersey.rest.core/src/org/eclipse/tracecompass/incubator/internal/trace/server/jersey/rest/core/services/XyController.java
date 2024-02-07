package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.CONSISTENT_PARENT;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.EXP_UUID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.INVALID_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS_EX;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.ITEMS_XY;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.MISSING_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_PROVIDER;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.OUTPUT_ID;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.PROVIDER_NOT_FOUND;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_EX;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_EX_TREE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TIMERANGE_TREE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TREE_ENTRIES;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.X_Y;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.RequestedQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TreeQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.XYResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.XYTreeResponse;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.GenericView;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.TreeModelWrapper;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import io.swagger.v3.oas.annotations.Hidden;
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
@Tag(name = X_Y)
@Path("/experiments/{expUUID}/outputs/XY/{outputId}/")
@SuppressWarnings("javadoc")
public class XyController {


    private final XyService xyService;
    private final TreeService treeService;
    private final ExperimentService experimentService;


    public XyController() {
        this.xyService = XyService.getInstance();
        this.treeService = TreeService.getInstance();
        this.experimentService = ExperimentService.getInstance();
    }

    /**
     * Query the provider for the XY tree
     *
     * @param expUUID
     *            desired experiment UUID
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch an XY tree as described by
     *            {@link QueryParameters}
     * @return an {@link GenericView} with the results
     */
    @POST
    @Path("tree")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "API to get the XY tree", description = TREE_ENTRIES, responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of XY entries. " +
                    CONSISTENT_PARENT, content = @Content(schema = @Schema(implementation = XYTreeResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getXyTree(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the XY tree. " + TIMERANGE_TREE, content = {
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
     * Query the provider for the XY view
     *
     * @param expUUID
     *            {@link UUID} of the experiment to query
     * @param outputId
     *            Output ID for the data provider to query
     * @param queryParameters
     *            Parameters to fetch XY as described by {@link QueryParameters}
     * @return an {@link GenericView} with the results
     */
    @POST
    @Path("xy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "API to get the XY model", description = "Unique endpoint for all xy models, " +
            "ensures that the same template is followed for all endpoints.", responses = {
                    @ApiResponse(responseCode = "200", description = "Return the queried XYResponse", content = @Content(schema = @Schema(implementation = XYResponse.class))),
                    @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = PROVIDER_NOT_FOUND, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = NO_PROVIDER, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response getXy(
            @Parameter(description = EXP_UUID) @PathParam("expUUID") UUID expUUID,
            @Parameter(description = OUTPUT_ID) @PathParam("outputId") String outputId,
            @RequestBody(description = "Query parameters to fetch the XY model. " + TIMERANGE + " " + ITEMS_XY, content = {
                    @Content(examples = @ExampleObject("{\"parameters\":{" + TIMERANGE_EX + "," + ITEMS_EX +
                            "}}"), schema = @Schema(implementation = RequestedQueryParameters.class))
            }, required = true) @NotNull @Valid final Body<GetXyRequestDto> body) {

//        TmfExperiment tmfExperiment = this.experimentService.getTmfExperiment(expUUID);
//        if (tmfExperiment == null) {
//            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
//        }
//
//
//        Response errorResponse = validateParameters(outputId, queryParameters);
//        if (errorResponse != null) {
//            return errorResponse;
//        }
        TmfExperiment tmfExperiment =  this.experimentService.getTmfExperiment(expUUID);
        if (tmfExperiment == null) {
            return Response.status(Status.NOT_FOUND).entity(NO_SUCH_TRACE).build();
        }

        if (body.getParameters().requestedItems == null || body.getParameters().requestedItems.isEmpty()) {
            TreeModelWrapper treeModelWrapper = (TreeModelWrapper) this.treeService.getTree(tmfExperiment, outputId, new GetTreeRequestDto(body.getParameters().requestedTimerange)).getModel();
            List<Long> items = null;
            if (treeModelWrapper != null) {
                items = treeModelWrapper.getEntries().stream()
                        .map(entryModel -> entryModel.getId())
                        .collect(Collectors.toList());
                body.getParameters().requestedItems = items;
            }
        }

        return Response.ok(this.xyService.getXy(tmfExperiment, outputId, body.getParameters())).build();



    }




    /**
     * Query the provider for XY tooltip, currently not implemented
     *
     * @param expUUID
     *            {@link UUID} of the experiment to query
     * @param outputId
     *            Output ID for the data provider to query
     * @param xValue
     *            Given X value to fetch the tooltip
     * @param yValue
     *            Given Y value to help fetch the tooltip, used to get the right
     *            point if two points have the same X value
     * @param entryId
     *            Entry Id or series Id
     * @return {@link GenericView} with the results
     */
    @GET
    @Hidden
    @Path("tooltip")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getXyTooltip(@PathParam("expUUID") UUID expUUID,
            @PathParam("outputId") String outputId,
            @QueryParam("xValue") long xValue,
            @QueryParam("yValue") long yValue,
            @QueryParam("entryId") long entryId) {
        return Response.status(Status.NOT_IMPLEMENTED).entity("XY tooltip are not implemented yet").build(); //$NON-NLS-1$
    }


}

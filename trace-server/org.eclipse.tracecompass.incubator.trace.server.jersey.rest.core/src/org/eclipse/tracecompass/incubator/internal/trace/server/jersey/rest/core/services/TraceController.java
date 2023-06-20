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

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.CANNOT_READ;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.MISSING_PARAMETERS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NAME_EXISTS;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NOT_SUPPORTED;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_SUCH_TRACE;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TRACE_CREATION_FAILED;
import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.TRACE_UUID;

import java.util.Optional;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.TraceQueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller of Trace API.
 *
 * @author Ahmad Faour
 */
@Path("traces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = EndpointConstants.TRA)
public class TraceController {

    private final TraceService traceService;


    public TraceController() {
        this.traceService = TraceService.getInstance();
    }

    /**
     * Getter method to access the list of traces
     *
     * @return a response containing the list of traces
     */
    @GET
    @Operation(summary = "Get the list of physical traces imported on the server", responses = {
            @ApiResponse(responseCode = "200", description = "Returns a list of traces", content = @Content(array = @ArraySchema(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Trace.class))))
    })
    public Response getTraces() {
        return Response.ok(this.traceService.getTraces()).build();
    }

    /**
     * Getter method to get a trace object
     *
     * @param uuid
     *            Unique trace ID
     * @return a response containing the trace
     */
    @GET
    @Path("/{uuid}")
    @Operation(summary = "Get the model object for a trace", responses = {
            @ApiResponse(responseCode = "200", description = "Return the trace model", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Trace.class))),
            @ApiResponse(responseCode = "404", description = NO_SUCH_TRACE, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response getTrace(@Parameter(description = TRACE_UUID) @PathParam("uuid") @NotNull UUID uuid) {
        ResponseBuilder responseBuilder = null;
        try {
            responseBuilder = Response.ok(this.traceService.getTrace(uuid));
        } catch (ClientErrorException e) {
            responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
        }

        return responseBuilder.build();
    }


    /**
     * Method to create the trace resources and add it to the trace manager.
     *
     * @param queryParameters
     *            Parameters to post a trace as described by
     *            {@link QueryParameters}
     * @return the new trace model object or the exception if it failed to load.
     */
    @POST
    @Operation(summary = "Import a trace", description = "Import a trace to the trace server. Return some base information once imported.", responses = {
            @ApiResponse(responseCode = "200", description = "The trace has been successfully added to the trace server", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Trace.class))),
            @ApiResponse(responseCode = "400", description = MISSING_PARAMETERS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = NO_SUCH_TRACE, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "406", description = CANNOT_READ, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = NAME_EXISTS, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = TRACE_CREATION_FAILED, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "501", description = NOT_SUPPORTED, content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response openTrace(@RequestBody(content = {
            @Content(schema = @Schema(implementation = TraceQueryParameters.class))
    }, required = true) @NotNull @Valid final Body<OpenTraceRequestDto> body, @QueryParam("filter") String regexFilter) {
        ResponseBuilder responseBuilder = null;

        if (body.getParameters().maxDepth > 0) {
            Optional<String> optionalRegexFilter = regexFilter != null && !regexFilter.trim().isEmpty() ? Optional.of(regexFilter.trim()) : Optional.empty();
            responseBuilder = Response.ok(this.traceService.openTraces(body.getParameters().name, body.getParameters().uri, body.getParameters().typeId, body.getParameters().maxDepth, optionalRegexFilter));
        } else {
            try {
                responseBuilder = Response.ok(this.traceService.openTrace(body.getParameters().name, body.getParameters().uri, body.getParameters().typeId));
            } catch (TmfTraceImportException | CoreException | IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
                responseBuilder = Response.status(Status.NOT_ACCEPTABLE.getStatusCode(), e.getMessage());
            } catch (ClientErrorException | ServerErrorException e) {
                responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
            }
        }


        return responseBuilder.build();
    }

    /**
     * Delete a trace from the manager
     *
     * @param uuid
     *            Unique trace ID
     * @return a not found response if there is no such trace or the entity.
     */
    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Remove a trace from the server and disk", responses = {
            @ApiResponse(responseCode = "200", description = "The trace was successfully deleted", content = @Content(schema = @Schema(implementation = org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.Trace.class))),
            @ApiResponse(responseCode = "404", description = NO_SUCH_TRACE, content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "The trace is in use by at least one experiment thus cannot be deleted", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response deleteTrace(@Parameter(description = TRACE_UUID) @PathParam("uuid") @NotNull UUID uuid) {
        ResponseBuilder responseBuilder = null;
        try {
            responseBuilder = Response.ok(this.traceService.deleteTrace(uuid));
        } catch (ClientErrorException | ServerErrorException e) {
            responseBuilder = Response.status(e.getResponse().getStatus(), e.getMessage());
        }

        return responseBuilder.build();
    }

}

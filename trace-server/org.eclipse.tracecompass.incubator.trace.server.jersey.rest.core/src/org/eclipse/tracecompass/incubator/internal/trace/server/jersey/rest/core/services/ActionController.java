package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

}

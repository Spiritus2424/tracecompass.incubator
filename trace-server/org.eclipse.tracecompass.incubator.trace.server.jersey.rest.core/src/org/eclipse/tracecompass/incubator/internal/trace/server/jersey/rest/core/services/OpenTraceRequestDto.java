package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

public class OpenTraceRequestDto {

    @NotNull
    public String uri;

    public String name;

    public String typeId;

    @DefaultValue("false")
    public boolean isRecursively;
}

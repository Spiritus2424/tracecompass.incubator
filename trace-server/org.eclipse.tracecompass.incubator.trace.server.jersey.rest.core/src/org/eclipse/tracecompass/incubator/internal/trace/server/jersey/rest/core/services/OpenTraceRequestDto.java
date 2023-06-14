package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.NotNull;

public class OpenTraceRequestDto {

    @NotNull
    public String uri;

    public String name;

    public String typeId;
}

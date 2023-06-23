package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OpenTraceRequestDto {

    @NotNull
    public String uri;

    public String name;

    public String typeId;

    @Min(0)
    @Max(3)
    public int maxDepth;
}

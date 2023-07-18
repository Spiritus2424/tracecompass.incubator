package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.NotNull;

public class TimeRange {

    @NotNull
    public long start;
    @NotNull
    public long end;

    public int nbTimes;
}

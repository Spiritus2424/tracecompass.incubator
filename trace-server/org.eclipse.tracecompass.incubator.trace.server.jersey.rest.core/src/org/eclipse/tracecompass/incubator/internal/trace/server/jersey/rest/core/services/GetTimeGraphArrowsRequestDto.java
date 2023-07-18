package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetTimeGraphArrowsRequestDto {

    @JsonProperty("requested_timerange")
    @NotNull
    public TimeRange requestedTimerange = new TimeRange();
}

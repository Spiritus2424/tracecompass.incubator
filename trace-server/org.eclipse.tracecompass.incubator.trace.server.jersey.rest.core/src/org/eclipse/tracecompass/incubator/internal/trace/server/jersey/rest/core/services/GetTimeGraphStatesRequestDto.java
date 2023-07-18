package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetTimeGraphStatesRequestDto {
    @JsonProperty("requested_timerange")
    @NotNull
    public TimeRange requestedTimerange = new TimeRange();

    @JsonProperty("requested_items")
    @NotNull
    public List<Integer> requestedItems = new ArrayList<>();
}

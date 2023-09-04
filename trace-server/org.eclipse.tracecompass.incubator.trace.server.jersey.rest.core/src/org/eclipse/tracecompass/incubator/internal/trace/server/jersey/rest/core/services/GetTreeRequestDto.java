package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetTreeRequestDto {

    @JsonProperty("requested_timerange")
    TimeRange requestedTimerange;

    @JsonProperty("requested_times")
    List<Long> requestedTimes;


    public GetTreeRequestDto() {
        this.requestedTimes = null;
        this.requestedTimerange = null;
    }

    public GetTreeRequestDto(TimeRange timeRange) {
        this.requestedTimerange = timeRange;
    }
}

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertexDto {
    @JsonProperty("timestamp")
    long timestamp;
    @JsonProperty("workerId")
    int workerId;

    public VertexDto() {
    }
}

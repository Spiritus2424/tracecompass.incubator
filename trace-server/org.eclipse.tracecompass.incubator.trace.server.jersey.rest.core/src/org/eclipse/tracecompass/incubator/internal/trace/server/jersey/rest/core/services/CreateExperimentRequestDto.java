package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateExperimentRequestDto {

    @JsonProperty("name")
    @NotNull
    public String experimentName;
    @NotNull
    public List<UUID> traces;
}

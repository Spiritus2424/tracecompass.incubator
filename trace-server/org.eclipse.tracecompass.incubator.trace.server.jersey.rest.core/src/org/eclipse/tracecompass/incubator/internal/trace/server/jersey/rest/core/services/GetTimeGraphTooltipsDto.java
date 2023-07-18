package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetTimeGraphTooltipsDto {
    @JsonProperty("requested_times")
    public List<Long> requestedTimes;

    @JsonProperty("requested_items")
    public  List<Integer> requestedItems;

    @JsonProperty("requested_element")
    @NotNull
    public Element requestedElement = new Element();


    public class Element {
        @NotNull
        public long time;

        @NotNull
        public ElementType elementType = ElementType.arrow;

        @NotNull
        public long duration;

        public long entryId;

        public long destinationId;
    }

    @SuppressWarnings("javadoc")
    public enum ElementType {
        state, annotation, arrow
    }
}

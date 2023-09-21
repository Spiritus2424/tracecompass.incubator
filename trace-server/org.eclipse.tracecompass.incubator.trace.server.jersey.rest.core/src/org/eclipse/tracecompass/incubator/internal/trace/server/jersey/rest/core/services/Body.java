package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import javax.validation.constraints.NotNull;

public class Body<T> {
    @NotNull
    private T parameters;

    public Body(T parameters) {
        this.parameters = parameters;
    }

    @SuppressWarnings("null")
    public Body() {}

    public T getParameters() {
        return this.parameters;
    }
}

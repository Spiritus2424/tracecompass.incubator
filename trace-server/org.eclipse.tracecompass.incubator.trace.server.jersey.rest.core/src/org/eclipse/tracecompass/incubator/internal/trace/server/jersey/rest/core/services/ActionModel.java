package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;


public class ActionModel<T> {

    private String id;
    private String name;
    private String description;
    private T parameters;


    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public T getParameters() {
        return this.parameters;
    }

}

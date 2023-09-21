package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.TmfVertex;

public class CreateCriticalPathDto {
    public TmfVertex startVertex;
    public TmfVertex endVertex;
}

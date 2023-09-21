package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;

public class GraphDto {
    public List<@NonNull ITimeGraphRowModel> rows = new ArrayList<>();
    public List<@NonNull ITimeGraphArrow> arrows = new ArrayList<>();

    public GraphDto() {
        this.rows = new ArrayList<>();
        this.arrows = new ArrayList<>();
    }

    public GraphDto(List<@NonNull ITimeGraphRowModel> rowModels, List<@NonNull ITimeGraphArrow> arrows) {
        this.rows = rowModels;
        this.arrows = arrows;
    }
}

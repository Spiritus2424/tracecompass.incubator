package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.webapp;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.graph.core.graph.historytree.TmfVertex;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TmfVertexSerializer extends StdSerializer<@NonNull TmfVertex> {

    private static final long serialVersionUID = 2520510727172468215L;

    public TmfVertexSerializer() {
        super(TmfVertex.class);
    }

    @Override
    public void serialize(@NonNull TmfVertex value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("workerId", value.getWorkerId()); //$NON-NLS-1$
        gen.writeNumberField("timestamp", value.getTimestamp()); //$NON-NLS-1$
        gen.writeEndObject();

    }


}

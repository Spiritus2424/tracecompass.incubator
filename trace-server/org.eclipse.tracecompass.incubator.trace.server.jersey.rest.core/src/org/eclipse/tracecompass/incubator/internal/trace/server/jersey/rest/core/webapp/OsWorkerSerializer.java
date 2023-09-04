package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.webapp;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


public class OsWorkerSerializer extends StdSerializer<@NonNull OsWorker>  {


    private static final long serialVersionUID = 1086643495212273170L;

    /**
     * Public constructor
     */
    public OsWorkerSerializer() {
        super(OsWorker.class);
    }

    @Override
    public void serialize(@NonNull OsWorker value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeStartObject();


      gen.writeObjectField("hostThread", value.getHostThread()); //$NON-NLS-1$
      gen.writeNumberField("start", value.getStart()); //$NON-NLS-1$
      gen.writeStringField("threadName", value.getName()); //$NON-NLS-1$
      gen.writeObjectField("status", value.getStatus()); //$NON-NLS-1$
      gen.writeObjectField("oldStatus", value.getOldStatus()); //$NON-NLS-1$


      gen.writeEndObject();
    }
}

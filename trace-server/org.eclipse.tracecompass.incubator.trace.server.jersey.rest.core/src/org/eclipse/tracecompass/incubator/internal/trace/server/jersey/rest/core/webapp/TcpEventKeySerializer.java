package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.webapp;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.matching.TcpEventKey;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TcpEventKeySerializer extends StdSerializer<@NonNull TcpEventKey>  {

    private static final long serialVersionUID = -5283328424574752568L;

    /**
     * Public constructor
     */
    public TcpEventKeySerializer() {
        super(TcpEventKey.class);
    }

    @Override
    public void serialize(@NonNull TcpEventKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeStartObject();
      gen.writeNumberField("seq", value.getSeq()); //$NON-NLS-1$
      gen.writeNumberField("ackseq", value.getAckseq()); //$NON-NLS-1$
      gen.writeNumberField("flags", value.getFlags()); //$NON-NLS-1$
      gen.writeEndObject();
    }

}

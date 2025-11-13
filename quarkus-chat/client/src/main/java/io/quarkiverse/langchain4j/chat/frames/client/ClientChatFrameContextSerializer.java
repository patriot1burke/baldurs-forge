package io.quarkiverse.langchain4j.chat.frames.client;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ClientChatFrameContextSerializer extends StdSerializer<ClientChatFrameContext> {

    public ClientChatFrameContextSerializer() {
        super(ClientChatFrameContext.class);
    }

    @Override
    public void serialize(ClientChatFrameContext value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("data", value.data);
        gen.writeObjectField("memory", value.memory);
        gen.writeObjectField("memoryId", value.memoryId);
        gen.writeEndObject();
    }
}

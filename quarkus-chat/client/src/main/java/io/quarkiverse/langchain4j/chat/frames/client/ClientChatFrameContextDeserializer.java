package io.quarkiverse.langchain4j.chat.frames.client;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ClientChatFrameContextDeserializer extends StdDeserializer<ClientChatFrameContext> {

    public ClientChatFrameContextDeserializer() {
        super(ClientChatFrameContext.class);
    }

    @Override
    public ClientChatFrameContext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode contextNode = p.getCodec().readTree(p);
        if (contextNode == null || contextNode.isNull()) {
            return null;
        }
        ClientChatFrameContext context = new ClientChatFrameContext();
        JsonNode dataNode = contextNode.get("data");
        JsonNode memoryNode = contextNode.get("memory");
        JsonNode memoryIdNode = contextNode.get("memoryId");

        if (memoryIdNode != null && !memoryIdNode.isNull()) {
            context.setMemoryId(memoryIdNode.asText());
        }
        if (dataNode != null && !dataNode.isNull()) {
            dataNode.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value != null && !value.isNull()) {
                    context.data().put(field.getKey(), value);
                }
            });
        }

        context.setMemory(memoryNode);

        return context;
    }
}

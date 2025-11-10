package io.quarkiverse.langchain4j.chat.frames;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatContextSerialization {

    public static ChatContext deserialize(ObjectMapper mapper, ChatContext context, ClientMemoryStore memory,
            InputStream entityStream) throws IOException, JsonProcessingException {
        // we have a static method for unit testing unmarshalling
        JsonNode node = mapper.readTree(entityStream);
        if (node == null || !node.isObject()) {
            return context;
        }
        JsonNode userMessageNode = node.get("userMessage");
        if (userMessageNode != null && !userMessageNode.isNull()) {
            context.setUserMessage(userMessageNode.asText());
        }
         JsonNode contextNode = node.get("context");
        if (contextNode == null || contextNode.isNull()) {
            return context;
        }
        JsonNode memoryIdNode = contextNode.get("memoryId");
        if (memoryIdNode != null && !memoryIdNode.isNull()) {
            context.setMemoryId(memoryIdNode.asText());
        }
        JsonNode memoryNode = contextNode.get("memory");
        if (memoryNode != null && !memoryNode.isNull()) {
            memory.readJson(memoryNode);
        }
    
        JsonNode sharedNode = contextNode.get("data");
        if (sharedNode != null && !sharedNode.isNull()) {
            sharedNode.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value != null && !value.isNull()) {
                    context.data().put(field.getKey(), value);
                }
            });
        }
        return context;
    }

    public static void serialize(ChatContext t, ClientMemoryStore memory, ObjectMapper mapper, Writer writer)
            throws IOException {
        writer.write("{");
        writer.write("\"response\":");
        if (t.response() != null) {
            mapper.writeValue(writer, t.response());
        } else {
            writer.write("null");
        }
        writer.write(",");        
        writer.write("\"context\":");
        writer.write("{");
        writer.write("\"memoryId\":");
        writer.write("\"" + t.memoryId() + "\"");
        writer.write(",");
        writer.write("\"data\":");
        mapper.writeValue(writer, t.data());
        writer.write(",");
        writer.write("\"memory\":");
        memory.writeJson(writer);
        writer.write("}");
    
        writer.write("}");
    }

}

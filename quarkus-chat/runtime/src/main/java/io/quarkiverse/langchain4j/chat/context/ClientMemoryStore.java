package io.quarkiverse.langchain4j.chat.context;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

/**
 * 
 * QuarkusAiServiceContext.close() will call deleteMessages() and this
 * bean is @RequestScoped so it would be out of scope.
 * 
 * So, we do not implement ChatMemoryStore directly and instead 
 * wrap it in a ClientMemoryStoreBean.
 * 
 */
@RequestScoped
public class ClientMemoryStore {
    @Inject
    ObjectMapper mapper;

    Map<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();

    public void deleteMessages(Object memoryId) {
        Log.info("Deleting messages for memoryId: " + memoryId);
        messages.remove(memoryId);
    }

    public List<ChatMessage> getMessages(Object memoryId) {
        Log.info("Getting messages for memoryId: " + memoryId);
        return messages.computeIfAbsent(memoryId.toString(), ignored -> new ArrayList<>());
    }

    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Log.info("Updating messages for memoryId: " + memoryId);
        this.messages.put(memoryId.toString(), messages);
    }

    public void readJson(JsonNode node) {
        if (node == null || !node.isObject()) {
            return;
        }
        node.fields().forEachRemaining(field -> {
            String memoryId = field.getKey();
            JsonNode memoryNode = field.getValue();
            try {
                // TODO: Fork chat memory deserialization or get them to make it public
                // this is expensive to deserialize, then serialize again, then deserialize
                // again
                String memoryJson = mapper.writeValueAsString(memoryNode);
                List<ChatMessage> memoryMessages = ChatMessageDeserializer.messagesFromJson(memoryJson);
                messages.put(memoryId, memoryMessages);
            } catch (Exception e) {
                throw new RuntimeException("Error reading memory: " + e.getMessage());
            }
        });
    }

    public void writeJson(Writer writer) {
        try {
            boolean first = true;
            writer.write("{");
            for (Map.Entry<String, List<ChatMessage>> entry : messages.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    writer.write(",");
                }
                String memoryId = entry.getKey();
                List<ChatMessage> memoryMessages = entry.getValue();
                String memoryJson = ChatMessageSerializer.messagesToJson(memoryMessages);
                writer.write("\"" + memoryId + "\": " + memoryJson);
            }
            writer.write("}");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error writing memory: " + e.getMessage());
        }
    }

    public void ping() {
        // exists to test CDI request scope
    }

    public static ClientMemoryStore pojo(ObjectMapper mapper) {
        ClientMemoryStore memory = new ClientMemoryStore();
        memory.mapper = mapper;
        return memory;
    }
}


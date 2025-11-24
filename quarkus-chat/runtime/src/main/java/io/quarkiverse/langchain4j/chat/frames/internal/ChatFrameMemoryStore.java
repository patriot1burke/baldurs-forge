package io.quarkiverse.langchain4j.chat.frames.internal;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;

@SessionScoped
public class ChatFrameMemoryStore implements ChatMemoryStore {
    @Inject
    ObjectMapper mapper;

    Map<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();

    public Map<String, List<ChatMessage>> messages() {
        return messages;
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Log.debugv("Deleting messages for memoryId: {0}", memoryId);
        messages.remove(memoryId);
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Log.debugv("Getting messages for memoryId: {0}", memoryId);
        return messages.computeIfAbsent(memoryId.toString(), ignored -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Log.debugv("Updating messages for memoryId: {0}", memoryId);
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
                String memoryId = entry.getKey();
                List<ChatMessage> memoryMessages = entry.getValue();
                if (memoryMessages.isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    writer.write(",");
                }
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
}

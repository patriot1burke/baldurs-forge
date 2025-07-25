package org.baldurs.forge.context;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.ChatMessage;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;

/**
 * 
 * Quarkus does not provide a way to hook in the chat memory store directly. It
 * also
 * will automatically use any ChatMemoryStore that is provided as a bean.
 * 
 * So, this class cannot implement ChatMemoryStore directly.
 */
@RequestScoped
public class ClientMemoryStore {
    @Inject
    ObjectMapper mapper;

    Map<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();

    public void deleteMessages(Object memoryId) {
        messages.remove(memoryId);
    }

    public List<ChatMessage> getMessages(Object memoryId) {
        return messages.computeIfAbsent(memoryId.toString(), ignored -> new ArrayList<>());
    }

    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
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

    /**
     * Wraps the ClientMemoryStore in a ChatMemoryStore.
     * ChatMemoryStore.deleteMessages can be called by Quarkus out of scope of a request.  Since ClientMemoryStore is request scoped,
     * this wrapper catches any CDI exceptions and eats them to avoid logging so many errors.
     * @return
     */
    public static ChatMemoryStore beanDelegate() {
        return new ChatMemoryStore() {

            @Override
            public void deleteMessages(Object memoryId) {
                ClientMemoryStore clientMemoryStore = null;
                try {
                    clientMemoryStore = CDI.current().select(ClientMemoryStore.class).get();
                    clientMemoryStore.ping(); // ping forces CDI to reference the instance
                } catch (Exception e) {
                    return;
                }

            }

            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                ClientMemoryStore clientMemoryStore = CDI.current().select(ClientMemoryStore.class).get();
                return clientMemoryStore.getMessages(memoryId);
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                ClientMemoryStore clientMemoryStore = CDI.current().select(ClientMemoryStore.class).get();
                clientMemoryStore.updateMessages(memoryId, messages);
            }

        };
    }
}


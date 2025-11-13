package io.quarkiverse.langchain4j.chat.frames.client;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ChatFrameRequest {
    ClientChatContext context;
    String userMessage = null;

    public String userMessage() {
        return userMessage;
    }

    public String memoryId() {
        return context.memoryId();
    }

    public void setMemoryId(String memoryId) {
        if (context == null) {
            context = new ClientChatContext();
        }
        context.setMemoryId(memoryId);
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setData(Map<String, Object> data) {
        if (context == null) {
            context = new ClientChatContext();
        }
        context.setData(data);
    }

    /**
     * Data serialized and shared with client.
     *
     * This is raw data and could contain JsonNode objects.
     * Preferably use the {@link #getData(String, Class)} method to get the data
     * as a specific type.
     *
     * @return
     */
    public Map<String, Object> data() {
        if (context == null) {
            context = new ClientChatContext();
        }
        return context.data();
    }

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    public <T> T getData(String key, Class<T> type) {
        if (context == null) {
            context = new ClientChatContext();
        }
        return context.getData(key, type);

    }

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    public <T> T getData(String key, TypeReference<T> type) {
        if (context == null) {
            context = new ClientChatContext();
        }
        return context.getData(key, type);
    }

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * Values should be automatically serializable to JSON via Jackson.
     *
     * @return
     */
    public void setData(String key, Object value) {
        if (context == null) {
            context = new ClientChatContext();
        }
        context.setData(key, value);
    }
}

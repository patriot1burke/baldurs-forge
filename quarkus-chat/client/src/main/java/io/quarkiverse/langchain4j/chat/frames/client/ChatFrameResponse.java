package io.quarkiverse.langchain4j.chat.frames.client;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ChatFrameResponse {
    ClientChatFrameContext context;
    List<ClientChatFrameMessage> response;

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
        if (context != null) {
            return context.data();
        }
        return null;
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
        if (context != null) {
            return context.getData(key, type);
        }
        return null;
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
        if (context != null) {
            return context.getData(key, type);
        }
        return null;
    }

    public ChatFrameRequest toRequest() {
        ChatFrameRequest request = new ChatFrameRequest();
        request.context = context;
        return request;
    }

    public ChatFrameRequest toRequest(String userMessage) {
        ChatFrameRequest request = toRequest();
        request.setUserMessage(userMessage);
        return request;
    }

    public List<ClientChatFrameMessage> response() {
        return response;
    }
}

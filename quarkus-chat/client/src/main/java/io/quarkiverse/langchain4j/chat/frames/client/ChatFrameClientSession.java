package io.quarkiverse.langchain4j.chat.frames.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ChatFrameClientSession {
    ClientChatFrameContext context;
    String userMessage = null;

    @JsonIgnore
    ObjectMapper mapper;

    @JsonIgnore
    WebTarget target;

    protected ChatFrameClientSession(ObjectMapper mapper, WebTarget target) {
        this.mapper = mapper;
        this.target = target;
    }

    public List<ClientChatFrameMessage> chat(String userMessage) {
        try {
            this.userMessage = userMessage;
            String json = mapper.writeValueAsString(this);
            json = target.request().post(Entity.json(json), String.class);
            ChatFrameResponse response = mapper.readValue(json, ChatFrameResponse.class);
            context = response.context;
            return response.response != null ? response.response : Collections.EMPTY_LIST;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String userMessage() {
        return userMessage;
    }

    public String memoryId() {
        return context.memoryId();
    }

    public ChatFrameClientSession memoryId(String memoryId) {
        if (context == null) {
            context = new ClientChatFrameContext();
        }
        context.setMemoryId(memoryId);
        return this;
    }

    public ChatFrameClientSession setUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public ChatFrameClientSession setData(Map<String, Object> data) {
        if (context == null) {
            context = new ClientChatFrameContext();
        }
        context.setData(data);
        return this;
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
            context = new ClientChatFrameContext();
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
            context = new ClientChatFrameContext();
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
            context = new ClientChatFrameContext();
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
    public ChatFrameClientSession setData(String key, Object value) {
        if (context == null) {
            context = new ClientChatFrameContext();
        }
        context.setData(key, value);
        return this;
    }
}

package io.quarkiverse.langchain4j.chat.frames.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.langchain4j.chat.frames.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;

@RequestScoped
@Default
public class ChatContextImpl implements ChatContext {

    Map<String, Object> data = new HashMap<>();

    List<ChatFrameMessage> response = new ArrayList<>();

    String userMessage = null;
    String memoryId = UUID.randomUUID().toString();

    boolean ignoreAIResponse = false;

    @Inject
    ChatFrameController chatFrameController;

    @Override
    public String userMessage() {
        return userMessage;
    }

    @Override
    public String memoryId() {
        return memoryId;
    }

    @Override
    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    @Override
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
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
    @Override
    public Map<String, Object> data() {
        return data;
    }

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    @Override
    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                data.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return type.cast(value);
    }

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    @Override
    public <T> T getData(String key, TypeReference<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                data.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
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
    @Override
    public void setData(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Arbitrary list of response objects serialized to JSON and sent back to client.
     *
     * @return
     */
    @Override
    public List<ChatFrameMessage> response() {
        return response;
    }

    // ChatFrameController methods

    @Override
    public String currentFrameId() {
        return chatFrameController.currentFrameId();
    }

    @Override
    public ChatFrameExecution currentFrame() {
        return chatFrameController.currentFrame();
    }

    @Override
    public ChatFrameExecution getFrame(String name) {
        return chatFrameController.getFrame(name);
    }

    @Override
    public void setFrame(String chatFrame) {
        chatFrameController.setFrame(chatFrame);
    }

    @Override
    public void pushFrame(String chatFrame) {
        chatFrameController.pushFrame(chatFrame);
    }

    @Override
    public void popFrame() {
        chatFrameController.popFrame();
    }

    @Override
    public void popFrame(boolean deleteMessages) {
        chatFrameController.popFrame(deleteMessages);
    }

    @Override
    public void pushFrame(String chatFrame, boolean deleteMessages) {
        chatFrameController.pushFrame(chatFrame, deleteMessages);
    }

    @Override
    public void setFrame(String chatFrame, boolean deleteMessages) {
        chatFrameController.setFrame(chatFrame, deleteMessages);
    }

    @Override
    public void clearMemory() {
        chatFrameController.clearMemory();
    }

}

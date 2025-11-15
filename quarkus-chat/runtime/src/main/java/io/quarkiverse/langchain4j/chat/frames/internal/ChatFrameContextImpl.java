package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.Type;
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

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;

@RequestScoped
@Default
public class ChatFrameContextImpl implements ChatFrameContext {

    Map<String, Object> data = new HashMap<>();
    Map<String, Object> parameters = new HashMap<>();

    List<ChatFrameMessage> response = new ArrayList<>();

    String userMessage = null;
    String systemMessage = null;
    String memoryId = UUID.randomUUID().toString();

    boolean ignoreAIResponse = false;

    boolean wipeScheduled = false;
    boolean wipeAborted = false;

    @Inject
    ObjectMapper mapper;

    @Inject
    ChatFrameController chatFrameController;

    @Override
    public String userMessage() {
        return userMessage;
    }

    @Override
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public String systemMessage() {
        return systemMessage;
    }

    @Override
    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
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
    public Map<String, Object> parameters() {
        return parameters;
    }

    @Override
    public <T> T parameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                parameters.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return type.cast(value);
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    @Override
    public <T> T parameter(String key, TypeReference<T> type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                parameters.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
    }

    @Override
    public <T> T parameter(String key, Type type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, mapper.constructType(type));
                parameters.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                data.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return type.cast(value);
    }

    @Override
    public <T> T getData(String key, TypeReference<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, type);
                data.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
    }

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

    @Override
    public void scheduleWipe() {
        wipeScheduled = true;
    }

    @Override
    public boolean wipeScheduled() {
        return wipeScheduled && !wipeAborted;
    }

    @Override
    public void abortWipe() {
        wipeAborted = true;
    }
}

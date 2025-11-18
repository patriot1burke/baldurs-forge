package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatFrameData {

    Map<String, Object> data = new ConcurrentHashMap<>();
    String name;
    String memoryId;
    ChatFrameData parent;

    public ChatFrameData(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String memoryId() {
        return memoryId;
    }

    public String name() {
        return name;
    }

    public ChatFrameData parent() {
        return parent;
    }

    public void setName(String frame) {
        this.name = frame;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public void setParent(ChatFrameData parent) {
        this.parent = parent;
    }

    ObjectMapper mapper;

    public Map<String, Object> data() {
        return data;
    }

    private Object getInternal(String key, boolean ignoreParent) {
        Object value = data.get(key);
        if (value == null) {
            if (!ignoreParent && parent != null) {
                value = parent.getInternal(key, ignoreParent);
            }
        }
        return value;
    }

    public <T> T getData(String key, Type type) {
        return getData(key, type, false);
    }

    public <T> T getData(String key, Type type, boolean ignoreParent) {
        Object value = getInternal(key, ignoreParent);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            try {
                value = mapper.treeToValue((JsonNode) value, mapper.constructType(type));
                data.put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
    }

    public void setData(String key, Object value) {
        data.put(key, value);
    }
}

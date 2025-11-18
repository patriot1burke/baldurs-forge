package io.quarkiverse.langchain4j.chat.frames.client;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ClientChatFrameData {

    Map<String, Object> data;
    String name;
    String memoryId;
    ClientChatFrameData parent;

    @JsonIgnore
    ObjectMapper mapper;

    void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        if (parent != null) {
            parent.setMapper(mapper);
        }
    }

    public String name() {
        return name;
    }

    public ClientChatFrameData name(String n) {
        this.name = n;
        return this;
    }

    public Map<String, Object> data() {
        if (data == null) {
            data = new ConcurrentHashMap<>();
        }
        return data;
    }

    private Object getInternal(String key, boolean ignoreParent) {
        if (data == null) {
            return null;
        }
        Object value = data.get(key);
        if (value == null) {
            if (!ignoreParent && parent != null) {
                value = parent.getInternal(key, ignoreParent);
            }
        }
        return value;
    }

    public <T> T data(String key, Class<T> type) {
        return data(key, type, false);
    }

    public <T> T data(String key, Type type) {
        return data(key, type, false);
    }

    public <T> T data(String key, Type type, boolean ignoreParent) {
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

    public ClientChatFrameData setData(String key, Object value) {
        if (data == null) {
            data = new ConcurrentHashMap<>();
        }
        data.put(key, value);
        return this;
    }
}

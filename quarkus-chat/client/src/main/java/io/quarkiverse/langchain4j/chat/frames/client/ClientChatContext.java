package io.quarkiverse.langchain4j.chat.frames.client;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ClientChatContextSerializer.class)
@JsonDeserialize(using = ClientChatContextDeserializer.class)
public class ClientChatContext {
    Map<String, Object> data = new HashMap<>();
    JsonNode memory = null;
    String memoryId = null;

    public String memoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setMemory(JsonNode memory) {
        this.memory = memory;
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
    public void setData(String key, Object value) {
        data.put(key, value);
    }
}

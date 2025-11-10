package io.quarkiverse.langchain4j.chat.frames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.RequestScoped;

/**
 * Can be marshalled to and from JSON with the {@link ChatContextReader} class.
 * 
 * Expects the following JSON format for requests:
 * {
 *    "userMessage": "...",
 *    "memoryId": "...",
 *    "context": {
 *       "data": {
 *           "...": "..."
 *       },
 *       "memory": "[...]" // List of Langchain4j chat messages in JSON format
 *    }
 * }
 * }
 * 
 * Outputs the following JSON format for responses:
 * {
 *    "response": "[...]",
 *    "memoryId": "...",
 *    "context": {
 *       "data": {
 *           "...": "..."
 *       },
 *       "memory": "[...]" // List of Langchain4j chat messages in JSON format
 *    }
 * }
 */
@RequestScoped
public class ChatContext {

    Map<String, Object> data = new HashMap<>();

    List<ResponseMessage> response = new ArrayList<>();

    String userMessage = null;
    String memoryId = UUID.randomUUID().toString();

    boolean ignoreAIResponse = false;

    public String userMessage() {
        return userMessage;
    }

    public String memoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

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

    /**
     * Arbitrary list of response objects serialized to JSON and sent back to client.  
     * 
     * @return
     */
    public List<ResponseMessage> response() {
        return response;
    }
}

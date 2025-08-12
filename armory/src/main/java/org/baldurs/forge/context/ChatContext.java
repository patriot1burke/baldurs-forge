package org.baldurs.forge.context;

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
 *       "shared": {
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
 *       "shared": {
 *           "...": "..."
 *       },
 *       "memory": "[...]" // List of Langchain4j chat messages in JSON format
 *    }
 * }
 */
@RequestScoped
public class ChatContext {

    Map<String, Object> serverContext = new HashMap<>();
    Map<String, Object> sharedContext = new HashMap<>();

    List<Object> response = new ArrayList<>();

    String userMessage = null;
    String memoryId = UUID.randomUUID().toString();

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
     * Data used to serve current request. This is not shared or sent back to remote
     * chat client.
     * 
     * @return
     */
    public Map<String, Object> serverContext() {
        return serverContext;
    }

    /**
     * Data serialized and shared with client.
     * 
     * This is raw data and could contain JsonNode objects.
     * Preferably use the {@link #getShared(String, Class)} method to get the data
     * as a specific type.
     * 
     * @return
     */
    public Map<String, Object> sharedContext() {
        return sharedContext;
    }

    /**
     * Data serialized and shared with client.
     * 
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     * 
     * This is an expensive operation as each call to this method will deserialize
     * the value from JSON.
     * 
     * @return
     */
    public <T> T getShared(String key, Class<T> type) {
        Object value = sharedContext.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.treeToValue((JsonNode) value, type);
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
     * This is an expensive operation as each call to this method will deserialize
     * the value from JSON.
     * 
     * @return
     */
    public <T> T getShared(String key, TypeReference<T> type) {
        Object value = sharedContext.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.treeToValue((JsonNode) value, type);
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
    public void setShared(String key, Object value) {
        sharedContext.put(key, value);
    }

    /**
     * Arbitrary list of response objects set back to client.
     * 
     * @return
     */
    public List<Object> response() {
        return response;
    }
}

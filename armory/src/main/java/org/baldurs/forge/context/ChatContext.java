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

    Map<String, Object> sharedContext = new HashMap<>();

    List<Object> response = new ArrayList<>();

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
     * Arbitrary list of response objects serialized to JSON and sent back to client.  
     * 
     * @return
     */
    public List<Object> response() {
        return response;
    }

    /**
     * Set internal state of chat engine to not return the next AI chat response to the client.  AI responses will
     * be ignored only once and only within the current request.
     * 
     * This is useful for cases where tool invocations want to take over the output sent back to the client.
     * @return
     */
    public void pushIgnoreAIResponse() {
        ignoreAIResponse = true;
    }

    /**
     * If true, the AI response will not be sent back as a client response.
     * This will reset the internal ignore flag to false.
     * 
     * @return  whether to ignore the next AI chat response
     */
    public boolean popIgnoreAIResponse() {
        boolean ignore = this.ignoreAIResponse;
        this.ignoreAIResponse = false;
        return ignore;
    }
}

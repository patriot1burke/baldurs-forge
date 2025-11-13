package io.quarkiverse.langchain4j.chat.frames;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Expects the following JSON format for requests:
 * {
 * "userMessage": "...",
 * "memoryId": "...",
 * "context": {
 * "data": {
 * "...": "..."
 * },
 * "memory": "[...]" // List of Langchain4j chat messages in JSON format
 * }
 * }
 * }
 *
 * Outputs the following JSON format for responses:
 * {
 * "response": "[...]",
 * "memoryId": "...",
 * "context": {
 * "data": {
 * "...": "..."
 * },
 * "memory": "[...]" // List of Langchain4j chat messages in JSON format
 * }
 * }
 */
public interface ChatContext {
    String userMessage();

    String memoryId();

    void setMemoryId(String memoryId);

    void setUserMessage(String userMessage);

    /**
     * Data serialized and shared with client.
     *
     * This is raw data and could contain JsonNode objects.
     * Preferably use the {@link #getData(String, Class)} method to get the data
     * as a specific type.
     *
     * @return
     */
    Map<String, Object> data();

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    <T> T getData(String key, Class<T> type);

    /**
     * Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    <T> T getData(String key, TypeReference<T> type);

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
    void setData(String key, Object value);

    /**
     * Arbitrary list of response objects serialized to JSON and sent back to
     * client.
     *
     * @return
     */
    List<ChatFrameMessage> response();

    String currentFrameId();

    ChatFrameExecution currentFrame();

    ChatFrameExecution getFrame(String name);

    /**
     * Clears the stack and sets the chat frame for the given context.
     *
     * @param chatFrame
     */
    void setFrame(String chatFrame);

    /**
     * Clears the stack and sets the chat frame for the given context.
     *
     * @param chatFrame
     * @param deleteMessages if true, deletes the messages for the ChatContext's memoryId.
     */
    void setFrame(String chatFrame, boolean deleteMessages);

    /**
     * Pushes the given chat frame onto the frame stack.
     *
     * Also deletes the messages for the ChatContext's memoryId.
     *
     * @param chatFrame
     */
    void pushFrame(String chatFrame);

    /**
     * Pushes the given chat frame onto the frame stack.
     *
     * @param chatFrame
     * @param deleteMessages if true, deletes the messages for the ChatContext's memoryId.
     */
    void pushFrame(String chatFrame, boolean deleteMessages);

    /**
     * Pops current frame off of the frame stack and also deletes the messages for the ChatContext's current memoryId.
     */
    void popFrame();

    /**
     * Pops current frame off of the frame stack and also deletes the messages for the ChatContext's current memoryId.
     *
     * @param deleteMessages if true, deletes the messages for the ChatContext's memoryId.
     */
    void popFrame(boolean deleteMessages);

    /**
     * Clears chat memory for the ChatContext's current memoryId.
     */
    void clearMemory();
}

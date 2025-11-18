package io.quarkiverse.langchain4j.chat.frames;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
public interface ChatFrameContext {
    String userMessage();

    String systemMessage();

    void setUserMessage(String userMessage);

    void setSystemMessage(String systemMessage);

    /**
     * The memoryId for the current frame.
     *
     * @return
     */
    String memoryId();

    /**
     * Session Data serialized and shared with client.
     *
     * This is raw data and could contain JsonNode objects.
     * Preferably use the {@link #getData(String, Type)} method to get the data
     * as a specific type.
     *
     * @return
     */
    Map<String, Object> data();

    /**
     * Session Data serialized and shared with client.
     *
     * Tools should only add data to the shared context if they need to pass data
     * back to the client.
     *
     * @return
     */
    <T> T getData(String key, Type type);

    /**
     * Session Data serialized and shared with client.
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
     * Removes the data for the given key.
     *
     * @param key
     */
    void removeData(String key);

    /**
     * Arbitrary list of response objects serialized to JSON and sent back to
     * client.
     *
     * @return
     */
    List<ChatFrameMessage> response();

    String currentFrameId();

    ChatFrameExecution currentFrame();

    /**
     * Clears the frame stack and sets the current frame.
     * CLeared frames are deleted along with their data and memory
     *
     * @param chatFrame
     */
    void setFrame(String chatFrame);

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
     * @param deleteMessages if true, deletes the chat memory for the current frame before pushing the new frame
     */
    void pushFrame(String chatFrame, boolean deleteMemory);

    /**
     * Pops current frame off of the frame stack and also deletes chat data and memory for that frame.
     */
    void popFrame();

    /**
     * Clears chat memory for current frame
     */
    void clearMemory();

    /**
     * Schedule a wipe of chat memory for current frame.
     */
    void scheduleWipe();

    /**
     * Abort a scheduled wipe of chat memory for current frame.
     * An abort cannot be canceled or overridden.
     */
    void abortWipe();

    /**
     * Checks if a wipe of chat memory is scheduled for current frame.
     *
     * @return true if a wipe is scheduled, false otherwise
     */
    boolean wipeScheduled();
}

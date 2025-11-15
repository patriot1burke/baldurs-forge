package io.quarkiverse.langchain4j.chat.frames;

/**
 * Manages and routes chat frames
 */
public interface ChatFrameController {

    void register(String name, ChatFrameExecution chatFrame);

    void setDefaultFrame(String chatFrame);

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

    /**
     * Executes the current chat frame for the given context. If the current chat frame is not found, executes the default chat
     * frame.
     */
    void chat();

    /**
     * Executes the current chat frame for the given context. If the current chat frame is not found, executes the fallback
     * frame.
     *
     * @param fallbackFrame
     */
    void chat(String fallbackFrame);

}
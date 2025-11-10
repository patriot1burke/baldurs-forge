package io.quarkiverse.langchain4j.chat.frames;

public interface ChatFrameManager {

    void register(String name, ChatFrameExecution chatFrame);

    void setDefaultFrame(ChatFrameExecution chatFrame);

    String currentFrameId();

    ChatFrameExecution currentFrame();

    ChatFrameExecution getFrame(String name);

    /**
     * Clears the stack and sets the chat frame for the given context.
     * @param chatFrame
     */
    void setFrame(String chatFrame);

    /**
     * Pushes the given chat frame onto the frame stack.
     * 
     * @param chatFrame
     */
    void pushFrame(String chatFrame);

    /**
     * Pops current frame off of the frame stack and also deletes the messages for the ChatContext's memoryId.
     */
    void popFrame();

    void chat();

}
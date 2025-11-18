package io.quarkiverse.langchain4j.chat.frames;

/**
 * Manages and routes chat frames
 */
public interface ChatFrameController {

    void register(String name, ChatFrameExecution chatFrame);

    void setDefaultFrame(String chatFrame);

    ChatFrameExecution getFrame(String name);

    boolean hasFrame(String name);

    void chat(ChatFrameContext context);
}
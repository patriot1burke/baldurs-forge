package io.quarkiverse.langchain4j.chat.frames.client;

public class ClientTextMessage extends ClientChatFrameMessage {

    protected String text;

    public String getText() {
        return text;
    }
}

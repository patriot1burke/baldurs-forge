package io.quarkiverse.langchain4j.chat.frames.client;

public class ClientStringMessage extends ClientChatEvent {

    protected String string;

    public String getString() {
        return string;
    }
}

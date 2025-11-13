package io.quarkiverse.langchain4j.chat.frames;

public class ChatFrameMessage {
    protected String type;

    protected ChatFrameMessage(String name) {
        this.type = name;
    }

    public ChatFrameMessage() {
    }

    public String getType() {
        return type;
    }

    public void setType(String name) {
        this.type = name;
    }
}

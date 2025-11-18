package io.quarkiverse.langchain4j.chat.frames;

public class ChatEvent {
    protected String type;

    protected ChatEvent(String name) {
        this.type = name;
    }

    public ChatEvent() {
    }

    public String getType() {
        return type;
    }

    public void setType(String name) {
        this.type = name;
    }
}

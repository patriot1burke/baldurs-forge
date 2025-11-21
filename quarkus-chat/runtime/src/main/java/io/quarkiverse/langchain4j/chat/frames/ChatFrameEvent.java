package io.quarkiverse.langchain4j.chat.frames;

public final class ChatFrameEvent {
    String type;
    Object value;

    public ChatFrameEvent(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public static ChatFrameEvent stringMessage(String message) {
        return new ChatFrameEvent("StringMessage", message);
    }

    public static ChatFrameEvent objectMessage(Object value) {
        return new ChatFrameEvent("ObjectMessage", value);
    }

}

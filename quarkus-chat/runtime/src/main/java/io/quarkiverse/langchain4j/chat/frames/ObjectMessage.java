package io.quarkiverse.langchain4j.chat.frames;

/**
 * A message that contains an arbitrary object.
 * It will be serialized to JSON and sent back to the client.
 */
public class ObjectMessage extends ChatFrameMessage {

    protected Object message;

    public ObjectMessage(Object message) {
        super("Message");
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }
}

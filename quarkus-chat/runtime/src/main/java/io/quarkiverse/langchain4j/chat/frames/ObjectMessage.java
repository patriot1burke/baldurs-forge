package io.quarkiverse.langchain4j.chat.frames;

/**
 * A message that contains an arbitrary object.
 * It will be serialized to JSON and sent back to the client.
 */
public class ObjectMessage extends ChatEvent {

    protected Object message;

    public ObjectMessage(Object message) {
        super("ObjectMessage");
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public static ObjectMessage from(Object message) {
        return new ObjectMessage(message);
    }
}

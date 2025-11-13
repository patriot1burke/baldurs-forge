package io.quarkiverse.langchain4j.chat.frames;

/**
 * A message that contains a text string.
 * It will be serialized to JSON and sent back to the client.
 */
public class StringMessage extends ChatFrameMessage {

    protected String string;

    public StringMessage(String string) {
        super("String");
        this.string = string;
    }

    public String getString() {
        return string;
    }
}

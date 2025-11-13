package io.quarkiverse.langchain4j.chat.frames;

/**
 * A message that contains a text string.
 * It will be serialized to JSON and sent back to the client.
 */
public class TextMessage extends ChatFrameMessage {

    protected String text;

    public TextMessage(String text) {
        super("Text");
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

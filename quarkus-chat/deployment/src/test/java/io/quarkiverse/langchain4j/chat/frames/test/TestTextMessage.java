package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.StringMessage;

public class TestTextMessage extends StringMessage {
    public TestTextMessage(String string) {
        super(string);
    }

    public static TestTextMessage from(String string) {
        return new TestTextMessage("TestText:" + string);
    }
}

package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.StringMessage;

public class TestIntegerMessage extends StringMessage {
    public TestIntegerMessage(String string) {
        super(string);
    }

    public static TestIntegerMessage from(int i) {
        return new TestIntegerMessage("TestInteger:" + i);
    }

    public static TestIntegerMessage from(Integer i) {
        return new TestIntegerMessage("TestInteger:" + i);
    }

}

package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameEvent;

public class TestMapper {
    public static ChatFrameEvent from(int i) {
        return ChatFrameEvent.stringMessage("TestInteger:" + i);
    }

    public static ChatFrameEvent from(Integer i) {
        return ChatFrameEvent.stringMessage("TestInteger:" + i);
    }

    public static ChatFrameEvent from(String string) {
        return ChatFrameEvent.stringMessage("TestText:" + string);
    }
}

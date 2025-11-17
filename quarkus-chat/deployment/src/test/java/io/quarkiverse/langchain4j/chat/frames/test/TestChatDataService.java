package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.FrameInject;

public class TestChatDataService {

    @ChatFrame("frame-data")
    public String frameData(ChatFrameContext context, @FrameInject String test) {
        if (test == null) {
            context.setData("test", "test");
            return "start";
        }
        return test;
    }

}

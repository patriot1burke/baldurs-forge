package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.FrameData;

public class TestChatDataService {

    @ChatFrame("frame-data")
    public String frameData(ChatFrameContext context, @FrameData String test) {
        if (test == null) {
            context.setData("test", "test");
            return "start";
        }
        return test;
    }

}

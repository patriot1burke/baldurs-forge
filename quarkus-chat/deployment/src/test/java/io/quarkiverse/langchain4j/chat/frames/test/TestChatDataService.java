package io.quarkiverse.langchain4j.chat.frames.test;

import org.junit.jupiter.api.Assertions;

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

    @ChatFrame("sent-data")
    public String callWithSentData(String sentData) {

        return sentData;
    }

    @ChatFrame("call-nested")
    public String callNested(ChatFrameContext context, String sentData, String parentData, String override) {
        Assertions.assertNull(sentData);
        if (parentData == null) {
            context.setData("parentData", "parentData");
            context.setData("override", "parent");
            context.pushFrame("nested");
            Assertions.assertEquals("nested", context.currentFrameId());
            context.setData("override", "nested");
            context.setData("newData", "nestedNewData");
            return "first";
        }
        Assertions.assertEquals("parentData", parentData);
        Assertions.assertEquals("parent", override);
        Assertions.assertNull(context.getData("newData", String.class));
        return "second";
    }

    @ChatFrame("nested")
    public String nested(ChatFrameContext context, String newData, String parentData, String override) {
        Assertions.assertEquals("parentData", parentData);
        Assertions.assertEquals("nested", override);
        Assertions.assertEquals("nestedNewData", newData);
        Assertions.assertEquals("nested", context.currentFrameId());
        context.popFrame();
        Assertions.assertEquals("call-nested", context.currentFrameId());
        return "nested";

    }

}

package io.quarkiverse.langchain4j.chat.frames.test;

import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.EventMapper;

public class ResponseChatService {

    @ChatFrame("text")
    @EventMapper(TestMapper.class)
    public String stringResponse() {
        return "stringResponse";
    }

    @ChatFrame("int")
    @EventMapper(TestMapper.class)
    public int intResponse() {
        return 123;
    }

    @ChatFrame("multi-result")
    @EventMapper(TestMapper.class)
    public Result<String> multiResult() {
        MockResult<String> result = new MockResult<String>(null);
        result.addToolResult("textResult");
        result.addToolResult(123);
        return result;
    }
}

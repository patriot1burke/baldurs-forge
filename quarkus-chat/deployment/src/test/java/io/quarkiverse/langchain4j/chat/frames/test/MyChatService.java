package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.DefaultChatFrame;

@ApplicationScoped
public class MyChatService {
    @Inject
    ChatFrameContext context;

    @ChatFrame
    @DefaultChatFrame
    public void defaultChat() {
        context.addEvent("defaultChat:" + context.userMessage());
    }

    @ChatFrame
    public void chatone() {
        context.addEvent("one:" + context.userMessage());
    }

    @ChatFrame("exception")
    public void exception() {
        throw new RuntimeException("test exception");
    }

    @ChatFrame("two")
    public void chatTwo(@MemoryId String memoryId, @UserMessage String userMessage, ChatFrameContext ctx) {
        Assertions.assertNotNull(memoryId);
        Assertions.assertNotNull(userMessage);
        Assertions.assertNotNull(ctx);
        ctx.addEvent("two:" + userMessage);
    }

    @ChatFrame("string-result")
    public String stringResult() {
        return "string-result";
    }

    @ChatFrame("null-string")
    public String nullString() {
        return null;
    }

    @ChatFrame("result")
    public Result<String> result() {
        return new MockResult<String>("result");
    }

    @ChatFrame("execution")
    public Result<String> resultWithExecution() {
        MockResult<String> result = new MockResult<String>(null);
        result.addToolResult("result-with-execution");
        return result;
    }

    @ChatFrame("null-result")
    public Result<String> nullResult() {
        MockResult<String> result = new MockResult<String>(null);
        return result;
    }

    @ChatFrame("null-execution")
    public Result<String> nullResultWithExecution() {
        MockResult<String> result = new MockResult<String>(null);
        result.addToolResult(null);
        return result;
    }
}

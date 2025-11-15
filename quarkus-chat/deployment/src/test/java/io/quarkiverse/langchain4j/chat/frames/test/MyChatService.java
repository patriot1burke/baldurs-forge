package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.DefaultChatFrame;
import io.quarkiverse.langchain4j.chat.frames.StringMessage;

@ApplicationScoped
public class MyChatService {
    @Inject
    ChatFrameContext context;

    @ChatFrame
    @DefaultChatFrame
    public void defaultChat() {
        context.response().add(new StringMessage("defaultChat:" + context.userMessage()));
    }

    @ChatFrame
    public void chatone() {
        context.response().add(new StringMessage("one:" + context.userMessage()));
    }

    @ChatFrame("two")
    public void chatTwo(@MemoryId String memoryId, @UserMessage String userMessage, ChatFrameContext ctx) {
        Assertions.assertNotNull(memoryId);
        Assertions.assertNotNull(userMessage);
        Assertions.assertNotNull(ctx);
        ctx.response().add(new StringMessage("two:" + userMessage));
    }
}

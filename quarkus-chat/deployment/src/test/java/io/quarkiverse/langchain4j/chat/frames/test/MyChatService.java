package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.langchain4j.chat.frames.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.DefaultChatFrame;
import io.quarkiverse.langchain4j.chat.frames.TextMessage;

@ApplicationScoped
public class MyChatService {
    @Inject
    ChatContext context;

    @ChatFrame
    @DefaultChatFrame
    public void defaultChat() {
        context.response().add(new TextMessage("defaultChat:" + context.userMessage()));
    }

    @ChatFrame
    public void chatone() {
        context.response().add(new TextMessage("one:" + context.userMessage()));
    }

    @ChatFrame("two")
    public void chatTwo() {
        context.response().add(new TextMessage("two:" + context.userMessage()));
    }
}

package io.quarkiverse.langchain4j.chat.frames.test;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;

public interface AnotherChat {

    @ChatFrame("another-chat")
    String hello(@UserMessage String message);
}
package io.quarkiverse.langchain4j.chat.frames.samples;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;

@ApplicationScoped
public class ChatService {

    @ChatFrame("hello")
    public String hello(@UserMessage String message) {
        return "Hello, " + message;
    }
}

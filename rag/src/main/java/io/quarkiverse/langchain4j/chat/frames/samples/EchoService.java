package io.quarkiverse.langchain4j.chat.frames.samples;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;

@ApplicationScoped
public class EchoService {

    @ChatFrame("echo")
    //@DefaultChatFrame
    public String echo(@UserMessage String message) {
        // for fast dev of UI
        return message;
    }
}

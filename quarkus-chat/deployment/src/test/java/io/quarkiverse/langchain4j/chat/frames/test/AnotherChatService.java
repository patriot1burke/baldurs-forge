package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@ApplicationScoped
@Default
public class AnotherChatService implements AnotherChat {

    @Override
    public String hello(String message) {
        return "hello:" + message;
    }

}

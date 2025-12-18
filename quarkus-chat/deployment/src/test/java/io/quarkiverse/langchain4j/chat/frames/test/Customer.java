package io.quarkiverse.langchain4j.chat.frames.test;

import io.quarkiverse.langchain4j.chat.frames.EventType;

@EventType("customer")
public record Customer(String name, String email) {

}

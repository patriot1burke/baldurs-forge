package io.quarkiverse.langchain4j.chat.frames.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ClientChatEvent {
    protected ClientChatEvent() {

    }
}

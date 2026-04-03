package org.baldurs.forge.messages;

import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("PushRoute")
public class PushRoute {
    @JsonValue
    String description;

    public PushRoute(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}

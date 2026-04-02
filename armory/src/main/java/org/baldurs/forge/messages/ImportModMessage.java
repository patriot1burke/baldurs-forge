package org.baldurs.forge.messages;

import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("ImportMod")
public class ImportModMessage {
    @JsonValue
    protected String filename;

    public ImportModMessage(String filename) {
        this.filename = filename;
    }
}

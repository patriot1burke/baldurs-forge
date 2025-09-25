package org.baldurs.forge.builder;

public interface BuilderPrompt {
    public String build(String memoryId, String type, String schema, String currentJson, String userMessage);

}

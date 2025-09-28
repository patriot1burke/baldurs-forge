package org.baldurs.forge.builder;

import dev.langchain4j.service.Result;

public interface BuilderPrompt {
    public Result<String> build(String memoryId, String type, String schema, String currentJson, String userMessage);

}

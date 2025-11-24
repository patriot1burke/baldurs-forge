package org.baldurs.forge.builder;

import jakarta.enterprise.context.SessionScoped;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@SessionScoped
@RegisterAiService
public interface RingBuilderPrompt extends BuilderPrompt {
    @Override
    @SystemMessage(fromResource = "prompts/equipmentBuilder.txt")
    @ToolBox({ RingBuilder.class })
    public Result<String> build(String type, String schema, String currentJson,
            @UserMessage String userMessage);
}

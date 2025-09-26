package org.baldurs.forge.builder;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
public interface RingBuilderPrompt extends BuilderPrompt {
    @Override
    @SystemMessage(fromResource = "prompts/equipmentBuilder.txt")
    @ToolBox({RingBuilder.class})
    public String build(@MemoryId String memoryId, String type, String schema, String currentJson, @UserMessage String userMessage);
}
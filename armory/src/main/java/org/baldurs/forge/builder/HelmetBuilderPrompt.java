package org.baldurs.forge.builder;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;

@ChatScoped
@RegisterAiService
public interface HelmetBuilderPrompt extends BuilderPrompt {
    @Override
    @SystemMessage(fromResource = "prompts/equipmentBuilder.txt")
    @ToolBox({ HelmetBuilder.class })
    public Result<String> build(String type, String schema, String currentJson,
            @UserMessage String userMessage);
}

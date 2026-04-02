package org.baldurs.forge.builder;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;

@RegisterAiService
@ChatScoped
public interface BodyArmorBuilderPrompt extends BuilderPrompt {

    @SystemMessage(fromResource = "prompts/equipmentBuilder.txt")
    @ToolBox({ BodyArmorBuilder.class })
    @Override
    public Result<String> build(String type, String schema, String currentJson,
            @UserMessage String userMessage);
}

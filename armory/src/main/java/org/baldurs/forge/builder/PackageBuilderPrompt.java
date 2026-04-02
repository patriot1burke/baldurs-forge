package org.baldurs.forge.builder;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;

@RegisterAiService
@ChatScoped
public interface PackageBuilderPrompt {
    @SystemMessage(fromResource = "prompts/equipmentModPackager.txt")
    @ToolBox({ PackageBuilder.class })
    public Result<String> buildPackage(@UserMessage String userMessage, String schema,
            String currentJson);
}

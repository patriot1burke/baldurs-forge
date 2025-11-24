package org.baldurs.forge.builder;

import jakarta.enterprise.context.SessionScoped;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
@SessionScoped
public interface ModPackagePrompt {
    @SystemMessage(fromResource = "prompts/equipmentModPackager.txt")
    @ToolBox({ ModPackager.class })
    public Result<String> packageMod(@UserMessage String userMessage, String schema,
            String currentJson);
}

package org.baldurs.forge.builder;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
public interface ModPackagePrompt {
    @SystemMessage(fromResource="prompts/equipmentModPackager.txt")
    @ToolBox({ModPackager.class})
    public Result<String> packageMod(@MemoryId String memoryId, @UserMessage String userMessage,  String schema, String currentJson);
}
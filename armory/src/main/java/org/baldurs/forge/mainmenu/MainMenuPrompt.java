package org.baldurs.forge.mainmenu;

import jakarta.enterprise.context.RequestScoped;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RequestScoped
@RegisterAiService
public interface MainMenuPrompt {

    @SystemMessage(fromResource = "prompts/mainMenuCommands.txt")
    @ToolBox({ MainMenuChatFrame.class })
    Result<String> chat(@MemoryId String memoryId, @UserMessage String message);
}

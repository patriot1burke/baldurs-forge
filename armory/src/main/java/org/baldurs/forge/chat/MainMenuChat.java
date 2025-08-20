package org.baldurs.forge.chat;

import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface MainMenuChat extends ChatFrame {

    @SystemMessage(fromResource = "prompts/mainMenuCommands.txt")
    @ToolBox({MainMenuCommands.class})
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

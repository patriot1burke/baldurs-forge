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
public interface BaldursForgeChat extends BaldursChat {

    @SystemMessage("""
                You are a natural language command interface for Baldur's Forge which is a Baldur's Gate 3 equipment database and mod creator. 
                Analyze the user request and forward the request to the appropriate tool.
 
    """)
    @ToolBox({EquipmentQueryCommands.class})
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    String generateBoost(@MemoryId String memoryId, @UserMessage String message);
}

package org.baldurs.forge.chat;

import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface BaldursForgeChat {

    @SystemMessage("""
                You are a natural language command interface for Baldur's Forge which is a Baldur's Gate 3 equipment database and mod creator. 
                Analyze the user request and forward the request to the appropriate tool.
 
    """)
    @ToolBox({LibraryCommands.class, EquipmentQueryCommands.class})
    String chat(@UserMessage String message);
}

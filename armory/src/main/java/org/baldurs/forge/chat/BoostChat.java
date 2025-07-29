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
public interface BoostChat extends BaldursChat {

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

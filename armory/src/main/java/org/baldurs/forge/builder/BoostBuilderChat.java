package org.baldurs.forge.builder;

import org.baldurs.forge.chat.BaldursChat;
import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface BoostBuilderChat extends BaldursChat {

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    @Tool(name="createBoostMacro", value="Translates a boost description into a boost macro.  Boosts add abilities to armor and weapons.")
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

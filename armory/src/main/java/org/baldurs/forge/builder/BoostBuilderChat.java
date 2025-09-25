package org.baldurs.forge.builder;

import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface BoostBuilderChat {

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    @Tool("Translates a boost description into a boost macro.  Boosts add abilities and stat bonuses and enchantments to items.")
    String createBoostMacro(@UserMessage String message);
}

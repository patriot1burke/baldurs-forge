package org.baldurs.forge.builder;

import jakarta.enterprise.context.RequestScoped;

import org.baldurs.forge.TemporaryChatMemoryProvider;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RequestScoped
@RegisterAiService(chatMemoryProviderSupplier = TemporaryChatMemoryProvider.class)
public interface BoostBuilderPrompt {

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    @Tool("Translates a boost description into a boost macro.  Boosts add abilities and stat bonuses and enchantments to items.")
    String createBoostMacro(@UserMessage String message);
}

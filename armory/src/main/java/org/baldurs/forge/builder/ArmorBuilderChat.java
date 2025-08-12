package org.baldurs.forge.builder;

import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface ArmorBuilderChat {

    @SystemMessage(fromResource = "prompts/armorBuilder.txt")
    @ToolBox({BodyArmorBuilder.class, BoostBuilderChat.class})
    public String buildBodyArmor(@MemoryId String memoryId, String type, String schema, String currentJson, @UserMessage String userMessage);
}
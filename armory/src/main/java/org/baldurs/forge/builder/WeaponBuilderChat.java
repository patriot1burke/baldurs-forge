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
public interface WeaponBuilderChat extends BuilderChat {
    @Override
    @SystemMessage(fromResource = "prompts/equipmentBuilder.txt")
    @ToolBox({WeaponBuilder.class})
    public String build(@MemoryId String memoryId, String type, String schema, String currentJson, @UserMessage String userMessage);
}
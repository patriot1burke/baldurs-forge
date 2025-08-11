package org.baldurs.forge.builder;

import org.baldurs.forge.context.ClientMemoryProvider;
import org.baldurs.forge.context.MessageWindowClientMemoryProvider;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = MessageWindowClientMemoryProvider.class)
public interface ArmorBuilderChat {

    @SystemMessage(fromResource = "prompts/armorBuilder.txt")
    @ToolBox({BodyArmorBuilder.class, BoostBuilderChat.class})
    public String buildBodyArmor(@MemoryId String memoryId, String type, String schema, String currentJson, @UserMessage String userMessage);
}
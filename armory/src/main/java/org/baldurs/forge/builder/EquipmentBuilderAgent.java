package org.baldurs.forge.builder;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
@RegisterAiService(chatMemoryProviderSupplier = JsonChatMemoryProvider.class)
public interface EquipmentBuilderAgent {

    @SystemMessage("""
        You are a helpful assistant that builds armor or weapon equipment for a game.
        You are building a JSON document based on the following JSON schema:

        {schema}

        Ask the user questions until they have provided all the information needed to build the equipment json document.
        When finished, call the finishEquipment tool to finish the equipment.

        """)
    @ToolBox(EquipmentBuilder.class)
    public String buildEquipment(String schema, @UserMessage String userMessage);
}
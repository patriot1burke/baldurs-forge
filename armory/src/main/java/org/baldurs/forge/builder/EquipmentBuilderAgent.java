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
public interface EquipmentBuilderAgent {

    @SystemMessage("""
        You are a helpful assistant that builds armor or weapon equipment for a game.
        You are building a JSON document based on the following JSON schema:

        {schema}

        This is the current JSON document: 
        
        {currentJson}

        Ask the user questions until they have provided all the required information needed to build the equipment json document.
        Add field values from any information you can infer from the user message.
        Make sure to update the current JSON document whenever you get new information from user using the updateEquipment tool.
        When all required fields are filled, ask the user if they want to fill in any optional fields.
        If all required fields are filled, summarize the created item and ask the user if they are finished or not.  If they are finished, call the tool 'finishEquipment' to finish the equipment.

        """)
    @ToolBox({EquipmentBuilder.class, BoostBuilderChat.class})
    public String buildEquipment(@MemoryId String memoryId, String schema, String currentJson, @UserMessage String userMessage);
}
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
public interface BaldursForgeChat extends BaldursChat {

    @SystemMessage("""
                You are a natural language command interface for Baldur's Forge which is a Baldur's Gate 3 equipment database and mod creator. 
                Analyze the user request and forward the request to the appropriate tool.

                If the user is looking for a specific armor or weapon by name, then forward the request to the 'findEquipmentByName' tool.
                If the user is doing a broader search for armor or weapons, then forward the request to the 'searchEquipmentDatabase' tool.
                If the user is looking to create a new body armor, then forward the request to the 'createNewBodyArmor' tool.
                If 'findEquipmentByName' cannot find the equipment, then forward the request to the 'searchEquipmentDatabase' tool.
                If the user is looking for data attribute values, then forward the request to the 'findDataAttributeValues' tool.
                If the user is looking for unique icons for a given armor type, then forward the request to the 'findUniqueIconsForArmorType' tool.

                If you cannot determine the appropriate tool to invoke, then summarize the tool actions that are available to the user and
                ask the user to clarify their request.

                Examples:
                Input: Show me the Ring of Protection
                Action: invoke the 'findEquipmentByName' tool with the argument 'Show me the Ring of Protection'

                Input: Show me all the armor in the database
                Action: invoke the 'searchEquipmentDatabase' tool with the argument 'Show me all the armor in the database'

                Input: Create new leather armor
                Action: invoke the 'createNewBodyArmor' tool with the argument 'Create new leather armor'

                Input: I want to create new chainmail armor
                Action: invoke the 'createNewBodyArmor' tool with the argument 'I want to create new chainmail armor'

                Input: Find attribute values for ArmorType
                Action: invoke the 'findDataAttributeValues' tool with the argument 'ArmorType'

                Input: Find data values for ProficiencyGroup
                Action: invoke the 'findDataAttributeValues' tool with the argument 'ProficiencyGroup'

                Input: Find unique icons for armor type 'HalfPlate'
                Action: invoke the 'findUniqueIconsForArmorType' tool with the argument 'HalfPlate'
 
    """)
    @ToolBox({ChatCommands.class})
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage(fromResource = "prompts/nl2boost.txt")
    String generateBoost(@MemoryId String memoryId, @UserMessage String message);
}

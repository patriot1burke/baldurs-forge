package org.baldurs.forge.mainmenu;

import org.baldurs.forge.TemporaryChatMemoryProvider;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
@RegisterAiService(chatLanguageModelSupplier = StrictJsonSchemaChatModelProvider.class, chatMemoryProviderSupplier = TemporaryChatMemoryProvider.class)
public interface MetadataPrompts {

    @SystemMessage(fromResource = "prompts/equipmentType.txt")
    EquipmentType equipmentType(@UserMessage String query);

    @SystemMessage(fromResource = "prompts/equipmentSlot.txt")
    EquipmentSlot equipmentSlot(@UserMessage String query);
    
}

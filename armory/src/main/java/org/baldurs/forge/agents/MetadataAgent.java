package org.baldurs.forge.agents;

import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatLanguageModelSupplier = StrictJsonSchemaChatModelProvider.class)
public interface MetadataAgent {

    @SystemMessage(fromResource = "prompts/equipmentType.txt")
    EquipmentType equipmentType(@UserMessage String query);

    @SystemMessage(fromResource = "prompts/equipmentSlot.txt")
    EquipmentSlot equipmentSlot(@UserMessage String query);
    
}

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

    @SystemMessage("""
            Your task is to extract metadata from a user query about searching a Baldur's Gate 3 equipment database.
            From the user's query, determine the type of equipment they are asking about based on these rules:

            - If the user is asking about any kind of weapon, the value Weapon should be used.
            - If the user is asking about any kind of armor, the value Armor should be used.
            - Necklaces and rings and amulets and cloaks are armor too.
            - If it is not known what type of equipment the user is asking about, the value All should be used.
                    """)
    EquipmentType equipmentType(@UserMessage String query);

    @SystemMessage("""
            From the user's query, determine the slot of equipment they are asking about.
            These are the rules for determining the slot:
            - Anything that can be worn on the head is a Helmet
            - Anything that can be worn on the hands like gloves or gauntlets is a Gloves
            - Anything that can be worn on the feet is a Boots
            - Anything that can be worn on the neck is a Amulet
            - Anything that can be worn on fingers is a Ring
            - If the user is asking about a weapon that can shoot, the value Ranged should be used.
            - If the user is asking about a weapon that does not shoot, the value Melee should be used.
            - If the user is asking about equipment that can be used as a shield, the value Offhand should be used.
            - If it is not known what slot of equipment the user is asking about, the value Unknown should be used.
            """)
    EquipmentSlot equipmentSlot(@UserMessage String query);
    
}

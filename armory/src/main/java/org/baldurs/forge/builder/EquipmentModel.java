package org.baldurs.forge.builder;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.langchain4j.model.output.structured.Description;

import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;

@Description("Defines attributes for armor or weapon equipment")
public record EquipmentModel(
                @JsonProperty(required = true)
                @Description("The type of equipment")
                EquipmentType type,
                @JsonProperty(required = true)
                @Description("The slot of the equipment")
                EquipmentSlot slot,
                @JsonProperty(required = true)
                @Description("The rarity of the equipment")
                Rarity rarity,
                @JsonProperty(required = true)
                @Description("The name of the equipment")
                String name,
                @JsonProperty(required = true)
                @Description("The description of the equipment")
                String description,
                @JsonProperty(required = false)
                @Description("The armor class of the equipment.  Not required for weapons.")
                int armorClass,
                @JsonProperty(required = false)
                @Description("The weapon type of the equipment.  Not required for armor.")
                String weaponType,
                @JsonProperty(required = false)
                @Description("The armor type of the equipment.  Not required for weapons.")
                String armorType
                ) {

}

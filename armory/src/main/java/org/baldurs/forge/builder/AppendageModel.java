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

public class AppendageModel extends BaseModel {
    public ArmorCategory armorCategory = ArmorCategory.None;

    public AppendageModel(
                ArmorCategory category,
                Rarity rarity,
                String name,
                String description,
                String boostMacro
                ) {
        super(rarity, name, description, boostMacro);
        this.armorCategory = category;
    }

    public AppendageModel() {
    }

}

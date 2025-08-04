package org.baldurs.forge.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;

import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;

public class BodyArmorModel extends BaseModel {
    public BodyArmorType type;
    public Integer armorClass;

    public BodyArmorModel(
                BodyArmorType type,
                Rarity rarity,
                String name,
                String description,
                Integer armorClass, 
                String boostMacro
                ) {
        super(rarity, name, description, boostMacro);
        this.armorClass = armorClass;
        this.type = type;
    }

    public BodyArmorModel() {
    }

    public static final String schema;

    static {
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                                        .addEnumProperty("type", Arrays.stream(BodyArmorType.values()).map(BodyArmorType::name).toList(), "The type of body armor")
                                        .addEnumProperty("rarity", Arrays.stream(Rarity.values()).map(Rarity::name).toList(), "The rarity of the bodyarmor")
                                        .addStringProperty("name", "The name of the body armor")
                                        .addStringProperty("description", "The description of the body armor")
                                        .addIntegerProperty("armorClass", "The armor class of the equipment.")
                                        .addStringProperty("boosts", "The boosts for the body armor.")
                                        .required(Arrays.asList("type", "rarity", "name"))
                                        .build();
        builder.name(BodyArmorModel.class.getSimpleName())
               .rootElement(rootElement);
        schema = builder.build().toString();
    }

}

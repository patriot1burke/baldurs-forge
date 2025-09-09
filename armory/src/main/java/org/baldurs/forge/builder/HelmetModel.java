package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.output.JsonSchemas;

public class HelmetModel extends AppendageModel {

    public HelmetModel(
            Rarity rarity,
            String name,
            String description,
            String boostMacro,
            ArmorCategory category,
            String parentModel) {
        super(category, rarity, name, description, boostMacro, parentModel);
    }

    public HelmetModel() {

    }
    
    public static final String schema;

    static {
        schema = SchemaUtil.schema(HelmetModel.class, "helmet");
    }

    @Override
    public String schema() {
        return schema;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String baseStat() {
        if (armorCategory == null || armorCategory == ArmorCategory.None) {
            return "ARM_Circlet";
        } else if (armorCategory == ArmorCategory.Light) {
            return "ARM_Helmet_Leather";
        } else if (armorCategory == ArmorCategory.Medium) {
            return "ARM_Helmet_Metal";
        } else if (armorCategory == ArmorCategory.Heavy) {
            return "ARM_Helmet_Metal";
        }
        return "ARM_Circlet";
    }

    public static final String TYPE = "helmet";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Helmet;
        return equipment;
    }

}

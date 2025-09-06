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
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.BoostService.BoostWriter;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

public class BodyArmorModel extends BaseModel {
    @Description("The type of body armor")
    @JsonProperty(required = true)
    public BodyArmorType type;
    @Description("The armor class of the body armor.  This will default to the base armor class of the body armor type.")
    public Integer armorClass;

    public BodyArmorModel(
                BodyArmorType type,
                Rarity rarity,
                String name,
                String description,
                Integer armorClass, 
                String boosts,
                String parentModel
                ) {
        super(rarity, name, description, boosts, parentModel);
        this.armorClass = armorClass;
        this.type = type;
    }

    public BodyArmorModel() {
    }

    public static final String schema;

    static {
        schema = SchemaUtil.schema(BodyArmorModel.class, "bodyArmor");
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
        return type == null ? null : type.baseStat;
    }

    public static final String TYPE = "bodyArmor";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        StatsArchive.Stat stat = library.archive().getStats().getByName(type.baseStat);
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Breast;
        if (armorClass == null) {
            equipment.armorClass = Integer.parseInt(stat.getField("ArmorClass"));
        } else {
            equipment.armorClass = armorClass;
        }
        return equipment;
    }

}

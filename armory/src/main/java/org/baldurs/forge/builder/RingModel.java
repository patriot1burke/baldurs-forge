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

public class RingModel extends BaseModel {

    public RingModel(
            Rarity rarity,
            String name,
            String description,
            String boosts,
            String parentModel) {
        super(rarity, name, description, boosts, parentModel);
    }

    public static final String schema;

    static {
        schema = SchemaUtil.schema(RingModel.class, "ring");
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
        return "ARM_Ring";
    }

    public static final String TYPE = "ring";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Ring;
        return equipment;
    }
}

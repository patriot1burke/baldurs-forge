package org.baldurs.forge.builder;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

public class BootsModel extends AppendageModel {

    public BootsModel(
            Rarity rarity,
            String name,
            String description,
            String boostMacro,
            ArmorCategory category,
            String parentModel) {
        super(category, rarity, name, description, boostMacro, parentModel);
    }

    public BootsModel() {
    }

    public static final String schema;

    static {
        schema = SchemaUtil.schema(BootsModel.class, "boots");
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
        return "ARM_Boots_Leather";
    }

    public static final String TYPE = "boots";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Boots;
        return equipment;
    }

}

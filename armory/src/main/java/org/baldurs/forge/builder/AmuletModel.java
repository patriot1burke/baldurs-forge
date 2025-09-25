package org.baldurs.forge.builder;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

public class AmuletModel extends BaseModel {

    public AmuletModel(
            Rarity rarity,
            String name,
            String description,
            String boosts,
            String parentModel) {
        super(rarity, name, description, boosts, parentModel);
    }

    public AmuletModel() {
    }

    public static final String schema;

    static {
        schema = SchemaUtil.schema(AmuletModel.class, "amulet");
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
        return "ARM_Amulet";
    }

    public static final String TYPE = "amulet";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Amulet;
        return equipment;
    }
}

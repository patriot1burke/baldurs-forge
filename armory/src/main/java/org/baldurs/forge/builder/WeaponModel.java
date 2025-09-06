package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

public class WeaponModel extends BaseModel {
    @Description("The type of weapon")
    @JsonProperty(required = true)
    public WeaponType type;

    @Description("Whether the weapon is magical")
    @JsonProperty(required = true)
    public Boolean magical;

    public WeaponModel(
                WeaponType type,
                Rarity rarity,
                String name,
                String description,
                String boosts,
                boolean magical,
                String parentModel
                ) {
        super(rarity, name, description, boosts, parentModel);
        this.type = type;
        this.magical = magical;
    }

    public WeaponModel() {
    }

    public static final String schema;

    static {
        schema = SchemaUtil.schema(WeaponModel.class, "weapon");
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

    public static final String TYPE = "weapon";

    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = super.toEquipmentModel(boostService, library);
        equipment.type = EquipmentType.Weapon;
        equipment.slot = type.isRanged() ? EquipmentSlot.Ranged : EquipmentSlot.Melee;

        StatsArchive.Stat stat = library.archive().getStats().getByName(type.baseStat);
        equipment.damage = stat.getField("Damage");
        equipment.damageType = stat.getField("Damage Type");
        equipment.versatileDamage = stat.getField("Versatile Damage");

        List<String> weaponProperties = new ArrayList<>();
        String properties = stat.getField("Weapon Properties");
        if (properties != null) {
            String[] props = properties.split(";");
            for (String prop : props) {
                weaponProperties.add(prop);
            }
        }
        equipment.weaponProperties = weaponProperties;

        return equipment;
    }
}

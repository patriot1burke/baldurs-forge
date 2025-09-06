package org.baldurs.forge.builder;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.BoostService.BoostWriter;
import org.baldurs.forge.services.LibraryService;


public abstract class BaseModel {
    @Description("The rarity and uniqueness of the item.")
    public Rarity rarity;
    @Description("The name of the item")
    @JsonProperty(required = true)
    public String name;
    @Description("The description of the item.  A short fun, flavor text, role-playing style description.")
    public String description;
    @Description("The boosts and stat bonuses and enchantments for the item.  This is a semicolon separated list of boost functions.")
    public String boosts;
    @Description("The visual model of the item inherited from an existing parent item in the database.  Change this if you want a different look for your item other than the default.")
    public String visualModel;

    protected BaseModel(
                Rarity rarity,
                String name,
                String description,
                String boosts,
                String parentModel
                ) {
        this.rarity = rarity;
        this.name = name;
        this.description = description;
        this.boosts = boosts;
        this.visualModel = parentModel;
    }

    protected BaseModel() {
    }

    public abstract String schema();
    public abstract String type();
    public abstract String baseStat();
    
    public EquipmentModel toEquipmentModel(BoostService boostService, LibraryService library) {
        EquipmentModel equipment = new EquipmentModel();
        equipment.rarity = rarity;
        equipment.name = name == null ? "New Equipment" : name;
        equipment.description = description;
        if (boosts != null) {
            BoostWriter writer = boostService.html();
            boostService.macros(boosts, writer);
            equipment.boostDescription = writer.toString();
        } else {
            equipment.boostDescription = "";
        }
        StatsArchive.Stat stat = library.archive().getStats().getByName(baseStat());
        RootTemplate template = null;
        if (visualModel != null) {
            template = library.archive().getRootTemplates().getRootTemplate(visualModel);
        } else {
            template = library.archive().getRootTemplates().getRootTemplate(stat.getField("RootTemplate"));
        }
        equipment.icon = library.icons().get(template.resolveIcon());
        return equipment;
    }
 
    

}

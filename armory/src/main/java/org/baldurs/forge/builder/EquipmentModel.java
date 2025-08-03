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

public class EquipmentModel {
    public EquipmentType type;
    public EquipmentSlot slot;
    public Rarity rarity;
    public String name;
    public String description;
    public int armorClass;
    public String weaponType;
    public String armorType;
    public String boostMacro;

    public EquipmentModel(
                EquipmentType type,
                EquipmentSlot slot,
                Rarity rarity,
                String name,
                String description,
                int armorClass,
                String weaponType,
                String armorType,
                String boostMacro
                ) {
        this.type = type;
        this.slot = slot;
        this.rarity = rarity;
        this.name = name;
        this.description = description;
        this.armorClass = armorClass;
        this.weaponType = weaponType;
        this.armorType = armorType;
        this.boostMacro = boostMacro;
    }

    public EquipmentModel() {
    }

}

package org.baldurs.forge.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EquipmentModel {
        public String id;
        public EquipmentType type;
        public EquipmentSlot slot;
        public Rarity rarity;
        public String name;
        public String description;
        public String boostDescription;
        public int armorClass;
        public String weaponType;
        public String armorType;
        public List<String> weaponProperties;
        public String icon;
        public String damage;
        public String damageType;
        public String versatileDamage;

        public EquipmentModel() {
        }

        public EquipmentModel(String id, EquipmentType type, EquipmentSlot slot, Rarity rarity, String name, String description, String boostDescription, int armorClass, String weaponType, String armorType, List<String> weaponProperties, String icon, String damage, String damageType, String versatileDamage) {
                this.id = id;
                this.type = type;
                this.slot = slot;
                this.rarity = rarity;
                this.name = name;
                this.description = description;
                this.boostDescription = boostDescription;
                this.armorClass = armorClass;
                this.weaponType = weaponType;
                this.armorType = armorType;
                this.weaponProperties = weaponProperties;
                this.icon = icon;
                this.damage = damage;
                this.damageType = damageType;
                this.versatileDamage = versatileDamage;
        }

        public static String toJson(List<EquipmentModel> models) {
                try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
                        return mapper.writeValueAsString(models);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                }
        }

        public static EquipmentModel from(Equipment equipment) {
                return new EquipmentModel(equipment.id(), equipment.type(), equipment.slot(), equipment.rarity(), equipment.name(), equipment.description(), equipment.boostDescription(), equipment.armorClass(), equipment.weaponType(), equipment.armorType(), equipment.weaponProperties(), equipment.icon(), equipment.damage(), equipment.damageType(), equipment.versatileDamage());
        }

}

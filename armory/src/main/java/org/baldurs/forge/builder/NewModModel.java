package org.baldurs.forge.builder;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class 
NewModModel {
    public static final String NEW_EQUIPMENT = "newEquipment";

    public String name;
    public String author;
    public String description;

    public int count;

    public Map<String, BodyArmorModel> bodyArmors;
    public Map<String, WeaponModel> weapons;

    @JsonIgnore
    public boolean isEmpty() {
        return bodyArmors == null || bodyArmors.isEmpty();
    }

}

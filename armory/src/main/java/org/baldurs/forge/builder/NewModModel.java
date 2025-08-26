package org.baldurs.forge.builder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NewModModel {
    public static final String NEW_EQUIPMENT = "newEquipment";

    public String name;
    public String author;
    public String description;
    public List<BodyArmorModel> bodyArmors;

    @JsonIgnore
    public boolean isEmpty() {
        return bodyArmors == null || bodyArmors.isEmpty();
    }

}

package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

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
    public Map<String, RingModel> rings;
    public Map<String, AmuletModel> amulets;
    public Map<String, HelmetModel> helmets;
    public Map<String, GlovesModel> gloves;
    public Map<String, BootsModel> boots;
    public Map<String, CloakModel> cloaks;


    @JsonIgnore
    public boolean isEmpty() {
        return bodyArmors == null || bodyArmors.isEmpty();
    }

    public void addEquipment(BaseModel equipment) {
        Map<String, Object> equipmentMap = equipmentMap(equipment);
        if (!equipmentMap.containsKey(equipment.name)) {
            count++;
        }
        equipmentMap.put(equipment.name, equipment);
    }

    public void removeEquipment(BaseModel equipment) {
        Map<String, Object> equipmentMap = equipmentMap(equipment);
        if (equipmentMap.containsKey(equipment.name)) {
            count--;
        }
        equipmentMap.remove(equipment.name);
    }

    public List<EquipmentModel> toEquipmentModels(BoostService boostService, LibraryService library) {
        List<EquipmentModel> equipmentModels = new ArrayList<>();
        if (bodyArmors != null) {
            for (BodyArmorModel bodyArmor : bodyArmors.values()) {
                equipmentModels.add(bodyArmor.toEquipmentModel(boostService, library));
            }
        }
        if (weapons != null) {
            for (WeaponModel weapon : weapons.values()) {
                equipmentModels.add(weapon.toEquipmentModel(boostService, library));
            }
        }
        if (rings != null) {
            for (RingModel ring : rings.values()) {
                equipmentModels.add(ring.toEquipmentModel(boostService, library));
            }
        }
        if (amulets != null) {
            for (AmuletModel amulet : amulets.values()) {
                equipmentModels.add(amulet.toEquipmentModel(boostService, library));
            }
        }
        if (helmets != null) {
            for (HelmetModel helmet : helmets.values()) {
                equipmentModels.add(helmet.toEquipmentModel(boostService, library));
            }
        }
        if (gloves != null) {
            for (GlovesModel gloves : gloves.values()) {
                equipmentModels.add(gloves.toEquipmentModel(boostService, library));
            }
        }
        if (boots != null) {
            for (BootsModel boots : boots.values()) {
                equipmentModels.add(boots.toEquipmentModel(boostService, library));
            }
        }
        if (cloaks != null) {
            for (CloakModel cloak : cloaks.values()) {
                equipmentModels.add(cloak.toEquipmentModel(boostService, library));
            }
        }   
        return equipmentModels;
    }

    public BaseModel findEquipmentByName(String name) {
        BaseModel equipment = null;
        equipment = bodyArmors == null ? null : bodyArmors.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = weapons == null ? null : weapons.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = rings == null ? null : rings.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = amulets == null ? null : amulets.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = helmets == null ? null : helmets.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = gloves == null ? null : gloves.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = boots == null ? null : boots.get(name);
        if (equipment != null) {
            return equipment;
        }
        equipment = cloaks == null ? null : cloaks.get(name);
        if (equipment != null) {
            return equipment;
        }
        return null;
    }

    private Map equipmentMap(BaseModel type) {
        if (type instanceof BodyArmorModel) {
            if (bodyArmors == null) {
                bodyArmors = new HashMap<>();
            }
            return bodyArmors;
        } else if (type instanceof WeaponModel) {
            if (weapons == null) {
                weapons = new HashMap<>();
            }
            return weapons;
        } else if (type instanceof RingModel) {
            if (rings == null) {
                rings = new HashMap<>();
            }
            return rings;
        } else if (type instanceof AmuletModel) {
            if (amulets == null) {
                amulets = new HashMap<>();
            }
            return amulets;
        } else if (type instanceof HelmetModel) {
            if (helmets == null) {
                helmets = new HashMap<>();
            }
            return helmets;
        } else if (type instanceof GlovesModel) {
            if (gloves == null) {
                gloves = new HashMap<>();
            }
            return gloves;
        } else if (type instanceof BootsModel) {
            if (boots == null) {
                boots = new HashMap<>();
            }
            return boots;
        } else if (type instanceof CloakModel) {
            if (cloaks == null) {
                cloaks = new HashMap<>();
            }
            return cloaks;
        }
        return null;

    }

}

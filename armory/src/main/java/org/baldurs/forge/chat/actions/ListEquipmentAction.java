package org.baldurs.forge.chat.actions;

import java.util.List;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.model.EquipmentModel;

public  class ListEquipmentAction extends Action {
    protected List<EquipmentModel> equipment;

    public ListEquipmentAction(List<EquipmentModel> equipment) {
        super("ListEquipment");
        this.equipment = equipment;
    }

    public List<EquipmentModel> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentModel> equipment) {
        this.equipment = equipment;
    }

}
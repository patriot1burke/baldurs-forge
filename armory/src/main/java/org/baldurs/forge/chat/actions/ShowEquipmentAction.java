package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.model.EquipmentModel;

public class ShowEquipmentAction extends Action {
    EquipmentModel equipment;

    public ShowEquipmentAction(EquipmentModel equipment) {
        super("ShowEquipment");
        this.equipment = equipment;
    }

    public EquipmentModel getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentModel equipment) {
        this.equipment = equipment;
    }

}
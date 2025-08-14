package org.baldurs.forge.chat.actions;

import org.baldurs.forge.model.EquipmentModel;

public class UpdateNewEquipmentAction extends MessageAction {
    protected EquipmentModel equipment;

    public UpdateNewEquipmentAction(String message) {
        super(message);
        this.type = "UpdateNewEquipment";
    }
}

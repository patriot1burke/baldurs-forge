package org.baldurs.forge.messages;

import org.baldurs.forge.chat.ObjectMessage;
import org.baldurs.forge.model.EquipmentModel;

public class UpdateNewEquipmentMessage extends ObjectMessage {

    public UpdateNewEquipmentMessage(String message) {
        super(message);
        this.type = "UpdateNewEquipment";
    }
}

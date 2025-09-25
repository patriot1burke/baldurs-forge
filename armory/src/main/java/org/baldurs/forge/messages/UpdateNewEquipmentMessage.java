package org.baldurs.forge.messages;

import org.baldurs.forge.chat.ObjectMessage;

public class UpdateNewEquipmentMessage extends ObjectMessage {

    public UpdateNewEquipmentMessage(String message) {
        super(message);
        this.type = "UpdateNewEquipment";
    }
}

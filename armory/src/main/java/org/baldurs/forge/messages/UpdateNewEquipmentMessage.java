package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ObjectMessage;

public class UpdateNewEquipmentMessage extends ObjectMessage {

    public UpdateNewEquipmentMessage(String message) {
        super(message);
        this.type = "UpdateNewEquipment";
    }
}

package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("UpdateNewEquipment")
public class UpdateNewEquipmentMessage {
    protected String message;
    protected int saveCount;

    public UpdateNewEquipmentMessage(String message, int saveCount) {
        this.message = message;
        this.saveCount = saveCount;
    }

    public String getMessage() {
        return message;
    }

    public int getSaveCount() {
        return saveCount;
    }
}

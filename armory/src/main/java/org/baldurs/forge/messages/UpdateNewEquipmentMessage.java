package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;

public class UpdateNewEquipmentMessage extends ChatFrameMessage {
    protected String message;
    protected int saveCount;

    public UpdateNewEquipmentMessage(String message, int saveCount) {
        super("UpdateNewEquipment");
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

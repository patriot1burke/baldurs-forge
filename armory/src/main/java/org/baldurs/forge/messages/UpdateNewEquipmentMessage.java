package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class UpdateNewEquipmentMessage {
    protected String message;
    protected int saveCount;

    private UpdateNewEquipmentMessage(String message, int saveCount) {
        this.message = message;
        this.saveCount = saveCount;
    }

    public String getMessage() {
        return message;
    }

    public int getSaveCount() {
        return saveCount;
    }

    /**
     * Add UpdateNewEquipmentAction to the response. If there is already one, remove it
     * before adding the new one.
     * This is to prevent duplicate update new equipment events sent to the client.
     *
     * @param context
     */
    public static void addResponse(ChatFrameContext context, String message, int saveCount) {
        context.addEvent("UpdateNewEquipment", new UpdateNewEquipmentMessage(message, saveCount), true);
    }

}

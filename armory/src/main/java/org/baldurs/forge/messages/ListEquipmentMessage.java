package org.baldurs.forge.messages;

import java.util.List;

import org.baldurs.forge.model.EquipmentModel;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class ListEquipmentMessage {
    private ListEquipmentMessage() {
    }

    /**
     * Add ListEquipmentAction to the response. If there is already one, remove it before adding the new one.
     * This is to prevent duplicate displays of the same equipment in the UI chat.
     *
     * @param context
     * @param equipment
     */
    public static void addResponse(ChatFrameContext context, List<EquipmentModel> equipment) {
        context.addEvent("ListEquipment", equipment, true);
    }

}

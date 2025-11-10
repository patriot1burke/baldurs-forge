package org.baldurs.forge.messages;

import java.util.List;

import org.baldurs.forge.model.EquipmentModel;

import io.quarkiverse.langchain4j.chat.frames.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ResponseMessage;

public  class ListEquipmentMessage extends ResponseMessage {
    protected List<EquipmentModel> equipment;

    private ListEquipmentMessage(List<EquipmentModel> equipment) {
        super("ListEquipment");
        this.equipment = equipment;
    }

    public List<EquipmentModel> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentModel> equipment) {
        this.equipment = equipment;
    }

    /**
     * Add ListEquipmentAction to the response.  If there is already one, remove it before adding the new one.
     * This is to prevent duplicate displays of the same equipment in the UI chat.
     * @param context
     * @param equipment
     */
    public static void addResponse(ChatContext context, List<EquipmentModel> equipment) {
        context.response().removeIf(action -> action instanceof ListEquipmentMessage);
        context.response().add(new ListEquipmentMessage(equipment));
    }

}
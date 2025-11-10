package org.baldurs.forge.messages;

import org.baldurs.forge.model.EquipmentModel;

import io.quarkiverse.langchain4j.chat.frames.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ResponseMessage;

public class ShowEquipmentMessage extends ResponseMessage {
    protected EquipmentModel equipment;

    private ShowEquipmentMessage(EquipmentModel equipment) {
        super("ShowEquipment");
        this.equipment = equipment;
    }

    public EquipmentModel getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentModel equipment) {
        this.equipment = equipment;
    }

    /**
     * Add ShowEquipmentAction to the response.  If there is already one, remove it before adding the new one.
     * This is to prevent duplicate displays of the same equipment in the UI chat.
     * @param context
     * @param equipment
     */
    public static void addResponse(ChatContext context, EquipmentModel equipment) {
        context.response().removeIf(action -> action instanceof ShowEquipmentMessage);
        context.response().add(new ShowEquipmentMessage(equipment));
    }

}
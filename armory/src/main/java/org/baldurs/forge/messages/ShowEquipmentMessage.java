package org.baldurs.forge.messages;

import org.baldurs.forge.model.EquipmentModel;

import io.quarkiverse.langchain4j.chat.frames.ChatEvent;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class ShowEquipmentMessage extends ChatEvent {
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
     * Add ShowEquipmentAction to the response. If there is already one, remove it before adding the new one.
     * This is to prevent duplicate displays of the same equipment in the UI chat.
     *
     * @param context
     * @param equipment
     */
    public static void addResponse(ChatFrameContext context, EquipmentModel equipment) {
        context.events().removeIf(action -> action instanceof ShowEquipmentMessage);
        context.events().add(new ShowEquipmentMessage(equipment));
    }

}

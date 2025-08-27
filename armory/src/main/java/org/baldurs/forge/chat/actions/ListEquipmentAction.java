package org.baldurs.forge.chat.actions;

import java.util.List;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;

public  class ListEquipmentAction extends Action {
    protected List<EquipmentModel> equipment;

    private ListEquipmentAction(List<EquipmentModel> equipment) {
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
        context.response().removeIf(action -> action instanceof ListEquipmentAction);
        context.response().add(new ListEquipmentAction(equipment));
    }

}
package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;

public class ShowEquipmentAction extends Action {
    protected EquipmentModel equipment;

    private ShowEquipmentAction(EquipmentModel equipment) {
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
        context.response().removeIf(action -> action instanceof ShowEquipmentAction);
        context.response().add(new ShowEquipmentAction(equipment));
    }

}
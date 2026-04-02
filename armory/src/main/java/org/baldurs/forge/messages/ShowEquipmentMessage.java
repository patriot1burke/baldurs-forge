package org.baldurs.forge.messages;

import org.baldurs.forge.model.EquipmentModel;

import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("ShowEquipment")
public class ShowEquipmentMessage {
    @JsonValue
    protected EquipmentModel equipment;

    public ShowEquipmentMessage(EquipmentModel equipment) {
        this.equipment = equipment;
    }
}

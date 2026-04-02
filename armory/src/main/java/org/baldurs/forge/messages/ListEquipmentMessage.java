package org.baldurs.forge.messages;

import java.util.List;

import org.baldurs.forge.model.EquipmentModel;

import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("ListEquipment")
public class ListEquipmentMessage {
    @JsonValue
    protected List<EquipmentModel> equipment;

    public ListEquipmentMessage(List<EquipmentModel> equipment) {
        this.equipment = equipment;
    }
}

package org.baldurs.forge.builder;

import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;

import org.baldurs.forge.messages.ListEquipmentMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.messages.UpdateNewEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.langchain4j.chatscopes.ChatRouteContext;
import io.quarkiverse.langchain4j.chatscopes.ChatRoutes;
import io.quarkiverse.langchain4j.chatscopes.ChatScope;
import io.quarkiverse.langchain4j.chatscopes.ChatScopeMemory;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;

@ChatScoped
public class NewModBuilder {

    NewModModel newMod;

    @Inject
    ChatRouteContext ctx;

    @Inject
    ObjectMapper mapper;

    @Inject
    LibraryService library;

    @Inject
    BoostService boostService;

    public void clear() {
        newMod = null;
    }

    public void addEquipment(BaseModel equipment) {
        if (newMod == null) {
            newMod = new NewModModel();
        }
        newMod.addEquipment(equipment);
        ctx.response().message("Finished building item!");
        ctx.response().event(
                new UpdateNewEquipmentMessage("To create a mod containing your newly built equipment, tell me to 'Package Mod'",
                        newMod.count));
    }

    public List<EquipmentModel> listBuiltEquipment() {
        if (newMod == null || newMod.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return newMod.toEquipmentModels(boostService, library);
    }

    public void showNewEquipment() {
        List<EquipmentModel> equipment = listBuiltEquipment();
        if (!equipment.isEmpty()) {
            ctx.response().event(new ListEquipmentMessage(equipment));
        } else {
            ctx.response().message("No new equipment.");
        }
    }

    public void deleteAllNewEquipment() {
        newMod = null;
        ctx.response().event(new UpdateNewEquipmentMessage(null, 0));
        ChatScopeMemory.clearMemory();
    }

    public void deleteNewEquipment(String name) {
        NewModModel newEquipment = newMod;
        if (newEquipment == null || newEquipment.isEmpty()) {
            ctx.response().message("No equipment to delete.");
            return;
        }

        BaseModel equipment = newEquipment.findEquipmentByName(name);
        if (equipment == null) {
            showNewEquipment();
            ctx.response().message("Equipment with name not found.");
            return;
        }
        newEquipment.removeEquipment(equipment);

        if (newEquipment.count == 0) {
            newMod = null;
        } else {
            showNewEquipment();
        }
        ChatScopeMemory.clearMemory();
        ctx.response().event(new UpdateNewEquipmentMessage(null, newEquipment.count));

        ctx.response().message("Equipment deleted.");
    }

    public void updateNewEquipment(String name) {
        NewModModel newEquipment = newMod;
        if (newEquipment == null || newEquipment.isEmpty()) {
            ctx.response().message("No equipment to update.");
            return;
        }
        BaseModel equipment = newEquipment.findEquipmentByName(name);
        if (equipment == null) {
            showNewEquipment();
            ctx.response().message("Equipment with name not found.");
            return;
        }
        ctx.response().event(new ShowEquipmentMessage(equipment.toEquipmentModel(boostService, library)));
        ChatScope.push(equipment.type());
        EquipmentBuilder builder = ChatRoutes.bean();
        builder.setModel(equipment);
        ChatRoutes.execute();
    }
}

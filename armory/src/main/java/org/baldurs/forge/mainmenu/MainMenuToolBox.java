package org.baldurs.forge.mainmenu;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.baldurs.forge.builder.AmuletModel;
import org.baldurs.forge.builder.BodyArmorModel;
import org.baldurs.forge.builder.BootsModel;
import org.baldurs.forge.builder.CloakModel;
import org.baldurs.forge.builder.GlovesModel;
import org.baldurs.forge.builder.HelmetModel;
import org.baldurs.forge.builder.NewModBuilder;
import org.baldurs.forge.builder.RingModel;
import org.baldurs.forge.builder.WeaponModel;
import org.baldurs.forge.messages.ImportModMessage;
import org.baldurs.forge.messages.ListEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.chatscopes.ChatRouteContext;
import io.quarkiverse.langchain4j.chatscopes.ChatRoutes;
import io.quarkiverse.langchain4j.chatscopes.ChatScope;
import io.quarkiverse.langchain4j.chatscopes.ChatScopeMemory;
import io.quarkus.logging.Log;

@ApplicationScoped
public class MainMenuToolBox {
    @Inject
    ChatRouteContext ctx;

    @Inject
    EquipmentDB equipmentDB;

    @Inject
    LibraryService library;

    @Inject
    NewModBuilder modBuilder;

    @Inject
    MainMenuPrompt chat;

    @Tool(value = "Search for armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database based on a natural language query", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String searchEquipmentDatabase(String query) {
        // query parameter is ignored, but tool invocations are fickle so keeping it as
        // a parameter.
        Log.debug("Searching equipment database with query: " + query);
        Log.info("Searching equipment database with user message: " + ctx.request().userMessage());
        List<EquipmentModel> models = equipmentDB.ragSearch(ctx.request().userMessage());
        if (models.isEmpty()) {
            ctx.response().message("Could not find any equipment that matched your query.");
        } else {
            ctx.response().message("I found some possible matches for your query.");
            ctx.response().event(new ListEquipmentMessage(models));
        }
        ChatScopeMemory.scheduleWipe();
        return null;
    }

    @Tool(value = "Find armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database by name", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String findEquipmentByName(String name) {
        Log.info("Finding equipment by name: " + name);
        EquipmentModel model = equipmentDB.findByName(name);
        if (model == null) {
            List<EquipmentModel> models = equipmentDB.ragSearch(ctx.request().userMessage());
            if (models.isEmpty()) {
                ctx.response().message("I could not find any equipment with that name or any similar names.");
            } else {
                ctx.response().message("I could not find an exact match for your query, but I found some possible matches.");
                ctx.response().event(new ListEquipmentMessage(models));
            }
        } else {
            ctx.response().message(
                    "I found what you were looking for.");
            ctx.response().event("ShowEquipment", model);
        }
        ChatScopeMemory.scheduleWipe();
        // can the tool return void or will it confuse LLM?
        return null;

    }

    @Tool(value = "Create new body armor.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewBodyArmor(String userMessage) {
        Log.info("Creating new body armor");
        ChatScope.push(BodyArmorModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewWeapon(String userMessage) {
        Log.info("Creating new weapon");
        ChatScope.push(WeaponModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new boots.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewBoots(String userMessage) {
        Log.info("Creating new boots");
        ChatScope.push(BootsModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewGloves(String userMessage) {
        Log.info("Creating new gloves");
        ChatScope.push(GlovesModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new helmet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewHelmet(String userMessage) {
        Log.info("Creating new helmet");
        ChatScope.push(HelmetModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new ring.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewRing(String userMessage) {
        Log.info("Creating new ring");
        ChatScope.push(RingModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new amulet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewAmulet(String userMessage) {
        Log.info("Creating new amulet");
        ChatScope.push(AmuletModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Create new cloak.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewCloak(String userMessage) {
        Log.info("Creating new cloak");
        ChatScope.push(CloakModel.TYPE);
        ChatRoutes.execute();
    }

    @Tool(value = "Find all values for data attribute by name.   This is a raw data untyped query.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String findDataAttributeValues(String attributeName) {
        Log.info("Finding data attribute values for: " + attributeName);
        List<String> values = library.getStatAttributeValues(attributeName);
        if (values.isEmpty()) {
            ctx.response().message("I could not find any values for attribute: " + attributeName);
        } else {
            ctx.response().event("ListDataAttributeValues", values);
        }
        ChatScopeMemory.scheduleWipe();
        return null;
    }

    @Tool(value = "Show all new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showNewEquipment() {
        Log.info("showNewEquipment");
        List<EquipmentModel> equipment = modBuilder.listBuiltEquipment();
        if (equipment.isEmpty()) {
            ctx.response().message("You have not created any new equipment yet.");
        } else {
            ctx.response().message("This is the equipment you have created so far:");
            ctx.response().event(new ListEquipmentMessage(equipment));
            ctx.response().message("Ask me to <i>Package Mod</i> to package up your new equipment.");
        }
        ChatScopeMemory.scheduleWipe();
        return null;
    }

    @Tool(value = "Delete new equipment item by name.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void deleteNewEquipmentByName(String name) {
        Log.info("deleteNewEquipment: " + name);
        modBuilder.deleteNewEquipment(name);
    }

    @Tool(value = "Update new equipment item by name.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void updateNewEquipmentByName(String name) {
        Log.info("updateNewEquipment: " + name);
        modBuilder.updateNewEquipment(name);
    }

    @Tool(value = "Delete all new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void deleteAllNewEquipment() {
        Log.info("deleteAllNewEquipment");
        modBuilder.deleteAllNewEquipment();
    }

    @Tool(value = "Package our new mod..", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void packageMod() {
        Log.info("packageMod");
        if (modBuilder.newMod() == null || modBuilder.newMod().isEmpty()) {
            ctx.response().message("You have not created any new equipment yet.");
            return;
        }
        ChatScope.push("buildPackage");
        ChatRoutes.execute();
    }

    @Tool(value = "Import a mod from a file.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String importMod() {
        Log.info("importMod");
        ctx.response().event(new ImportModMessage("Import"));
        ctx.response().message("Please select a file to import.");
        ChatScopeMemory.scheduleWipe();
        return null;
    }
}

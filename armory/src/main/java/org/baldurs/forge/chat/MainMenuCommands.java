package org.baldurs.forge.chat;

import java.util.List;

import org.baldurs.forge.builder.BodyArmorBuilder;
import org.baldurs.forge.builder.ModPackager;
import org.baldurs.forge.chat.actions.ImportModAction;
import org.baldurs.forge.chat.actions.ListEquipmentAction;
import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.chat.actions.ShowEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MainMenuCommands {
    @Inject
    ChatContext context;

    @Inject
    EquipmentDB equipmentDB;

    @Inject
    BodyArmorBuilder bodyArmorBuilder;

    @Inject
    ChatService chatService;

    @Inject
    LibraryService library;

    @Inject
    ModPackager modPackager;

   

    @Tool("Search for armor or weapons in the equipment database based on a natural language query")
    public String searchEquipmentDatabase(String query) {
        Log.info("Searching equipment database for: " + query);
        List<EquipmentModel> models = equipmentDB.ragSearch(query);
        if (models.isEmpty()) {
            return "Could not find equipment";
        } else {
            context.response().add(new ListEquipmentAction(models));
            return "I found some possible matches for your query.";
        }
    }

    @Tool("Find armor or weapons in the equipment database by name")
    public String findEquipmentByName(String name) {
        Log.info("Finding equipment by name: " + name);
        EquipmentModel model = equipmentDB.findByName(name);
        if (model == null) {
            throw new RuntimeException("Could not find the equipment.  Try searching the database");
        }
        context.response().add(new ShowEquipmentAction(model));
        return "I found an exact match for your query.";
    }

    @Tool("Create new body armor.")
    public String createNewBodyArmor(String userMessage) {
        Log.info("Creating new body armor");
        return bodyArmorBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Find all values for data attribute by name.   This is a raw data untyped query.")
    public String findDataAttributeValues(String attributeName) {
        Log.info("Finding data attribute values for: " + attributeName);
        List<String> values = library.getStatAttributeValues(attributeName);
        if (values.isEmpty()) {
            return "I could not find any values for attribute: " + attributeName;
        } else {
            context.response().add(new MessageAction(values));
            return "Query was successful";
        }
    }

    @Tool("Show all new equipment the user has created.")
    public String showNewEquipment() {
        Log.info("showNewEquipment");
        modPackager.showNewEquipment();
        return "Here is all the equipment the user has created.";
    }

    @Tool("Delete new equipment item by name.")
    public String deleteNewEquipmentByName(String name) {
        Log.info("deleteNewEquipment: " + name);
        return modPackager.deleteNewEquipment(name);
    }

    @Tool("Update new equipment item by name.")
    public String updateNewEquipmentByName(String name) {
        Log.info("updateNewEquipment: " + name);
        return modPackager.updateNewEquipment(name);
    }

    @Tool("Delete all new equipment the user has created.")
    public void deleteAllNewEquipment() {
        Log.info("deleteAllNewEquipment");
        modPackager.deleteAllNewEquipment();
    }

    @Tool("Package mod with any new equipment the user has created.")
    public String packageMod() {
        Log.info("packageMod");
        return modPackager.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Import a mod from a file.")
    public String importMod() {
        Log.info("importMod");
        context.response().add(new ImportModAction());
        return "Please select a file to import.";
    }
}

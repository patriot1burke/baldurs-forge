package org.baldurs.forge.chat;

import java.util.List;

import org.baldurs.forge.builder.AmuletBuilder;
import org.baldurs.forge.builder.BodyArmorBuilder;
import org.baldurs.forge.builder.BootsBuilder;
import org.baldurs.forge.builder.CloakBuilder;
import org.baldurs.forge.builder.GlovesBuilder;
import org.baldurs.forge.builder.HelmetBuilder;
import org.baldurs.forge.builder.ModPackager;
import org.baldurs.forge.builder.RingBuilder;
import org.baldurs.forge.builder.WeaponBuilder;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.messages.ImportModMessage;
import org.baldurs.forge.messages.ListEquipmentMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
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
    WeaponBuilder weaponBuilder;

    @Inject
    BootsBuilder bootsBuilder;

    @Inject
    GlovesBuilder glovesBuilder;

    @Inject
    HelmetBuilder helmetBuilder;

    @Inject
    RingBuilder ringBuilder;

    @Inject
    AmuletBuilder amuletBuilder;
    @Inject
    CloakBuilder cloakBuilder;

    @Inject
    ChatFrameService chatService;

    @Inject
    LibraryService library;

    @Inject
    ModPackager modPackager;

    @Inject
    MainMenuChat chat;

    @Startup
    public void start() {
        chatService.setDefaultChatFrame(chat);
    }

    @Tool("Search for armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database based on a natural language query")
    public String searchEquipmentDatabase(String query) {
        // query parameter is ignored, but tool invocations are fickle so keeping it as a parameter.
        Log.debug("Searching equipment database with query: " + query);
        Log.info("Searching equipment database with user message: " + context.userMessage());
        List<EquipmentModel> models = equipmentDB.ragSearch(context.userMessage());
        context.suppressAIResponse();
        if (models.isEmpty()) {
            context.response().add(new ObjectMessage("Could not find any equipment that matched your query."));
            // supressing AI response, but send something meaningful back to LLM
            return "Could not find equipment that matched your query.";
        } else {
            ListEquipmentMessage.addResponse(context, models);
            context.response().add(new ObjectMessage("I found some possible matches for your query."));
            // supressing AI response, but send something meaningful back to LLM
            return "I found some matches for your query.";
        }
    }

    @Tool("Find armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database by name")
    public String findEquipmentByName(String name) {
        Log.info("Finding equipment by name: " + name);
        EquipmentModel model = equipmentDB.findByName(name);
        context.suppressAIResponse();
        if (model == null) {
            List<EquipmentModel> models = equipmentDB.ragSearch(context.userMessage());
            if (models.isEmpty()) {
                context.response()
                        .add(new ObjectMessage("I could not find any equipment with that name or any similar names."));
            } else {
                context.response().add(new ObjectMessage(
                        "I could not find an exact match for your query, but I found some possible matches."));
                ListEquipmentMessage.addResponse(context, models);
            }
        } else {
            ShowEquipmentMessage.addResponse(context, model);
        }
        // supressing AI response, but no matter what tell the LLM we found an exact match so it doesn't try a search tool call, even on an error.
        return "I found an exact match for your query.";

    }

    @Tool("Create new body armor.")
    public String createNewBodyArmor(String userMessage) {
        Log.info("Creating new body armor");
        return bodyArmorBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new weapon.")
    public String createNewWeapon(String userMessage) {
        Log.info("Creating new weapon");
        return weaponBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new boots.")
    public String createNewBoots(String userMessage) {
        Log.info("Creating new boots");
        return bootsBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new gloves.")
    public String createNewGloves(String userMessage) {
        Log.info("Creating new gloves");
        return glovesBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new helmet.")
    public String createNewHelmet(String userMessage) {
        Log.info("Creating new helmet");
        return helmetBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new ring.")
    public String createNewRing(String userMessage) {
        Log.info("Creating new ring");
        return ringBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new amulet.")
    public String createNewAmulet(String userMessage) {
        Log.info("Creating new amulet");
        return amuletBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Create new cloak.")
    public String createNewCloak(String userMessage) {
        Log.info("Creating new cloak");
        return cloakBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Find all values for data attribute by name.   This is a raw data untyped query.")
    public String findDataAttributeValues(String attributeName) {
        Log.info("Finding data attribute values for: " + attributeName);
        List<String> values = library.getStatAttributeValues(attributeName);
        if (values.isEmpty()) {
            return "I could not find any values for attribute: " + attributeName;
        } else {
            context.response().add(new ObjectMessage(values));
            return "Query was successful";
        }
    }

    @Tool("Show all new equipment the user has created.")
    public String showNewEquipment() {
        Log.info("showNewEquipment");
        List<EquipmentModel> equipment = modPackager.listBuiltEquipment();
        context.suppressAIResponse();
        if (equipment.isEmpty()) {
            context.response().add(new ObjectMessage("You have not created any new equipment yet."));
        } else {
            context.response().add(new ObjectMessage("This is the equipment you have created so far:"));
            ListEquipmentMessage.addResponse(context, equipment);
            context.response().add(new ObjectMessage("Ask me to <i>Package Mod</i> to package up your new equipment."));
        }
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
        ImportModMessage.addResponse(context);
        return "Please select a file to import.";
    }
}

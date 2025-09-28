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

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MainMenuChatFrame implements ChatFrame {
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
    MainMenuPrompt chat;

    @Inject
    ChatMemoryStore chatMemoryStore;

    @Startup
    public void start() {
        chatService.setDefaultChatFrame(this);
    }

    public static final String CLEAR_MEMORY_ON_EXIT = "clearMemoryOnExit";

    @Override
    public String chat() {
        Log.info("MainMenu with user message: " + context.userMessage());
        Result<String> result = chat.chat(context.memoryId(), context.userMessage());
        if (result.content() != null) {
            Log.info("MainMenu with content: " + result.content());
            return result.content();
        }
        String msg = null;
        boolean clearMemory = false;
        if (result.toolExecutions().isEmpty()) {
            Log.info("MainMenu with no tool executions");
            chatMemoryStore.deleteMessages(context.memoryId());
            return null;
        } else {
            Log.info("MainMenu with multiple tool executions");
            for (ToolExecution execution : result.toolExecutions()) {
                if (execution.result() == null) continue;
                if (execution.result().contains(CLEAR_MEMORY_ON_EXIT)) {
                    clearMemory = true;
                } else {
                    if (msg == null) {
                        msg = execution.result();
                    } else {
                        msg += execution.result() + "\n";
                    }
                }
            }
        }
        // If there is a message, assume that a tool is continuing the conversation and has set up chat memory how it wants it.
        if (clearMemory && msg == null) {
            chatMemoryStore.deleteMessages(context.memoryId());
        }
        return msg;
    }

    @Tool(value = "Search for armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database based on a natural language query", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String searchEquipmentDatabase(String query) {
        // query parameter is ignored, but tool invocations are fickle so keeping it as
        // a parameter.
        Log.debug("Searching equipment database with query: " + query);
        Log.info("Searching equipment database with user message: " + context.userMessage());
        List<EquipmentModel> models = equipmentDB.ragSearch(context.userMessage());
        if (models.isEmpty()) {
            context.response().add(new ObjectMessage("Could not find any equipment that matched your query."));
        } else {
            context.response().add(new ObjectMessage("I found some possible matches for your query."));
            ListEquipmentMessage.addResponse(context, models);
        }
        return CLEAR_MEMORY_ON_EXIT;
    }

    @Tool(value = "Find armor or weapons or rings or amulets or boots or gloves or helmets or shields in the equipment database by name", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String findEquipmentByName(String name) {
        Log.info("Finding equipment by name: " + name);
        EquipmentModel model = equipmentDB.findByName(name);
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
            context.response().add(new ObjectMessage(
                "I found what you were looking for."));
            ShowEquipmentMessage.addResponse(context, model);
        }
        return CLEAR_MEMORY_ON_EXIT;

    }

    @Tool(value = "Create new body armor.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewBodyArmor(String userMessage) {
        Log.info("Creating new body armor");
        return bodyArmorBuilder.startBuilding();
    }

    @Tool(value = "Create new weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewWeapon(String userMessage) {
        Log.info("Creating new weapon");
        return weaponBuilder.startBuilding();
    }

    @Tool(value = "Create new boots.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewBoots(String userMessage) {
        Log.info("Creating new boots");
        return bootsBuilder.startBuilding();
    }

    @Tool(value = "Create new gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewGloves(String userMessage) {
        Log.info("Creating new gloves");
        return glovesBuilder.startBuilding();
    }

    @Tool(value = "Create new helmet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewHelmet(String userMessage) {
        Log.info("Creating new helmet");
        return helmetBuilder.startBuilding();
    }

    @Tool(value = "Create new ring.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewRing(String userMessage) {
        Log.info("Creating new ring");
        return ringBuilder.startBuilding();
    }

    @Tool(value = "Create new amulet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewAmulet(String userMessage) {
        Log.info("Creating new amulet");
        return amuletBuilder.startBuilding();
    }

    @Tool(value = "Create new cloak.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String createNewCloak(String userMessage) {
        Log.info("Creating new cloak");
        return cloakBuilder.startBuilding();
    }

    @Tool(value = "Find all values for data attribute by name.   This is a raw data untyped query.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String findDataAttributeValues(String attributeName) {
        Log.info("Finding data attribute values for: " + attributeName);
        List<String> values = library.getStatAttributeValues(attributeName);
        if (values.isEmpty()) {
            context.response().add(new ObjectMessage("I could not find any values for attribute: " + attributeName));
        } else {
            context.response().add(new ObjectMessage(values));
        }
        return CLEAR_MEMORY_ON_EXIT;
    }

    @Tool(value = "Show all new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showNewEquipment() {
        Log.info("showNewEquipment");
        List<EquipmentModel> equipment = modPackager.listBuiltEquipment();
        if (equipment.isEmpty()) {
            context.response().add(new ObjectMessage("You have not created any new equipment yet."));
        } else {
            context.response().add(new ObjectMessage("This is the equipment you have created so far:"));
            ListEquipmentMessage.addResponse(context, equipment);
            context.response().add(new ObjectMessage("Ask me to <i>Package Mod</i> to package up your new equipment."));
        }
        return CLEAR_MEMORY_ON_EXIT;
    }

    @Tool(value = "Delete new equipment item by name.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String deleteNewEquipmentByName(String name) {
        Log.info("deleteNewEquipment: " + name);
        return modPackager.deleteNewEquipment(name);
    }

    @Tool(value = "Update new equipment item by name.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String updateNewEquipmentByName(String name) {
        Log.info("updateNewEquipment: " + name);
        return modPackager.updateNewEquipment(name);
    }

    @Tool(value = "Delete all new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void deleteAllNewEquipment() {
        Log.info("deleteAllNewEquipment");
        modPackager.deleteAllNewEquipment();
    }

    @Tool(value = "Package mod with any new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String packageMod() {
        Log.info("packageMod");
        return modPackager.startPackaging();
    }

    @Tool(value = "Import a mod from a file.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String importMod() {
        Log.info("importMod");
        ImportModMessage.addResponse(context);
        context.response().add(new ObjectMessage("Please select a file to import."));
        return CLEAR_MEMORY_ON_EXIT;
    }
}

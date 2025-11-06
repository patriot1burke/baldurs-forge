package org.baldurs.forge.mainmenu;

import java.util.ArrayList;
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
import org.baldurs.forge.messages.ImportModMessage;
import org.baldurs.forge.messages.ListEquipmentMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;
import org.baldurs.forge.services.MarkdownToHtmlService;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkiverse.langchain4j.chat.context.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameService;
import io.quarkiverse.langchain4j.chat.frames.ObjectMessage;
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

    @Inject
    MarkdownToHtmlService renderer;

    @Startup
    public void start() {
        chatService.setDefaultFrame(this);
    }

    public static final String CLEAR_MEMORY_ON_EXIT = "clearMemoryOnExit";

    @Override
    public void chat() {
        Log.info("MainMenu with user message: " + context.userMessage());
        Result<String> result = chat.chat(context.memoryId(), context.userMessage());
        if (result.content() != null) {
            context.response().add(new ObjectMessage(renderer.markdownToHtml(result.content())));
            return;
        }
        boolean clearMemory = false;
        if (result.toolExecutions().isEmpty()) {
            Log.info("MainMenu with no tool executions");
            chatMemoryStore.deleteMessages(context.memoryId());
            return;
        } else {
            for (ToolExecution execution : result.toolExecutions()) {
                Log.info("MainMenu with tool " + execution.request().name() + " execution: " + execution.result());
                // exeuctionIds.add(execution.request().id());
                if (execution.resultObject() == null) {
                    Log.info("MainMenu with null tool execution result");
                    continue;
                } else if (execution.resultObject().equals(CLEAR_MEMORY_ON_EXIT)) {
                    Log.info("MainMenu with CLEAR_MEMORY_ON_EXIT tool execution result");
                    clearMemory = true;
                } else {
                    throw new RuntimeException("This should be unreachable resultObject: " + execution.resultObject());
                }
            }
        }
        if (clearMemory) {
            chatMemoryStore.deleteMessages(context.memoryId());
        }
        return;
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
    public void createNewBodyArmor(String userMessage) {
        Log.info("Creating new body armor");
        bodyArmorBuilder.startBuild();
    }

    @Tool(value = "Create new weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewWeapon(String userMessage) {
        Log.info("Creating new weapon");
        weaponBuilder.startBuild();
    }

    @Tool(value = "Create new boots.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewBoots(String userMessage) {
        Log.info("Creating new boots");
        bootsBuilder.startBuild();
    }

    @Tool(value = "Create new gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewGloves(String userMessage) {
        Log.info("Creating new gloves");
        glovesBuilder.startBuild();
    }

    @Tool(value = "Create new helmet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewHelmet(String userMessage) {
        Log.info("Creating new helmet");
        helmetBuilder.startBuild();
    }

    @Tool(value = "Create new ring.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewRing(String userMessage) {
        Log.info("Creating new ring");
        ringBuilder.startBuild();
    }

    @Tool(value = "Create new amulet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewAmulet(String userMessage) {
        Log.info("Creating new amulet");
        amuletBuilder.startBuild();
    }

    @Tool(value = "Create new cloak.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void createNewCloak(String userMessage) {
        Log.info("Creating new cloak");
        cloakBuilder.startBuild();
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
    public void deleteNewEquipmentByName(String name) {
        Log.info("deleteNewEquipment: " + name);
        modPackager.deleteNewEquipment(name);
    }

    @Tool(value = "Update new equipment item by name.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void updateNewEquipmentByName(String name) {
        Log.info("updateNewEquipment: " + name);
        modPackager.updateNewEquipment(name);
    }

    @Tool(value = "Delete all new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void deleteAllNewEquipment() {
        Log.info("deleteAllNewEquipment");
        modPackager.deleteAllNewEquipment();
    }

    @Tool(value = "Package mod with any new equipment the user has created.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void packageMod() {
        Log.info("packageMod");
        modPackager.startPackageMod();
    }

    @Tool(value = "Import a mod from a file.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String importMod() {
        Log.info("importMod");
        ImportModMessage.addResponse(context);
        context.response().add(new ObjectMessage("Please select a file to import."));
        return CLEAR_MEMORY_ON_EXIT;
    }
}

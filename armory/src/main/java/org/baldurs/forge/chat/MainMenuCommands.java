package org.baldurs.forge.chat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.baldurs.forge.builder.BodyArmorBuilder;
import org.baldurs.forge.chat.actions.ListEquipmentAction;
import org.baldurs.forge.chat.actions.ShowEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.scanner.BaldursArchive;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
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
}

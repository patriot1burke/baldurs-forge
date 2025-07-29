package org.baldurs.forge.chat;

import java.util.List;

import org.baldurs.forge.builder.EquipmentBuilder;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.toolbox.EquipmentDB;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EquipmentQueryCommands {
    @Inject
    ChatContext context;

    @Inject
    EquipmentDB equipmentDB;

    @Inject
    EquipmentBuilder equipmentBuilder;

    @Inject
    ChatService chatService;

    public static class ListEquipmentAction extends Action {
        List<EquipmentModel> equipment;

        public ListEquipmentAction(List<EquipmentModel> equipment) {
            super("ListEquipment");
            this.equipment = equipment;
        }

        public List<EquipmentModel> getEquipment() {
            return equipment;
        }

        public void setEquipment(List<EquipmentModel> equipment) {
            this.equipment = equipment;
        }

    }

    public static class ShowEquipmentAction extends Action {
        EquipmentModel equipment;

        public ShowEquipmentAction(EquipmentModel equipment) {
            super("ShowEquipment");
            this.equipment = equipment;
        }

        public EquipmentModel getEquipment() {
            return equipment;
        }

        public void setEquipment(EquipmentModel equipment) {
            this.equipment = equipment;
        }

    }

    @Tool("Search show items in the equipment database based on a natural language query")
    public void searchEquipmentDatabase(String query) {
        Log.info("Searching equipment database for: " + query);
        List<EquipmentModel> models = equipmentDB.ragSearch(query);
        if (models.isEmpty()) {
            context.response().add(new MessageAction("Could not find equipment"));
        } else {
            context.response().add(new ListEquipmentAction(models));
        }
    }

    @Tool("Find an item in the equipment database by name")
    public void findEquipmentByName(String name) {
        Log.info("Finding equipment by name: " + name);
        EquipmentModel model = equipmentDB.findByName(name);
        if (model == null) {
            context.response().add(new MessageAction("Could not find equipment"));
        }
        context.response().add(new ShowEquipmentAction(model));
    }

    @Tool("Create new equipment, for example, a new weapon or armor.")
    public String createNewEquipment(String userMessage) {
        Log.info("Creating new equipment");
        return equipmentBuilder.chat(context.memoryId(), context.userMessage());
    }

    @Tool("Generate a boost for equipment")
    public void generateBoost(String userMessage) {
        Log.info("Generating boost for equipment");
        chatService.setChatFrame(context, BoostChat.class);
        context.response().add(new MessageAction("What boosts do you want to add?"));
    }

}

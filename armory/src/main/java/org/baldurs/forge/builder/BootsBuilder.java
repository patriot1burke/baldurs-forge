package org.baldurs.forge.builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownStringMessage;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive.Stat;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ResultMessageTypes;
import io.quarkus.logging.Log;

@ApplicationScoped
public class BootsBuilder extends EquipmentBuilder {

    @Inject
    BootsBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            BootsModel bootsModel = new BootsModel();
            return bootsModel;
        };
    }

    @ChatFrame(BootsModel.TYPE)
    @ResultMessageTypes(MarkdownStringMessage.class)
    public Result<String> buildBoots(BootsModel current) {
        return build(current);
    }

    @Tool("Set the name for the current boots.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current boots.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current boots.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the armor category for the current boots.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        current.armorCategory = armorCategory;
        addShowEquipmentAction(current);
    }

    @Tool("Add boost to the current boots.")
    public void addBoost(String boostDescription) throws Exception {
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current boots.")
    public void setBoost(String boostDescription) throws Exception {
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        super.setBoost(current, boostDescription);
    }

    @Tool("Set the visual model for the current boots.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool(value = "Summarizes available visual models for the current boots type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        BootsModel boots = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        return showVisualModels(visualModelPredicate(boots));
    }

    private Predicate<? super Stat> visualModelPredicate(BootsModel boots) {
        return stat -> {
            String slot = stat.getField("Slot");
            if (slot == null || (slot != null && !slot.equals("Boots"))) {
                return false;
            }
            if (boots.armorCategory != null && boots.armorCategory != ArmorCategory.None) {
                String properties = stat.getField("Proficiency Group");
                String searchString = boots.armorCategory.name() + "Armor";
                return properties != null && properties.contains(searchString);
            } else {
                return true;
            }
        };
    }

    @Tool(value = "When finished building boots, call this tool to finish the boots.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        BootsModel current = context.getData(CURRENT_EQUIPMENT, BootsModel.class);
        finishEquipment(current);
    }

}

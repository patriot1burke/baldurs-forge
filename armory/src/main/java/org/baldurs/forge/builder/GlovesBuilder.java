package org.baldurs.forge.builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownStringMessage;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.EventMapper;
import io.quarkus.logging.Log;

@ApplicationScoped
public class GlovesBuilder extends EquipmentBuilder {

    @Inject
    GlovesBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            GlovesModel bootsModel = new GlovesModel();
            return bootsModel;
        };
    }

    @ChatFrame(GlovesModel.TYPE)
    @EventMapper(MarkdownStringMessage.class)
    public Result<String> buildGloves(GlovesModel current) {
        return build(current);
    }

    @Tool("Set the name for the current gloves.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current gloves.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current gloves.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the armor category for the current gloves.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        current.armorCategory = armorCategory;
        addShowEquipmentAction(current);
    }

    @Tool("Add boost to the current gloves.")
    public void addBoost(String boostDescription) throws Exception {
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current gloves.")
    public void setBoost(String boostDescription) throws Exception {
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        super.setBoost(current, boostDescription);
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(GlovesModel gloves) {
        return stat -> {
            String slot = stat.getField("Slot");
            if (slot == null || (slot != null && !slot.equals("Gloves"))) {
                return false;
            }
            if (gloves.armorCategory != null && gloves.armorCategory != ArmorCategory.None) {
                String properties = stat.getField("Proficiency Group");
                String searchString = gloves.armorCategory.name() + "Armor";
                return properties != null && properties.contains(searchString);
            } else {
                return true;
            }
        };
    }

    @Tool("Set the visual model for the current gloves.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool(value = "Summarizes available visual models for the current gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        GlovesModel gloves = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        return showVisualModels(visualModelPredicate(gloves));
    }

    @Tool(value = "When finished building gloves, call this tool to finish the gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        GlovesModel current = context.getData(CURRENT_EQUIPMENT, GlovesModel.class);
        finishEquipment(current);
    }

}

package org.baldurs.forge.builder;

import java.util.function.Predicate;

import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownToHtml;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chatscopes.ChatRoute;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;
import io.quarkus.logging.Log;

@ChatScoped
public class GlovesBuilder extends EquipmentBuilder {

    @Inject
    GlovesBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    GlovesModel model = new GlovesModel();

    @Override
    public GlovesModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (GlovesModel) model;
    }

    @ChatRoute(GlovesModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildGloves() {
        return build();
    }

    @Tool("Set the name for the current gloves.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current gloves.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current gloves.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the armor category for the current gloves.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        model().armorCategory = armorCategory;
        addShowEquipmentAction();
    }

    @Tool("Add boost to the current gloves.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current gloves.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
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
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool(value = "Summarizes available visual models for the current gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate(model()));
    }

    @Tool(value = "When finished building gloves, call this tool to finish the gloves.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

    @Tool(value = "Cancel or abort building the current gloves without saving them.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void cancelEquipment() {
        super.cancelEquipment();
    }

}

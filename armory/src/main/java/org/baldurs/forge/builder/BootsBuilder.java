package org.baldurs.forge.builder;

import java.util.function.Predicate;

import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownToHtml;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive.Stat;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chatscopes.ChatRoute;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;
import io.quarkus.logging.Log;

@ChatScoped
public class BootsBuilder extends EquipmentBuilder {

    @Inject
    BootsBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    BootsModel model = new BootsModel();

    @Override
    public BootsModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (BootsModel) model;
    }

    @ChatRoute(BootsModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildBoots() {
        return build();
    }

    @Tool("Set the name for the current boots.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current boots.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current boots.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the armor category for the current boots.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        model().armorCategory = armorCategory;
        addShowEquipmentAction();
    }

    @Tool("Add boost to the current boots.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current boots.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Tool("Set the visual model for the current boots.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool(value = "Summarizes available visual models for the current boots type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate(model()));
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
        super.finishEquipment();
    }

}

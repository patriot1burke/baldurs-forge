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
public class AmuletBuilder extends EquipmentBuilder {

    @Inject
    AmuletBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    AmuletModel model = new AmuletModel();

    @Override
    public AmuletModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (AmuletModel) model;
    }

    @ChatRoute(AmuletModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildAmulet() {
        return build();
    }

    @Tool("Set the name for the current amulet.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current amulet.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current amulet.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the visual model for the current amulet.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate());
    }

    @Tool("Add boost to the current amulet.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current amulet.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Tool(value = "Summarizes available visual models for the current amulet type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate());
    }

    private Predicate<? super Stat> visualModelPredicate() {
        return (stat) -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Amulet");
        };
    }

    @Tool(value = "When finished building amulet, call this tool to finish the amulet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

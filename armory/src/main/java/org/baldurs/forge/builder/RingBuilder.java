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
public class RingBuilder extends EquipmentBuilder {

    @Inject
    RingBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    RingModel model = new RingModel();

    @Override
    public RingModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (RingModel) model;
    }

    @ChatRoute(RingModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildRing() {
        return build();
    }

    @Tool("Set the name for the current ring.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current ring.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current ring.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the visual model for the current ring.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate());
    }

    @Tool("Add boost to the current ring.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current ring.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Ring");
        };
    }

    @Tool(value = "Summarizes available visual models for the current ring.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate());
    }

    @Tool(value = "When finished building ring, call this tool to finish the ring.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

    @Tool(value = "Cancel or abort building the current ring without saving it.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void cancelEquipment() {
        super.cancelEquipment();
    }

}

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
import io.quarkiverse.langchain4j.chat.frames.ResultEventTypes;
import io.quarkus.logging.Log;

@ApplicationScoped
public class RingBuilder extends EquipmentBuilder {

    @Inject
    RingBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            RingModel bootsModel = new RingModel();
            return bootsModel;
        };
    }

    @ChatFrame(RingModel.TYPE)
    @ResultEventTypes(MarkdownStringMessage.class)
    public Result<String> buildRing(RingModel current) {
        return build(current);
    }

    @Tool("Set the name for the current ring.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current ring.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current ring.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the visual model for the current ring.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        setVisualModel(current, visualModel, visualModelPredicate());
    }

    @Tool("Add boost to the current ring.")
    public void addBoost(String boostDescription) throws Exception {
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current ring.")
    public void setBoost(String boostDescription) throws Exception {
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        super.setBoost(current, boostDescription);
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
        RingModel current = context.getData(CURRENT_EQUIPMENT, RingModel.class);
        finishEquipment(current);
    }

}

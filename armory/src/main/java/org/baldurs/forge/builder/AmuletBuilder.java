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
public class AmuletBuilder extends EquipmentBuilder {

    @Inject
    AmuletBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            AmuletModel amuletModel = new AmuletModel();
            return amuletModel;
        };
    }

    @ChatFrame(AmuletModel.TYPE)
    @ResultMessageTypes(MarkdownStringMessage.class)
    public Result<String> buildAmulet(AmuletModel current) {
        return build(current);
    }

    @Tool("Set the name for the current amulet.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current amulet.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current amulet.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the visual model for the current amulet.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        setVisualModel(current, visualModel, visualModelPredicate());
    }

    @Tool("Add boost to the current amulet.")
    public void addBoost(String boostDescription) throws Exception {
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current amulet.")
    public void setBoost(String boostDescription) throws Exception {
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        super.setBoost(current, boostDescription);
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
        AmuletModel current = context.getData(CURRENT_EQUIPMENT, AmuletModel.class);
        finishEquipment(current);
    }

}

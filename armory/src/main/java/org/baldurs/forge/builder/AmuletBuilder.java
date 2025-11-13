package org.baldurs.forge.builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
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
    protected Class<? extends BaseModel> baseModelClass() {
        return AmuletModel.class;
    }

    @Override
    protected String schema() {
        return AmuletModel.schema;
    }

    @Override
    public String type() {
        return AmuletModel.TYPE;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            AmuletModel amuletModel = new AmuletModel();
            return amuletModel;
        };
    }

    @ChatFrame(AmuletModel.TYPE)
    public void build() {
        super.build();
    }

    @Tool("Set the name for the current amulet.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }

    @Tool("Set the description for the current amulet.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current amulet.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current amulet.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        super.setVisualModel(visualModel);
    }

    @Tool("Set the armor category for the current amulet.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        set(current -> ((BootsModel) current).armorCategory = armorCategory);
    }

    @Tool("Add boost to the current amulet.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current amulet.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Override
    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Amulet");
        };
    }

    @Tool(value = "Summarizes available visual models for the current amulet type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return super.showVisualModels();
    }

    @Tool(value = "When finished building amulet, call this tool to finish the amulet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

package org.baldurs.forge.builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CloakBuilder extends EquipmentBuilder {

    @Inject
    CloakBuilderChat agent;

    @Override
    protected BuilderChat agent() {
        return agent;
    }

    @Override
    protected Class<? extends BaseModel> baseModelClass() {
        return CloakModel.class;
    }
    @Override
    protected String schema() {
        return CloakModel.schema;
    }
    @Override
    protected String type() {
        return CloakModel.TYPE;
    }
    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            CloakModel cloakModel = new CloakModel();
            return cloakModel;
        };
    }

    @Startup
    public void start() {
        chatService.register(type(), this);
    }

    @Tool("Set the name for the current cloak.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }


    @Tool("Set the description for the current cloak.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current cloak.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current cloak.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        super.setVisualModel(visualModel);
    }

    @Tool("Set the armor category for the current cloak.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        set(current -> ((BootsModel) current).armorCategory = armorCategory);
    }

    @Tool("Add boost to the current cloak.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current cloak.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Override
    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Cloak");
        };
    }

    @Tool("Summarizes available visual models for the current cloak type.")
    public String showVisualModels() {
       return super.showVisualModels();
    }

    @Tool("When finished building cloak, call this tool to finish the cloak.")
    public String finishEquipment() throws Exception {
        return super.finishEquipment();
    }


}

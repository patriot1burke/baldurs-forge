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
public class GlovesBuilder extends EquipmentBuilder {

    @Inject
    GlovesBuilderChat agent;

    @Override
    protected BuilderChat agent() {
        return agent;
    }

    @Override
    protected Class<? extends BaseModel> baseModelClass() {
        return GlovesModel.class;
    }
    @Override
    protected String schema() {
        return GlovesModel.schema;
    }
    @Override
    protected String type() {
        return GlovesModel.TYPE;
    }
    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            GlovesModel bootsModel = new GlovesModel();
            return bootsModel;
        };
    }

    @Startup
    public void start() {
        chatService.register(type(), this);
    }

    @Tool("Set the name for the current gloves.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }


    @Tool("Set the description for the current gloves.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current gloves.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current gloves.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        super.setVisualModel(visualModel);
    }

    @Tool("Set the armor category for the current gloves.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        set(current -> ((BootsModel) current).armorCategory = armorCategory);
    }

    @Tool("Add boost to the current gloves.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current gloves.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Override
    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        GlovesModel gloves = context.getShared(CURRENT_EQUIPMENT, GlovesModel.class);
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

    @Tool("Summarizes available visual models for the current gloves.")
    public String showVisualModels() {
        return super.showVisualModels();
    }

    @Tool("When finished building gloves, call this tool to finish the gloves.")
    public String finishEquipment() throws Exception {
        return super.finishEquipment();
    }


}

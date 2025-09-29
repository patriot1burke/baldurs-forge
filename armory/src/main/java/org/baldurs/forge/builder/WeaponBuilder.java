package org.baldurs.forge.builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WeaponBuilder extends EquipmentBuilder {

    @Inject
    WeaponBuilderPrompt agent;
    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Class<? extends BaseModel> baseModelClass() {
        return WeaponModel.class;
    }
    @Override
    protected String schema() {
        return WeaponModel.schema;
    }
    @Override
    protected String type() {
        return WeaponModel.TYPE;
    }
    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            WeaponModel weapon = new WeaponModel();
            weapon.magical = true;
            return weapon;
        };
    }

    @Startup
    public void start() {
        chatService.register(type(), this::build);
    }

    @Tool("Set the name for the current weapon.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }


    @Tool("Set the description for the current weapon.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current weapon.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current weapon.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        super.setVisualModel(visualModel);
    }

    @Tool("Set the type for the current weapon.")
    public void setType(WeaponType type) {
        Log.info("Setting type: " + type);
        set(current -> ((WeaponModel) current).type = type);
    }

    @Tool("Set magical for the current weapon.")
    public void setMagical(Boolean magical) {
        Log.info("Setting magical: " + magical);
        set(current -> ((WeaponModel) current).magical = magical);
    }

    @Tool("Add boost to the current weapon.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current weapon.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }
    @Override
    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        WeaponModel weapon = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        if (weapon == null || weapon.type == null) {
            return null;
        }
        String searchString = weapon.type.name() + "s";
        return stat -> {
            String properties = stat.getField("Proficiency Group");
            return properties != null && properties.contains(searchString);
        };
    }

    @Tool(value = "Summarizes available visual models for the current weapon type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        WeaponModel weapon = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        if (weapon == null || weapon.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because weapon type is not set");
        }
        return super.showVisualModels();
    }
    @Tool(value = "When finished building weapon, call this tool to finish the weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }


}

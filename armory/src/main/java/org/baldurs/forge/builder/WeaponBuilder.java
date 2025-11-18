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
public class WeaponBuilder extends EquipmentBuilder {

    @Inject
    WeaponBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            WeaponModel weapon = new WeaponModel();
            weapon.magical = true;
            return weapon;
        };
    }

    @ChatFrame(WeaponModel.TYPE)
    @ResultEventTypes(MarkdownStringMessage.class)
    public Result<String> buildWeapon(WeaponModel current) {
        return build(current);
    }

    @Tool("Set the name for the current weapon.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current weapon.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current weapon.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the visual model for the current weapon.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool("Set the type for the current weapon.")
    public void setType(WeaponType type) {
        Log.info("Setting type: " + type);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        current.type = type;
        addShowEquipmentAction(current);
    }

    @Tool("Set magical for the current weapon.")
    public void setMagical(Boolean magical) {
        Log.info("Setting magical: " + magical);
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        current.magical = magical;
        addShowEquipmentAction(current);
    }

    @Tool("Add boost to the current weapon.")
    public void addBoost(String boostDescription) throws Exception {
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current weapon.")
    public void setBoost(String boostDescription) throws Exception {
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        super.setBoost(current, boostDescription);
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(WeaponModel weapon) {
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
        return showVisualModels(visualModelPredicate(weapon));
    }

    @Tool(value = "When finished building weapon, call this tool to finish the weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        WeaponModel current = context.getData(CURRENT_EQUIPMENT, WeaponModel.class);
        finishEquipment(current);
    }

}

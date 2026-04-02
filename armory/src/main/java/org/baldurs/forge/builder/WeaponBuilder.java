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
public class WeaponBuilder extends EquipmentBuilder {

    @Inject
    WeaponBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    WeaponModel model = new WeaponModel();

    @Override
    public WeaponModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WeaponModel) model;
    }

    @ChatRoute(WeaponModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildWeapon() {
        return build();
    }

    @Tool("Set the name for the current weapon.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current weapon.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current weapon.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the visual model for the current weapon.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool("Set the type for the current weapon.")
    public void setType(WeaponType type) {
        Log.info("Setting type: " + type);
        model().type = type;
        addShowEquipmentAction();
    }

    @Tool("Set magical for the current weapon.")
    public void setMagical(Boolean magical) {
        Log.info("Setting magical: " + magical);
        model().magical = magical;
        addShowEquipmentAction();
    }

    @Tool("Add boost to the current weapon.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current weapon.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
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
        if (model() == null || model().type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because weapon type is not set");
        }
        return showVisualModels(visualModelPredicate(model()));
    }

    @Tool(value = "When finished building weapon, call this tool to finish the weapon.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

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
public class HelmetBuilder extends EquipmentBuilder {

    @Inject
    HelmetBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    HelmetModel model = new HelmetModel();

    @Override
    public HelmetModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (HelmetModel) model;
    }

    @ChatRoute(HelmetModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildHelmet() {
        return build();
    }

    @Tool("Set the name for the current helmet.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current helmet.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current helmet.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the visual model for the current helmet.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool("Set the armor category for the current helmet.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        model().armorCategory = armorCategory;
        addShowEquipmentAction();
    }

    @Tool("Add boost to the current helmet.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current helmet.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(HelmetModel helmet) {
        return stat -> {
            String slot = stat.getField("Slot");
            if (slot == null || (slot != null && !slot.equals("Helmet"))) {
                return false;
            }
            if (helmet.armorCategory != null && helmet.armorCategory != ArmorCategory.None) {
                String properties = stat.getField("Proficiency Group");
                String searchString = helmet.armorCategory.name() + "Armor";
                return properties != null && properties.contains(searchString);
            } else {
                return true;
            }
        };
    }

    @Tool(value = "Summarizes available visual models for the current helmet type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate(model()));
    }

    @Tool(value = "When finished building helmet, call this tool to finish the helmet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

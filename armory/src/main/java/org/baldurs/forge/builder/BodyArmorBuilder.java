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
public class BodyArmorBuilder extends EquipmentBuilder {

    @Inject
    BodyArmorBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    BodyArmorModel model = new BodyArmorModel();

    @Override
    public BodyArmorModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (BodyArmorModel) model;
    }

    @ChatRoute(BodyArmorModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildBodyArmor() {
        return build();
    }

    @Tool("Set the name for the current body armor.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current body armor.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current body armor.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the armor class for the current body armor.")
    public void setArmorClass(Integer armorClass) {
        Log.info("Setting armor class: " + armorClass);
        model().armorClass = armorClass;
        addShowEquipmentAction();
    }

    @Tool("Set the type for the current body armor.")
    public void setType(BodyArmorType type) {
        Log.info("Setting type: " + type);
        model().type = type;
        addShowEquipmentAction();
    }

    @Tool("Add boost to body armor.")
    public void addBoost(String boost) throws Exception {
        super.addBoost(boost);
    }

    @Tool("Set boost for body armor.")
    public void setBoost(String boost) throws Exception {
        super.setBoost(boost);
    }

    @Tool(value = "Summarizes available visual models for the current body armor type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        if (model().type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because armor type is not set");
        }
        return showVisualModels(visualModelPredicate(model()));
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(BodyArmorModel armor) {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Breast");
        };
    }

    @Tool("Set the visual model for the current body armor.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool(value = "When finished building body armor, call this tool to finish the body armor.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

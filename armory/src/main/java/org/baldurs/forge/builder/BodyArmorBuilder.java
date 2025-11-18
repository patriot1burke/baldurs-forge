package org.baldurs.forge.builder;

import java.util.function.Predicate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownStringMessage;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

import com.google.common.base.Supplier;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ResultMessageTypes;
import io.quarkus.logging.Log;

@ApplicationScoped
public class BodyArmorBuilder extends EquipmentBuilder {

    @Inject
    BodyArmorBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> new BodyArmorModel();
    }

    @ChatFrame(BodyArmorModel.TYPE)
    @ResultMessageTypes(MarkdownStringMessage.class)
    public Result<String> buildBodyArmor(BodyArmorModel current) {
        return build(current);
    }

    @Tool("Set the name for the current body armor.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current body armor.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current body armor.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the armor class for the current body armor.")
    public void setArmorClass(Integer armorClass) {
        Log.info("Setting armor class: " + armorClass);
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        current.armorClass = armorClass;
        addShowEquipmentAction(current);
    }

    @Tool("Set the type for the current body armor.")
    public void setType(BodyArmorType type) {
        Log.info("Setting type: " + type);
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        current.type = type;
        addShowEquipmentAction(current);
    }

    @Tool("Add boost to body armor.")
    public void addBoost(String boost) throws Exception {
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        super.addBoost(current, boost);
    }

    @Tool("Set boost for body armor.")
    public void setBoost(String boost) throws Exception {
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        super.setBoost(current, boost);
    }

    @Tool(value = "Summarizes available visual models for the current body armor type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        BodyArmorModel armor = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        if (armor.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because armor type is not set");
        }
        return showVisualModels(visualModelPredicate(armor));
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
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool(value = "When finished building body armor, call this tool to finish the body armor.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        BodyArmorModel current = context.getData(CURRENT_EQUIPMENT, BodyArmorModel.class);
        finishEquipment(current);
    }

}

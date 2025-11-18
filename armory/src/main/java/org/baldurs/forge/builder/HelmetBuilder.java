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
import io.quarkiverse.langchain4j.chat.frames.ResultMessageTypes;
import io.quarkus.logging.Log;

@ApplicationScoped
public class HelmetBuilder extends EquipmentBuilder {

    @Inject
    HelmetBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            HelmetModel bootsModel = new HelmetModel();
            return bootsModel;
        };
    }

    @ChatFrame(HelmetModel.TYPE)
    @ResultMessageTypes(MarkdownStringMessage.class)
    public Result<String> buildHelmet(HelmetModel current) {
        return build(current);
    }

    @Tool("Set the name for the current helmet.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current helmet.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current helmet.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the visual model for the current helmet.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool("Set the armor category for the current helmet.")
    public void setArmorCategory(ArmorCategory armorCategory) {
        Log.info("Setting armorCategory: " + armorCategory);
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        current.armorCategory = armorCategory;
        addShowEquipmentAction(current);
    }

    @Tool("Add boost to the current helmet.")
    public void addBoost(String boostDescription) throws Exception {
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current helmet.")
    public void setBoost(String boostDescription) throws Exception {
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        super.setBoost(current, boostDescription);
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
        HelmetModel helmet = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        return showVisualModels(visualModelPredicate(helmet));
    }

    @Tool(value = "When finished building helmet, call this tool to finish the helmet.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        HelmetModel current = context.getData(CURRENT_EQUIPMENT, HelmetModel.class);
        finishEquipment(current);
    }

}

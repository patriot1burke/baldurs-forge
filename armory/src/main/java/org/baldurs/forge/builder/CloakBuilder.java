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
import io.quarkiverse.langchain4j.chat.frames.EventMapper;
import io.quarkus.logging.Log;

@ApplicationScoped
public class CloakBuilder extends EquipmentBuilder {

    @Inject
    CloakBuilderPrompt agent;

    @Override
    protected BuilderPrompt agent() {
        return agent;
    }

    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            CloakModel cloakModel = new CloakModel();
            return cloakModel;
        };
    }

    @ChatFrame(CloakModel.TYPE)
    @EventMapper(MarkdownStringMessage.class)
    public Result<String> buildCloak(CloakModel current) {
        return build(current);
    }

    @Tool("Set the name for the current cloak.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        current.name = name;
        addShowEquipmentAction(current);
    }

    @Tool("Set the description for the current cloak.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        current.description = description;
        addShowEquipmentAction(current);
    }

    @Tool("Set the rarity for the current cloak.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        current.rarity = rarity;
        addShowEquipmentAction(current);
    }

    @Tool("Set the visual model for the current cloak.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        setVisualModel(current, visualModel, visualModelPredicate(current));
    }

    @Tool("Add boost to the current cloak.")
    public void addBoost(String boostDescription) throws Exception {
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        super.addBoost(current, boostDescription);
    }

    @Tool("Set boost macro for the current cloak.")
    public void setBoost(String boostDescription) throws Exception {
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        super.setBoost(current, boostDescription);
    }

    @Tool(value = "Summarizes available visual models for the current cloak type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        CloakModel cloak = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        return showVisualModels(visualModelPredicate(cloak));
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(CloakModel cloak) {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Cloak");
        };
    }

    @Tool(value = "When finished building cloak, call this tool to finish the cloak.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        CloakModel current = context.getData(CURRENT_EQUIPMENT, CloakModel.class);
        finishEquipment(current);
    }

}

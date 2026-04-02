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
public class CloakBuilder extends EquipmentBuilder {

    @Inject
    CloakBuilderPrompt agent;

    @Override
    public BuilderPrompt agent() {
        return agent;
    }

    CloakModel model = new CloakModel();

    @Override
    public CloakModel model() {
        return model;
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (CloakModel) model;
    }

    @ChatRoute(CloakModel.TYPE)
    @MarkdownToHtml
    public Result<String> buildCloak() {
        return build();
    }

    @Tool("Set the name for the current cloak.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        model().name = name;
        addShowEquipmentAction();
    }

    @Tool("Set the description for the current cloak.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        model().description = description;
        addShowEquipmentAction();
    }

    @Tool("Set the rarity for the current cloak.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        model().rarity = rarity;
        addShowEquipmentAction();
    }

    @Tool("Set the visual model for the current cloak.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        setVisualModel(visualModel, visualModelPredicate(model()));
    }

    @Tool("Add boost to the current cloak.")
    public void addBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current cloak.")
    public void setBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Tool(value = "Summarizes available visual models for the current cloak type.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public String showVisualModels() {
        return showVisualModels(visualModelPredicate(model()));
    }

    private Predicate<? super StatsArchive.Stat> visualModelPredicate(CloakModel cloak) {
        return stat -> {
            String slot = stat.getField("Slot");
            return slot != null && slot.equals("Cloak");
        };
    }

    @Tool(value = "When finished building cloak, call this tool to finish the cloak.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishEquipment() throws Exception {
        super.finishEquipment();
    }

}

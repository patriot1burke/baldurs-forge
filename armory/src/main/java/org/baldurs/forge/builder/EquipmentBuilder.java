package org.baldurs.forge.builder;

import java.util.List;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.baldurs.forge.messages.ListVisualModelsMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chatscopes.ChatRouteContext;
import io.quarkiverse.langchain4j.chatscopes.ChatScope;
import io.quarkiverse.langchain4j.chatscopes.ChatScopeMemory;
import io.quarkus.logging.Log;

public abstract class EquipmentBuilder {

    @Inject
    BoostService boostService;

    @Inject
    LibraryService library;

    @Inject
    BoostBuilderPrompt boostBuilder;

    @Inject
    ChatRouteContext ctx;

    @Inject
    ObjectMapper mapper;

    @PostConstruct
    public void init() {
        BaseModel current = model();
        current.name = "New Item";
        current.rarity = Rarity.Common;
    }

    public abstract <T extends BaseModel> T model();

    public abstract void setModel(BaseModel model);

    public abstract BuilderPrompt agent();

    public Result<String> build() {
        Log.info("chat: " + ctx.request().userMessage());
        String currentJson = null;
        try {
            currentJson = mapper.writeValueAsString(model());
        } catch (JsonProcessingException e) {
            Log.error("Error serializing current", e);
            throw new RuntimeException("Error serializing current", e);
        }
        Log.info("Current JSON: " + currentJson);
        Result<String> result = agent().build(model().type(), model().schema(), currentJson,
                ctx.request().userMessage());
        return result;
    }

    public void addShowEquipmentAction() {
        if (model().baseStat() == null) {
            return;
        }
        EquipmentModel equipment = model().toEquipmentModel(boostService, library);
        ctx.response().event(new ShowEquipmentMessage(equipment));
    }

    @Inject
    NewModBuilder modBuilder;

    public void finishEquipment() throws Exception {
        addShowEquipmentAction();
        ChatScope.pop();
        if (model().rarity == null) {
            model().rarity = Rarity.Common;
        }
        modBuilder.addEquipment(model());
        ctx.response().message("Finished building item!");
        ctx.response().event("PopRoute", "pop");
        ChatScopeMemory.scheduleWipe();
    }

    public void setVisualModel(String visualModel, Predicate<? super Stat> visualModelPredicate) {
        List<RootTemplate> rootTemplates = library.findRootIconsFrom(visualModelPredicate);
        boolean found = false;
        for (RootTemplate rootTemplate : rootTemplates) {
            if (rootTemplate.MapKey.equals(visualModel)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Could not find visual model.");
        }
        model().visualModel = visualModel;
        addShowEquipmentAction();
    }

    public void addBoost(String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("addBoost: " + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(ctx.request().userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            ctx.response().message(enchantment);
            ctx.response().message("Could not create a boost macro from your description.");
            return;
        }
        if (model().boosts == null || model().boosts.isEmpty()) {
            model().boosts = enchantment;
        } else {
            model().boosts += ";" + enchantment;
        }
        addShowEquipmentAction();
    }

    public void setBoost(String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("setBoost: " + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(ctx.request().userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            ctx.response().message(enchantment);
            ctx.response().message("Could not create a boost macro from your description.");
            return;
        }
        model().boosts = enchantment;
        addShowEquipmentAction();
    }

    // todo, may be able to have a void return type.  Nervous that AI gets confused if it does not get a return value for this tool.
    public String showVisualModels(Predicate<? super Stat> visualModelPredicate) {
        if (visualModelPredicate == null) {
            ctx.response().message("Item not finished yet.  Cannot search for visual models.");
            return null;
        }
        List<RootTemplate> rootTemplates = library.findRootIconsFrom(visualModelPredicate);
        ListVisualModelsMessage action = new ListVisualModelsMessage();
        for (RootTemplate rootTemplate : rootTemplates) {
            String icon = rootTemplate.resolveIcon();
            if (icon == null) {
                continue;
            }
            String iconPath = library.icons().get(icon);
            if (iconPath == null) {
                continue;
            }
            action.add(iconPath, rootTemplate.MapKey);
        }
        ctx.response().event(action);
        String message = "There are " + rootTemplates.size()
                + " visual models available. Choose one of the parent ids from the list above if you want a different look for your weapon.";
        ctx.response().message(message);

        return null;
    }
}

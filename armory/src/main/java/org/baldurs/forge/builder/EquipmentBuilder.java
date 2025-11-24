package org.baldurs.forge.builder;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.baldurs.forge.messages.ListVisualModelsMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.messages.UpdateNewEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkus.logging.Log;

public abstract class EquipmentBuilder {
    public static final String CURRENT_EQUIPMENT = "current";

    @Inject
    ChatFrameContext context;

    ObjectMapper mapper;

    @Inject
    BoostService boostService;

    @Inject
    LibraryService library;

    @Inject
    BoostBuilderPrompt boostBuilder;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    protected abstract BuilderPrompt agent();

    protected abstract Supplier<BaseModel> supplier();

    protected BaseModel create() {
        BaseModel current = supplier().get();
        current.name = "New Item";
        current.rarity = Rarity.Common;
        return current;
    }

    public void startBuild() {
        // clear chat history and start conversation with this builder
        BaseModel current = create();
        context.pushFrame(current.type());
        context.setData(CURRENT_EQUIPMENT, current);
        context.currentFrame().chat();
    }

    public Result<String> build(BaseModel current) {
        Log.info("chat: " + context.currentFrameId() + " " + context.userMessage());
        String currentJson = null;
        try {
            currentJson = mapper.writeValueAsString(current);
        } catch (JsonProcessingException e) {
            Log.error("Error serializing current", e);
            throw new RuntimeException("Error serializing current", e);
        }
        Log.info("Current JSON: " + currentJson);
        Result<String> result = agent().build(current.type(), current.schema(), currentJson,
                context.userMessage());
        return result;
    }

    public void addShowEquipmentAction(BaseModel baseModel) {
        if (baseModel == null || baseModel.baseStat() == null) {
            return;
        }
        EquipmentModel equipment = baseModel.toEquipmentModel(boostService, library);
        ShowEquipmentMessage.addResponse(context, equipment);
    }

    public void finishEquipment(BaseModel current) throws Exception {
        addShowEquipmentAction(current);
        context.popFrame();
        if (current.rarity == null) {
            current.rarity = Rarity.Common;
        }
        NewModModel newEquipment = context.getData(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            newEquipment = new NewModModel();
            context.setData(NewModModel.NEW_EQUIPMENT, newEquipment);
        }
        newEquipment.addEquipment(current);
        context.addEvent("Finished building item!");
        UpdateNewEquipmentMessage.addResponse(context, "To create a mod containing your newly built equipment, tell me to '"
                + ModPackager.PACKAGE_MODE_CHAT_COMMAND + "'", newEquipment.count);
        Log.info("Finished equipment");
        context.scheduleWipe();
    }

    public void setVisualModel(BaseModel current, String visualModel, Predicate<? super Stat> visualModelPredicate) {
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
        current.visualModel = visualModel;
        addShowEquipmentAction(current);
    }

    public void addBoost(BaseModel current, String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("addBoost: " + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.addEvent(enchantment);
            context.addEvent("Could not create a boost macro from your description.");
            return;
        }
        if (current.boosts == null || current.boosts.isEmpty()) {
            current.boosts = enchantment;
        } else {
            current.boosts += ";" + enchantment;
        }
        context.setData(CURRENT_EQUIPMENT, current);
        addShowEquipmentAction(current);
    }

    public void setBoost(BaseModel current, String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("setBoost: " + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.addEvent(enchantment);
            context.addEvent("Could not create a boost macro from your description.");
            return;
        }
        current.boosts = enchantment;
        addShowEquipmentAction(current);
    }

    // todo, may be able to have a void return type.  Nervous that AI gets confused if it does not get a return value for this tool.
    public String showVisualModels(Predicate<? super Stat> visualModelPredicate) {
        if (visualModelPredicate == null) {
            context.addEvent("Item not finished yet.  Cannot search for visual models.");
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
        action.addResponse(context);
        String message = "There are " + rootTemplates.size()
                + " visual models available. Choose one of the parent ids from the list above if you want a different look for your weapon.";
        context.addEvent(message);

        return null;
    }
}

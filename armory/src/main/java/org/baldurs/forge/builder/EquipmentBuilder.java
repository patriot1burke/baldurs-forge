package org.baldurs.forge.builder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.baldurs.forge.chat.ChatFrame;
import org.baldurs.forge.chat.ChatFrameService;
import org.baldurs.forge.chat.actions.ListVisualModelsAction;
import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.chat.actions.ShowEquipmentAction;
import org.baldurs.forge.chat.actions.UpdateNewEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.service.MemoryId;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class EquipmentBuilder implements ChatFrame {
    public static final String CURRENT_EQUIPMENT = "currentEquipment";

    @Inject
    ChatContext context;

    ObjectMapper mapper;

    @Inject
    ChatFrameService chatService;

    @Inject
    BoostService boostService;

    @Inject
    LibraryService library;

    @Inject
    BoostBuilderChat boostBuilder;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    protected abstract BuilderChat agent();
    protected abstract Class<? extends BaseModel> baseModelClass();
    protected abstract String schema();
    protected abstract String type();
    protected abstract Supplier<BaseModel> supplier();

    protected BaseModel create() {
        BaseModel current = supplier().get();
        current.name = "New Item";
        current.rarity = Rarity.Common;
        return current;
    }

    public String chat(@MemoryId String memoryId, String userMessage) {
        Log.info("chat: " + memoryId + " " + userMessage);
        chatService.setChatFrame(context, type());
        String currentJson = "{}";
        BaseModel current = null;
        if ((current = context.getShared(CURRENT_EQUIPMENT, baseModelClass())) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing equipment", e);
            }
        } else {
            current = create();
            context.setShared(CURRENT_EQUIPMENT, current);
        }
        Log.info("Current JSON: " + currentJson);
        return agent().build(context.memoryId(), type(), schema(), currentJson, userMessage);
    }


    public void addShowEquipmentAction(BaseModel baseModel) {
        if (baseModel == null || baseModel.baseStat() == null) {
            return;
        }
        EquipmentModel equipment = baseModel.toEquipmentModel(boostService, library);
        ShowEquipmentAction.addResponse(context, equipment);
    }

    public String finishEquipment() throws Exception {
        BaseModel current = null;
        if ((current = context.getShared(CURRENT_EQUIPMENT, baseModelClass())) == null) {
            return "No equipment to finish";
        }
        addShowEquipmentAction(current);
        chatService.popChatFrame(context);
        context.setShared(CURRENT_EQUIPMENT, null);
        if (current.rarity == null) {
            current.rarity = Rarity.Common;
        }
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            newEquipment = new NewModModel();
        }
        newEquipment.addEquipment(current);
        context.setShared(NewModModel.NEW_EQUIPMENT, newEquipment);
        context.response().add(new MessageAction("Finished building item!"));
        context.response().add(new UpdateNewEquipmentAction("To create a mod containing your newly built equipment, tell me to '" + ModPackager.PACKAGE_MODE_CHAT_COMMAND + "'"));
        context.suppressAIResponse();
        Log.info("Finishing equipment");
        String json = logJson(current);
        return json;
    }

    protected String logJson(BaseModel equipment)  {
        try {
        String equipmentJson = mapper.writeValueAsString(equipment);
        Log.info("Equipment JSON: " + equipmentJson);
        return equipmentJson;
        } catch (Exception e) {
            throw new RuntimeException("Error logging weapon json", e);
        }
    }
    
    protected void set(Consumer<BaseModel> consumer) {
        BaseModel current = context.getShared(CURRENT_EQUIPMENT, baseModelClass());
        if (current == null) {
            current = create();
        }
        consumer.accept(current);
        context.setShared(CURRENT_EQUIPMENT, current);
        logJson(current);
        addShowEquipmentAction(current);
    }

    public void setVisualModel(String visualModel) {
        Predicate<? super Stat> visualModelPredicate = visualModelPredicate();
        if (visualModelPredicate == null) {
            throw new RuntimeException("Item is not finished.  Cannot set visual model yet." + visualModel);
        }
        List<RootTemplate> rootTemplates = library.findRootIconsFrom(visualModelPredicate);
        boolean found = false;
        for (RootTemplate rootTemplate : rootTemplates) {
            if (rootTemplate.MapKey.equals(visualModel)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Could not find visual model." );
        }
        set(current -> current.visualModel = visualModel);
    }

    public void addBoost(String boostDescription) throws Exception {
         // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("addBoost: "  + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.response().add(new MessageAction(enchantment));
            context.response().add(new MessageAction("Could not create a boost macro from your description."));
            return;
        }
        BaseModel current = context.getShared(CURRENT_EQUIPMENT, baseModelClass());
        if (current == null) {
            current = create();
        }
        if (current.boosts == null || current.boosts.isEmpty()) {
            current.boosts = enchantment;
        } else {
            current.boosts += ";" + enchantment;
        }
        context.setShared(CURRENT_EQUIPMENT, current);
        addShowEquipmentAction(current);
        logJson(current);
    }

    public void setBoost(String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("setBoost: "  + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.response().add(new MessageAction(enchantment));
            context.response().add(new MessageAction("Could not create a boost macro from your description."));
            return;
        }
        set(current -> current.boosts = enchantment);
    }

    protected abstract Predicate<? super StatsArchive.Stat> visualModelPredicate();

    public String showVisualModels() {
        Predicate<? super Stat> visualModelPredicate = visualModelPredicate();
        if (visualModelPredicate == null) {
            throw new RuntimeException("Item not finished yet.  Cannot search for visual models.");
        }
       List<RootTemplate> rootTemplates = library.findRootIconsFrom(visualModelPredicate);
        ListVisualModelsAction action = new ListVisualModelsAction();
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
        context.response().add(action);
        String message = "There are " + rootTemplates.size() + " visual models available. Choose one of the parent ids from the list above if you want a different look for your weapon.";
        context.response().add(new MessageAction(message));

        // Ignore the next AI chat response because the AI often says it cannot find anything
        // if a data list is not sent back as a tool result.
        context.suppressAIResponse();
        // Had to return something because AI would get confused sometimes.
        return "Found some visual models";

    }
}

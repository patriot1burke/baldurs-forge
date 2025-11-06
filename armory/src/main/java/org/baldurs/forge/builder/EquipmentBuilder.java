package org.baldurs.forge.builder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.baldurs.forge.messages.ListVisualModelsMessage;
import org.baldurs.forge.messages.ShowEquipmentMessage;
import org.baldurs.forge.messages.UpdateNewEquipmentMessage;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;
import org.baldurs.forge.services.MarkdownToHtmlService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkiverse.langchain4j.chat.context.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameService;
import io.quarkiverse.langchain4j.chat.frames.ObjectMessage;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class EquipmentBuilder {
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
    BoostBuilderPrompt boostBuilder;

    @Inject
    ChatMemoryStore chatMemoryStore;

    @Inject
    MarkdownToHtmlService renderer;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    protected abstract BuilderPrompt agent();
    protected abstract Class<? extends BaseModel> baseModelClass();
    protected abstract String schema();
    public abstract String type();
    protected abstract Supplier<BaseModel> supplier();

    protected BaseModel create() {
        BaseModel current = supplier().get();
        current.name = "New Item";
        current.rarity = Rarity.Common;
        return current;
    }

    public void startBuild() {
        // clear chat history and start conversation with this builder
        chatMemoryStore.deleteMessages(context.memoryId());
        chatService.pushFrame(type());
        build();
    }

    public void build() {
        Log.info("chat: " + context.memoryId() + " " + context.userMessage());
        String currentJson = "{}";
        BaseModel current = null;
        if ((current = context.getData(CURRENT_EQUIPMENT, baseModelClass())) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing equipment", e);
            }
        } else {
            current = create();
            context.setData(CURRENT_EQUIPMENT, current);
        }
        Log.info("Current JSON: " + currentJson);
        Result<String> result = agent().build(context.memoryId(), type(), schema(), currentJson, context.userMessage());
        if (result.content() != null) {
            Log.info("EquipmentBuilder with content: " + result.content());
            String html = renderer.markdownToHtml(result.content());
            context.response().add(new ObjectMessage(html));
            return;
        }
        String msg = null;
        if (result.toolExecutions().isEmpty()) {
            Log.info("EquipmentBuilder with no tool executions");
            return;
        } else {
            for (ToolExecution execution : result.toolExecutions()) {
                Log.info("EquipmentBuilder with tool " + execution.request().name() + " execution: " + execution.result());
                if (execution.resultObject() == null) {
                    Log.info("EquipmentBuilder with null/void tool execution result");
                    continue;
                } else {
                    if (msg == null || msg.isEmpty()) {
                        msg = execution.result();
                    } else {
                        msg += execution.result() + "\n";
                    }
                }
            }
            
        }
        if (msg != null) {
            context.response().add(new ObjectMessage(renderer.markdownToHtml(msg)));
        }
    }


    public void addShowEquipmentAction(BaseModel baseModel) {
        if (baseModel == null || baseModel.baseStat() == null) {
            return;
        }
        EquipmentModel equipment = baseModel.toEquipmentModel(boostService, library);
        ShowEquipmentMessage.addResponse(context, equipment);
    }

    public void finishEquipment() throws Exception {
        BaseModel current = null;
        if ((current = context.getData(CURRENT_EQUIPMENT, baseModelClass())) == null) {
            throw new RuntimeException("No equipment to finish");
        }
        addShowEquipmentAction(current);
        chatService.popFrame();
        context.setData(CURRENT_EQUIPMENT, null);
        if (current.rarity == null) {
            current.rarity = Rarity.Common;
        }
        NewModModel newEquipment = context.getData(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            newEquipment = new NewModModel();
        }
        newEquipment.addEquipment(current);
        context.setData(NewModModel.NEW_EQUIPMENT, newEquipment);
        context.response().add(new ObjectMessage("Finished building item!"));
        context.response().add(new UpdateNewEquipmentMessage("To create a mod containing your newly built equipment, tell me to '" + ModPackager.PACKAGE_MODE_CHAT_COMMAND + "'"));
        Log.info("Finished equipment");
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
        BaseModel current = context.getData(CURRENT_EQUIPMENT, baseModelClass());
        if (current == null) {
            current = create();
        }
        consumer.accept(current);
        context.setData(CURRENT_EQUIPMENT, current);
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
            context.response().add(new ObjectMessage(enchantment));
            context.response().add(new ObjectMessage("Could not create a boost macro from your description."));
            return;
        }
        BaseModel current = context.getData(CURRENT_EQUIPMENT, baseModelClass());
        if (current == null) {
            current = create();
        }
        if (current.boosts == null || current.boosts.isEmpty()) {
            current.boosts = enchantment;
        } else {
            current.boosts += ";" + enchantment;
        }
        context.setData(CURRENT_EQUIPMENT, current);
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
            context.response().add(new ObjectMessage(enchantment));
            context.response().add(new ObjectMessage("Could not create a boost macro from your description."));
            return;
        }
        set(current -> current.boosts = enchantment);
    }

    protected abstract Predicate<? super StatsArchive.Stat> visualModelPredicate();

    // todo, may be able to have a void return type.  Nervous that AI gets confused if it does not get a return value for this tool.
    public String showVisualModels() {
        Predicate<? super Stat> visualModelPredicate = visualModelPredicate();
        if (visualModelPredicate == null) {
            context.response().add(new ObjectMessage("Item not finished yet.  Cannot search for visual models."));
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
        context.response().add(action);
        String message = "There are " + rootTemplates.size() + " visual models available. Choose one of the parent ids from the list above if you want a different look for your weapon.";
        context.response().add(new ObjectMessage(message));

        return null;
    }
}

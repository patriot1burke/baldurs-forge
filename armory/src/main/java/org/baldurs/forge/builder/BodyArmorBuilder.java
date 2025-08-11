package org.baldurs.forge.builder;

import java.util.List;

import org.baldurs.forge.chat.BaldursChat;
import org.baldurs.forge.chat.ChatService;
import org.baldurs.forge.chat.MessageAction;
import org.baldurs.forge.chat.RenderService;
import org.baldurs.forge.chat.actions.ShowEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.toolbox.BoostService;
import org.baldurs.forge.toolbox.LibraryService;
import org.baldurs.forge.toolbox.BoostService.BoostWriter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BodyArmorBuilder implements BaldursChat {
    private static final String CURRENT_BODY_ARMOR = "currentBodyArmor";

    @Inject
    ArmorBuilderChat agent;

    @Inject
    ChatContext context;

    ObjectMapper mapper;

    @Inject
    ChatService chatService;

    @Inject
    RenderService renderer;


    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    public String chat(@MemoryId String memoryId, String userMessage) {
        Log.info("BodyArmorBuilder.chat: " + memoryId + " " + userMessage);
        chatService.setChatFrame(context, BodyArmorBuilder.class);
        String currentJson = "{}";
        BodyArmorModel current = null;
        if ((current = context.getShared(CURRENT_BODY_ARMOR, BodyArmorModel.class)) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing body armor", e);
            }
        }
        Log.info("Current JSON: " + currentJson);
        String response = agent.buildBodyArmor(context.memoryId(), "body armor", "updateBodyArmor", "finishBodyArmor", BodyArmorModel.schema, currentJson, userMessage);
        String html = renderer.markdownToHtml(response);
        return html;
    }

    @Inject
    BoostService boostService;

    @Inject
    LibraryService library;

    public void addShowEquipmentAction(BodyArmorModel armor) {
        if (armor == null || armor.type == null) {
            return;
        }
        EquipmentModel equipment = new EquipmentModel();
        equipment.type = EquipmentType.Armor;
        equipment.slot = EquipmentSlot.Breast;
        equipment.rarity = armor.rarity;
        equipment.name = armor.name == null ? "New Body Armor" : armor.name;
        equipment.description = armor.description;
        if (armor.boosts != null) {
            BoostWriter writer = boostService.html();
            boostService.macros(armor.boosts, writer);
            equipment.boostDescription = writer.toString();
        } else {
            equipment.boostDescription = "";
        }
        StatsArchive.Stat stat = library.archive().getStats().getByName(armor.type.baseStat);
        if (armor.armorClass == null) {
            equipment.armorClass = Integer.parseInt(stat.getField("ArmorClass"));
        } else {
            equipment.armorClass = armor.armorClass;
        }
        RootTemplate template = null;
        if (armor.visualModel != null) {
            template = library.archive().getRootTemplates().getRootTemplate(armor.visualModel);
        } else {
            template = library.archive().getRootTemplates().getRootTemplate(stat.getField("RootTemplate"));
        }
        equipment.icon = library.icons().get(template.resolveIcon());
        context.response().add(new ShowEquipmentAction(equipment));
    }

    @Tool("Update the current body armor json document.")
    public String updateBodyArmor(BodyArmorModel bodyArmor) {
        Log.info("Updating body armor: ");
        logBodyArmorJson(bodyArmor);
        BodyArmorModel current = null;
        if ((current = context.getShared(CURRENT_BODY_ARMOR, BodyArmorModel.class)) != null) {
            if (bodyArmor.name != null) {
                current.name = bodyArmor.name;
            }
            if (bodyArmor.description != null) {
                current.description = bodyArmor.description;
            }
            if (bodyArmor.boosts != null) {
                current.boosts = bodyArmor.boosts;
            }
            if (bodyArmor.armorClass != null) {
                current.armorClass = bodyArmor.armorClass;
            }
            if (bodyArmor.type != null) {
                current.type = bodyArmor.type;
            }
            if (bodyArmor.rarity != null) {
                current.rarity = bodyArmor.rarity;
            }
            if (bodyArmor.visualModel != null) {
                current.visualModel = bodyArmor.visualModel;
            }
        } else {
            current = bodyArmor;
        }
        String json =logBodyArmorJson(current);
        context.setShared(CURRENT_BODY_ARMOR, current);
        Log.info("End of updateBodyArmor");
        addShowEquipmentAction(current);
        return json;
    }

    @Tool("When finished building body armor, call this tool to finish the body armor.")
    public String finishBodyArmor() throws Exception {
        BodyArmorModel current = null;
        if ((current = context.getShared(CURRENT_BODY_ARMOR, BodyArmorModel.class)) == null) {
            return "No body armor to finish";
        }
        addShowEquipmentAction(current);
        chatService.popChatFrame(context);
        context.setShared(CURRENT_BODY_ARMOR, null);
        Log.info("Finishing body armor");
        String armorJson = logBodyArmorJson(current);
        return armorJson;
    }

    private String logBodyArmorJson(BodyArmorModel armor)  {
        try {
        String armorJson = mapper.writeValueAsString(armor);
        Log.info("Body Armor JSON: " + armorJson);
        return armorJson;
        } catch (Exception e) {
            throw new RuntimeException("Error logging body armor json", e);
        }
    }


    @Tool("Add boost macro to body armor.  Called after the createBoostMacro tool is called.")
    public void addBodyArmorBoost(String boostMacro) throws Exception {
        Log.info("Adding boost macro to body armor: "  + boostMacro);
        BodyArmorModel armor = context.getShared(CURRENT_BODY_ARMOR, BodyArmorModel.class);
        if (armor.boosts == null || armor.boosts.isEmpty()) {
            armor.boosts = boostMacro;
        } else {
            armor.boosts += ";" + boostMacro;
        }
        context.setShared(CURRENT_BODY_ARMOR, armor);
        addShowEquipmentAction(armor);
        logBodyArmorJson(armor);
    }

    @Tool("Summarizes available visual models for the current body armor type.")
    public String showBodyArmorVisualModels() {
        BodyArmorModel armor = context.getShared(CURRENT_BODY_ARMOR, BodyArmorModel.class);
        if (armor == null || armor.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because body armor type is not set");
        }
        Log.info("Finding visual models for body armor type: " + armor.type.name());
        List<RootTemplate> rootTemplates = library.findRootIconsFrom(stat -> stat.getField("ArmorType") != null && stat.getField("ArmorType").equals(armor.type.name()) && stat.getField("Slot") != null && stat.getField("Slot").equals("Breast"));
        String html = "<ul>";
        for (RootTemplate rootTemplate : rootTemplates) {
            html += "<li><img src=\"" + library.icons().get(rootTemplate.resolveIcon()) + "\" height=\"64\" width=\"64\"/> " + rootTemplate.MapKey + "</li>";
        }
        html += "</ul>";
        context.response().add(new MessageAction(html));
        String message = "There are " + rootTemplates.size() + " visual models available.  Here are a few of them, a full list is available above.";
        for (int i = 0; i < rootTemplates.size() && i < 3; i++) {
            String name = library.archive().localizations.getLocalization(rootTemplates.get(i).DisplayName);

            message += name + ", ";
        }
        context.response().add(new MessageAction(message));

        // Had to return something because AI would specify that nothing was found.
        // I really wanted to just return html via the context.response() and output nothing and let the client render the html however it wants.
        return message;
    }

}

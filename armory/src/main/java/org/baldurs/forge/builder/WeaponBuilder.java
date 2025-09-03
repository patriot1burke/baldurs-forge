package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baldurs.forge.chat.ChatFrame;
import org.baldurs.forge.chat.ChatService;
import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.chat.actions.UpdateNewEquipmentAction;
import org.baldurs.forge.chat.RenderService;
import org.baldurs.forge.chat.actions.ShowEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;
import org.baldurs.forge.services.BoostService.BoostWriter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WeaponBuilder implements ChatFrame {
    public static final String CURRENT_WEAPON = "currentWeapon";

    @Inject
    WeaponBuilderChat agent;

    @Inject
    ChatContext context;

    ObjectMapper mapper;

    @Inject
    ChatService chatService;

    @Inject
    RenderService renderer;
    
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

    public String chat(@MemoryId String memoryId, String userMessage) {
        Log.info("WeaponBuilder.chat: " + memoryId + " " + userMessage);
        chatService.setChatFrame(context, WeaponBuilder.class);
        String currentJson = "{}";
        WeaponModel current = null;
        if ((current = context.getShared(CURRENT_WEAPON, WeaponModel.class)) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing weapon", e);
            }
        }
        Log.info("Current JSON: " + currentJson);
        return agent.buildWeapon(context.memoryId(), "weapon", WeaponModel.schema, currentJson, userMessage);
    }


    public void addShowEquipmentAction(WeaponModel weapon) {
        if (weapon == null || weapon.type == null) {
            return;
        }
        EquipmentModel equipment = weaponModelToEquipmentModel(weapon);
        ShowEquipmentAction.addResponse(context, equipment);
    }

    public EquipmentModel weaponModelToEquipmentModel(WeaponModel weapon) {
        EquipmentModel equipment = new EquipmentModel();
        equipment.type = EquipmentType.Weapon;
        equipment.slot = weapon.type.isRanged() ? EquipmentSlot.Ranged : EquipmentSlot.Melee;
        equipment.rarity = weapon.rarity;
        equipment.name = weapon.name == null ? "New Weapon" : weapon.name;
        equipment.description = weapon.description;
        if (weapon.boosts != null) {
            BoostWriter writer = boostService.html();
            boostService.macros(weapon.boosts, writer);
            equipment.boostDescription = writer.toString();
        } else {
            equipment.boostDescription = "";
        }
        StatsArchive.Stat stat = library.archive().getStats().getByName(weapon.type.baseStat);

        RootTemplate template = null;
        if (weapon.visualModel != null) {
            template = library.archive().getRootTemplates().getRootTemplate(weapon.visualModel);
        } else {
            template = library.archive().getRootTemplates().getRootTemplate(stat.getField("RootTemplate"));
        }
        equipment.damage = stat.getField("Damage");
        equipment.damageType = stat.getField("Damage Type");
        equipment.versatileDamage = stat.getField("Versatile Damage");

        List<String> weaponProperties = new ArrayList<>();
        String properties = stat.getField("Weapon Properties");
        if (properties != null) {
            String[] props = properties.split(";");
            for (String prop : props) {
                weaponProperties.add(prop);
            }
        }
        equipment.weaponProperties = weaponProperties;

        equipment.icon = library.icons().get(template.resolveIcon());
        return equipment;
    }

    @Tool("Update the current weapon json document.")
    public String updateWeapon(WeaponModel weapon) {
        Log.info("Updating weapon: ");
        logJson(weapon);
        WeaponModel current = null;
        if ((current = context.getShared(CURRENT_WEAPON, WeaponModel.class)) != null) {
            if (weapon.name != null) {
                current.name = weapon.name;
            }
            if (weapon.description != null) {
                current.description = weapon.description;
            }
            if (weapon.boosts != null) {
                current.boosts = weapon.boosts;
            }
            if (weapon.type != null) {
                current.type = weapon.type;
            }
            if (weapon.rarity != null) {
                current.rarity = weapon.rarity;
            }
            if (weapon.visualModel != null) {
                current.visualModel = weapon.visualModel;
            }
            if (weapon.magical != null) {
                current.magical = weapon.magical;
            }
        } else {
            current = weapon;
            if (current.rarity == null) {
                current.rarity = Rarity.Common;
            }
        }
        String json = logJson(current);
        context.setShared(CURRENT_WEAPON, current);
        Log.info("End of updateWeapon");
        addShowEquipmentAction(current);
        return json;
    }

    @Tool("When finished building weapon, call this tool to finish the weapon.")
    public String finishEquipment() throws Exception {
        WeaponModel current = null;
        if ((current = context.getShared(CURRENT_WEAPON, WeaponModel.class)) == null) {
            return "No weapon to finish";
        }
        addShowEquipmentAction(current);
        chatService.popChatFrame(context);
        context.setShared(CURRENT_WEAPON, null);
        if (current.rarity == null) {
            current.rarity = Rarity.Common;
        }
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            newEquipment = new NewModModel();
        }
        if (newEquipment.weapons == null) {
            newEquipment.weapons = new HashMap<>();
        }
        // for updates
        if (!newEquipment.weapons.containsKey(current.name)) {
            newEquipment.count++;
        }
        newEquipment.weapons.put(current.name, current);
        context.setShared(NewModModel.NEW_EQUIPMENT, newEquipment);
        context.response().add(new MessageAction("Weapon finished!"));
        context.response().add(new UpdateNewEquipmentAction("To create and export a mod containing your newly built weapon, tell me to '" + ModPackager.PACKAGE_MODE_CHAT_COMMAND + "'"));
        context.pushIgnoreAIResponse();
        Log.info("Finishing weapon");
        String weaponJson = logJson(current);
        return weaponJson;
    }

    private String logJson(WeaponModel weapon)  {
        try {
        String weaponJson = mapper.writeValueAsString(weapon);
        Log.info("Weapon JSON: " + weaponJson);
        return weaponJson;
        } catch (Exception e) {
            throw new RuntimeException("Error logging weapon json", e);
        }
    }


    @Tool("Add boost to weapon.")
    public void addWeaponBoost(String boostDescription) throws Exception {
         // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("addWeaponBoost: "  + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.response().add(new MessageAction(enchantment));
            context.response().add(new MessageAction("Could not create a boost macro from your description."));
            return;
        }
        WeaponModel weapon = context.getShared(CURRENT_WEAPON, WeaponModel.class);
        if (weapon.boosts == null || weapon.boosts.isEmpty()) {
            weapon.boosts = enchantment;
        } else {
            weapon.boosts += ";" + enchantment;
        }
        context.setShared(CURRENT_WEAPON, weapon);
        addShowEquipmentAction(weapon);
        logJson(weapon);
    }

    @Tool("Set boost macro for weapon.")
    public void setWeaponBoost(String boostDescription) throws Exception {
        // keep the boostMacro parameter as tool invocation is flaky otherwise
        // AI gets confused
        Log.info("setWeaponBoost: "  + boostDescription);
        String enchantment = boostBuilder.createBoostMacro(context.userMessage());
        Log.info("Enchantment: " + enchantment);
        if (enchantment.indexOf('(') < 0) {
            context.response().add(new MessageAction(enchantment));
            context.response().add(new MessageAction("Could not create a boost macro from your description."));
            return;
        }
        WeaponModel weapon = context.getShared(CURRENT_WEAPON, WeaponModel.class);
        weapon.boosts = enchantment;
        context.setShared(CURRENT_WEAPON, weapon);
        addShowEquipmentAction(weapon);
        logJson(weapon);
    }

    @Tool("Summarizes available visual models for the current weapon type.")
    public String showVisualModels() {
        WeaponModel weapon = context.getShared(CURRENT_WEAPON, WeaponModel.class);
        if (weapon == null || weapon.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because weapon type is not set");
        }
        Log.info("Finding visual models for weapon type: " + weapon.type.name());
        String searchString = weapon.type.name() + "s";
        List<RootTemplate> rootTemplates = library.findRootIconsFrom(stat -> {
            String properties = stat.getField("Proficiency Group");
            return properties != null && properties.contains(searchString);
        });
        String html = "<ul class=\"icon-list\">";
        for (RootTemplate rootTemplate : rootTemplates) {
            html += "<li><img src=\"" + library.icons().get(rootTemplate.resolveIcon()) + "\" height=\"64\" width=\"64\"/> " + rootTemplate.MapKey + "</li>";
        }
        html += "</ul>";
        context.response().add(new MessageAction(html));
        String message = "There are " + rootTemplates.size() + " visual models available. Choose one of the parent ids from the list above if you want a different look for your weapon.";
        context.response().add(new MessageAction(message));

        // Ignore the next AI chat response because the AI often says it cannot find anything
        // if a data list is not sent back as a tool result.
        context.pushIgnoreAIResponse();

        // Had to return something because AI would get confused sometimes.
        return message;
    }

}

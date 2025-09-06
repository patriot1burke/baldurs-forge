package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WeaponBuilder extends EquipmentBuilder {

    @Inject
    WeaponBuilderChat agent;
    @Override
    protected BuilderChat agent() {
        return agent;
    }

    @Override
    protected Class<? extends BaseModel> baseModelClass() {
        return WeaponModel.class;
    }
    @Override
    protected String schema() {
        return WeaponModel.schema;
    }
    @Override
    protected String type() {
        return WeaponModel.TYPE;
    }
    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> {
            WeaponModel weapon = new WeaponModel();
            weapon.magical = true;
            return weapon;
        };
    }

    @Startup
    public void start() {
        chatService.register(type(), this);
    }

    @Tool("Set the name for the current weapon.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }


    @Tool("Set the description for the current weapon.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current weapon.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current weapon.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        set(current -> current.visualModel = visualModel);
    }

    @Tool("Set the type for the current weapon.")
    public void setType(WeaponType type) {
        Log.info("Setting type: " + type);
        set(current -> ((WeaponModel) current).type = type);
    }

    @Tool("Set magical for the current weapon.")
    public void setMagical(Boolean magical) {
        Log.info("Setting magical: " + magical);
        set(current -> ((WeaponModel) current).magical = magical);
    }

    @Tool("Add boost to the current weapon.")
    public void addWeaponBoost(String boostDescription) throws Exception {
        super.addBoost(boostDescription);
    }

    @Tool("Set boost macro for the current weapon.")
    public void setWeaponBoost(String boostDescription) throws Exception {
        super.setBoost(boostDescription);
    }

    @Tool("Summarizes available visual models for the current weapon type.")
    public String showVisualModels() {
        WeaponModel weapon = context.getShared(CURRENT_EQUIPMENT, WeaponModel.class);
        if (weapon == null || weapon.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because weapon type is not set");
        }
        String searchString = weapon.type.name() + "s";
        return showVisualModels(stat -> {
            String properties = stat.getField("Proficiency Group");
            return properties != null && properties.contains(searchString);
        });
    }
    @Tool("When finished building weapon, call this tool to finish the weapon.")
    public String finishEquipment() throws Exception {
        return super.finishEquipment();
    }


}

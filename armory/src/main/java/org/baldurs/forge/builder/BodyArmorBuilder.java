package org.baldurs.forge.builder;

import org.baldurs.forge.model.Rarity;

import com.google.common.base.Supplier;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BodyArmorBuilder extends EquipmentBuilder {

    @Inject
    BodyArmorBuilderChat agent;

    @Override
    protected BuilderChat agent() {
        return agent;
    }

    @Override
    protected Class<? extends BaseModel> baseModelClass() {
        return BodyArmorModel.class;
    }
    @Override
    protected String schema() {
        return BodyArmorModel.schema;
    }
    @Override
    protected String type() {
        return BodyArmorModel.TYPE;
    }
    @Override
    protected Supplier<BaseModel> supplier() {
        return () -> new BodyArmorModel();
    }

    @Startup
    public void start() {
        chatService.register(type(), this);
    }

 
    @Tool("Set the name for the current body armor.")
    public void setName(String name) {
        Log.info("Setting name: " + name);
        set(current -> current.name = name);
    }


    @Tool("Set the description for the current body armor.")
    public void setDescription(String description) {
        Log.info("Setting description: " + description);
        set(current -> current.description = description);
    }

    @Tool("Set the rarity for the current body armor.")
    public void setRarity(Rarity rarity) {
        Log.info("Setting rarity: " + rarity);
        set(current -> current.rarity = rarity);
    }

    @Tool("Set the visual model for the current body armor.")
    public void setVisualModel(String visualModel) {
        Log.info("Setting visual model: " + visualModel);
        set(current -> current.visualModel = visualModel);
    }

    @Tool("Set the armor class for the current body armor.")
    public void setArmorClass(Integer armorClass) {
        Log.info("Setting armor class: " + armorClass);
        set(current -> ((BodyArmorModel) current).armorClass = armorClass);
    }
    @Tool("Set the type for the current body armor.")
    public void setType(BodyArmorType type) {
        Log.info("Setting type: " + type);
        set(current -> ((BodyArmorModel) current).type = type);
    }

    @Tool("Add boost to body armor.")
    public void addBoost(String boost) throws Exception {
        super.addBoost(boost);
    }

    @Tool("Set boost for body armor.")
    public void setBoost(String boost) throws Exception {
        super.setBoost(boost);
    }

    @Tool("Summarizes available visual models for the current body armor type.")
    public String showVisualModels() {
        BodyArmorModel armor = context.getShared(CURRENT_EQUIPMENT, BodyArmorModel.class);
        if (armor == null || armor.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because armor type is not set");
        }
        return showVisualModels(stat -> stat.getField("ArmorType") != null && stat.getField("ArmorType").equals(armor.type.name()) && stat.getField("Slot") != null && stat.getField("Slot").equals("Breast"));
    }

    @Tool("When finished building body armor, call this tool to finish the body armor.")
    @Override
    public String finishEquipment() throws Exception {
        return super.finishEquipment();
    }

}

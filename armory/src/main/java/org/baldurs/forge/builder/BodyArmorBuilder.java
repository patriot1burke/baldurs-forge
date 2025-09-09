package org.baldurs.forge.builder;

import java.util.function.Predicate;

import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.StatsArchive;

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
        super.setVisualModel(visualModel);
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

    @Override
    protected Predicate<? super StatsArchive.Stat> visualModelPredicate() {
        BodyArmorModel armor = context.getShared(CURRENT_EQUIPMENT, BodyArmorModel.class);
        if (armor == null || armor.type == null) {
            return null;
        }
        return stat -> {
            String armorType = stat.getField("ArmorType");
            String slot = stat.getField("Slot");
            return armor != null && armorType != null && armor.type != null && armorType.equals(armor.type.name()) && slot != null && slot.equals("Breast");
        };
    }

    @Tool("Summarizes available visual models for the current body armor type.")
    public String showVisualModels() {
        BodyArmorModel armor = context.getShared(CURRENT_EQUIPMENT, BodyArmorModel.class);
        if (armor == null || armor.type == null) {
            throw new RuntimeException("Cannot determine vailable visual models because armor type is not set");
        }
        return super.showVisualModels();
    }

    @Tool("When finished building body armor, call this tool to finish the body armor.")
    @Override
    public String finishEquipment() throws Exception {
        return super.finishEquipment();
    }

}

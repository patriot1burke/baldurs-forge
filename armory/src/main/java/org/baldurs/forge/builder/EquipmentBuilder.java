package org.baldurs.forge.builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.baldurs.forge.chat.BaldursChat;
import org.baldurs.forge.chat.ChatService;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.output.JsonSchemas;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EquipmentBuilder implements BaldursChat {
    @Inject
    EquipmentBuilderAgent agent;

    @Inject
    ChatContext context;

    ObjectMapper mapper;

    @Inject
    ChatService chatService;

    String schema;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);

        JsonSchema.Builder builder = JsonSchema.builder();
        builder.name(EquipmentModel.class.getSimpleName())
               .rootElement(JsonObjectSchema.builder()
                                            .addEnumProperty("type", Arrays.stream(EquipmentType.values()).map(EquipmentType::name).toList(), "The type of equipment")
                                            .addEnumProperty("slot", Arrays.stream(EquipmentSlot.values()).map(EquipmentSlot::name).toList(), "The slot of the equipment")
                                            .addEnumProperty("rarity", Arrays.stream(Rarity.values()).map(Rarity::name).toList(), "The rarity of the equipment")
                                            .addStringProperty("name", "The name of the equipment")
                                            .addStringProperty("description", "The description of the equipment")
                                            .addIntegerProperty("armorClass", "The armor class of the equipment.  Not required for weapons.")
                                            .addStringProperty("weaponType", "The weapon type of the equipment.  Not required for armor.")
                                            .addStringProperty("armorType", "The armor type of the equipment.  Not required for weapons.")
                                            .required(Arrays.asList("type", "slot", "rarity", "name", "description"))
                                            .build());
        this.schema = builder.build().toString();
    }

    public String chat(@MemoryId String memoryId, String userMessage) {
        Log.info("EquipmentBuilder.chat: " + memoryId + " " + userMessage);
        chatService.setChatFrame(context, EquipmentBuilder.class);
        String currentJson = "{}";
        EquipmentModel current = null;
        if ((current = context.getShared("currentEquipment", EquipmentModel.class)) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing equipment", e);
            }
        }
        Log.info("Current JSON: " + currentJson);
        String response = agent.buildEquipment(context.memoryId(), schema, currentJson, userMessage);
        Parser parser = Parser.builder().build();
        Node document = parser.parse(response);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        return html;
    }

    @Tool("Call this every time you change the equipment json documentyou are building.")
    public void updateEquipment(EquipmentModel equipment) {
        Log.info("Updating equipment: " + equipment);
        context.setShared("currentEquipment", equipment);
    }

    //@Tool("Show what you have so far for the equipment you are building.")
    public String showCurrentEquipmentBuilder(EquipmentModel currentJson) {
        Log.info("Showing current equipment: " + currentJson);
        context.response().add(currentJson);
        return "This is what I have so far.";
    }

    @Tool("When finished building equipment, call this tool to finish the equipment.")
    public String finishEquipment(EquipmentModel newEquipment) throws Exception {
        chatService.popChatFrame(context);
        context.setShared("currentEquipment", null);
        Log.info("Finishing equipment");
        ObjectMapper mapper = new ObjectMapper();
        String equipmentJson = mapper.writeValueAsString(newEquipment);
        Log.info("Equipment JSON: " + equipmentJson);
        return equipmentJson;
    }

}

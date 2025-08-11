package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;

public class BodyArmorModel extends BaseModel {
    @Description("The type of body armor")
    @JsonProperty(required = true)
    public BodyArmorType type;
    @Description("The armor class of the body armor.  This will default to the base armor class of the body armor type.")
    public Integer armorClass;

    public BodyArmorModel(
                BodyArmorType type,
                Rarity rarity,
                String name,
                String description,
                Integer armorClass, 
                String boosts,
                String parentModel
                ) {
        super(rarity, name, description, boosts, parentModel);
        this.armorClass = armorClass;
        this.type = type;
    }

    public BodyArmorModel() {
    }

    public static final String schema;

    static {
        // TODO:  can call JsonSchemas.jsonSchemaFrom(BodyArmorModel.class) directly after my langchain4j patch is merged and released
        // right now langchain4j does not look at super classes for the schema
        JsonObjectSchema baseModelSchema = BaseModel.schema();
        JsonObjectSchema bodyArmorSchema = (JsonObjectSchema)JsonSchemas.jsonSchemaFrom(BodyArmorModel.class).get().rootElement();
        Set<String> required = new HashSet<>(baseModelSchema.required());
        required.addAll(bodyArmorSchema.required());
        Map<String, JsonSchemaElement> properties = new HashMap<>(baseModelSchema.properties());
        properties.putAll(bodyArmorSchema.properties());
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                                        .addProperties(properties)
                                        .required(new ArrayList<>(required))
                                        .build();
        builder.name("bodyArmor")
               .rootElement(rootElement);
        schema = builder.build().toString();
    }

}

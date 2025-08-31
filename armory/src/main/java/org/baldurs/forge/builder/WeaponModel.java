package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baldurs.forge.model.Rarity;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

public class WeaponModel extends BaseModel {
    @Description("The type of weapon")
    @JsonProperty(required = true)
    public WeaponType type;

    @Description("Whether the weapon is magical")
    @JsonProperty(required = true)
    public Boolean magical;

    public WeaponModel(
                WeaponType type,
                Rarity rarity,
                String name,
                String description,
                String boosts,
                boolean magical,
                String parentModel
                ) {
        super(rarity, name, description, boosts, parentModel);
        this.type = type;
        this.magical = magical;
    }

    public WeaponModel() {
    }

    public static final String schema;

    static {
        // TODO:  can call JsonSchemas.jsonSchemaFrom(BodyArmorModel.class) directly after my langchain4j patch is merged and released
        // right now langchain4j does not look at super classes for the schema
        JsonObjectSchema baseModelSchema = BaseModel.schema();
        JsonObjectSchema weaponSchema = (JsonObjectSchema)JsonSchemas.jsonSchemaFrom(WeaponModel.class).get().rootElement();
        Set<String> required = new HashSet<>(baseModelSchema.required());
        required.addAll(weaponSchema.required());
        Map<String, JsonSchemaElement> properties = new HashMap<>(baseModelSchema.properties());
        properties.putAll(weaponSchema.properties());
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                                        .addProperties(properties)
                                        .required(new ArrayList<>(required))
                                        .build();
        builder.name("weapon")
               .rootElement(rootElement);
        schema = builder.build().toString();
    }

}

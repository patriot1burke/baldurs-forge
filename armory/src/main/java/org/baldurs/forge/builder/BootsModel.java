package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.output.JsonSchemas;

import org.baldurs.forge.model.Rarity;

public class BootsModel extends AppendageModel {

    public BootsModel(
            Rarity rarity,
            String name,
            String description,
            String boostMacro,
            ArmorCategory category,
            String parentModel) {
        super(category, rarity, name, description, boostMacro, parentModel);
    }


    public static final String schema;

    static {
        // TODO:  can call JsonSchemas.jsonSchemaFrom(BodyArmorModel.class) directly after my langchain4j patch is merged and released
        // right now langchain4j does not look at super classes for the schema
        JsonObjectSchema baseModelSchema = BaseModel.schema();
        JsonObjectSchema appendageSchema = AppendageModel.schema();
        JsonObjectSchema modelSchema = (JsonObjectSchema)JsonSchemas.jsonSchemaFrom(BootsModel.class).get().rootElement();
        Set<String> required = new HashSet<>(baseModelSchema.required());
        required.addAll(baseModelSchema.required());
        required.addAll(appendageSchema.required());
        required.addAll(modelSchema.required());
        Map<String, JsonSchemaElement> properties = new HashMap<>(baseModelSchema.properties());
        properties.putAll(baseModelSchema.properties());
        properties.putAll(appendageSchema.properties());
        properties.putAll(modelSchema.properties());
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                                        .addProperties(properties)
                                        .required(new ArrayList<>(required))
                                        .build();
        builder.name("boots")
               .rootElement(rootElement);
        schema = builder.build().toString();
    }

}

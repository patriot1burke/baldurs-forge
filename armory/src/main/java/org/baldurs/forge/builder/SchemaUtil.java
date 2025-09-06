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

public class SchemaUtil {
    // TODO: can call JsonSchemas.jsonSchemaFrom(BodyArmorModel.class) directly
    // after my langchain4j patch is merged and released
    // right now langchain4j does not look at super classes for the schema

    public static String schema(Class clazz, String name) {
        Set<String> required = new HashSet<>();
        Map<String, JsonSchemaElement> properties = new HashMap<>();
        build(clazz, required, properties);

        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                .addProperties(properties)
                .required(new ArrayList<>(required))
                .build();
        JsonSchema.Builder builder = JsonSchema.builder();
        builder.name(name)
                .rootElement(rootElement);
        return builder.build().toString();
    }

    private static void build(Class clazz, Set<String> required, Map<String, JsonSchemaElement> properties) {
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            build(clazz.getSuperclass(), required, properties);
        }
        JsonObjectSchema schema = (JsonObjectSchema) JsonSchemas.jsonSchemaFrom(clazz).get().rootElement();
        required.addAll(schema.required());
        properties.putAll(schema.properties());
    }
}

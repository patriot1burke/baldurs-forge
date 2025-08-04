package org.baldurs.forge.builder;

import java.util.Arrays;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import org.baldurs.forge.model.Rarity;

public class GlovesModel extends AppendageModel {

    public GlovesModel(
            Rarity rarity,
            String name,
            String description,
            String boostMacro,
            ArmorCategory category) {
        super(category, rarity, name, description, boostMacro);
    }

    public static final String schema;

    static {
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                .addEnumProperty("rarity", Arrays.stream(Rarity.values()).map(Rarity::name).toList(),
                        "The rarity of the gloves")
                .addStringProperty("name", "The name of the gloves")
                .addStringProperty("description", "The description of the gloves")
                .addStringProperty("boosts", "The boosts for the gloves.")
                .addEnumProperty("armorCategory", Arrays.stream(ArmorCategory.values()).map(ArmorCategory::name).toList(), "The armor category of the gloves")
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(GlovesModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

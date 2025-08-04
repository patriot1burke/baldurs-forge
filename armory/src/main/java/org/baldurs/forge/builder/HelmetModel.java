package org.baldurs.forge.builder;

import java.util.Arrays;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import org.baldurs.forge.model.Rarity;

public class HelmetModel extends AppendageModel {

    public HelmetModel(
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
                        "The rarity of the helmet")
                .addStringProperty("name", "The name of the helmet")
                .addStringProperty("description", "The description of the helmet")
                .addStringProperty("boosts", "The boosts for the helmet.")
                .addEnumProperty("armorCategory", Arrays.stream(ArmorCategory.values()).map(ArmorCategory::name).toList(), "The armor category of the helmet")
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(HelmetModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

package org.baldurs.forge.builder;

import java.util.Arrays;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
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
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                .addEnumProperty("rarity", Arrays.stream(Rarity.values()).map(Rarity::name).toList(),
                        "The rarity of the boots")
                .addStringProperty("name", "The name of the boots")
                .addStringProperty("description", "The description of the boots")
                .addStringProperty("boosts", "The boosts for the boots.")
                .addEnumProperty("armorCategory", Arrays.stream(ArmorCategory.values()).map(ArmorCategory::name).toList(), "The armor category of the boots")
                .addStringProperty("parentModel", "The parent visual model of the boots.")
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(BootsModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

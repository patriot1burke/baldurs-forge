package org.baldurs.forge.builder;

import java.util.Arrays;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import org.baldurs.forge.model.Rarity;

public class AmuletModel extends BaseModel {

    public AmuletModel(
            Rarity rarity,
            String name,
            String description,
            String boosts,
            String parentModel) {
        super(rarity, name, description, boosts, parentModel);
    }

    public static final String schema;

    static {
        JsonSchema.Builder builder = JsonSchema.builder();
        JsonObjectSchema rootElement = JsonObjectSchema.builder()
                .addEnumProperty("rarity", Arrays.stream(Rarity.values()).map(Rarity::name).toList(),
                        "The rarity of the amulet")
                .addStringProperty("name", "The name of the amulet")
                .addStringProperty("description", "The description of the amulet")
                .addStringProperty("boosts", "The boosts for the amulet.")
                .addStringProperty("parentModel", "The parent visualmodel of the amulet.")
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(AmuletModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

package org.baldurs.forge.builder;

import java.util.Arrays;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import org.baldurs.forge.model.Rarity;

public class RingModel extends BaseModel {

    public RingModel(
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
                        "The rarity of the ring")
                .addStringProperty("name", "The name of the ring")
                .addStringProperty("description", "The description of the ring")
                .addStringProperty("boosts", "The boosts for the ring.")
                .addStringProperty("parentModel", "The parent visual model of the ring.")
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(RingModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

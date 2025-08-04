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
            String boosts) {
        super(rarity, name, description, boosts);
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
                .required(Arrays.asList("rarity", "name"))
                .build();
        builder.name(RingModel.class.getSimpleName())
                .rootElement(rootElement);
        schema = builder.build().toString();
    }
}

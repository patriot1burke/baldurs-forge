package org.baldurs.forge.builder;

import org.baldurs.forge.model.Rarity;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

public abstract class AppendageModel extends BaseModel {
    @Description("The armor category.  Can be None, Light, Medium, or Heavy.")
    public ArmorCategory armorCategory = ArmorCategory.None;

    public AppendageModel(
                ArmorCategory category,
                Rarity rarity,
                String name,
                String description,
                String boostMacro,
                String parentModel
                ) {
        super(rarity, name, description, boostMacro, parentModel);
        this.armorCategory = category;
    }

    public AppendageModel() {
    }
}

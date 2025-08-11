package org.baldurs.forge.builder;

import java.util.Optional;

import org.baldurs.forge.model.Rarity;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;

public class BaseModel {
    @Description("The rarity and uniqueness of the item.")
    public Rarity rarity;
    @Description("The name of the item")
    @JsonProperty(required = true)
    public String name;
    @Description("The description of the item.  A short fun, flavor text, role-playing style description.")
    public String description;
    @Description("The boosts and stat bonuses and enchantments for the item.  This is a semicolon separated list of boost functions.")
    public String boosts;
    @Description("The visual model of the item inherited from an existing parent item in the database.  Change this if you want a different look for your item other than the default.")
    public String parentModel;

    protected BaseModel(
                Rarity rarity,
                String name,
                String description,
                String boosts,
                String parentModel
                ) {
        this.rarity = rarity;
        this.name = name;
        this.description = description;
        this.boosts = boosts;
        this.parentModel = parentModel;
    }

    protected BaseModel() {
    }

    public static JsonObjectSchema schema() {
        return (JsonObjectSchema)JsonSchemas.jsonSchemaFrom(BaseModel.class).get().rootElement();
    }

 
    

}

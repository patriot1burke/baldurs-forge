package org.baldurs.forge.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.output.structured.Description;

import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;

public class BaseModel {
    public Rarity rarity;
    public String name;
    public String description;
    public String boosts;
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

 
    

}

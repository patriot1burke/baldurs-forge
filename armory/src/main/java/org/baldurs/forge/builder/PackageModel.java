package org.baldurs.forge.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.output.JsonSchemas;
public class PackageModel {

    @Description("The name of the mod.")
    @JsonProperty(required = true)
    public String name;
    @Description("The author of the mod.")
    @JsonProperty(required = true)
    public String author;
    @Description("The description of the mod.")
    @JsonProperty(required = true)
    public String description;

    public static final String schema; 

    static {
        JsonObjectSchema packageModelSchema = (JsonObjectSchema)JsonSchemas.jsonSchemaFrom(PackageModel.class).get().rootElement();
        JsonSchema.Builder builder = JsonSchema.builder();
        builder.name("packageModel")
               .rootElement(packageModelSchema);
        schema = builder.build().toString();
    }
}

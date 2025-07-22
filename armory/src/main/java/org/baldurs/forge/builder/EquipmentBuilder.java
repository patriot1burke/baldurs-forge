package org.baldurs.forge.builder;

import java.util.Optional;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.output.JsonSchemas;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EquipmentBuilder {
    @Inject
    EquipmentBuilderAgent agent;

    String schema;

    @PostConstruct
    public void init() {
        Optional<JsonSchema> schema = JsonSchemas.jsonSchemaFrom(EquipmentModel.class);
        this.schema = schema.get().toString();
    }

    public String buildEquipment(String userMessage) {
        String response = agent.buildEquipment(schema, userMessage);
        Parser parser = Parser.builder().build();
        Node document = parser.parse(response);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        return html;
    }

    @Tool("When finished building equipment, call this tool to finish the equipment.")
    public String finishEquipment(EquipmentModel newEquipment) throws Exception {
        Log.info("Finishing equipment");
        ObjectMapper mapper = new ObjectMapper();
        String equipmentJson = mapper.writeValueAsString(newEquipment);
        Log.info("Equipment JSON: " + equipmentJson);
        return equipmentJson;
    }

}

package org.baldurs.forge.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("ListVisualModels")
public class ListVisualModelsMessage {
    @JsonValue
    protected List<Map<String, String>> visualModels = new ArrayList<>();

    public void add(String icon, String id) {
        visualModels.add(Map.of("icon", icon, "id", id));
    }
}

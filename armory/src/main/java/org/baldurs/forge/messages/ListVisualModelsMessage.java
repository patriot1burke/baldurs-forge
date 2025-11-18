package org.baldurs.forge.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.quarkiverse.langchain4j.chat.frames.ChatEvent;

public class ListVisualModelsMessage extends ChatEvent {
    protected List<Map<String, String>> visualModels = new ArrayList<>();

    public ListVisualModelsMessage() {
        super("ListVisualModels");
    }

    public List<Map<String, String>> getVisualModels() {
        return visualModels;
    }

    public void add(String icon, String id) {
        visualModels.add(Map.of("icon", icon, "id", id));
    }

}

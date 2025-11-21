package org.baldurs.forge.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class ListVisualModelsMessage {
    protected List<Map<String, String>> visualModels = new ArrayList<>();

    public void add(String icon, String id) {
        visualModels.add(Map.of("icon", icon, "id", id));
    }

    /**
     * Add ListVisualModelsAction to the response. If there is already one, remove it
     * before adding the new one.
     * This is to prevent duplicate displays of the same equipment in the UI chat.
     *
     * @param context
     * @param equipment
     */
    public void addResponse(ChatFrameContext context) {
        context.addEvent("ListVisualModels", visualModels, true);
    }

}

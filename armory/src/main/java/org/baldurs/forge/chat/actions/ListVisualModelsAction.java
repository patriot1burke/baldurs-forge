package org.baldurs.forge.chat.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.baldurs.forge.chat.Action;

public class ListVisualModelsAction extends Action {
    protected List<Map<String, String>> visualModels = new ArrayList<>();

    public ListVisualModelsAction() {
        super("ListVisualModels");
    }
    public List<Map<String, String>> getVisualModels() {
        return visualModels;
    }


    public void add(String icon, String id) {
        visualModels.add(Map.of("icon", icon, "id", id));
    }

}

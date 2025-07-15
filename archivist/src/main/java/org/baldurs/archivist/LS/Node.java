package org.baldurs.archivist.LS;

import java.util.*;

/**
 * Node class for hierarchical data structures
 */
public class Node {
    public String name;
    public Node parent;
    public Map<String, NodeAttribute> attributes = new HashMap<>();
    public Map<String, List<Node>> children = new HashMap<>();
    public Integer line = null;
    public String keyAttribute = null;

    public int getChildCount() {
        return children.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public int getTotalChildCount() {
        int count = 0;
        for (List<Node> childList : children.values()) {
            for (Node child : childList) {
                count += 1 + child.getTotalChildCount();
            }
        }
        return count;
    }

    public void appendChild(Node child) {
        children.computeIfAbsent(child.name, k -> new ArrayList<>()).add(child);
    }
} 
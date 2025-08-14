package org.baldurs.forge.chat;

public class Action {
    protected String type;

    public Action(String name) {
        this.type = name;
    }

    public Action() {
    }

    public String getType() {
        return type;
    }

    public void setType(String name) {
        this.type = name;
    }



}

package org.baldurs.forge.chat;

public class ResponseMessage {
    protected String type;

    public ResponseMessage(String name) {
        this.type = name;
    }

    public ResponseMessage() {
    }

    public String getType() {
        return type;
    }

    public void setType(String name) {
        this.type = name;
    }



}

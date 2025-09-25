package org.baldurs.forge.chat;

public class ObjectMessage extends ResponseMessage {

    protected Object message;

    public ObjectMessage(Object message) {
        super("Message");
        this.message = message;
    }


    public Object getMessage() {    
        return message;
    }
}

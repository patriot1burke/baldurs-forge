package org.baldurs.forge.chat;

public class MessageAction extends Action {

    Object message;

    public MessageAction(Object message) {
        super("Message");
        this.message = message;
    }

    public Object getMessage() {    
        return message;
    }
}

package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;

public class MessageAction extends Action {

    protected Object message;

    public MessageAction(Object message) {
        super("Message");
        this.message = message;
    }


    public Object getMessage() {    
        return message;
    }
}

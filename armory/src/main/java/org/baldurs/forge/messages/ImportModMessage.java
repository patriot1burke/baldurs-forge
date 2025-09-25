package org.baldurs.forge.messages;

import org.baldurs.forge.chat.ResponseMessage;
import org.baldurs.forge.context.ChatContext;

public class ImportModMessage extends ResponseMessage {

    private ImportModMessage() {
        super("ImportMod");
    }

    /**
     * Add ImportModAction to the response.  If there is already one, remove it before adding the new one.
     * This is to prevent duplicate import events sent to the client.
     * @param context
     */
    public static void addResponse(ChatContext context) {
        context.response().removeIf(action -> action instanceof ImportModMessage);
        context.response().add(new ImportModMessage());
    }

}

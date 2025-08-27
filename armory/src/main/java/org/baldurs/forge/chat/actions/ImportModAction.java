package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.context.ChatContext;

public class ImportModAction extends Action {

    private ImportModAction() {
        super("ImportMod");
    }

    /**
     * Add ImportModAction to the response.  If there is already one, remove it before adding the new one.
     * This is to prevent duplicate import events sent to the client.
     * @param context
     */
    public static void addResponse(ChatContext context) {
        context.response().removeIf(action -> action instanceof ImportModAction);
        context.response().add(new ImportModAction());
    }

}

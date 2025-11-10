package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ChatContext;
import io.quarkiverse.langchain4j.chat.frames.ResponseMessage;

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

package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ChatEvent;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class ImportModMessage extends ChatEvent {

    private ImportModMessage() {
        super("ImportMod");
    }

    /**
     * Add ImportModAction to the response. If there is already one, remove it before adding the new one.
     * This is to prevent duplicate import events sent to the client.
     *
     * @param context
     */
    public static void addResponse(ChatFrameContext context) {
        context.events().removeIf(action -> action instanceof ImportModMessage);
        context.events().add(new ImportModMessage());
    }

}

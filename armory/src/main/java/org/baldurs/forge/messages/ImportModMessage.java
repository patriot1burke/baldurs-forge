package org.baldurs.forge.messages;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class ImportModMessage {

    protected String message;

    private ImportModMessage() {
    }

    /**
     * Add ImportModAction to the response. If there is already one, remove it before adding the new one.
     * This is to prevent duplicate import events sent to the client.
     *
     * @param context
     */
    public static void addResponse(ChatFrameContext context) {
        context.addEvent("ImportMod", "ImportMod", true);
    }

}

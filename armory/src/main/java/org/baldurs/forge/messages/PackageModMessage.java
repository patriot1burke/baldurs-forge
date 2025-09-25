package org.baldurs.forge.messages;

import org.baldurs.forge.chat.ResponseMessage;
import org.baldurs.forge.context.ChatContext;

public class PackageModMessage extends ResponseMessage {
    protected String filename;

    private PackageModMessage(String filename) {
        super("PackageMod");
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    /**
     * Add PackageModAction to the response.  If there is already one, remove it before adding the new one.
     * This is to prevent duplicate package events sent to the client.
     * @param context
     * @param filename
     */
    public static void addResponse(ChatContext context, String filename) {
        context.response().removeIf(action -> action instanceof PackageModMessage);
        context.response().add(new PackageModMessage(filename));
    }
}

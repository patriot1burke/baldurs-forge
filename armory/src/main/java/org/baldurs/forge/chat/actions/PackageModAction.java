package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;
import org.baldurs.forge.context.ChatContext;

public class PackageModAction extends Action {
    protected String filename;

    private PackageModAction(String filename) {
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
        context.response().removeIf(action -> action instanceof PackageModAction);
        context.response().add(new PackageModAction(filename));
    }
}

package org.baldurs.forge.messages;

import org.baldurs.forge.builder.NewModModel;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;

public class PackageModMessage extends ChatFrameMessage {
    protected String filename;
    protected NewModModel newEquipment;

    private PackageModMessage(String filename, NewModModel newEquipment) {
        super("PackageMod");
        this.newEquipment = newEquipment;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public NewModModel getNewEquipment() {
        return newEquipment;
    }

    /**
     * Add PackageModAction to the response. If there is already one, remove it before adding the new one.
     * This is to prevent duplicate package events sent to the client.
     *
     * @param context
     * @param filename
     */
    public static void addResponse(ChatFrameContext context, String filename, NewModModel newEquipment) {
        context.response().removeIf(action -> action instanceof PackageModMessage);
        context.response().add(new PackageModMessage(filename, newEquipment));
    }
}

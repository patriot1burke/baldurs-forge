package org.baldurs.forge.messages;

import org.baldurs.forge.builder.NewModModel;

import io.quarkiverse.langchain4j.chat.frames.ChatEvent;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;

public class PackageModMessage extends ChatEvent {
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
        context.events().removeIf(action -> action instanceof PackageModMessage);
        context.events().add(new PackageModMessage(filename, newEquipment));
    }
}

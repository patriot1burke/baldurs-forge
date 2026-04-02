package org.baldurs.forge.messages;

import org.baldurs.forge.builder.NewModModel;

import io.quarkiverse.langchain4j.chatscopes.EventType;

@EventType("PackageMod")
public class PackageModMessage {
    protected String filename;
    protected NewModModel newEquipment;

    public PackageModMessage(String filename, NewModModel newEquipment) {
        this.newEquipment = newEquipment;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public NewModModel getNewEquipment() {
        return newEquipment;
    }
}

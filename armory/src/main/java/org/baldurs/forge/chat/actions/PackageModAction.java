package org.baldurs.forge.chat.actions;

import org.baldurs.forge.chat.Action;

public class PackageModAction extends Action {
    protected String filename;

    public PackageModAction(String filename) {
        super("PackageMod");
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}

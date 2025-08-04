package org.baldurs.forge.scanner;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RootTemplate {
    public String Stats;
    public String MapKey;
    public String DisplayName;
    public String Description;
    public String ParentTemplateId;
    public String icon;
    @JsonIgnore
    public ArchiveSource source;
    @JsonIgnore
    public RootTemplateArchive archive;

    public RootTemplate() {
    }

    public RootTemplate(String stats, String mapKey, String displayName, String description, String parentTemplateId, String icon, ArchiveSource source, RootTemplateArchive archive) {
        Stats = stats;
        MapKey = mapKey;
        DisplayName = displayName;
        Description = description;
        ParentTemplateId = parentTemplateId;
        this.icon = icon;
        this.source = source;
        this.archive = archive;
    }

    public String resolveIcon() {
        if (icon != null) return icon;
        
        RootTemplate parent = archive.templates.get(ParentTemplateId);
        if (parent != null) {
            return parent.resolveIcon();
        } else {
            return null;
        }
    }

    public RootTemplate resolveTemplateThatDefinesIcon() {
        if (icon != null) return this;
        if (ParentTemplateId == null) return null;
        return archive.templates.get(ParentTemplateId).resolveTemplateThatDefinesIcon();
    }
}

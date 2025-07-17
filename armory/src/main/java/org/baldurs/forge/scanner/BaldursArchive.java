package org.baldurs.forge.scanner;

public class BaldursArchive {
    public LocalizationArchive localizations = new LocalizationArchive();
    public StatsArchive stats = new StatsArchive();
    public RootTemplateArchive rootTemplates = new RootTemplateArchive();

    public LocalizationArchive getLocalizations() {
        return localizations;
    }
    public StatsArchive getStats() {
        return stats;
    }
    public RootTemplateArchive getRootTemplates() {
        return rootTemplates;
    }
}

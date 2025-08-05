package org.baldurs.forge.toolbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baldurs.archivist.PackageReader;
import org.baldurs.archivist.LS.Converter;
import org.baldurs.forge.model.StatModel;
import org.baldurs.forge.scanner.ArchiveSource;
import org.baldurs.forge.scanner.BaldursArchive;
import org.baldurs.forge.scanner.ModuleInfo;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.scanner.StatsArchive.Stat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LibraryService {
    private boolean initialized = false;

    @Inject
    @ConfigProperty(name = "baldurs.forge.data.path", defaultValue = "/home/bburke/projects/baldurs-forge-data")
    private String rootPath;

    String cachePath = "/cache";
    Path modsPath;

    @PostConstruct
    public void init() {
        cachePath = rootPath + cachePath;
        modsPath = Path.of(rootPath + "/mods");
        scanFiles();
        mergeArchives();
    }

    public Path modsPath() {
        return modsPath;
    }

    private Map<String, ArchiveSource> archives = new HashMap<>();

    private Map<String, String> icons = new HashMap<>();
    private BaldursArchive archive;

    private void mergeArchives() {
        archive = new BaldursArchive();
        for (ArchiveSource source : archives.values()) {
            mergeArchive(source);
        }
    }

    private void mergeArchive(ArchiveSource source) {
        Log.infof("Merging archive: %s", source.name);
        archive.stats.merge(source.archive.stats);
        archive.rootTemplates.merge(source.archive.rootTemplates);
        archive.localizations.merge(source.archive.localizations);
        icons.putAll(source.icons);
    }


    private void scanFiles() {
        if (initialized)
            return;
        initialized = true;
        try {
            Log.info("Loading game data...");
            loadCoreGameData();
            loadMods();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void loadMods() throws Exception {
        for (Path modPath : Files.list(modsPath).toList()) {
            if (Files.isDirectory(modPath)) {
                Path archivePath = modPath.resolve("archive.json");
                if (Files.exists(archivePath)) {
                    ArchiveSource source = ArchiveSource.load(archivePath);
                    Log.info("Loaded mod " + source.name);
                    archives.put(source.name, source);
                }
            }
        }
    }

    private void loadCoreGameData() throws IOException, Exception {
        Path root = Path.of(cachePath);
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        BaldursArchive library = new BaldursArchive();
        ArchiveSource source = new ArchiveSource();
        source.name = "Core Game";
        source.archive = library;

        Path statsPath = root.resolve("stats.json");
        if (Files.exists(statsPath)) {
            library.getStats().load(source, statsPath);
        } else {
            library.getStats()
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/shared/Public/Shared/Stats/Generated/Data"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/shared/Public/SharedDev/Stats/Generated/Data"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/gustav/Public/GustavDev/Stats/Generated/Data"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/gustav/Public/Gustav/Stats/Generated/Data"))
                    .save(statsPath);
        }

        Path rootTemplatesPath = root.resolve("root-templates.json");
        if (Files.exists(rootTemplatesPath)) {
            library.getRootTemplates().load(source, rootTemplatesPath);
        } else {
            library.getRootTemplates()
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/shared/Public/Shared/RootTemplates/_merged.lsx"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/shared/Public/SharedDev/RootTemplates/_merged.lsx"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/gustav/Public/GustavDev/RootTemplates/_merged.lsx"))
                    .scan(source, Path.of("/mnt/c/Users/patri/mods/gustav/Public/Gustav/RootTemplates/_merged.lsx"))
                    .save(rootTemplatesPath);
        }

        Path localizationPath = root.resolve("localization.json");
        if (Files.exists(localizationPath)) {
            library.getLocalizations().load(localizationPath);
        } else {
            library.getLocalizations()
                    .scan(Path.of("/mnt/c/Users/patri/mods/bg3-localization/Localization/English/english.xml"))
                    .save(localizationPath);
        }
        Path icons = Path.of(rootPath).resolve("images/icons");
        Path itemsIcons = icons.resolve("items-tooltip");
        for (Path itemIcon : Files.list(itemsIcons).toList()) {
            String name = itemIcon.getFileName().toString().replace(".144.png", "");
            source.icons.put(name, "/static/img/icons/items-tooltip/" + name + ".144.png");
        }
        Path skillsIcons = icons.resolve("skills");
        for (Path skillIcon : Files.list(skillsIcons).toList()) {
            String name = skillIcon.getFileName().toString().replace(".png", "");
            source.icons.put(name, "/static/img/icons/skills/" + name + ".png");
        }
        archives.put(source.name, source);
    }

    public BaldursArchive archive() {
        return archive;
    }
    public Map<String, String> icons() {
        return icons;
    }

    public ArchiveSource uploadMod(Path pak) throws Exception {
        ArchiveSource source = ArchiveSource.unpackMod(pak);
        archives.put(source.name, source);
        mergeArchive(source);
        return source;
    }

    public String findLocalization(String handle) {
        return archive.localizations.getLocalization(handle);
    }


    public RootTemplate findRootTemplateByStatName(String statName) {
        //Log.infof("Finding root template for stat: %s", statName);
        for (ArchiveSource source : archives.values()) {
            for (RootTemplate rootTemplate : source.archive.getRootTemplates().templates.values()) {
                if (statName.equals(rootTemplate.Stats)) {
                    return rootTemplate;
                }
            }
        }
        return null;
    }

    public StatModel getStatByName(String name, @P(value = "Add parent data?", required = false) boolean parentData) {
        for (ArchiveSource source : archives.values()) {
            StatsArchive.Stat stat = source.archive.stats.getByName(name);
            if (stat != null) {
                if (parentData) {
                    return new StatModel(stat.name, stat.type, stat.using, stat.aggregateData());
                } else {
                    return new StatModel(stat.name, stat.type, stat.using, stat.data);
                }
            }
        }
        return null;
    }

    public List<String> getStatAttributeValues(String attributeName) {
        for (ArchiveSource source : archives.values()) {
            Set<String> values = source.archive.stats.collectAttributesValues(attributeName);
            List<String> list = new ArrayList<>(values);
            Collections.sort(list);
            return list;
        }
        return Collections.emptyList();
    }

    public List<String> getAllBoostFunctionSignatures() {
        Map<String, Set<String>> boosts = new HashMap<>();

        for (ArchiveSource source : archives.values()) {
            Set<String> macros = source.archive.stats.collectAttributesValues("Boosts");
            macros.addAll(source.archive.stats.collectAttributesValues("PassivesOnEquip"));
            macros.addAll(source.archive.stats.collectAttributesValues("DefaultBoosts"));
            macros.addAll(source.archive.stats.collectAttributesValues("BoostsOnEquipMainHand"));
            macros.addAll(source.archive.stats.collectAttributesValues("PassivesOffHand"));
            return getFunctions(boosts, macros);
        }
        return Collections.emptyList();
    }

    private List<String> getFunctions(Map<String, Set<String>> boosts, Set<String> macros) {
        for (String macro : macros) {
            String[] tokens = MacroService.splitMacro(macro);
            for (String token : tokens) {
                try {
                    String expression = token.trim();
                    int index = token.indexOf(":");
                    if (index > 0) {
                        expression = token.substring(index + 1);
                    }
                    index = expression.indexOf('(');
                    if (index <= 0) {
                        continue;
                    }
                    String functionName = expression.substring(0, index).trim();
                    int closingIndex = expression.lastIndexOf(')');
                    String params = expression.substring(index + 1, closingIndex);
                    params = params.trim();
                    Set<String> functionParams = boosts.computeIfAbsent(functionName, k -> new LinkedHashSet<>());
                    if (params.isEmpty()) {
                        functionParams.add("");
                        continue;
                    }
                    String[] paramTokens = params.split(",");
                    String parameters = null;
                    for (String param : paramTokens) {
                        param = param.trim();
                        if (parameters != null)
                            parameters += ",";
                        if (parameters == null)
                            parameters = "";
                        if (param.matches("^-?\\d+$")) {
                            parameters += "number";
                        } else if (param.matches("^\\d+d\\d+$")) {
                            parameters += "die_roll";
                        } else {
                            parameters += param;
                        }
                    }
                    functionParams.add(parameters);
                } catch (Exception e) {
                    Log.error("Error parsing boost function: '" + macro + "' '" + token + "'", e);
                    throw e;
                }
            }
        }
        List<String> functions = new ArrayList<>();
        for (String function : boosts.keySet()) {
            for (String param : boosts.get(function)) {
                functions.add(function + "(" + param + ")");
            }
        }
        Collections.sort(functions, Comparator.naturalOrder());
        return functions;
    }

    public List<RootTemplate> findRootIconsFrom(String... data) {
        Predicate<? super Stat> predicate = stat -> {
            for (int i = 0; i < data.length; i += 2) {
                String name = data[i];
                String value = data[i + 1];
                if (stat.getField(name) == null || !stat.getField(name).contains(value)) {
                    return false;
                }
            }
            return true;
        };
        return findRootIconsFrom(predicate);
    }

    public List<RootTemplate> findRootIconsFrom(Predicate<? super Stat> predicate) {
        return archive.stats.getArmor().values().stream()
                    .filter(predicate)
                    .map(stat -> stat.getField("RootTemplate"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(archive.getRootTemplates()::getRootTemplate)
                    .filter(Objects::nonNull)
                    .map(RootTemplate::resolveTemplateThatDefinesIcon)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }


}
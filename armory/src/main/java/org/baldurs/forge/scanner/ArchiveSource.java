package org.baldurs.forge.scanner;

import java.util.Map;
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
import java.util.Set;

import org.baldurs.archivist.PackageReader;
import org.baldurs.archivist.LS.Converter;
import org.baldurs.forge.model.StatModel;
import org.baldurs.forge.scanner.ArchiveSource;
import org.baldurs.forge.scanner.BaldursArchive;
import org.baldurs.forge.scanner.ModuleInfo;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;

import io.quarkus.logging.Log;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArchiveSource {
    public String name;
    public String modPath;
    public Map<String, String> icons;
    @JsonIgnore
    public BaldursArchive archive;

    private static void deleteDirectoryRecursively(Path path) throws Exception {
        if (Files.exists(path)) {
            for (Path p : Files.walk(path)
                    .sorted((p1, p2) -> -p1.compareTo(p2)) // Sort in reverse order to delete files before directories
                    .toList()) {
                try {
                    Files.delete(p);
                } catch (Exception e) {
                    Log.errorf("Failed to delete file/directory: %s", p, e);
                }
            }
        }
    }

    public static ArchiveSource load(Path archivePath) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArchiveSource source = objectMapper.readValue(archivePath.toFile(), ArchiveSource.class);
        source.archive = new BaldursArchive();
        Path dir = archivePath.getParent();
        Path statsPath = dir.resolve("stats.json");
        if (Files.exists(statsPath)) {
            source.archive.getStats().load(source, statsPath);
        }
        Path rootTemplatesPath = dir.resolve("root-templates.json");
        if (Files.exists(rootTemplatesPath)) {
            source.archive.getRootTemplates().load(source, rootTemplatesPath);
        }
        Path localizationPath = dir.resolve("localization.json");
        if (Files.exists(localizationPath)) {
            source.archive.getLocalizations().load(localizationPath);
        }
        return source;
    }

    public static ArchiveSource unpackMod(Path pak) throws Exception {
        Path extractPath = pak.resolveSibling(pak.getFileName().toString().replace(".pak", ""));
        if (Files.exists(extractPath)) {
            // Remove existing directory and all its contents
            deleteDirectoryRecursively(extractPath);
        }
        PackageReader reader = new PackageReader(pak);
        reader.extract(extractPath);
        ModuleInfo moduleInfo = ModuleInfo.scan(extractPath);
        Log.infof("Module info: %s", moduleInfo);
        ArchiveSource archiveSource = new ArchiveSource();
        archiveSource.archive = new BaldursArchive();
        archiveSource.name = moduleInfo.name();
        archiveSource.modPath = extractPath.toString();

        BaldursArchive archive = archiveSource.archive;

        Path publicPath = extractPath.resolve("Public");
        publicPath = publicPath.resolve(moduleInfo.folder());
        if (!Files.exists(publicPath)) {
            Files.delete(pak);
            deleteDirectoryRecursively(extractPath);
            throw new Exception("Public path for mod " + moduleInfo.name() + " not found: " + publicPath);
        }
        Path statsPath = publicPath.resolve("Stats/Generated/Data");
        if (!Files.exists(statsPath)) {
            Files.delete(pak);
            deleteDirectoryRecursively(extractPath);
            throw new Exception("Stats path for mod " + moduleInfo.name() + " not found: " + statsPath);
        }

        // Scan all .txt files in statsPath for the archive
        try {
            for (Path txtFile : Files.walk(statsPath)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .toList()) {
                archive.getStats().scan(archiveSource, txtFile);
                Log.infof("Scanned stats file: %s", txtFile);
            }
        } catch (Exception e) {
            Log.errorf("Failed to walk stats directory: %s", statsPath, e);
            Files.delete(pak);
            deleteDirectoryRecursively(extractPath);
            throw e;
        }
        archive.getStats().save(extractPath.resolve("stats.json"));

        Path rootTemplatesPath = publicPath.resolve("RootTemplates");
        // Scan all .txt files in statsPath for the archive
        try {
            for (Path lsfFile : Files.walk(rootTemplatesPath)
                    .filter(path -> path.toString().endsWith(".lsf"))
                    .toList()) {
                Path lsxPath = lsfFile.resolveSibling(lsfFile.getFileName().toString().replace(".lsf", ".lsx"));
                Converter.lsfToLsx(lsfFile, lsxPath);
                archive.getRootTemplates().scan(archiveSource, lsxPath);
                Log.infof("Scanned root template file: %s", lsfFile);
            }
        } catch (Exception e) {
            Log.errorf("Failed to walk root templates directory: %s", rootTemplatesPath, e);
            Files.delete(pak);
            deleteDirectoryRecursively(extractPath);
            throw e;
        }

        Path modPath = extractPath.resolve("Mods");
        Path localizationPath = modPath.resolve("Localization/English");
        if (Files.exists(localizationPath)) {
            scanLocalization(pak, extractPath, archive, localizationPath);
        }
        modPath = modPath.resolve(moduleInfo.folder());
        localizationPath = modPath.resolve("Localization/English");
        if (Files.exists(localizationPath)) {
            scanLocalization(pak, extractPath, archive, localizationPath);
        }
        archive.getLocalizations().save(extractPath.resolve("localization.json"));

        // extract icons from .DDS files
        Path controllerIconsPath = modPath.resolve("GUI/Assets/ControllerUIIcons");
        Path itemsIconsPath = controllerIconsPath.resolve("items_png");
        if (Files.exists(itemsIconsPath)) {
            convertDDStoPNG(archiveSource, itemsIconsPath);
        }
        Path skillsIconsPath = controllerIconsPath.resolve("skills_png");
        if (Files.exists(skillsIconsPath)) {
            convertDDStoPNG(archiveSource, skillsIconsPath);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(extractPath.resolve("archive.json").toFile(), archiveSource);

        return archiveSource;
    }

    private static void convertDDStoPNG(ArchiveSource archiveSource, Path icons) throws IOException, InterruptedException {
        for (Path ddsFile : Files.walk(icons)
                .filter(path -> path.toString().endsWith(".dds") || path.toString().endsWith(".DDS"))
                .toList()) {
            String pngName = ddsFile.getFileName().toString().replace(".dds", ".png").replace(".DDS", ".png");
            String iconName = ddsFile.getFileName().toString().replace(".dds", "").replace(".DDS", "");
            Path pngPath = icons.resolve(pngName);
            if (Files.exists(pngPath)) {
                continue;
            }
            String cmd = String.format("convert %s %s", ddsFile, pngPath);
            Process process = new ProcessBuilder(cmd.split(" "))
            .start();
            process.waitFor();
            archiveSource.icons.put(iconName, pngPath.toString());
            //Log.infof("Scanned item icon file: %s", ddsFile);
        }
    }

    private static void scanLocalization(Path pak, Path extractPath, BaldursArchive archive, Path localizationPath)
            throws IOException, Exception {
        try {
            for (Path locaFile : Files.walk(localizationPath)
                    .filter(path -> path.toString().endsWith(".loca"))
                    .toList()) {
                Path xmlPath = locaFile.resolveSibling(locaFile.getFileName().toString().replace(".loca", ".xml"));
                Converter.locaToXml(locaFile, xmlPath);
                archive.getLocalizations().scan(xmlPath);
                Log.infof("Scanned localization file: %s", locaFile);
            }
        } catch (Exception e) {
            Log.errorf("Failed to walk localization directory: %s", localizationPath, e);
            Files.delete(pak);
            deleteDirectoryRecursively(extractPath);
            throw e;
        }
    }

}

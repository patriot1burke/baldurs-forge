package org.baldurs.forge.builder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.baldurs.archivist.IdMaker;
import org.baldurs.archivist.PackageWriter;
import org.baldurs.archivist.LS.Converter;
import org.baldurs.archivist.LS.PackedVersion;
import org.baldurs.forge.chat.ChatFrame;
import org.baldurs.forge.chat.ChatService;
import org.baldurs.forge.chat.actions.PackageModAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModPackager implements ChatFrame {

    public static final String PACKAGE_MODE_CHAT_COMMAND = "Package mod";

    @Inject
    ChatContext context;

    @Inject
    ChatService chatService;

    @Inject
    EquipmentBuilderChat agent;

    @Inject
    LibraryService library;

    ObjectMapper mapper;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Override
    public String chat(String memoryId, String userMessage) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            return "You have not created any new equipment to package.";
        }
        chatService.setChatFrame(context, ModPackager.class);
        String currentJson = "{}";
        PackageModel current = null;
        if ((current = context.getShared(CURRENT_PACKAGE, PackageModel.class)) != null) {
            try {
                currentJson = mapper.writeValueAsString(current);
            } catch (Exception e) {
                Log.warn("Error serializing package", e);
            }
        }
        return agent.packageMod(memoryId, userMessage, PackageModel.schema, currentJson);
    }
    private static final String CURRENT_PACKAGE = "currentPackage";

    @Tool("Update the current package json document.")
    public String updatePackage(PackageModel packageModel) throws Exception {
        PackageModel current = context.getShared(CURRENT_PACKAGE, PackageModel.class);
        if (current == null) {
            current = packageModel;
        }
        if (packageModel.name != null) {
            current.name = packageModel.name;
        }
        if (packageModel.author != null) {
            current.author = packageModel.author;
        }
        if (packageModel.description != null) {
            current.description = packageModel.description;
        }
        context.setShared(CURRENT_PACKAGE, current);
        return mapper.writeValueAsString(current);
    }

    @Tool("Package the mod.")
    public String finishPackage(PackageModel packageModel) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            return "No equipment to package.";
        }
        newEquipment.name = packageModel.name;
        newEquipment.author = packageModel.author;
        newEquipment.description = packageModel.description;
        context.setShared(NewModModel.NEW_EQUIPMENT, newEquipment);
        chatService.popChatFrame(context);
        String baseFileName = toAlphaNumericUnderscore(newEquipment.name);

        context.response().add(new PackageModAction(baseFileName + ".pak"));
        return "The mod is finished.";
    }

    public File packageMod(NewModModel newEquipment) throws Exception {
        Path tempDir = Files.createTempDirectory("mod");
        Log.info("Packaging mod in temp directory: " + tempDir.toString());
        String modUUID = IdMaker.uuid();
        String baseFileName = toAlphaNumericUnderscore(newEquipment.name);
        Path modDir = Files.createDirectory(tempDir.resolve(baseFileName));
        String folder = baseFileName + "_" + modUUID;
        Path modsDir = Files.createDirectories(modDir.resolve("Mods").resolve(folder));
        Path localizationDir = Files.createDirectories(modsDir.resolve("Localization/English"));
        Path publicDir = Files.createDirectories(modDir.resolve("Public").resolve(folder));
        Path rootTemplatesDir = Files.createDirectories(publicDir.resolve("RootTemplates"));
        Path statsDir = Files.createDirectories(publicDir.resolve("Stats/Generated"));
        Path statsDataDir = Files.createDirectory(statsDir.resolve("Data"));
        
        InputStream inputStream = getClass().getResourceAsStream("/mod-template/meta.lsx");
        String meta = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        meta = meta.replace("{AUTHOR}", newEquipment.author);
        meta = meta.replace("{DESCRIPTION}", newEquipment.description == null ? "" : newEquipment.description);
        meta = meta.replace("{FOLDER}", folder);
        meta = meta.replace("{NAME}", newEquipment.name);
        PackedVersion version = new PackedVersion();
        version.major = 1;
        version.minor = 0;
        version.revision = 0;
        version.build = 0;
        long version64 = version.toVersion64();
        meta = meta.replace("{VERSION64}", Long.toString(version64));
        meta = meta.replace("{UUID}", modUUID);
        inputStream.close();

        Files.writeString(modsDir.resolve("meta.lsx"), meta);


        String statPrefix = randomStatPrefix();

        String tab = "        ";

        String gameObjects = "";

        String armorData = "";

        String treasure = "";

        String localizations = "";



        for (BodyArmorModel bodyArmor : newEquipment.bodyArmors) {
            String nameHandle = IdMaker.handle();
            localizations += "    <content contentuid=\"" + nameHandle + "\" version=\"1\">" + bodyArmor.name + "</content>\n";
            String descriptionHandle = "";
            if (bodyArmor.description != null) {
                descriptionHandle = IdMaker.handle();
                localizations += "    <content contentuid=\"" + descriptionHandle + "\" version=\"1\">" + bodyArmor.description + "</content>\n";
            }
            String MapKey = IdMaker.uuid();
            String statName = statPrefix + "_" + toAlphaNumericUnderscore(bodyArmor.name);
            String parentTemplateId = bodyArmor.visualModel;
            if (parentTemplateId == null) {
                RootTemplate rootTemplate = library.findRootTemplateByStatName(bodyArmor.type.getBaseStat());
                parentTemplateId = rootTemplate.MapKey;
            }
            gameObjects += tab + "<node id=\"GameObjects\">\n";
            gameObjects += tab + "  <attribute id=\"DisplayName\" type=\"TranslatedString\" handle=\"" + nameHandle + "\" version=\"1\"/>\n";
            gameObjects += tab + "  <attribute id=\"Description\" type=\"TranslatedString\" handle=\"" + descriptionHandle + "\" version=\"1\"/>\n";
            gameObjects += tab + "  <attribute id=\"LevelName\" type=\"FixedString\" value=\"\"/>\n";
            gameObjects += tab + "  <attribute id=\"MapKey\" type=\"FixedString\" value=\"" + MapKey + "\"/>\n";
            gameObjects += tab + "  <attribute id=\"Name\" type=\"LSString\" value=\"" + statName + "\"/>\n";
            gameObjects += tab + "  <attribute id=\"ParentTemplateId\" type=\"FixedString\" value=\"" + parentTemplateId + "\"/>\n";
            gameObjects += tab + "  <attribute id=\"Stats\" type=\"FixedString\" value=\"" + statName + "\"/>\n";
            gameObjects += tab + "  <attribute id=\"TechnicalDescription\" type=\"TranslatedString\" handle=\"\" version=\"0\"/>\n";
            gameObjects += tab + "  <attribute id=\"Type\" type=\"FixedString\" value=\"item\"/>\n";
            gameObjects += tab + "  <attribute id=\"_OriginalFileVersion_\" type=\"int64\" value=\"144115207403209023\"/>\n";
            gameObjects += tab + "</node>\n";

            armorData += "new entry \"" + statName + "\"\n";
            armorData += "type \"Armor\"\n";
            armorData += "using \"" + bodyArmor.type.getBaseStat() + "\"\n";
            armorData += "data \"RootTemplate\" \"" + MapKey + "\"\n";
            if (bodyArmor.rarity != null) {
                armorData += "data \"Rarity\" \"" + bodyArmor.rarity.name() + "\"\n";
            }
            if (bodyArmor.armorClass != null) {
                armorData += "data \"ArmorClass\" \"" + bodyArmor.armorClass + "\"\n";
            }
            armorData += "\n";

            treasure += "new subtable \"1,1\"\n";
            treasure += "object category \"I_" + statName + "\",1,0,0,0,0,0,0,0\n";
            
        }



        inputStream = getClass().getResourceAsStream("/mod-template/LsxBoilerplate.lsx");
        String lsxBoilerplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        lsxBoilerplate = lsxBoilerplate.replace("{GAME_OBJECTS}", gameObjects);
        Path lsxBoilerplateFile = Files.writeString(rootTemplatesDir.resolve("Merged.lsx"), lsxBoilerplate);
        Converter.lsxToLsf(lsxBoilerplateFile, rootTemplatesDir.resolve("Merged.lsf"));

        Files.writeString(statsDataDir.resolve("Armor.txt"), armorData);

        inputStream = getClass().getResourceAsStream("/mod-template/TreasureTable.txt");
        String treasureTable = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        treasureTable += treasure;
        Files.writeString(statsDir.resolve("TreasureTable.txt"), treasureTable);
        
        localizations = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                       + "<contentList>\n"
                       + localizations
                       + "</contentList>";
        Path locaXmlPath = localizationDir.resolve("english.xml");
        Files.writeString(locaXmlPath, localizations);

        Converter.xmlToLoca(locaXmlPath, localizationDir.resolve("english.loca"));

        PackageWriter writer = new PackageWriter();
        Path outputPak = tempDir.resolve(baseFileName + ".pak");
        writer.archive(modDir, outputPak);

        Log.info("Packaged mod to " + outputPak.toString());

        return outputPak.toFile();
    }

    // Replaces any non-alphanumeric character in the input string with an underscore
    public static String toAlphaNumericUnderscore(String input) {
        if (input == null) return null;
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

    // Returns a string of 3 random capital letters
    public static String randomStatPrefix() {
        StringBuilder sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            char c = (char) ('A' + (int)(Math.random() * 26));
            sb.append(c);
        }
        return sb.toString();
    }
    
}

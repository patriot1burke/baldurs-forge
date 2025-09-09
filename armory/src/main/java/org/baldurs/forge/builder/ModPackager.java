package org.baldurs.forge.builder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.baldurs.archivist.IdMaker;
import org.baldurs.archivist.PackageWriter;
import org.baldurs.archivist.LS.Converter;
import org.baldurs.archivist.LS.PackedVersion;
import org.baldurs.forge.chat.ChatFrame;
import org.baldurs.forge.chat.ChatFrameService;
import org.baldurs.forge.chat.actions.ListEquipmentAction;
import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.chat.actions.PackageModAction;
import org.baldurs.forge.chat.actions.UpdateNewEquipmentAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModPackager implements ChatFrame {

    public static final String PACKAGE_MODE_CHAT_COMMAND = "Package mod";

    @Inject
    ChatContext context;

    @Inject
    ChatFrameService chatService;

    @Inject
    EquipmentBuilderChat agent;

    @Inject
    LibraryService library;

    @Inject
    BodyArmorBuilder bodyArmorBuilder;

    @Inject
    BoostService boostService;

    ObjectMapper mapper;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Startup
    public void start() {
        chatService.register(ModPackager.class.getName(), this);
    }


    @Override
    public String chat(String memoryId, String userMessage) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            return "You have not created any new equipment to package.";
        }
        chatService.setChatFrame(context, ModPackager.class.getName());
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

        PackageModAction.addResponse(context, baseFileName + ".pak");
        return "The mod is finished.";
    }

    public List<EquipmentModel> listBuiltEquipment() {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null || newEquipment.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return newEquipment.toEquipmentModels(boostService, library);
    }

    public void showNewEquipment() {
        List<EquipmentModel> equipment = listBuiltEquipment();
        if (!equipment.isEmpty()) {
            ListEquipmentAction.addResponse(context, equipment);
        } else {
            context.response().add(new MessageAction("No new equipment."));
        }
    }

    public void deleteAllNewEquipment() {
        context.setShared(NewModModel.NEW_EQUIPMENT, null);
        context.response().add(new UpdateNewEquipmentAction(null));
    }

    public String deleteNewEquipment(String name) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null || newEquipment.isEmpty()) {
            return "No equipment to delete.";
        }

        BaseModel equipment = newEquipment.findEquipmentByName(name);
        if (equipment == null) {
            showNewEquipment();
            return "Equipment with name not found.";
        }
        newEquipment.removeEquipment(equipment);

        if (newEquipment.count == 0) {
            context.setShared(NewModModel.NEW_EQUIPMENT, null);
        } else {
            context.setShared(NewModModel.NEW_EQUIPMENT, newEquipment);
            showNewEquipment();
        }
        
        context.response().add(new UpdateNewEquipmentAction(null));

        return "Equipment deleted.";
    }

    public String updateNewEquipment(String name) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null || newEquipment.isEmpty()) {
            return "No equipment to update.";
        }
        BaseModel equipment = newEquipment.findEquipmentByName(name);
        if (equipment == null) {
            showNewEquipment();
            return "Equipment with name not found.";
        }
        context.setShared(EquipmentBuilder.CURRENT_EQUIPMENT, equipment);
        return chatService.getChatFrame(equipment.type()).chat(context.memoryId(), context.userMessage());
    }

    public void addGameObject(StringBuilder localizations, StringBuilder gameObjects, String name, String description,
            String visualModel, String baseStat, String statName, String MapKey) {
        String nameHandle = IdMaker.handle();
        localizations.append("    <content contentuid=\"" + nameHandle + "\" version=\"1\">" + name
                + "</content>\n");
        String descriptionHandle = "";
        if (description != null) {
            descriptionHandle = IdMaker.handle();
            localizations.append("    <content contentuid=\"" + descriptionHandle + "\" version=\"1\">"
                    + description + "</content>\n");
        }
        String parentTemplateId = visualModel;
        if (parentTemplateId == null) {
            RootTemplate rootTemplate = library.findRootTemplateByStatName(baseStat);
            parentTemplateId = rootTemplate.MapKey;
        }
        gameObjects.append("        <node id=\"GameObjects\">\n");
        gameObjects.append("          <attribute id=\"Description\" type=\"TranslatedString\" handle=\""
                + descriptionHandle + "\" version=\"1\"/>\n");
        gameObjects.append("          <attribute id=\"DisplayName\" type=\"TranslatedString\" handle=\"" + nameHandle
                + "\" version=\"1\"/>\n");
        gameObjects.append("          <attribute id=\"LevelName\" type=\"FixedString\" value=\"\"/>\n");
        gameObjects.append("          <attribute id=\"MapKey\" type=\"FixedString\" value=\"" + MapKey + "\"/>\n");
        gameObjects.append("          <attribute id=\"Name\" type=\"LSString\" value=\"" + statName + "\"/>\n");
        gameObjects.append("          <attribute id=\"ParentTemplateId\" type=\"FixedString\" value=\""
                + parentTemplateId + "\"/>\n");
        gameObjects.append("          <attribute id=\"Stats\" type=\"FixedString\" value=\"" + statName + "\"/>\n");
        gameObjects.append(
                "          <attribute id=\"TechnicalDescription\" type=\"TranslatedString\" handle=\"\" version=\"0\"/>\n");
        gameObjects.append("          <attribute id=\"Type\" type=\"FixedString\" value=\"item\"/>\n");
        gameObjects.append(
                "          <attribute id=\"_OriginalFileVersion_\" type=\"int64\" value=\"144115207403209023\"/>\n");
        gameObjects.append("        </node>\n");
    }

    public void addArmor(BaseModel armor, String statPrefix, StringBuilder localizations, StringBuilder gameObjects, StringBuilder treasure, StringBuilder armorData) {
        String name = armor.name;
        String description = armor.description;
        String visualModel = armor.visualModel;
        String baseStat = armor.baseStat();
        String statName = statPrefix + "_" + toAlphaNumericUnderscore(name);
        String MapKey = IdMaker.uuid();

        addGameObject(localizations, gameObjects, name, description, visualModel, baseStat, statName, MapKey);

        treasure.append("new subtable \"1,1\"\n");
        treasure.append("object category \"I_" + statName + "\",1,0,0,0,0,0,0,0\n");

        armorData.append("new entry \"" + statName + "\"\n");
        armorData.append("type \"Armor\"\n");
        armorData.append("using \"" + armor.baseStat() + "\"\n");
        armorData.append("data \"RootTemplate\" \"" + MapKey + "\"\n");
        if (armor.rarity != null) {
            armorData.append("data \"Rarity\" \"" + armor.rarity.name() + "\"\n");
        }
        if (armor.boosts != null) {
            armorData.append("data \"Boosts\" \"" + armor.boosts + "\"\n");
        }
        armorData.append("data \"MinLevel\" \"1\"\n");

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

        StringBuilder gameObjects = new StringBuilder();

        StringBuilder armorData = new StringBuilder();

        StringBuilder treasure = new StringBuilder();

        StringBuilder localizations = new StringBuilder();

        if (newEquipment.bodyArmors != null) {

            for (BodyArmorModel bodyArmor : newEquipment.bodyArmors.values()) {
                addArmor(bodyArmor, statPrefix, localizations, gameObjects, treasure, armorData);
                if (bodyArmor.armorClass != null) {
                    armorData.append("data \"ArmorClass\" \"" + bodyArmor.armorClass + "\"\n");
                }
                armorData.append("\n");
            }
        }
        if (newEquipment.helmets != null) {
            for (HelmetModel helmet : newEquipment.helmets.values()) {
                addArmor(helmet, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        if (newEquipment.gloves != null) {
            for (GlovesModel glove : newEquipment.gloves.values()) {
                addArmor(glove, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        if (newEquipment.boots != null) {
            for (BootsModel boot : newEquipment.boots.values()) {
                addArmor(boot, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        if (newEquipment.rings != null) {
            for (RingModel ring : newEquipment.rings.values()) {
                addArmor(ring, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        if (newEquipment.amulets != null) {
            for (AmuletModel amulet : newEquipment.amulets.values()) {
                addArmor(amulet, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        if (newEquipment.cloaks != null) {
            for (CloakModel cloak : newEquipment.cloaks.values()) {
                addArmor(cloak, statPrefix, localizations, gameObjects, treasure, armorData);
                armorData.append("\n");
            }
        }
        String weaponData = "";
        
        if (newEquipment.weapons != null) {
            for (WeaponModel weapon : newEquipment.weapons.values()) {
                String statName = statPrefix + "_" + toAlphaNumericUnderscore(weapon.name);
                String mapKey = IdMaker.uuid();
                addGameObject(localizations, gameObjects, weapon.name, weapon.description, weapon.visualModel,
                        weapon.type.getBaseStat(), statName, mapKey);
                weaponData += "new entry \"" + statName + "\"\n";
                weaponData += "type \"Weapon\"\n";
                weaponData += "using \"" + weapon.type.getBaseStat() + "\"\n";
                weaponData += "data \"RootTemplate\" \"" + mapKey + "\"\n";
                if (weapon.rarity != null) {
                    weaponData += "data \"Rarity\" \"" + weapon.rarity.name() + "\"\n";
                }
                String boosts = weapon.boosts;
                if (boosts != null && weapon.magical) {
                    boosts = boosts + ";" + "WeaponProperty(Magical)";
                } else if (weapon.magical) {
                    boosts = "WeaponProperty(Magical)";
                }
                if (boosts != null) {
                    weaponData += "data \"DefaultBoosts\" \"" + boosts + "\"\n";
                }
                if (weapon.magical) {
                    StatsArchive.Stat stat = library.archive().getStats().getByName(weapon.type.baseStat);
                    String properties = stat.getField("Weapon Properties");
                    properties += ";Magical";
                    weaponData += "data \"Weapon Properties\" \"" + properties + "\"\n";
                }
                weaponData += "data \"MinLevel\" \"1\"\n";
                weaponData += "\n";

                treasure.append("new subtable \"1,1\"\n");
                treasure.append("object category \"I_" + statName + "\",1,0,0,0,0,0,0,0\n");
            }
        }

        inputStream = getClass().getResourceAsStream("/mod-template/LsxBoilerplate.lsx");
        String lsxBoilerplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        lsxBoilerplate = lsxBoilerplate.replace("{GAME_OBJECTS}", gameObjects);
        Path lsxBoilerplateFile = Files.writeString(rootTemplatesDir.resolve("Merged.lsx"), lsxBoilerplate);
        Converter.lsxToLsf(lsxBoilerplateFile, rootTemplatesDir.resolve("Merged.lsf"));

        Files.writeString(statsDataDir.resolve("Armor.txt"), armorData.toString());
        Files.writeString(statsDataDir.resolve("Weapon.txt"), weaponData);

        inputStream = getClass().getResourceAsStream("/mod-template/TreasureTable.txt");
        String treasureTable = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        treasureTable += treasure.toString();
        Files.writeString(statsDir.resolve("TreasureTable.txt"), treasureTable);

        String finalLocalizations = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<contentList>\n"
                + localizations.toString()
                + "</contentList>";
        Path locaXmlPath = localizationDir.resolve("english.xml");
        Files.writeString(locaXmlPath, finalLocalizations);

        Converter.xmlToLoca(locaXmlPath, localizationDir.resolve("english.loca"));

        PackageWriter writer = new PackageWriter();
        Path outputPak = tempDir.resolve(baseFileName + ".pak");
        writer.archive(modDir, outputPak);

        Log.info("Packaged mod to " + outputPak.toString());
        return outputPak.toFile();
    }

    // Replaces any non-alphanumeric character in the input string with an
    // underscore
    public static String toAlphaNumericUnderscore(String input) {
        if (input == null)
            return null;
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

    // Returns a string of 3 random capital letters
    public static String randomStatPrefix() {
        StringBuilder sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            char c = (char) ('A' + (int) (Math.random() * 26));
            sb.append(c);
        }
        return sb.toString();
    }

}

package org.baldurs.forge.builder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.baldurs.archivist.IdMaker;
import org.baldurs.archivist.LS.PackedVersion;
import org.baldurs.forge.chat.ChatFrame;
import org.baldurs.forge.chat.ChatService;
import org.baldurs.forge.chat.actions.PackageModAction;
import org.baldurs.forge.context.ChatContext;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Inject;

public class ModPackager implements ChatFrame {

    @Inject
    ChatContext context;

    @Inject
    ChatService chatService;

    @Inject
    EquipmentBuilderChat agent;

    @Override
    public String chat(String memoryId, String userMessage) {
        chatService.setChatFrame(context, ModPackager.class);
        return agent.packageMod(memoryId, userMessage);
    }

    @Tool("Package the mod.")
    public String packageMod(String name) {
        NewModModel newEquipment = context.getShared(NewModModel.NEW_EQUIPMENT, NewModModel.class);
        if (newEquipment == null) {
            return "No equipment to package.";
        }
        if (newEquipment.name == null) {
            newEquipment.name = name;
        }
        context.setShared(NewModModel.NEW_EQUIPMENT, newEquipment);
        chatService.popChatFrame(context);
        context.response().add(new PackageModAction());
        return "The mod was packaged.";
    }

    public File packageMod(NewModModel newEquipment) throws Exception {
        Path tempDir = Files.createTempDirectory("mod");
        String modUUID = IdMaker.uuid();
        Path modDir = Files.createDirectory(tempDir.resolve(newEquipment.name));
        Path localizationDir = Files.createDirectories(modDir.resolve("Localization/English"));
        Path modsDir = Files.createDirectories(modDir.resolve("Mods").resolve(newEquipment.name + "_" + modUUID));
        Path publicDir = Files.createDirectories(modDir.resolve("Public").resolve(newEquipment.name + "_" + modUUID));
        Path rootTemplatesDir = Files.createDirectories(publicDir.resolve("RootTemplates"));
        Path statsDir = Files.createDirectories(publicDir.resolve("Stats/Generated"));
        Path statsDataDir = Files.createDirectory(statsDir.resolve("Data"));
        
        InputStream inputStream = getClass().getResourceAsStream("/mod-template/meta.lsx");
        String meta = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        meta = meta.replace("{AUTHOR}", newEquipment.author);
        meta = meta.replace("{DESCRIPTION}", newEquipment.description == null ? "" : newEquipment.description);
        meta = meta.replace("{FOLDER}", newEquipment.name + "_" + modUUID);
        meta = meta.replace("{NAME}", newEquipment.name);
        PackedVersion version = new PackedVersion();
        version.major = 1;
        version.minor = 0;
        version.revision = 0;
        version.build = 0;
        long version64 = version.toVersion64();
        meta = meta.replace("{VERSION64}", Long.toString(version64));
        meta = meta.replace("{UUID}", modUUID);

        Path metaFile = Files.writeString(modDir.resolve("meta.lsx"), meta);
        

        return null;


    }
}

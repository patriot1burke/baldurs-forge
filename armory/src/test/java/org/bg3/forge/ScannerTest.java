package org.bg3.forge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.scanner.IconCollector;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.RootTemplateArchive;
import org.baldurs.forge.scanner.StatsArchive;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.output.JsonSchemas;


public class ScannerTest {

    @Test
    public void testJsonSchema() throws Exception {
        Optional<JsonSchema> schema = JsonSchemas.jsonSchemaFrom(EquipmentModel.class);
        System.out.println(schema.get().toString());
    }

    //@Test
    public void testObjecvtMapper() throws Exception{
        String hello = "hello \"world\"";
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(hello));

    }

    //@Test
    public void testIconExtractor() throws Exception {
        /*
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/SharedDev/GUI/Icons_Skills.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/SharedDev/Assets/Textures/Icons/Icons_Skills.dds",
        "/home/bburke/projects/bg3-forge/icons/skills"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Skills.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Skills.dds",
        "/home/bburke/projects/bg3-forge/icons/skills"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items_2.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items_2.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items_3.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items_3.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items_4.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items_4.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items_5.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items_5.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        IconCollector.extractIcons("/mnt/c/Users/patri/mods/shared/Public/Shared/GUI/Icons_Items_6.lsx",
        "/mnt/c/Users/patri/mods/icons/Public/Shared/Assets/Textures/Icons/Icons_Items_6.dds",
        "/home/bburke/projects/bg3-forge/icons/items"
        );
        */
    }


    //@Test
    public void testEntryScanner() throws IOException {
  
    }

    //@Test
    public void testRootTemplateScanner() throws Exception {
    
    }
}

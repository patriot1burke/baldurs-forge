package org.burkecentral.bg3mods;
import org.baldurs.archivist.LS.Localization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
public class LocalizationTest {

    @Test
    public void testLocalization() throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource("Localization.loca");
        Path resourcePath = Paths.get(resourceUrl.toURI());
        assertTrue(resourcePath.toFile().exists());
        Localization.LocaReader reader = new Localization.LocaReader(new FileInputStream(resourcePath.toFile()));
        Localization.LocaResource resource = reader.read();

        Localization.LocaXmlWriter writer = new Localization.LocaXmlWriter(
            new FileOutputStream(resourcePath.resolveSibling("Localization.loca.xml").toFile()));
        writer.write(resource);
    }

}

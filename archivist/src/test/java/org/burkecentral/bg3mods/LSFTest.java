package org.burkecentral.bg3mods;

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

import org.baldurs.archivist.LS.Node;
import org.baldurs.archivist.LS.Region;
import org.baldurs.archivist.LS.Resource;
import org.baldurs.archivist.LS.Resources.LSF.LSFReader;
import org.baldurs.archivist.LS.Resources.LSX.LSXReader;
import org.baldurs.archivist.LS.Resources.LSX.LSXWriter;
    
public class LSFTest {
    @Test
    public void testLSF() throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource("merged.lsf");
        Path resourcePath = Paths.get(resourceUrl.toURI());
        assertTrue(resourcePath.toFile().exists());
        LSFReader reader = new LSFReader(resourcePath);
        Resource resource = reader.read();
        Path outputPath = resourcePath.resolveSibling("merged.lsx");
        System.out.println("Writing to: " + outputPath);
        LSXWriter writer = new LSXWriter(new FileOutputStream(outputPath.toFile()));
        writer.prettyPrint = true;
        writer.write(resource);

        LSXReader lsxReader = new LSXReader(new FileInputStream(outputPath.toFile()));
        Resource resource2 = lsxReader.read();
        assertEquals(resource.metadataFormat, resource2.metadataFormat);
        assertEquals(resource.metadata.majorVersion, resource2.metadata.majorVersion);
        assertEquals(resource.metadata.minorVersion, resource2.metadata.minorVersion);
        assertEquals(resource.metadata.revision, resource2.metadata.revision);
        assertEquals(resource.metadata.buildNumber, resource2.metadata.buildNumber);
        assertEquals(resource.metadata.timestamp, resource2.metadata.timestamp);
        assertEquals(resource.metadataFormat, resource2.metadataFormat);
        assertTrue(resource.regions.size() == resource2.regions.size());
        assertTrue(resource.regions.keySet().containsAll(resource2.regions.keySet()));
        for (String regionName : resource.regions.keySet()) {
            Region region1 = resource.regions.get(regionName);
            Region region2 = resource2.regions.get(regionName);
            assertEquals(region1.regionName, region2.regionName);
            assertEquals(region1.children.size(), region2.children.size());
            assertTrue(region1.children.keySet().containsAll(region2.children.keySet()));
            for (String childName : region1.children.keySet()) {
                List<Node> children1 = region1.children.get(childName);
                List<Node> children2 = region2.children.get(childName);
                assertEquals(children1.size(), children2.size());
            }
        }
     }   
} 
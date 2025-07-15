package org.burkecentral.bg3mods;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.baldurs.archivist.LS.Resource;
import org.baldurs.archivist.LS.Resources.LSF.LSFReader;
    
public class LSFTest {
    @Test
    public void testLSF() throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource("merged.lsf");
        Path resourcePath = Paths.get(resourceUrl.toURI());
        assertTrue(resourcePath.toFile().exists());
        LSFReader reader = new LSFReader(resourcePath);
        Resource resource = reader.read();
        System.out.println(resource);
    
        // Get the Path for a Java resource
        if (resourceUrl != null) {
        } else {
            throw new RuntimeException("Resource 'merged.lsf' not found");
        }
    }
} 
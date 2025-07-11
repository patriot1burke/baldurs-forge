package org.baldurs.archivist;

import java.io.File;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "extract", description = "Extract all files from a .pak file")
class ExtractCommand implements Runnable {
    
    @Parameters(index = "0", description = "The input .pak file")
    File pakFile;

    @Parameters(index = "1", description = "The output directory")
    File outputDir;

    @Override
    public void run() {
        try {
            PackageReader reader = new PackageReader(pakFile.toPath());
            reader.extract(outputDir.toPath());
            System.out.println("Extraction completed successfully!");
        } catch (Exception e) {
            System.err.println("Extraction failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
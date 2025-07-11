package org.baldurs.archivist;

import java.io.File;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "archive", description = "Create a .pak file from a directory")
class ArchiveCommand implements Runnable {
    
    @Parameters(index = "0", description = "The input directory")
    File inputDir;

    @Parameters(index = "1", description = "The output .pak file")
    File outputPak;

    @Override
    public void run() {
        try {
            System.out.println("Input directory: " + inputDir.getAbsolutePath());
            System.out.println("Output .pak file: " + outputPak.getAbsolutePath());
            if (!inputDir.exists()) {
                System.err.println("Input directory does not exist");
                return;
            }
 
            PackageWriter writer = new PackageWriter();
            writer.archive(inputDir.toPath(), outputPak.toPath());
        } catch (Exception e) {
            System.err.println("Archive failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
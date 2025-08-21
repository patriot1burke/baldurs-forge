package org.baldurs.archivist;

import java.io.File;

import org.baldurs.archivist.LS.Converter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "convert", description = "Convert a file to another format")
class ConvertCommand implements Runnable {
    @Parameters(index = "0", description = "The input file.  Must be .lsx, .lsf, .loca, or .xml (localization xml format)")
    File inFile;

    @Parameters(index = "1", description = "The output file.  Must be .lsx, .lsf, .loca, or .xml (localization xml format)")
    File toFile;

    @Override
    public void run() {
        try {
            if (inFile.getName().endsWith(".lsf")) {
                if (!toFile.getName().endsWith(".lsx")) {
                    System.err.println("Input .lsf file can only be converted to a .lsx file");
                    return;
                }
                Converter.lsfToLsx(inFile.toPath(), toFile.toPath());
            } else if (inFile.getName().endsWith(".lsx")) {
                if (!toFile.getName().endsWith(".lsf")) {
                    System.err.println("Input .lsx file can only be converted to a .lsf file");
                    return;
                }
                Converter.lsxToLsf(inFile.toPath(), toFile.toPath());
            } else if (inFile.getName().endsWith(".loca")) {
                if (!toFile.getName().endsWith(".xml")) {
                    System.err.println("Input .loca file can only be converted to a .xml file");
                    return;
                }
                Converter.locaToXml(inFile.toPath(), toFile.toPath());
            } else if (inFile.getName().endsWith(".xml")) {
                if (!toFile.getName().endsWith(".loca")) {
                    System.err.println("Input .xml file can only be converted to a .loca file");
                    return;
                }
                Converter.xmlToLoca(inFile.toPath(), toFile.toPath());
            } else {
                System.err.println("Cannot convert " + inFile.getName() + " to " + toFile.getName());
            }
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
        }
    }
}

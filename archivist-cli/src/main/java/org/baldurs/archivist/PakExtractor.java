package org.baldurs.archivist;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine;
import jakarta.inject.Inject;


@TopCommand
@Command(name = "archivist", mixinStandardHelpOptions = true, 
subcommands = {ExtractCommand.class, ArchiveCommand.class, ConvertCommand.class},
description = "Tool for extracting and archiving Larian Studios .pak files and converting between formats")
public class PakExtractor implements Runnable {
    
    @Inject
    CommandLine.IFactory factory;

    @Override
    public void run() {
        // Show help if no subcommand is specified
        System.out.println("Use 'extract','archive' or 'convert' subcommand. Use --help for more information.");
    }
}

package org.baldurs.archivist;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine;
import jakarta.inject.Inject;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

@TopCommand
@Command(name = "pak-tool", mixinStandardHelpOptions = true, 
subcommands = {ExtractCommand.class, ArchiveCommand.class},
description = "Tool for extracting and archiving Larian Studios .pak files")
public class PakExtractor implements Runnable {
    
    @Inject
    CommandLine.IFactory factory;

    @Override
    public void run() {
        // Show help if no subcommand is specified
        System.out.println("Use 'extract' or 'archive' subcommand. Use --help for more information.");
    }
}

package org.baldurs.archivist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4SafeDecompressor;
import net.jpountz.xxhash.XXHashFactory;

public class LZ4CompressionUtil {

    public static int decompressCLI(byte[] src, byte[] dest) {
        try {
            //System.out.println("Decompressing LZ4 CLI");
            
            // Create a temporary file
            Path tempFile = Files.createTempFile("lz4_src_", ".tmp");
            
            // Write the source data to the temporary file
            Files.write(tempFile, src);
            
            // Create output temporary file
            Path outputTempFile = Files.createTempFile("lz4_dest_", ".tmp");
            
            // Clean up the temporary files when done
            tempFile.toFile().deleteOnExit();
            outputTempFile.toFile().deleteOnExit();
            
            // Execute LZ4 CLI command
            //System.out.println("Executing LZ4 CLI command");
            ProcessBuilder pb = new ProcessBuilder("lz4", "-f", "-d", tempFile.toString(), outputTempFile.toString());
            //System.out.println("Command: " + pb.command());
            //pb.inheritIO();
            Process process = pb.start();
            //System.out.println("Process: " + process.pid());
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("LZ4 CLI command failed with exit code: " + exitCode);
            }
            
            // Read the decompressed data from the output file
            byte[] decompressedData = Files.readAllBytes(outputTempFile);
            
            // Copy the decompressed data to the destination array
            int bytesToCopy = Math.min(decompressedData.length, dest.length);
            System.arraycopy(decompressedData, 0, dest, 0, bytesToCopy);
            
            // Clean up temporary files
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(outputTempFile);
            
            // Return the number of bytes decompressed
            return bytesToCopy;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute LZ4 CLI command", e);
        }
    }

    public static int decompress(byte[] src, byte[] dest) {
        LZ4Factory factory = LZ4Factory.safeInstance();
        LZ4SafeDecompressor decompressor = factory.safeDecompressor();
        return decompressor.decompress(src, dest);
    }
    public static int decompress2(byte[] src, byte[] dest) {
        try {
            LZ4Factory factory = LZ4Factory.nativeInstance();
            LZ4SafeDecompressor decompressor = factory.safeDecompressor();
            LZ4FrameInputStream lz4 = new LZ4FrameInputStream(new ByteArrayInputStream(src), decompressor, XXHashFactory.fastestInstance().hash32(), false);
            return lz4.read(dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int apacheDecompress(byte[] src, byte[] dest) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(src);
        FramedLZ4CompressorInputStream flz4 = new FramedLZ4CompressorInputStream(bais, true);
        return flz4.read(dest);
    }




}

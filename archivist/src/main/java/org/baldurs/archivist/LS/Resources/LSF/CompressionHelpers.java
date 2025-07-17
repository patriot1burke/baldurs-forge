package org.baldurs.archivist.LS.Resources.LSF;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.baldurs.archivist.LS.Enums.CompressionMethod;
import org.baldurs.archivist.LS.Enums.LSCompressionLevel;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

/**
 * Compression utilities for LSF files
 * Ported from C# CompressionHelpers.cs
 */
public class CompressionHelpers {
    
    /**
     * Compress data using the specified compression method and level
     */
    public static byte[] compress(byte[] data, CompressionMethod method, LSCompressionLevel level) {
        return compress(data, method, level, false);
    }
    
    /**
     * Compress data using the specified compression method and level, with optional chunked compression
     */
    public static byte[] compress(byte[] data, CompressionMethod method, LSCompressionLevel level, boolean chunked) {
        if (method == CompressionMethod.None) {
            return data;
        }
        
        if (method == CompressionMethod.LZ4) {
            return compressLZ4(data, level, chunked);
        }
        
        throw new UnsupportedOperationException("Compression method " + method + " is not supported");
    }
    
    /**
     * Compress data using LZ4 compression
     */
    private static byte[] compressLZ4(byte[] data, LSCompressionLevel level, boolean chunked) {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor;
        
        switch (level) {
            case Fast:
                compressor = factory.fastCompressor();
                break;
            case Max:
                compressor = factory.highCompressor();
                break;
            case Default:
            default:
                compressor = factory.fastCompressor();
                break;
        }
        
        int maxCompressedLength = compressor.maxCompressedLength(data.length);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, data.length, compressed, 0, maxCompressedLength);
        
        // Create a new array with the exact compressed size
        byte[] result = new byte[compressedLength];
        System.arraycopy(compressed, 0, result, 0, compressedLength);
        
        return result;
    }
    
    /**
     * Create compression flags from compression method and level
     */
    public static int makeCompressionFlags(CompressionMethod method, LSCompressionLevel level) {
        int flags = 0;
        
        // Set compression method
        switch (method) {
            case None:
                flags |= 0x00;
                break;
            case Zlib:
                flags |= 0x01;
                break;
            case LZ4:
                flags |= 0x02;
                break;
            case Zstd:
                flags |= 0x03;
                break;
        }
        
        // Set compression level
        switch (level) {
            case Fast:
                flags |= 0x10;
                break;
            case Default:
                flags |= 0x20;
                break;
            case Max:
                flags |= 0x40;
                break;
        }
        
        return flags;
    }
} 
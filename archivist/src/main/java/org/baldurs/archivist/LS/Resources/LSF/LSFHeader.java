package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * LSF header structure
 */
public class LSFHeader {
    /**
     * Possibly version number? (major, minor, rev, build)
     */
    public int engineVersion;
    
    public LSFHeader() {}
    
    public LSFHeader(int engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public static LSFHeader fromBuffer(ByteBuffer buffer) {
        LSFHeader header = new LSFHeader();
        header.engineVersion = buffer.getInt();
        return header;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(engineVersion);
    }
} 
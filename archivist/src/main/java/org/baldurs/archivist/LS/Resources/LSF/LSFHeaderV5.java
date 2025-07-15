package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * LSF header structure for V5
 */
public class LSFHeaderV5 {
    /**
     * Possibly version number? (major, minor, rev, build)
     */
    public long engineVersion;
    
    public LSFHeaderV5() {}
    
    public LSFHeaderV5(long engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public static LSFHeaderV5 fromBuffer(ByteBuffer buffer) {
        LSFHeaderV5 header = new LSFHeaderV5();
        header.engineVersion = buffer.getLong();
        return header;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putLong(engineVersion);
    }
} 
package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * LSF file signature; should be the same as LSFHeader.Signature
 */
public class LSFMagic {
    public int magic;
    public int version;
    
    public LSFMagic() {}
    
    public LSFMagic(int magic, int version) {
        this.magic = magic;
        this.version = version;
    }
    
    public static LSFMagic fromBuffer(ByteBuffer buffer) {
        LSFMagic magic = new LSFMagic();
        magic.magic = buffer.getInt();
        magic.version = buffer.getInt();
        return magic;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(magic);
        buffer.putInt(version);
    }
} 
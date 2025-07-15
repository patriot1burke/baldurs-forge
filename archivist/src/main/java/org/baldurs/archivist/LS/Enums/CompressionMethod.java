package org.baldurs.archivist.LS.Enums;

public enum CompressionMethod {
    None((byte)0),
    Zlib((byte)1),
    LZ4((byte)2),
    Zstd((byte)3)
    ;

    private byte value;

    private CompressionMethod(byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return value;
    }

    public static CompressionMethod fromValue(int value) {
        return CompressionMethod.values()[value & 0x0F];
    }
} 
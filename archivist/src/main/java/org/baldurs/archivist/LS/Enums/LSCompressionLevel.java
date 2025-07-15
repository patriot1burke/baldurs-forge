package org.baldurs.archivist.LS.Enums;

public enum LSCompressionLevel {
    Fast((byte)0x10),
    Default((byte)0x20),
    Max((byte)0x40)
    ;

    private byte value;

    private LSCompressionLevel(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static LSCompressionLevel fromValue(int value) {
        value = value & 0xF0;
        if (value == 0x10) {
            return LSCompressionLevel.Fast;
        } else if (value == 0x20) {
            return LSCompressionLevel.Default;
        } else if (value == 0x40) {
            return LSCompressionLevel.Max;
        }
        throw new IllegalArgumentException("Invalid compression level value: " + value);
    }
} 
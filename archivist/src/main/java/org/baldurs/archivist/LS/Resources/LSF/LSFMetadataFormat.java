package org.baldurs.archivist.LS.Resources.LSF;

/**
 * Extended node/attribute format indicator
 */
public enum LSFMetadataFormat {
    NONE(0),
    KEYS_AND_ADJACENCY(1),
    NONE2(2); // Behaves same way as None
    
    private final int value;
    
    LSFMetadataFormat(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static LSFMetadataFormat fromInt(int value) {
        for (LSFMetadataFormat format : values()) {
            if (format.value == value) {
                return format;
            }
        }
        return NONE;
    }
} 
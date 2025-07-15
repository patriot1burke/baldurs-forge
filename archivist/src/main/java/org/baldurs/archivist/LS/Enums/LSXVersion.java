package org.baldurs.archivist.LS.Enums;

public enum LSXVersion {
    /**
     * Version used in D:OS 2 (DE)
     */
    V3(3),
    
    /**
     * Version used in BG3
     * Replaces type IDs with type names
     */
    V4(4);

    private final int value;

    LSXVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
} 
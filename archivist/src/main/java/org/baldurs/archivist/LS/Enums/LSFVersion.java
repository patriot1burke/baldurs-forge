package org.baldurs.archivist.LS.Enums;

public enum LSFVersion {
    /**
     * Initial version of the LSF format
     */
    VER_INITIAL(0x01),

    /**
     * LSF version that added chunked compression for substreams
     */
    VER_CHUNKED_COMPRESS(0x02),

    /**
     * LSF version that extended the node descriptors
     */
    VER_EXTENDED_NODES(0x03),

    /**
     * BG3 version, no changes found so far apart from version numbering
     */
    VER_BG3(0x04),

    /**
     * BG3 version with updated header metadata
     */
    VER_BG3_EXTENDED_HEADER(0x05),

    /**
     * BG3 version with node key names
     */
    VER_BG3_NODE_KEYS(0x06),

    /**
     * BG3 Patch 3 version with unknown additions
     */
    VER_BG3_PATCH3(0x07),

    /**
     * Latest input version supported by this library
     */
    MAX_READ_VERSION(0x07),

    /**
     * Latest output version supported by this library
     */
    MAX_WRITE_VERSION(0x07);

    private final int value;

    LSFVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LSFVersion fromInt(int value) {
        for (LSFVersion version : LSFVersion.values()) {
            if (version.getValue() == value) {
                return version;
            }
        }
        throw new IllegalArgumentException("Invalid LSF version: " + value);
    }
} 
package org.baldurs.archivist.LS;

/**
 * LSMetadata structure for file metadata
 */
public class LSMetadata {
    public static final int CURRENT_MAJOR_VERSION = 33;

    public long timestamp;
    public long majorVersion;
    public long minorVersion;
    public long revision;
    public long buildNumber;

    public String toString() {
        return String.format("LSMetadata(majorVersion=%d, minorVersion=%d, revision=%d, buildNumber=%d, timestamp=%d)", majorVersion, minorVersion, revision, buildNumber, timestamp);
    }

    public boolean equals(LSMetadata other) {
        return majorVersion == other.majorVersion && minorVersion == other.minorVersion && revision == other.revision && buildNumber == other.buildNumber && timestamp == other.timestamp;
    }
} 
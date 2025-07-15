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
} 
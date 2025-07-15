package org.baldurs.archivist.LS;

/**
 * LSBHeader structure for LSB file headers
 */
public class LSBHeader {
    /**
     * LSB file signature since BG3
     */
    public static final byte[] SIGNATURE_BG3 = "LSFM".getBytes();

    /**
     * LSB signature up to FW3 (DOS2 DE)
     */
    public static final int SIGNATURE_FW3 = 0x40000000;

    public long signature;
    public long totalSize;
    public long bigEndian;
    public long unknown;
    public LSMetadata metadata;
} 
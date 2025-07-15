package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * Key attribute name definition for a specific node in the LSF file
 */
public class LSFKeyEntry {
    /**
     * Index of the node
     */
    public int nodeIndex;
    /**
     * Name of key attribute
     * (16-bit MSB: index into name hash table, 16-bit LSB: offset in hash chain)
     */
    public int keyName;
    
    /**
     * Index into name hash table
     */
    public int getKeyNameIndex() {
        return (keyName >> 16) & 0xffff;
    }
    
    /**
     * Offset in hash chain
     */
    public int getKeyNameOffset() {
        return keyName & 0xffff;
    }
    
    public static LSFKeyEntry fromBuffer(ByteBuffer buffer) {
        LSFKeyEntry entry = new LSFKeyEntry();
        entry.nodeIndex = buffer.getInt();
        entry.keyName = buffer.getInt();
        return entry;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(nodeIndex);
        buffer.putInt(keyName);
    }
} 
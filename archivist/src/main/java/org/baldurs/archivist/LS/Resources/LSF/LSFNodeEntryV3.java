package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * Node (structure) entry in the LSF file (V3)
 */
public class LSFNodeEntryV3 {
    /**
     * Name of this node
     * (16-bit MSB: index into name hash table, 16-bit LSB: offset in hash chain)
     */
    public int nameHashTableIndex;
    /**
     * Index of the parent node
     * (-1: this node is a root region)
     */
    public int parentIndex;
    /**
     * Index of the next sibling of this node
     * (-1: this is the last node)
     */
    public int nextSiblingIndex;
    /**
     * Index of the first attribute of this node
     * (-1: node has no attributes)
     */
    public int firstAttributeIndex;
    
    /**
     * Index into name hash table
     */
    public int getNameIndex() {
        return (nameHashTableIndex >> 16) & 0xffff;
    }
    
    /**
     * Offset in hash chain
     */
    public int getNameOffset() {
        return nameHashTableIndex & 0xffff;
    }
    
    public static LSFNodeEntryV3 fromBuffer(ByteBuffer buffer) {
        LSFNodeEntryV3 entry = new LSFNodeEntryV3();
        entry.nameHashTableIndex = buffer.getInt();
        entry.parentIndex = buffer.getInt();
        entry.nextSiblingIndex = buffer.getInt();
        entry.firstAttributeIndex = buffer.getInt();
        return entry;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(nameHashTableIndex);
        buffer.putInt(parentIndex);
        buffer.putInt(nextSiblingIndex);
        buffer.putInt(firstAttributeIndex);
    }
} 
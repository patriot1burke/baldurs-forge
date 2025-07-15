package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;

/**
 * V3 attribute extension in the LSF file
 */
public class LSFAttributeEntryV3 {
    /**
     * Name of this attribute
     * (16-bit MSB: index into name hash table, 16-bit LSB: offset in hash chain)
     */
    public int nameHashTableIndex;
    
    /**
     * 6-bit LSB: Type of this attribute (see NodeAttribute.DataType)
     * 26-bit MSB: Length of this attribute
     */
    public int typeAndLength;
    
    /**
     * Index of the next attribute in this node
     * (-1: this is the last attribute)
     */
    public int nextAttributeIndex;
    
    /**
     * Absolute position of attribute value in the value stream
     */
    public int offset;
    
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
    
    /**
     * Type of this attribute (see NodeAttribute.DataType)
     */
    public int getTypeId() {
        return typeAndLength & 0x3f;
    }
    
    /**
     * Length of this attribute
     */
    public int getLength() {
        return typeAndLength >> 6;
    }
    
    public static LSFAttributeEntryV3 fromBuffer(ByteBuffer buffer) {
        LSFAttributeEntryV3 entry = new LSFAttributeEntryV3();
        entry.nameHashTableIndex = buffer.getInt();
        entry.typeAndLength = buffer.getInt();
        entry.nextAttributeIndex = buffer.getInt();
        entry.offset = buffer.getInt();
        return entry;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(nameHashTableIndex);
        buffer.putInt(typeAndLength);
        buffer.putInt(nextAttributeIndex);
        buffer.putInt(offset);
    }
} 
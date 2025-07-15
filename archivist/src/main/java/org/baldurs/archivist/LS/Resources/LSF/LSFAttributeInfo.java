package org.baldurs.archivist.LS.Resources.LSF;

/**
 * Processed attribute information for an attribute in the LSF file
 */
public class LSFAttributeInfo {
    /**
     * Index into name hash table
     */
    public int nameIndex;
    /**
     * Offset in hash chain
     */
    public int nameOffset;
    /**
     * Type of this attribute (see NodeAttribute.DataType)
     */
    public int typeId;
    /**
     * Length of this attribute
     */
    public int length;
    /**
     * Absolute position of attribute data in the values section
     */
    public int dataOffset;
    /**
     * Index of the next attribute in this node
     * (-1: this is the last attribute)
     */
    public int nextAttributeIndex;
} 
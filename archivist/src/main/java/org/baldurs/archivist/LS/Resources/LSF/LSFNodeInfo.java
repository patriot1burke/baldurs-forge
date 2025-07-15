package org.baldurs.archivist.LS.Resources.LSF;

/**
 * Processed node information for a node in the LSF file
 */
public class LSFNodeInfo {
    /**
     * Index of the parent node
     * (-1: this node is a root region)
     */
    public int parentIndex;
    /**
     * Index into name hash table
     */
    public int nameIndex;
    /**
     * Offset in hash chain
     */
    public int nameOffset;
    /**
     * Index of the first attribute of this node
     * (-1: node has no attributes)
     */
    public int firstAttributeIndex;
    public String keyAttribute = null;
} 
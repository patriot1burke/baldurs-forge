package org.baldurs.archivist.LS.Resources.LSF;

import java.nio.ByteBuffer;


/**
 * LSF metadata structure for V6
 */
public class LSFMetadataV6 {
    /**
     * Total uncompressed size of the string hash table
     */
    public int stringsUncompressedSize;
    /**
     * Compressed size of the string hash table
     */
    public int stringsSizeOnDisk;
    /**
     * Total uncompressed size of the node key attribute table
     */
    public int keysUncompressedSize;
    /**
     * Compressed size of the node key attribute table
     */
    public int keysSizeOnDisk;
    /**
     * Total uncompressed size of the node list
     */
    public int nodesUncompressedSize;
    /**
     * Compressed size of the node list
     */
    public int nodesSizeOnDisk;
    /**
     * Total uncompressed size of the attribute list
     */
    public int attributesUncompressedSize;
    /**
     * Compressed size of the attribute list
     */
    public int attributesSizeOnDisk;
    /**
     * Total uncompressed size of the raw value buffer
     */
    public int valuesUncompressedSize;
    /**
     * Compressed size of the raw value buffer
     */
    public int valuesSizeOnDisk;
    /**
     * Compression method and level used for the string, node, attribute and value buffers.
     * Uses the same format as packages (see BinUtils.MakeCompressionFlags)
     */
    public byte compressionFlags;
    /**
     * Possibly unused, always 0
     */
    public byte unknown2;
    public short unknown3;
    /**
     * Extended node/attribute format indicator
     */
    public LSFMetadataFormat metadataFormat;
    
    public static LSFMetadataV6 fromBuffer(ByteBuffer buffer) {
        LSFMetadataV6 metadata = new LSFMetadataV6();
        metadata.stringsUncompressedSize = buffer.getInt();
        metadata.stringsSizeOnDisk = buffer.getInt();
        metadata.keysUncompressedSize = buffer.getInt();
        metadata.keysSizeOnDisk = buffer.getInt();
        metadata.nodesUncompressedSize = buffer.getInt();
        metadata.nodesSizeOnDisk = buffer.getInt();
        metadata.attributesUncompressedSize = buffer.getInt();
        metadata.attributesSizeOnDisk = buffer.getInt();
        metadata.valuesUncompressedSize = buffer.getInt();
        metadata.valuesSizeOnDisk = buffer.getInt();
        metadata.compressionFlags = buffer.get();
        metadata.unknown2 = buffer.get();
        metadata.unknown3 = buffer.getShort();
        metadata.metadataFormat = LSFMetadataFormat.fromInt(buffer.getInt());
        return metadata;
    }
    
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(stringsUncompressedSize);
        buffer.putInt(stringsSizeOnDisk);
        buffer.putInt(keysUncompressedSize);
        buffer.putInt(keysSizeOnDisk);
        buffer.putInt(nodesUncompressedSize);
        buffer.putInt(nodesSizeOnDisk);
        buffer.putInt(attributesUncompressedSize);
        buffer.putInt(attributesSizeOnDisk);
        buffer.putInt(valuesUncompressedSize);
        buffer.putInt(valuesSizeOnDisk);
        buffer.put(compressionFlags);
        buffer.put(unknown2);
        buffer.putShort(unknown3);
        buffer.putInt(metadataFormat.getValue());
    }
} 
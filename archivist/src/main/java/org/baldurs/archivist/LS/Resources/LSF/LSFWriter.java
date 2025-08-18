package org.baldurs.archivist.LS.Resources.LSF;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.baldurs.archivist.LS.*;
import org.baldurs.archivist.LS.Enums.*;

import com.google.common.io.LittleEndianDataOutputStream;

/**
 * LSF (Larian Save Format) writer
 * Ported from C# LSFWriter.cs
 */
public class LSFWriter implements AutoCloseable {
    
    private static final int STRING_HASH_MAP_SIZE = 0x200;
    
    private final OutputStream stream;
    private LittleEndianDataOutputStream writer;
    private LSMetadata meta;
    
    private ByteArrayOutputStream nodeStream;
    private LittleEndianDataOutputStream nodeWriter;
    private int nextNodeIndex = 0;
    private Map<Node, Integer> nodeIndices;
    
    private ByteArrayOutputStream attributeStream;
    private LittleEndianDataOutputStream attributeWriter;
    private int nextAttributeIndex = 0;
    
    private ByteArrayOutputStream valueStream;
    private LittleEndianDataOutputStream valueWriter;
    
    private ByteArrayOutputStream keyStream;
    private LittleEndianDataOutputStream keyWriter;
    
    private List<List<String>> stringHashMap;
    private List<Integer> nextSiblingIndices;
    
    public LSFVersion version = LSFVersion.MAX_WRITE_VERSION;
    public LSFMetadataFormat metadataFormat = LSFMetadataFormat.NONE;
    public CompressionMethod compression = CompressionMethod.None;
    public LSCompressionLevel compressionLevel = LSCompressionLevel.Default;
    
    public LSFWriter(OutputStream stream) {
        this.stream = stream;
    }
    
    public void write(Resource resource) throws IOException {
        if (version.getValue() > LSFVersion.MAX_WRITE_VERSION.getValue()) {
            String msg = String.format("Writing LSF version %d is not supported (highest is %d)", 
                version.getValue(), LSFVersion.MAX_WRITE_VERSION.getValue());
            throw new InvalidDataException(msg);
        }
        
        meta = resource.metadata;
        
        this.writer = new LittleEndianDataOutputStream(stream);
        this.nodeStream = new ByteArrayOutputStream();
        this.nodeWriter = new LittleEndianDataOutputStream(nodeStream);
        this.attributeStream = new ByteArrayOutputStream();
        this.attributeWriter = new LittleEndianDataOutputStream(attributeStream);
        this.valueStream = new ByteArrayOutputStream();
        this.valueWriter = new LittleEndianDataOutputStream(valueStream);
        this.keyStream = new ByteArrayOutputStream();
        this.keyWriter = new LittleEndianDataOutputStream(keyStream);
        
        try {
            
            nextNodeIndex = 0;
            nextAttributeIndex = 0;
            nodeIndices = new HashMap<>();
            nextSiblingIndices = null;
            stringHashMap = new ArrayList<>(STRING_HASH_MAP_SIZE);
            while (stringHashMap.size() < STRING_HASH_MAP_SIZE) {
                stringHashMap.add(new ArrayList<>());
            }
            
            if (metadataFormat != LSFMetadataFormat.NONE) {
                computeSiblingIndices(resource);
            }
            
            writeRegions(resource);
            
            byte[] stringBuffer = null;
            try (ByteArrayOutputStream stringStream = new ByteArrayOutputStream();
                 LittleEndianDataOutputStream stringWriter = new LittleEndianDataOutputStream(stringStream)) {
                writeStaticStrings(stringWriter);
                stringBuffer = stringStream.toByteArray();
            }
            
            byte[] nodeBuffer = nodeStream.toByteArray();
            byte[] attributeBuffer = attributeStream.toByteArray();
            byte[] valueBuffer = valueStream.toByteArray();
            byte[] keyBuffer = keyStream.toByteArray();
            
            LSFMagic magic = new LSFMagic();
            magic.magic = ByteBuffer.wrap(LSFCommon.SIGNATURE).order(ByteOrder.LITTLE_ENDIAN).getInt();
            magic.version = (int) version.getValue();
            writeStruct(writer, magic);
            
            PackedVersion gameVersion = new PackedVersion();
            gameVersion.major = resource.metadata.majorVersion;
            gameVersion.minor = resource.metadata.minorVersion;
            gameVersion.revision = resource.metadata.revision;
            gameVersion.build = resource.metadata.buildNumber;
            
            if (version.getValue() < LSFVersion.VER_BG3_EXTENDED_HEADER.getValue()) {
                LSFHeader header = new LSFHeader();
                header.engineVersion = gameVersion.toVersion32();
                writeStruct(writer, header);
            } else {
                LSFHeaderV5 header = new LSFHeaderV5();
                header.engineVersion = gameVersion.toVersion64();
                writeStruct(writer, header);
            }
            
            boolean chunked = version.getValue() >= LSFVersion.VER_CHUNKED_COMPRESS.getValue();
            byte[] stringsCompressed = CompressionHelpers.compress(stringBuffer, compression, compressionLevel);
            byte[] nodesCompressed = CompressionHelpers.compress(nodeBuffer, compression, compressionLevel, chunked);
            byte[] attributesCompressed = CompressionHelpers.compress(attributeBuffer, compression, compressionLevel, chunked);
            byte[] valuesCompressed = CompressionHelpers.compress(valueBuffer, compression, compressionLevel, chunked);
            byte[] keysCompressed;
            
            if (metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
                keysCompressed = CompressionHelpers.compress(keyBuffer, compression, compressionLevel, chunked);
            } else {
                // Avoid generating a key blob with compression headers if key data should not be written at all
                keysCompressed = new byte[0];
            }
            
            if (version.getValue() < LSFVersion.VER_BG3_NODE_KEYS.getValue()) {
                LSFMetadataV5 meta = new LSFMetadataV5();
                meta.stringsUncompressedSize = stringBuffer.length;
                meta.nodesUncompressedSize = nodeBuffer.length;
                meta.attributesUncompressedSize = attributeBuffer.length;
                meta.valuesUncompressedSize = valueBuffer.length;
                //System.out.println("meta.stringsUncompressedSize: " + meta.stringsUncompressedSize);
                //System.out.println("meta.nodesUncompressedSize: " + meta.nodesUncompressedSize);
                //System.out.println("meta.attributesUncompressedSize: " + meta.attributesUncompressedSize);
                //System.out.println("meta.valuesUncompressedSize: " + meta.valuesUncompressedSize);
                
                if (compression == CompressionMethod.None) {
                    meta.stringsSizeOnDisk = 0;
                    meta.nodesSizeOnDisk = 0;
                    meta.attributesSizeOnDisk = 0;
                    meta.valuesSizeOnDisk = 0;
                } else {
                    meta.stringsSizeOnDisk = stringsCompressed.length;
                    meta.nodesSizeOnDisk = nodesCompressed.length;
                    meta.attributesSizeOnDisk = attributesCompressed.length;
                    meta.valuesSizeOnDisk = valuesCompressed.length;
                }
                
                meta.compressionFlags = (byte) CompressionHelpers.makeCompressionFlags(compression, compressionLevel);
                meta.unknown2 = 0;
                meta.unknown3 = 0;
                meta.metadataFormat = metadataFormat;
                
                writeStruct(writer, meta);
            } else {
                LSFMetadataV6 meta = new LSFMetadataV6();
                meta.stringsUncompressedSize = stringBuffer.length;
                meta.keysUncompressedSize = keyBuffer.length;
                meta.nodesUncompressedSize = nodeBuffer.length;
                meta.attributesUncompressedSize = attributeBuffer.length;
                meta.valuesUncompressedSize = valueBuffer.length;
                //System.out.println("meta.stringsUncompressedSize: " + meta.stringsUncompressedSize);
                //System.out.println("meta.keysUncompressedSize: " + meta.keysUncompressedSize);
                //System.out.println("meta.nodesUncompressedSize: " + meta.nodesUncompressedSize);
                //System.out.println("meta.attributesUncompressedSize: " + meta.attributesUncompressedSize);
                //System.out.println("meta.valuesUncompressedSize: " + meta.valuesUncompressedSize);
                
                if (compression == CompressionMethod.None) {
                    meta.stringsSizeOnDisk = 0;
                    meta.keysSizeOnDisk = 0;
                    meta.nodesSizeOnDisk = 0;
                    meta.attributesSizeOnDisk = 0;
                    meta.valuesSizeOnDisk = 0;
                } else {
                    meta.stringsSizeOnDisk = stringsCompressed.length;
                    meta.keysSizeOnDisk = keysCompressed.length;
                    meta.nodesSizeOnDisk = nodesCompressed.length;
                    meta.attributesSizeOnDisk = attributesCompressed.length;
                    meta.valuesSizeOnDisk = valuesCompressed.length;
                }
                
                meta.compressionFlags = (byte) CompressionHelpers.makeCompressionFlags(compression, compressionLevel);
                meta.unknown2 = 0;
                meta.unknown3 = 0;
                meta.metadataFormat = metadataFormat;
                
                writeStruct(writer, meta);
            }
            
            writer.write(stringsCompressed);
            writer.write(nodesCompressed);
            writer.write(attributesCompressed);
            writer.write(valuesCompressed);
            writer.write(keysCompressed);
        } finally {
            // Close all streams
            if (writer != null) writer.close();
            if (nodeWriter != null) nodeWriter.close();
            if (attributeWriter != null) attributeWriter.close();
            if (valueWriter != null) valueWriter.close();
            if (keyWriter != null) keyWriter.close();
        }
    }
    
    private int computeSiblingIndices(Node node) {
        int index = nextNodeIndex;
        nextNodeIndex++;
        nextSiblingIndices.add(-1);
        
        int lastSiblingIndex = -1;
        for (List<Node> children : node.children.values()) {
            for (Node child : children) {
                int childIndex = computeSiblingIndices(child);
                if (lastSiblingIndex != -1) {
                    nextSiblingIndices.set(lastSiblingIndex, childIndex);
                }
                lastSiblingIndex = childIndex;
            }
        }
        
        return index;
    }
    
    private void computeSiblingIndices(Resource resource) {
        nextNodeIndex = 0;
        nextSiblingIndices = new ArrayList<>();
        
        int lastRegionIndex = -1;
        for (Region region : resource.regions.values()) {
            int regionIndex = computeSiblingIndices(region);
            if (lastRegionIndex != -1) {
                nextSiblingIndices.set(lastRegionIndex, regionIndex);
            }
            lastRegionIndex = regionIndex;
        }
    }
    
    private void writeRegions(Resource resource) throws IOException {
        nextNodeIndex = 0;
        if (version.getValue() >= LSFVersion.VER_EXTENDED_NODES.getValue() 
                && metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
            //System.out.println("writing regions V3");
        } else {
            //System.out.println("writing regions V2");
        }
        for (Region region : resource.regions.values()) {
            if (version.getValue() >= LSFVersion.VER_EXTENDED_NODES.getValue() 
                && metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
                writeNodeV3(region);
            } else {
                writeNodeV2(region);
            }
        }
    }
    
    private void writeNodeAttributesV2(Node node) throws IOException {
        long lastOffset = valueStream.size();
        for (Map.Entry<String, NodeAttribute> entry : node.attributes.entrySet()) {
            writeAttributeValue(valueWriter, entry.getValue());
            
            LSFAttributeEntryV2 attributeInfo = new LSFAttributeEntryV2();
            long length = valueStream.size() - lastOffset;
            attributeInfo.typeAndLength = (int) entry.getValue().getType().getValue() | ((int) length << 6);
            attributeInfo.nameHashTableIndex = addStaticString(entry.getKey());
            attributeInfo.nodeIndex = nextNodeIndex;
            writeStruct(attributeWriter, attributeInfo);
            nextAttributeIndex++;
            
            lastOffset = valueStream.size();
        }
    }
    
    private void writeNodeAttributesV3(Node node) throws IOException {
        long lastOffset = valueStream.size();
        int numWritten = 0;
        for (Map.Entry<String, NodeAttribute> entry : node.attributes.entrySet()) {
            writeAttributeValue(valueWriter, entry.getValue());
            numWritten++;
            
            LSFAttributeEntryV3 attributeInfo = new LSFAttributeEntryV3();
            long length = valueStream.size() - lastOffset;
            attributeInfo.typeAndLength = (int) entry.getValue().getType().getValue() | ((int) length << 6);
            attributeInfo.nameHashTableIndex = addStaticString(entry.getKey());
            if (numWritten == node.attributes.size()) {
                attributeInfo.nextAttributeIndex = -1;
            } else {
                attributeInfo.nextAttributeIndex = nextAttributeIndex + 1;
            }
            attributeInfo.offset = (int) lastOffset;
            writeStruct(attributeWriter, attributeInfo);
            
            nextAttributeIndex++;
            lastOffset = valueStream.size();
        }
    }
    
    private void writeNodeChildren(Node node) throws IOException {
        for (List<Node> children : node.children.values()) {
            for (Node child : children) {
                if (version.getValue() >= LSFVersion.VER_EXTENDED_NODES.getValue() 
                    && metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
                    writeNodeV3(child);
                } else {
                    writeNodeV2(child);
                }
            }
        }
    }
    
    private void writeNodeV2(Node node) throws IOException {
        LSFNodeEntryV2 nodeInfo = new LSFNodeEntryV2();
        if (node.parent == null) {
            nodeInfo.parentIndex = -1;
        } else {
            nodeInfo.parentIndex = nodeIndices.get(node.parent);
        }
        
        nodeInfo.nameHashTableIndex = addStaticString(node.name);
        
        if (node.attributes.size() > 0) {
            nodeInfo.firstAttributeIndex = nextAttributeIndex;
            writeNodeAttributesV2(node);
        } else {
            nodeInfo.firstAttributeIndex = -1;
        }
        
        writeStruct(nodeWriter, nodeInfo);
        nodeIndices.put(node, nextNodeIndex);
        nextNodeIndex++;
        
        writeNodeChildren(node);
    }
    
    private void writeNodeV3(Node node) throws IOException {
        LSFNodeEntryV3 nodeInfo = new LSFNodeEntryV3();
        if (node.parent == null) {
            nodeInfo.parentIndex = -1;
        } else {
            nodeInfo.parentIndex = nodeIndices.get(node.parent);
        }
        
        nodeInfo.nameHashTableIndex = addStaticString(node.name);
        
        // Assumes we calculated indices first using computeSiblingIndices()
        nodeInfo.nextSiblingIndex = nextSiblingIndices.get(nextNodeIndex);
        
        if (node.attributes.size() > 0) {
            nodeInfo.firstAttributeIndex = nextAttributeIndex;
            writeNodeAttributesV3(node);
        } else {
            nodeInfo.firstAttributeIndex = -1;
        }
        
        writeStruct(nodeWriter, nodeInfo);
        
        if (node.keyAttribute != null && metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
            LSFKeyEntry keyInfo = new LSFKeyEntry();
            keyInfo.nodeIndex = nextNodeIndex;
            keyInfo.keyName = addStaticString(node.keyAttribute);
            writeStruct(keyWriter, keyInfo);
        }
        
        nodeIndices.put(node, nextNodeIndex);
        nextNodeIndex++;
        
        writeNodeChildren(node);
    }
    
    private void writeTranslatedFSString(LittleEndianDataOutputStream writer, TranslatedFSString fs) throws IOException {
        if (version.getValue() >= LSFVersion.VER_BG3.getValue() ||
            (meta.majorVersion > 4 ||
            (meta.majorVersion == 4 && meta.revision > 0) ||
            (meta.majorVersion == 4 && meta.revision == 0 && meta.buildNumber >= 0x1a))) {
            writer.writeInt(fs.version);
        } else {
            writeStringWithLength(writer, fs.value != null ? fs.value : "");
        }
        
        writeStringWithLength(writer, fs.handle);
        
        writer.writeInt(fs.arguments.size());
        for (TranslatedFSStringArgument arg : fs.arguments) {
            writeStringWithLength(writer, arg.key);
            writeTranslatedFSString(writer, arg.string);
            writeStringWithLength(writer, arg.value);
        }
    }
    
    private void writeAttributeValue(LittleEndianDataOutputStream writer, NodeAttribute attr) throws IOException {
        switch (attr.getType()) {
            case String:
            case Path:
            case FixedString:
            case LSString:
            case WString:
            case LSWString:
                writeString(writer, (String) attr.getValue());
                break;
                
            case TranslatedString:
                TranslatedString ts = (TranslatedString) attr.getValue();
                if (version.getValue() >= LSFVersion.VER_BG3.getValue()) {
                    writer.writeShort(ts.version);
                } else {
                    writeStringWithLength(writer, ts.value != null ? ts.value : "");
                }
                writeStringWithLength(writer, ts.handle);
                break;
                
            case TranslatedFSString:
                TranslatedFSString fs = (TranslatedFSString) attr.getValue();
                writeTranslatedFSString(writer, fs);
                break;
                
            case ScratchBuffer:
                byte[] buffer = (byte[]) attr.getValue();
                writer.write(buffer);
                break;
                
            default:
                BinUtils.writeAttribute(writer, attr);
                break;
        }
    }
    
    private int addStaticString(String s) {
        int hashCode = s.hashCode();
        int bucket = (hashCode & 0x1ff) ^ ((hashCode >> 9) & 0x1ff) ^ ((hashCode >> 18) & 0x1ff) ^ ((hashCode >> 27) & 0x1ff);
        for (int i = 0; i < stringHashMap.get(bucket).size(); i++) {
            if (stringHashMap.get(bucket).get(i).equals(s)) {
                return (bucket << 16) | i;
            }
        }
        
        stringHashMap.get(bucket).add(s);
        return (bucket << 16) | (stringHashMap.get(bucket).size() - 1);
    }
    
    private void writeStaticStrings(LittleEndianDataOutputStream writer) throws IOException {
        writer.writeInt(stringHashMap.size());
        for (List<String> entry : stringHashMap) {
            writer.writeShort(entry.size());
            for (String s : entry) {
                writeStaticString(writer, s);
            }
        }
    }
    
    private void writeStaticString(LittleEndianDataOutputStream writer, String s) throws IOException {
        byte[] utf = s.getBytes(StandardCharsets.UTF_8);
        writer.writeShort(utf.length);
        writer.write(utf);
    }
    
    private void writeStringWithLength(LittleEndianDataOutputStream writer, String s) throws IOException {
        byte[] utf = s.getBytes(StandardCharsets.UTF_8);
        writer.writeInt(utf.length + 1);
        writer.write(utf);
        writer.write(0);
    }
    
    private void writeString(LittleEndianDataOutputStream writer, String s) throws IOException {
        byte[] utf = s.getBytes(StandardCharsets.UTF_8);
        writer.write(utf);
        writer.write(0);
    }
    
    private void writeStruct(LittleEndianDataOutputStream writer, Object struct) throws IOException {
        // This is a simplified version - in a real implementation, you'd use reflection
        // or generate specific write methods for each struct type
        if (struct instanceof LSFMagic) {
            LSFMagic magic = (LSFMagic) struct;
            writer.writeInt(magic.magic);
            writer.writeInt(magic.version);
        } else if (struct instanceof LSFHeader) {
            LSFHeader header = (LSFHeader) struct;
            writer.writeInt(header.engineVersion);
        } else if (struct instanceof LSFHeaderV5) {
            LSFHeaderV5 header = (LSFHeaderV5) struct;
            writer.writeLong(header.engineVersion);
        } else if (struct instanceof LSFMetadataV5) {
            LSFMetadataV5 meta = (LSFMetadataV5) struct;
            writer.writeInt(meta.stringsUncompressedSize);
            writer.writeInt(meta.stringsSizeOnDisk);
            writer.writeInt(meta.nodesUncompressedSize);
            writer.writeInt(meta.nodesSizeOnDisk);
            writer.writeInt(meta.attributesUncompressedSize);
            writer.writeInt(meta.attributesSizeOnDisk);
            writer.writeInt(meta.valuesUncompressedSize);
            writer.writeInt(meta.valuesSizeOnDisk);
            writer.writeByte(meta.compressionFlags);
            writer.writeByte(meta.unknown2);
            writer.writeShort(meta.unknown3);
            writer.writeInt(meta.metadataFormat.getValue());
        } else if (struct instanceof LSFMetadataV6) {
            LSFMetadataV6 meta = (LSFMetadataV6) struct;
            writer.writeInt(meta.stringsUncompressedSize);
            writer.writeInt(meta.stringsSizeOnDisk);
            writer.writeInt(meta.keysUncompressedSize);
            writer.writeInt(meta.keysSizeOnDisk);
            writer.writeInt(meta.nodesUncompressedSize);
            writer.writeInt(meta.nodesSizeOnDisk);
            writer.writeInt(meta.attributesUncompressedSize);
            writer.writeInt(meta.attributesSizeOnDisk);
            writer.writeInt(meta.valuesUncompressedSize);
            writer.writeInt(meta.valuesSizeOnDisk);
            writer.writeByte(meta.compressionFlags);
            writer.writeByte(meta.unknown2);
            writer.writeShort(meta.unknown3);
            writer.writeInt(meta.metadataFormat.getValue());
        } else if (struct instanceof LSFNodeEntryV2) {
            LSFNodeEntryV2 node = (LSFNodeEntryV2) struct;
            writer.writeInt(node.nameHashTableIndex);
            writer.writeInt(node.firstAttributeIndex);
            writer.writeInt(node.parentIndex);
        } else if (struct instanceof LSFNodeEntryV3) {
            LSFNodeEntryV3 node = (LSFNodeEntryV3) struct;
            writer.writeInt(node.nameHashTableIndex);
            writer.writeInt(node.parentIndex);
            writer.writeInt(node.nextSiblingIndex);
            writer.writeInt(node.firstAttributeIndex);
        } else if (struct instanceof LSFAttributeEntryV2) {
            LSFAttributeEntryV2 attr = (LSFAttributeEntryV2) struct;
            writer.writeInt(attr.nameHashTableIndex);
            writer.writeInt(attr.typeAndLength);
            writer.writeInt(attr.nodeIndex);
        } else if (struct instanceof LSFAttributeEntryV3) {
            LSFAttributeEntryV3 attr = (LSFAttributeEntryV3) struct;
            writer.writeInt(attr.nameHashTableIndex);
            writer.writeInt(attr.typeAndLength);
            writer.writeInt(attr.nextAttributeIndex);
            writer.writeInt(attr.offset);
        } else if (struct instanceof LSFKeyEntry) {
            LSFKeyEntry key = (LSFKeyEntry) struct;
            writer.writeInt(key.nodeIndex);
            writer.writeInt(key.keyName);
        }
    }
    
    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }
} 
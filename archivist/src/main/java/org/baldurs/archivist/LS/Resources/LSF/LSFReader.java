package org.baldurs.archivist.LS.Resources.LSF;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.baldurs.archivist.LZ4CompressionUtil;
import org.baldurs.archivist.LS.AttributeType;
import org.baldurs.archivist.LS.BinUtils;
import org.baldurs.archivist.LS.InvalidDataException;
import org.baldurs.archivist.LS.Node;
import org.baldurs.archivist.LS.NodeAttribute;
import org.baldurs.archivist.LS.PackedVersion;
import org.baldurs.archivist.LS.Region;
import org.baldurs.archivist.LS.Resource;
import org.baldurs.archivist.LS.TranslatedFSString;
import org.baldurs.archivist.LS.TranslatedFSStringArgument;
import org.baldurs.archivist.LS.TranslatedString;
import org.baldurs.archivist.LS.Enums.CompressionMethod;
import org.baldurs.archivist.LS.Enums.LSCompressionLevel;
import org.baldurs.archivist.LS.Enums.LSFVersion;

/**
 * LSF (Larian Save Format) reader
 * Converted from C# LSFReader.cs
 */
public class LSFReader implements AutoCloseable {

    // Debug flags (equivalent to C# #define)
    private static final boolean DEBUG_LSF_SERIALIZATION = false;
    private static final boolean DUMP_LSF_SERIALIZATION = false;

    /**
     * Input stream
     */
    // private final InputStream stream;
    // private final boolean keepOpen;

    private MappedByteBuffer map;

    /**
     * Static string hash map
     */
    private List<List<String>> names;
    /**
     * Preprocessed list of nodes (structures)
     */
    private List<LSFNodeInfo> nodes;
    /**
     * Preprocessed list of node attributes
     */
    private List<LSFAttributeInfo> attributes;
    /**
     * Node instances
     */
    private List<Node> nodeInstances;
    /**
     * Version of the file we're serializing
     */
    private LSFVersion version;
    /**
     * Game version that generated the LSF file
     */
    private PackedVersion gameVersion;
    private LSFMetadataV6 metadata;

    private ByteBuffer values;

    public LSFReader(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        map.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void close() throws Exception {
        map.clear();
    }

    /**
     * Reads the static string hash table from the specified stream.
     */
    private void readNames(ByteBuffer buffer) throws IOException {

        // Format:
        // 32-bit hash entry count (N)
        // N x 16-bit chain length (L)
        // L x 16-bit string length (S)
        // [S bytes of UTF-8 string data]

        int numHashEntries = buffer.getInt();
        System.out.println("numHashEntries: " + numHashEntries);
        while (numHashEntries-- > 0) {
            List<String> hash = new ArrayList<>();
            names.add(hash);

            short numStrings = buffer.getShort();
            while (numStrings-- > 0) {
                short nameLen = buffer.getShort();
                byte[] bytes = new byte[nameLen];
                buffer.get(bytes);
                String name = new String(bytes, StandardCharsets.UTF_8);
                hash.add(name);
                // System.out.printf("%3X/%d: %s%n", names.size() - 1, hash.size() - 1, name);
            }
        }
    }

    /**
     * Reads the structure headers for the LSOF resource
     */
    private void readNodes(ByteBuffer buffer, boolean longNodes) throws IOException {
        if (DEBUG_LSF_SERIALIZATION) {
            System.out.println(" ----- DUMP OF NODE TABLE -----");
        }

        int index = 0;

        while (buffer.hasRemaining()) {
            System.out.println("Adding node " + index);
            LSFNodeInfo resolved = new LSFNodeInfo();

            if (longNodes) {
                LSFNodeEntryV3 item = LSFNodeEntryV3.fromBuffer(buffer);
                resolved.parentIndex = item.parentIndex;
                resolved.nameIndex = item.getNameIndex();
                resolved.nameOffset = item.getNameOffset();
                resolved.firstAttributeIndex = item.firstAttributeIndex;
            } else {
                LSFNodeEntryV2 item = LSFNodeEntryV2.fromBuffer(buffer);
                resolved.parentIndex = item.parentIndex;
                resolved.nameIndex = item.getNameIndex();
                resolved.nameOffset = item.getNameOffset();
                resolved.firstAttributeIndex = item.firstAttributeIndex;
            }

            if (DEBUG_LSF_SERIALIZATION) {
                System.out.printf("%d: %s @ %X (parent %d, firstAttribute %d)%n",
                        index, names.get(resolved.nameIndex).get(resolved.nameOffset),
                        map.position(), resolved.parentIndex, resolved.firstAttributeIndex);
            }

            nodes.add(resolved);
            index++;
        }
    }

    /**
     * Reads the V2 attribute headers for the LSOF resource
     */
    private void readAttributesV2(ByteBuffer buffer) throws IOException {

        if (DEBUG_LSF_SERIALIZATION) {
            List<LSFAttributeEntryV2> rawAttributes = new ArrayList<>();
        }

        List<Integer> prevAttributeRefs = new ArrayList<>();
        int dataOffset = 0;
        int index = 0;

        while (buffer.hasRemaining()) {
            LSFAttributeEntryV2 attribute = LSFAttributeEntryV2.fromBuffer(buffer);

            LSFAttributeInfo resolved = new LSFAttributeInfo();
            resolved.nameIndex = attribute.getNameIndex();
            resolved.nameOffset = attribute.getNameOffset();
            resolved.typeId = attribute.getTypeId();
            resolved.length = attribute.getLength();
            resolved.dataOffset = dataOffset;
            resolved.nextAttributeIndex = -1;

            int nodeIndex = attribute.nodeIndex + 1;
            if (prevAttributeRefs.size() > nodeIndex) {
                if (prevAttributeRefs.get(nodeIndex) != -1) {
                    attributes.get(prevAttributeRefs.get(nodeIndex)).nextAttributeIndex = index;
                }
                prevAttributeRefs.set(nodeIndex, index);
            } else {
                while (prevAttributeRefs.size() < nodeIndex) {
                    prevAttributeRefs.add(-1);
                }
                prevAttributeRefs.add(index);
            }

            if (DEBUG_LSF_SERIALIZATION) {
                // rawAttributes.add(attribute);
            }

            dataOffset += resolved.length;
            attributes.add(resolved);
            index++;
        }

        if (DEBUG_LSF_SERIALIZATION) {
            System.out.println(" ----- DUMP OF ATTRIBUTE REFERENCES -----");
            for (int i = 0; i < prevAttributeRefs.size(); i++) {
                System.out.printf("Node %d: last attribute %d%n", i, prevAttributeRefs.get(i));
            }

            System.out.println(" ----- DUMP OF V2 ATTRIBUTE TABLE -----");
            for (int i = 0; i < attributes.size(); i++) {
                LSFAttributeInfo resolved = attributes.get(i);
                // LSFAttributeEntryV2 attribute = rawAttributes.get(i);

                String debug = String.format(
                        "%d: %s (offset %X, typeId %d, nextAttribute %d)",
                        i, names.get(resolved.nameIndex).get(resolved.nameOffset),
                        resolved.dataOffset, resolved.typeId, resolved.nextAttributeIndex);
                System.out.println(debug);
            }
        }
    }

    /**
     * Reads the V3 attribute headers for the LSOF resource
     */
    private void readAttributesV3(ByteBuffer buffer) throws IOException {

        while (buffer.hasRemaining()) {
            LSFAttributeEntryV3 attribute = LSFAttributeEntryV3.fromBuffer(buffer);

            LSFAttributeInfo resolved = new LSFAttributeInfo();
            resolved.nameIndex = attribute.getNameIndex();
            resolved.nameOffset = attribute.getNameOffset();
            resolved.typeId = attribute.getTypeId();
            resolved.length = attribute.getLength();
            resolved.dataOffset = attribute.offset;
            resolved.nextAttributeIndex = attribute.nextAttributeIndex;

            attributes.add(resolved);
        }

        if (DEBUG_LSF_SERIALIZATION) {
            System.out.println(" ----- DUMP OF V3 ATTRIBUTE TABLE -----");
            for (int i = 0; i < attributes.size(); i++) {
                LSFAttributeInfo resolved = attributes.get(i);

                String debug = String.format(
                        "%d: %s (offset %X, typeId %d, length %d, nextAttribute %d)",
                        i, names.get(resolved.nameIndex).get(resolved.nameOffset),
                        resolved.dataOffset, resolved.typeId, resolved.length, resolved.nextAttributeIndex);
                System.out.println(debug);
            }
        }
    }

    /**
     * Reads the key attribute definitions
     */
    private void readKeys(ByteBuffer buffer) throws IOException {

        if (DEBUG_LSF_SERIALIZATION) {
            System.out.println(" ----- DUMP OF KEY TABLE -----");
        }

        while (buffer.hasRemaining()) {
            LSFKeyEntry key = LSFKeyEntry.fromBuffer(buffer);
            String keyAttribute = names.get(key.getKeyNameIndex()).get(key.getKeyNameOffset());
            System.out.println("keyAttribute: " + keyAttribute);
            LSFNodeInfo node = nodes.get((int) key.nodeIndex);
            node.keyAttribute = keyAttribute;

            if (DEBUG_LSF_SERIALIZATION) {
                String debug = String.format(
                        "%d (%s): %s",
                        key.nodeIndex, names.get(node.nameIndex).get(node.nameOffset), keyAttribute);
                System.out.println(debug);
            }
        }
    }

    private ByteBuffer decompress(int sizeOnDisk, int uncompressedSize,
            String debugDumpTo, boolean allowChunked) throws IOException {
        if (sizeOnDisk == 0 && uncompressedSize != 0) {
            System.out.println("data is not compressed");
            byte[] buf = new byte[uncompressedSize];
            map.get(buf);

            if (DUMP_LSF_SERIALIZATION) {
                try (FileOutputStream nodesFile = new FileOutputStream(debugDumpTo)) {
                    nodesFile.write(buf);
                }
            }

            ByteBuffer bufBuf = ByteBuffer.wrap(buf);
            bufBuf.order(ByteOrder.LITTLE_ENDIAN);
            return bufBuf;
        }

        if (sizeOnDisk == 0 && uncompressedSize == 0) { // no data
            return ByteBuffer.wrap(new byte[0]);
        }

        CompressionMethod compressionMethod = CompressionMethod.fromValue(metadata.compressionFlags);
        if (compressionMethod != CompressionMethod.LZ4 && compressionMethod != CompressionMethod.None) {
            throw new InvalidDataException(
                    "Unsupported compression method: " + CompressionMethod.fromValue(metadata.compressionFlags));
        }

        boolean chunked = (version.getValue() >= LSFVersion.VER_CHUNKED_COMPRESS.getValue() && allowChunked);
        //System.out.println("Compression Level: " + LSCompressionLevel.fromValue(metadata.compressionFlags));
        //System.out.println("Compression Method: " + compressionMethod);
        //System.out.println("chunked: " + chunked);
        boolean isCompressed = compressionMethod != CompressionMethod.None;
        int compressedSize = isCompressed ? sizeOnDisk : uncompressedSize;
        byte[] compressed = new byte[compressedSize];
        byte[] buf = new byte[uncompressedSize];
        map.get(compressed);
        // LZ4CompressionUtil.decompress(compressed, buf);

        if (chunked) {
            System.out.println("decompressing with CLI");
            int bytesRead = LZ4CompressionUtil.decompressCLI(compressed, buf);
            System.out.println("frame bytesRead: " + bytesRead);
        } else {
            int bytesRead = LZ4CompressionUtil.decompress(compressed, buf);
            System.out.println("bytesRead: " + bytesRead);
        }

        ByteBuffer bufBuf = ByteBuffer.wrap(buf);
        bufBuf.order(ByteOrder.LITTLE_ENDIAN);
        return bufBuf;
    }

    private void readHeaders(ByteBuffer buffer) throws IOException {
        LSFMagic magic = LSFMagic.fromBuffer(buffer);

        // Convert signature to int for comparison
        int expectedSignature = ByteBuffer.wrap(LSFCommon.SIGNATURE).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (magic.magic != expectedSignature) {
            String msg = String.format(
                    "Invalid LSF signature; expected %08X, got %08X",
                    expectedSignature, magic.magic);
            throw new InvalidDataException(msg);
        }

        if (magic.version < LSFVersion.VER_INITIAL.getValue() ||
                magic.version > LSFVersion.MAX_READ_VERSION.getValue()) {
            String msg = String.format("LSF version %d is not supported", magic.version);
            throw new InvalidDataException(msg);
        }

        version = LSFVersion.fromInt((int) magic.version);

        if (version.getValue() >= LSFVersion.VER_BG3_EXTENDED_HEADER.getValue()) {
            LSFHeaderV5 hdr = LSFHeaderV5.fromBuffer(map);
            gameVersion = PackedVersion.fromInt64(hdr.engineVersion);

            // Workaround for merged LSF files with missing engine version number
            if (gameVersion.major == 0) {
                gameVersion.major = 4;
                gameVersion.minor = 0;
                gameVersion.revision = 9;
                gameVersion.build = 0;
            }
        } else {
            LSFHeader hdr = LSFHeader.fromBuffer(map);
            gameVersion = PackedVersion.fromInt32(hdr.engineVersion);
        }
        System.out.println("version: " + version);
        if (version.getValue() < LSFVersion.VER_BG3_NODE_KEYS.getValue()) {
            LSFMetadataV5 meta = LSFMetadataV5.fromBuffer(map);
            metadata = new LSFMetadataV6();
            metadata.stringsUncompressedSize = meta.stringsUncompressedSize;
            metadata.stringsSizeOnDisk = meta.stringsSizeOnDisk;
            metadata.nodesUncompressedSize = meta.nodesUncompressedSize;
            metadata.nodesSizeOnDisk = meta.nodesSizeOnDisk;
            metadata.attributesUncompressedSize = meta.attributesUncompressedSize;
            metadata.attributesSizeOnDisk = meta.attributesSizeOnDisk;
            metadata.valuesUncompressedSize = meta.valuesUncompressedSize;
            metadata.valuesSizeOnDisk = meta.valuesSizeOnDisk;
            metadata.compressionFlags = meta.compressionFlags;
            metadata.metadataFormat = meta.metadataFormat;
        } else {
            metadata = LSFMetadataV6.fromBuffer(map);
        }
    }

    public Resource read() throws IOException {
        readHeaders(map);

        System.out.println("stringsSizeOnDisk: " + metadata.stringsSizeOnDisk);
        System.out.println("stringsUncompressedSize: " + metadata.stringsUncompressedSize);
        System.out.println("nodesSizeOnDisk: " + metadata.nodesSizeOnDisk);
        System.out.println("nodesUncompressedSize: " + metadata.nodesUncompressedSize);
        System.out.println("attributesSizeOnDisk: " + metadata.attributesSizeOnDisk);
        System.out.println("attributesUncompressedSize: " + metadata.attributesUncompressedSize);
        System.out.println("valuesSizeOnDisk: " + metadata.valuesSizeOnDisk);
        System.out.println("--------------------------------");
        names = new ArrayList<>();
        System.out.println("reading names");
        ByteBuffer namesStream = decompress(metadata.stringsSizeOnDisk,
                metadata.stringsUncompressedSize, "strings.bin", false);
        readNames(namesStream);

        nodes = new ArrayList<>();

        System.out.println("reading nodes");
        ByteBuffer nodesStream = decompress(metadata.nodesSizeOnDisk,
                metadata.nodesUncompressedSize, "nodes.bin", true);
        boolean hasAdjacencyData = version.getValue() >= LSFVersion.VER_EXTENDED_NODES.getValue()
                && metadata.metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY;
        readNodes(nodesStream, hasAdjacencyData);

        System.out.println("reading attributes");
        attributes = new ArrayList<>();
        ByteBuffer attributesStream = decompress(metadata.attributesSizeOnDisk,
                metadata.attributesUncompressedSize, "attributes.bin", true);
        hasAdjacencyData = version.getValue() >= LSFVersion.VER_EXTENDED_NODES.getValue()
                && metadata.metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY;
        if (hasAdjacencyData) {
            readAttributesV3(attributesStream);
        } else {
            readAttributesV2(attributesStream);
        }

        System.out.println("reading values");
        this.values = decompress(metadata.valuesSizeOnDisk,
                metadata.valuesUncompressedSize, "values.bin", true);

        if (metadata.metadataFormat == LSFMetadataFormat.KEYS_AND_ADJACENCY) {
            ByteBuffer keysStream = decompress(metadata.keysSizeOnDisk,
                    metadata.keysUncompressedSize, "keys.bin", true);
        System.out.println("reading keys");
        readKeys(keysStream);
        }

        Resource resource = new Resource();
        resource.metadataFormat = metadata.metadataFormat;
        System.out.println("reading regions");
        readRegions(resource, this.values);

        resource.metadata.majorVersion = gameVersion.major;
        resource.metadata.minorVersion = gameVersion.minor;
        resource.metadata.revision = gameVersion.revision;
        resource.metadata.buildNumber = gameVersion.build;

        return resource;
    }

    private void readRegions(Resource resource, ByteBuffer attrReader) throws IOException {
        nodeInstances = new ArrayList<>();
        System.out.println("nodes.size(): " + nodes.size());
 
        for (int i = 0; i < nodes.size(); i++) {
            //System.out.println("i: " + i);
            LSFNodeInfo defn = nodes.get(i);
            //System.out.println("defn.parentIndex: " + defn.parentIndex);
            //System.out.println("defn.nameIndex: " + defn.nameIndex);
            //System.out.println("defn.nameOffset: " + defn.nameOffset);
            //System.out.println("defn.firstAttributeIndex: " + defn.firstAttributeIndex);
            //System.out.println("defn.keyAttribute: " + defn.keyAttribute);
            System.out.println("--------------------------------");
            if (defn.parentIndex == -1) {
                Region region = new Region();
                readNode(defn, region, attrReader);
                region.keyAttribute = defn.keyAttribute;
                nodeInstances.add(region);
                region.regionName = region.name;
                resource.regions.put(region.name, region);
            } else {
                Node node = new Node();
                readNode(defn, node, attrReader);
                node.keyAttribute = defn.keyAttribute;
                System.out.println("parentIndex: " + defn.parentIndex);
                node.parent = nodeInstances.get(defn.parentIndex);
                nodeInstances.get(defn.parentIndex).appendChild(node);
                nodeInstances.add(node);
            }
        }
    }

    private void readNode(LSFNodeInfo defn, Node node, ByteBuffer attributeReader) throws IOException {
        node.name = names.get(defn.nameIndex).get(defn.nameOffset);
        System.out.printf("Begin node %s%n", node.name);
        System.out.printf("Defn firstAttributeIndex: %d%n", defn.firstAttributeIndex);

        if (DEBUG_LSF_SERIALIZATION) {
            // NodeSerializationSettings debugSerializationSettings = new
            // NodeSerializationSettings();
        }

        if (defn.firstAttributeIndex != -1) {
            LSFAttributeInfo attribute = attributes.get(defn.firstAttributeIndex);
            System.out.printf("Attribute nameIndex: %d, nameOffset: %d%n, nextAttributeIndex: %d\n",
                    attribute.nameIndex, attribute.nameOffset, attribute.nextAttributeIndex);
            while (true) {
                // Note: In Java we need to handle stream positioning differently
                // This is a simplified approach - in practice you'd need to implement
                // proper stream positioning or use a different approach

                AttributeType type = AttributeType.fromInt((int) attribute.typeId);
                System.out.println("Attribute type: " + type);
                if (type != AttributeType.None) {
                    NodeAttribute value = readAttribute(type,
                            attributeReader, attribute.length);
                    List<String> list = names.get(attribute.nameIndex);
                    if (list == null) {
                        System.out.printf("list is null for nameIndex: %d%n", attribute.nameIndex);
                    }
                    if (list.isEmpty()) {
                        System.out.printf("list is empty for nameIndex: %d%n", attribute.nameIndex);
                    }
                    String string = list.get(attribute.nameOffset);
                    node.attributes.put(string, value);
                }

                if (DEBUG_LSF_SERIALIZATION) {
                    // System.out.printf(" %X: %s (%s)%n", attribute.dataOffset,
                    // names.get(attribute.nameIndex).get(attribute.nameOffset),
                    // value.asString());
                }

                if (attribute.nextAttributeIndex == -1) {
                    break;
                } else {
                    attribute = attributes.get(attribute.nextAttributeIndex);
                }
            }
        }
    }

    private NodeAttribute readAttribute(AttributeType type, ByteBuffer reader, int length) throws IOException {
        // LSF and LSB serialize the buffer types differently, so specialized
        // code is added to the LSB and LSF serializers, and the common code is
        // available in BinUtils.ReadAttribute()
        switch (type) {
            case String:
            case Path:
            case FixedString:
            case LSString:
            case WString:
            case LSWString:
                NodeAttribute attr = new NodeAttribute(type);
                attr.setValue(readString(reader, length));
                return attr;

            case TranslatedString:
                NodeAttribute attr2 = new NodeAttribute(type);
                TranslatedString str = new TranslatedString();

                if (version.getValue() >= LSFVersion.VER_BG3.getValue() ||
                        (gameVersion.major > 4 ||
                                (gameVersion.major == 4 && gameVersion.revision > 0) ||
                                (gameVersion.major == 4 && gameVersion.revision == 0 && gameVersion.build >= 0x1a))) {
                    str.version = reader.getShort();
                } else {
                    str.version = 0;
                    int valueLength = reader.getInt();
                    str.value = readString(reader, valueLength);
                }

                int handleLength = reader.getInt();
                str.handle = readString(reader, handleLength);

                attr2.setValue(str);
                return attr2;

            case TranslatedFSString:
                NodeAttribute attr3 = new NodeAttribute(type);
                attr3.setValue(readTranslatedFSString(reader));
                return attr3;

            case ScratchBuffer:
                NodeAttribute attr4 = new NodeAttribute(type);
                byte[] bytes = new byte[length];
                reader.get(bytes);
                attr4.setValue(bytes);
                return attr4;

            default:
                return BinUtils.readAttribute(type, reader);
        }
    }

    private TranslatedFSString readTranslatedFSString(ByteBuffer reader) throws IOException {
        TranslatedFSString str = new TranslatedFSString();

        if (version.getValue() >= LSFVersion.VER_BG3.getValue()) {
            str.version = reader.getShort();
        } else {
            str.version = 0;
            int valueLength = reader.getInt();
            str.value = readString(reader, valueLength);
        }

        int handleLength = reader.getInt();
        str.handle = readString(reader, handleLength);

        int arguments = reader.getInt();
        str.arguments = new ArrayList<>(arguments);
        for (int i = 0; i < arguments; i++) {
            TranslatedFSStringArgument arg = new TranslatedFSStringArgument();
            int argKeyLength = reader.getInt();
            arg.key = readString(reader, argKeyLength);

            arg.string = readTranslatedFSString(reader);

            int argValueLength = reader.getInt();
            arg.value = readString(reader, argValueLength);

            str.arguments.add(arg);
        }

        return str;
    }

    private String readString(ByteBuffer reader, int length) throws IOException {
        byte[] bytes = new byte[length - 1];
        reader.get(bytes);

        // Remove null bytes at the end of the string
        int lastNull = bytes.length;
        while (lastNull > 0 && bytes[lastNull - 1] == 0) {
            lastNull--;
        }

        byte nullTerminator = reader.get();
        if (nullTerminator != 0) {
            throw new InvalidDataException("String is not null-terminated");
        }

        return new String(bytes, 0, lastNull, StandardCharsets.UTF_8);
    }

    private String readString(ByteBuffer reader) throws IOException {
        List<Byte> bytes = new ArrayList<>();
        while (true) {
            byte b = reader.get();
            if (b != 0) {
                bytes.add(b);
            } else {
                break;
            }
        }

        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }

        return new String(byteArray, StandardCharsets.UTF_8);
    }

}

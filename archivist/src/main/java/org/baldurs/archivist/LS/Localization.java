package org.baldurs.archivist.LS;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Localization utilities for reading/writing .loca and .xml files
 * Ported from C# Localization.cs
 */
public class Localization {

    /**
     * Header structure for .loca files
     */
    public static class LocaHeader {
        public static final int DEFAULT_SIGNATURE = 0x41434f4c; // 'LOCA'
        
        public int signature;
        public int numEntries;
        public int textsOffset;
        
        public static int getSize() {
            return 12; // 3 ints * 4 bytes each
        }
    }

    /**
     * Entry structure for .loca files
     */
    public static class LocaEntry {
        public byte[] key = new byte[64];
        public short version;
        public int length;
        
        public String getKeyString() {
            int nameLen;
            for (nameLen = 0; nameLen < key.length && key[nameLen] != 0; nameLen++) {}
            return new String(key, 0, nameLen, StandardCharsets.UTF_8);
        }
        
        public void setKeyString(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            key = new byte[64];
            System.arraycopy(bytes, 0, key, 0, Math.min(bytes.length, 64));
        }
        
        public static int getSize() {
            return 70; // 64 bytes + 2 bytes + 4 bytes
        }
    }

    /**
     * Localized text entry
     */
    public static class LocalizedText {
        public String key;
        public short version;
        public String text;
        
        public LocalizedText() {}
        
        public LocalizedText(String key, short version, String text) {
            this.key = key;
            this.version = version;
            this.text = text;
        }
    }

    /**
     * Localization resource containing multiple entries
     */
    public static class LocaResource {
        public List<LocalizedText> entries = new ArrayList<>();
    }

    /**
     * Reader for .loca files
     */
    public static class LocaReader implements AutoCloseable {
        private final InputStream stream;
        
        public LocaReader(InputStream stream) {
            this.stream = stream;
        }
        
        @Override
        public void close() throws IOException {
            stream.close();
        }
        
        public LocaResource read() throws IOException {
            DataInputStream reader = new DataInputStream(stream);
            LocaResource loca = new LocaResource();
            
            // Read header
            LocaHeader header = new LocaHeader();
            header.signature = Integer.reverseBytes(reader.readInt());
            header.numEntries = Integer.reverseBytes(reader.readInt());
            header.textsOffset = Integer.reverseBytes(reader.readInt());
            
            if (header.signature != LocaHeader.DEFAULT_SIGNATURE) {
                throw new InvalidDataException("Incorrect signature in localization file");
            }
            
            // Read entries
            LocaEntry[] entries = new LocaEntry[header.numEntries];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = new LocaEntry();
                reader.readFully(entries[i].key);
                entries[i].version = Short.reverseBytes(reader.readShort());
                entries[i].length = Integer.reverseBytes(reader.readInt());
            }
            
            // Seek to texts section
            long currentPos = LocaHeader.getSize() + (long) LocaEntry.getSize() * entries.length;
            if (currentPos != header.textsOffset) {
                // Skip to the correct position
                long skipBytes = header.textsOffset - currentPos;
                if (skipBytes > 0) {
                    reader.skipBytes((int) skipBytes);
                }
            }
            
            // Read text entries
            for (LocaEntry entry : entries) {
                byte[] textBytes = new byte[entry.length - 1];
                reader.readFully(textBytes);
                String text = new String(textBytes, StandardCharsets.UTF_8);
                
                loca.entries.add(new LocalizedText(
                    entry.getKeyString(),
                    entry.version,
                    text
                ));
                
                // Skip null terminator
                reader.readByte();
            }
            
            return loca;
        }
    }

    /**
     * Writer for .loca files
     */
    public static class LocaWriter {
        private final OutputStream stream;
        
        public LocaWriter(OutputStream stream) {
            this.stream = stream;
        }
        
        public void write(LocaResource res) throws IOException {
            DataOutputStream writer = new DataOutputStream(stream);
            
            // Write header
            LocaHeader header = new LocaHeader();
            header.signature = LocaHeader.DEFAULT_SIGNATURE;
            header.numEntries = res.entries.size();
            header.textsOffset = LocaHeader.getSize() + LocaEntry.getSize() * res.entries.size();
            
            writer.writeInt(Integer.reverseBytes(header.signature));
            writer.writeInt(Integer.reverseBytes(header.numEntries));
            writer.writeInt(Integer.reverseBytes(header.textsOffset));
            
            // Write entries
            for (LocalizedText entry : res.entries) {
                LocaEntry locaEntry = new LocaEntry();
                locaEntry.setKeyString(entry.key);
                locaEntry.version = entry.version;
                locaEntry.length = entry.text.getBytes(StandardCharsets.UTF_8).length + 1;
                
                writer.write(locaEntry.key);
                writer.writeShort(Short.reverseBytes(locaEntry.version));
                writer.writeInt(Integer.reverseBytes(locaEntry.length));
            }
            
            // Write text entries
            for (LocalizedText entry : res.entries) {
                byte[] bin = entry.text.getBytes(StandardCharsets.UTF_8);
                writer.write(bin);
                writer.write(0); // null terminator
            }
        }
    }

    /**
     * Reader for .xml files
     */
    public static class LocaXmlReader implements AutoCloseable {
        private final InputStream stream;
        
        public LocaXmlReader(InputStream stream) {
            this.stream = stream;
        }
        
        @Override
        public void close() throws IOException {
            stream.close();
        }
        
        public LocaResource read() throws IOException {
            LocaResource resource = new LocaResource();
            
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(stream);
                
                Element root = document.getDocumentElement();
                if (!"contentList".equals(root.getTagName())) {
                    throw new InvalidFormatException("Root element must be 'contentList'");
                }
                
                NodeList contentNodes = root.getElementsByTagName("content");
                for (int i = 0; i < contentNodes.getLength(); i++) {
                    Element contentElement = (Element) contentNodes.item(i);
                    String key = contentElement.getAttribute("contentuid");
                    String versionStr = contentElement.getAttribute("version");
                    short version = versionStr != null && !versionStr.isEmpty() ? 
                        Short.parseShort(versionStr) : 1;
                    String text = contentElement.getTextContent();
                    
                    resource.entries.add(new LocalizedText(key, version, text));
                }
                
            } catch (ParserConfigurationException | SAXException e) {
                throw new InvalidFormatException("Failed to parse XML: " + e.getMessage());
            }
            
            return resource;
        }
    }

    /**
     * Writer for .xml files
     */
    public static class LocaXmlWriter {
        private final OutputStream stream;
        
        public LocaXmlWriter(OutputStream stream) {
            this.stream = stream;
        }
        
        public void write(LocaResource res) throws IOException {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.newDocument();
                
                Element root = document.createElement("contentList");
                document.appendChild(root);
                
                for (LocalizedText entry : res.entries) {
                    Element contentElement = document.createElement("content");
                    contentElement.setAttribute("contentuid", entry.key);
                    contentElement.setAttribute("version", String.valueOf(entry.version));
                    contentElement.setTextContent(entry.text);
                    root.appendChild(contentElement);
                }
                
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(stream);
                transformer.transform(source, result);
                
            } catch (ParserConfigurationException | TransformerException e) {
                throw new IOException("Failed to write XML: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Supported localization file formats
     */
    public enum LocaFormat {
        Loca,
        Xml
    }

    /**
     * Utility methods for localization operations
     */
    public static class LocaUtils {
        
        /**
         * Determine file format from extension
         */
        public static LocaFormat extensionToFileFormat(String path) {
            String extension = getFileExtension(path).toLowerCase();
            
            switch (extension) {
                case ".loca":
                    return LocaFormat.Loca;
                case ".xml":
                    return LocaFormat.Xml;
                default:
                    throw new IllegalArgumentException("Unrecognized file extension: " + extension);
            }
        }
        
        /**
         * Load localization resource from file
         */
        public static LocaResource load(String inputPath) throws IOException {
            return load(inputPath, extensionToFileFormat(inputPath));
        }
        
        /**
         * Load localization resource from file with specified format
         */
        public static LocaResource load(String inputPath, LocaFormat format) throws IOException {
            try (InputStream stream = Files.newInputStream(Paths.get(inputPath))) {
                return load(stream, format);
            }
        }
        
        /**
         * Load localization resource from stream
         */
        public static LocaResource load(InputStream stream, LocaFormat format) throws IOException {
            switch (format) {
                case Loca:
                    try (LocaReader reader = new LocaReader(stream)) {
                        return reader.read();
                    }
                    
                case Xml:
                    try (LocaXmlReader reader = new LocaXmlReader(stream)) {
                        return reader.read();
                    }
                    
                default:
                    throw new IllegalArgumentException("Invalid loca format");
            }
        }
        
        /**
         * Save localization resource to file
         */
        public static void save(LocaResource resource, String outputPath) throws IOException {
            save(resource, outputPath, extensionToFileFormat(outputPath));
        }
        
        /**
         * Save localization resource to file with specified format
         */
        public static void save(LocaResource resource, String outputPath, LocaFormat format) throws IOException {
            // Ensure directory exists
            Path path = Paths.get(outputPath);
            Files.createDirectories(path.getParent());
            
            try (OutputStream file = Files.newOutputStream(path)) {
                switch (format) {
                    case Loca:
                        LocaWriter writer = new LocaWriter(file);
                        writer.write(resource);
                        break;
                        
                    case Xml:
                        LocaXmlWriter xmlWriter = new LocaXmlWriter(file);
                        xmlWriter.write(resource);
                        break;
                        
                    default:
                        throw new IllegalArgumentException("Invalid loca format");
                }
            }
        }
        
        /**
         * Get file extension from path
         */
        private static String getFileExtension(String path) {
            int lastDot = path.lastIndexOf('.');
            return lastDot >= 0 ? path.substring(lastDot) : "";
        }
    }
} 
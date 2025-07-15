package org.baldurs.archivist.LS.Resources.LSX;

import org.baldurs.archivist.LS.*;
import org.baldurs.archivist.LS.Enums.LSXVersion;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * LSX Writer class for writing LSX format files
 * Ported from C# LSXWriter.cs
 */
public class LSXWriter {
    private final OutputStream stream;
    private XMLStreamWriter writer;

    public boolean prettyPrint = false;
    public LSXVersion version = LSXVersion.V4;
    public NodeSerializationSettings serializationSettings = new NodeSerializationSettings();

    private static class PrettyPrintingXMLStreamWriter implements XMLStreamWriter {
        private XMLStreamWriter writer;
        private int indentLevel = 0;
        private static final String INDENT_STRING = "    ";
        private boolean isStartElement = false;

        public PrettyPrintingXMLStreamWriter(XMLStreamWriter writer) {
            this.writer = writer;
        }

        private void writeIndent() throws XMLStreamException {
            for (int i = 0; i < indentLevel; i++) {
                writer.writeCharacters(INDENT_STRING);
            }
        }
        
        private void writeNewline() throws XMLStreamException {
            writer.writeCharacters("\n");
        }
        
        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeStartElement(localName);
            indentLevel++;
        }
        
        @Override
        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeStartElement(namespaceURI, localName);
            indentLevel++;
        }
        
        @Override
        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeStartElement(prefix, localName, namespaceURI);
            indentLevel++;
        }
        
        @Override
        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeEmptyElement(namespaceURI, localName);
        }
        
        @Override
        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeEmptyElement(prefix, localName, namespaceURI);
        }
        
        @Override
        public void writeEmptyElement(String localName) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeEmptyElement(localName);
        }
        
        @Override
        public void writeEndElement() throws XMLStreamException {
            indentLevel--;
            writeNewline();
            writeIndent();
            writer.writeEndElement();
        }
        
        @Override
        public void writeEndDocument() throws XMLStreamException {
            writer.writeEndDocument();
        }
        
        @Override
        public void close() throws XMLStreamException {
            writer.close();
        }
        
        @Override
        public void flush() throws XMLStreamException {
            writer.flush();
        }
        
        @Override
        public void writeAttribute(String localName, String value) throws XMLStreamException {
            writer.writeAttribute(localName, value);
        }
        
        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        }
        
        @Override
        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            writer.writeAttribute(namespaceURI, localName, value);
        }
        
        @Override
        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            writer.writeNamespace(prefix, namespaceURI);
        }
        
        @Override
        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            writer.writeDefaultNamespace(namespaceURI);
        }
        
        @Override
        public void writeComment(String data) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeComment(data);
        }
        
        @Override
        public void writeProcessingInstruction(String target) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeProcessingInstruction(target);
        }
        
        @Override
        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            writeNewline();
            writeIndent();
            writer.writeProcessingInstruction(target, data);
        }
        
        @Override
        public void writeCData(String data) throws XMLStreamException {
            writer.writeCData(data);
        }
        
        @Override
        public void writeDTD(String dtd) throws XMLStreamException {
            writer.writeDTD(dtd);
        }
        
        @Override
        public void writeEntityRef(String name) throws XMLStreamException {
            writer.writeEntityRef(name);
        }
        
        @Override
        public void writeStartDocument() throws XMLStreamException {
            writer.writeStartDocument();
        }
        
        @Override
        public void writeStartDocument(String version) throws XMLStreamException {
            writer.writeStartDocument(version);
        }
        
        @Override
        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            writer.writeStartDocument(encoding, version);
        }
        
        @Override
        public void writeCharacters(String text) throws XMLStreamException {
            writer.writeCharacters(text);
        }
        
        @Override
        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            writer.writeCharacters(text, start, len);
        }
        
        @Override
        public String getPrefix(String uri) throws XMLStreamException {
            return writer.getPrefix(uri);
        }
        
        @Override
        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            writer.setPrefix(prefix, uri);
        }
        
        @Override
        public void setDefaultNamespace(String uri) throws XMLStreamException {
            writer.setDefaultNamespace(uri);
        }
        
        @Override
        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            writer.setNamespaceContext(context);
        }
        
        @Override
        public NamespaceContext getNamespaceContext() {
            return writer.getNamespaceContext();
        }
        
        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return writer.getProperty(name);
        }
    }

    public LSXWriter(OutputStream stream) {
        this.stream = stream;
    }

    private void prepareWrite(Long majorVersion) throws XMLStreamException {
        if (version == LSXVersion.V3 && majorVersion != null && majorVersion == 4) {
            throw new InvalidDataException("Cannot resave a BG3 (v4.x) resource in D:OS2 (v3.x) file format, maybe you have the wrong game selected?");
        }

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        writer = factory.createXMLStreamWriter(stream, "UTF-8");
        
        if (prettyPrint) {
            writer = new PrettyPrintingXMLStreamWriter(writer);
        }
    }

    public void write(Resource rsrc) throws XMLStreamException, IOException {
        System.out.println("Writing resource: " + rsrc.metadata);
        prepareWrite(rsrc.metadata.majorVersion);
        try {
            writer.writeStartElement("save");

            writer.writeStartElement("version");

            writer.writeAttribute("major", String.valueOf(rsrc.metadata.majorVersion));
            writer.writeAttribute("minor", String.valueOf(rsrc.metadata.minorVersion));
            writer.writeAttribute("revision", String.valueOf(rsrc.metadata.revision));
            writer.writeAttribute("build", String.valueOf(rsrc.metadata.buildNumber));
            writer.writeAttribute("lslib_meta", serializationSettings.buildMeta());
            writer.writeEndElement(); // version

            writeRegions(rsrc);

            writer.writeEndElement(); // save
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void write(Node node) throws XMLStreamException, IOException {
        prepareWrite(null);
        try {
            writeNode(node);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeRegions(Resource rsrc) throws XMLStreamException {
        for (Map.Entry<String, Region> entry : rsrc.regions.entrySet()) {
            writer.writeStartElement("region");
            writer.writeAttribute("id", entry.getKey());
            writeNode(entry.getValue());
            writer.writeEndElement(); // region
        }
    }

    private void writeTranslatedFSString(TranslatedFSString fs) throws XMLStreamException {
        writer.writeStartElement("string");
        writer.writeAttribute("value", fs.value);
        writeTranslatedFSStringInner(fs);
        writer.writeEndElement(); // string
    }

    private void writeTranslatedFSStringInner(TranslatedFSString fs) throws XMLStreamException {
        writer.writeAttribute("handle", fs.handle);
        writer.writeAttribute("arguments", String.valueOf(fs.arguments.size()));

        if (fs.arguments != null && fs.arguments.size() > 0) {
            writer.writeStartElement("arguments");
            for (int i = 0; i < fs.arguments.size(); i++) {
                TranslatedFSStringArgument argument = fs.arguments.get(i);
                writer.writeStartElement("argument");
                writer.writeAttribute("key", argument.key);
                writer.writeAttribute("value", argument.value);
                writeTranslatedFSString(argument.string);
                writer.writeEndElement(); // argument
            }
            writer.writeEndElement(); // arguments
        }
    }

    private void writeNode(Node node) throws XMLStreamException {
        writer.writeStartElement("node");
        writer.writeAttribute("id", node.name);

        if (node.keyAttribute != null) {
            writer.writeAttribute("key", node.keyAttribute);
        }

        for (Map.Entry<String, NodeAttribute> entry : node.attributes.entrySet()) {
            String key = entry.getKey();
            NodeAttribute attribute = entry.getValue();
            
            writer.writeStartElement("attribute");
            writer.writeAttribute("id", key);
            
            if (version.getValue() >= LSXVersion.V4.getValue()) {
                writer.writeAttribute("type", AttributeTypeMaps.ID_TO_TYPE.get(attribute.getType()));
            } else {
                writer.writeAttribute("type", String.valueOf(attribute.getType().ordinal()));
            }

            if (attribute.getType() == AttributeType.TranslatedString) {
                TranslatedString ts = (TranslatedString) attribute.getValue();
                writer.writeAttribute("handle", ts.handle);
                if (ts.value != null) {
                    writer.writeAttribute("value", ts.toString());
                } else {
                    writer.writeAttribute("version", String.valueOf(ts.version));
                }
            } else if (attribute.getType() == AttributeType.TranslatedFSString) {
                TranslatedFSString fs = (TranslatedFSString) attribute.getValue();
                writer.writeAttribute("value", fs.value);
                writeTranslatedFSStringInner(fs);
            } else {
                // Replace bogus 001F characters found in certain LSF nodes
                String value = attribute.asString(serializationSettings).replace("\u001f", "");
                writer.writeAttribute("value", value);
            }

            writer.writeEndElement(); // attribute
        }

        if (node.getChildCount() > 0) {
            writer.writeStartElement("children");
            for (Map.Entry<String, List<Node>> entry : node.children.entrySet()) {
                for (Node child : entry.getValue()) {
                    writeNode(child);
                }
            }
            writer.writeEndElement(); // children
        }

        writer.writeEndElement(); // node
    }
} 
package org.baldurs.archivist.LS.Resources.LSX;

import org.baldurs.archivist.LS.*;
import org.baldurs.archivist.LS.Enums.LSXVersion;
import org.baldurs.archivist.LS.Resources.LSF.LSFMetadataFormat;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * LSX Reader class for parsing LSX format files
 */
public class LSXReader implements AutoCloseable {
    private final InputStream stream;
    private XMLStreamReader reader;
    private Resource resource;
    private Region currentRegion;
    private List<Node> stack;
    public int lastLine, lastColumn;
    private LSXVersion version = LSXVersion.V3;
    public NodeSerializationSettings serializationSettings = new NodeSerializationSettings();
    private NodeAttribute lastAttribute = null;
    private int valueOffset = 0;

    public LSXReader(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }

    private void readTranslatedFSString(TranslatedFSString fs) throws XMLStreamException {
        fs.value = reader.getAttributeValue(null, "value");
        fs.handle = reader.getAttributeValue(null, "handle");
        assert fs.handle != null;

        int arguments = Integer.parseInt(reader.getAttributeValue(null, "arguments"));
        fs.arguments = new ArrayList<>(arguments);
        
        if (arguments > 0) {
            // Skip to arguments element
            while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT);
            if (!"arguments".equals(reader.getLocalName())) {
                throw new InvalidFormatException("Expected <arguments>: " + reader.getLocalName());
            }

            int processedArgs = 0;
            while (processedArgs < arguments && reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    if (!"argument".equals(reader.getLocalName())) {
                        throw new InvalidFormatException("Expected <argument>: " + reader.getLocalName());
                    }

                    TranslatedFSStringArgument arg = new TranslatedFSStringArgument();
                    arg.key = reader.getAttributeValue(null, "key");
                    arg.value = reader.getAttributeValue(null, "value");

                    // Skip to string element
                    while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT);
                    if (!"string".equals(reader.getLocalName())) {
                        throw new InvalidFormatException("Expected <string>: " + reader.getLocalName());
                    }

                    arg.string = new TranslatedFSString();
                    readTranslatedFSString(arg.string);

                    fs.arguments.add(arg);
                    processedArgs++;

                    // Skip to end of argument
                    while (reader.hasNext() && reader.next() != XMLStreamConstants.END_ELEMENT);
                }
            }

            // Skip to end of arguments
            while (reader.hasNext() && reader.next() != XMLStreamConstants.END_ELEMENT);
            // Close outer element
            while (reader.hasNext() && reader.next() != XMLStreamConstants.END_ELEMENT);
            assert processedArgs == arguments;
        }
    }

    private void readElement() throws XMLStreamException {
        String elementName = reader.getLocalName();
        
        switch (elementName) {
            case "save":
                // Root element
                if (stack.size() > 0)
                    throw new InvalidFormatException("Node <save> was unexpected.");
                break;

            case "header":
                // LSX metadata part 1
                resource.metadata.timestamp = Long.parseUnsignedLong(reader.getAttributeValue(null, "time"));
                break;

            case "version":
                // LSX metadata part 2
                resource.metadata.majorVersion = Integer.parseUnsignedInt(reader.getAttributeValue(null, "major"));
                resource.metadata.minorVersion = Integer.parseUnsignedInt(reader.getAttributeValue(null, "minor"));
                resource.metadata.revision = Integer.parseUnsignedInt(reader.getAttributeValue(null, "revision"));
                resource.metadata.buildNumber = Integer.parseUnsignedInt(reader.getAttributeValue(null, "build"));
                version = (resource.metadata.majorVersion >= 4) ? LSXVersion.V4 : LSXVersion.V3;
                String lslibMeta = reader.getAttributeValue(null, "lslib_meta");
                serializationSettings.initFromMeta(lslibMeta != null ? lslibMeta : "");
                resource.metadataFormat = serializationSettings.lsfMetadata;
                break;

            case "region":
                if (currentRegion != null)
                    throw new InvalidFormatException("A <region> can only start at the root level of a resource.");

                assert reader.getEventType() == XMLStreamConstants.START_ELEMENT;
                Region region = new Region();
                region.regionName = reader.getAttributeValue(null, "id");
                assert region.regionName != null;
                resource.regions.put(region.regionName, region);
                currentRegion = region;
                break;

            case "node":
                if (currentRegion == null)
                    throw new InvalidFormatException("A <node> must be located inside a region.");

                Node node;
                if (stack.size() == 0) {
                    // The node is the root node of the region
                    node = currentRegion;
                } else {
                    // New node under the current parent
                    node = new Node();
                    node.parent = stack.get(stack.size() - 1);
                    node.line = reader.getLocation().getLineNumber();
                }

                node.name = reader.getAttributeValue(null, "id");
                assert node.name != null;
                if (node.parent != null) {
                    node.parent.appendChild(node);
                }

                node.keyAttribute = reader.getAttributeValue(null, "key");

                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    stack.add(node);
                }
                break;

            case "attribute":
                int attrTypeId;
                String typeStr = reader.getAttributeValue(null, "type");
                if (!typeStr.matches("\\d+")) {
                    attrTypeId = AttributeTypeMaps.TYPE_TO_ID.get(typeStr).getValue();
                } else {
                    attrTypeId = Integer.parseInt(typeStr);
                }

                String attrName = reader.getAttributeValue(null, "id");
                if (attrTypeId > 33) // TranslatedFSString is the highest value
                    throw new InvalidFormatException("Unsupported attribute data type: " + attrTypeId);

                assert attrName != null;
                NodeAttribute attr = new NodeAttribute(AttributeType.fromInt(attrTypeId));
                attr.line = reader.getLocation().getLineNumber();

                String attrValue = reader.getAttributeValue(null, "value");
                if (attrValue != null) {
                    attr.fromString(attrValue, serializationSettings);
                } else {
                    // Preallocate value for vector/matrix types
                    switch (attr.getType()) {
                        case Vec2: attr.setValue(new float[2]); break;
                        case Vec3: attr.setValue(new float[3]); break;
                        case Vec4: attr.setValue(new float[4]); break;
                        case Mat2: attr.setValue(new float[2*2]); break;
                        case Mat3: attr.setValue(new float[3*3]); break;
                        case Mat3x4: attr.setValue(new float[3*4]); break;
                        case Mat4: attr.setValue(new float[4*4]); break;
                        case Mat4x3: attr.setValue(new float[4*3]); break;
                        case TranslatedString: break;
                        case TranslatedFSString: break;
                        default: throw new RuntimeException("Attribute of type " + attr.getType() + " should have an inline value!");
                    }

                    valueOffset = 0;
                    lastAttribute = attr;
                }

                if (attr.getType() == AttributeType.TranslatedString) {
                    if (attr.getValue() == null) {
                        attr.setValue(new TranslatedString());
                    }

                    TranslatedString ts = (TranslatedString) attr.getValue();
                    ts.handle = reader.getAttributeValue(null, "handle");
                    if (ts.handle == null) {
                        throw new InvalidFormatException("TranslatedString handle is null for attribute " + attrName);
                    }

                    if (attrValue == null) {
                        ts.version = Short.parseShort(reader.getAttributeValue(null, "version"));
                    }
                } else if (attr.getType() == AttributeType.TranslatedFSString) {
                    TranslatedFSString fs = (TranslatedFSString) attr.getValue();
                    readTranslatedFSString(fs);
                }

                stack.get(stack.size() - 1).attributes.put(attrName, attr);
                break;

            case "float2":
                {
                    float[] val = (float[]) lastAttribute.getValue();
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                    break;
                }

            case "float3":
                {
                    float[] val = (float[]) lastAttribute.getValue();
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "z"));
                    break;
                }

            case "float4":
                {
                    float[] val = (float[]) lastAttribute.getValue();
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "z"));
                    val[valueOffset++] = Float.parseFloat(reader.getAttributeValue(null, "w"));
                    break;
                }

            case "mat2":
            case "mat3":
            case "mat4":
                // These are read in the float2/3/4 nodes
                break;

            case "children":
                // Child nodes are handled in the "node" case
                break;

            default:
                throw new InvalidFormatException("Unknown element encountered: " + elementName);
        }
    }

    private void readEndElement() {
        String elementName = reader.getLocalName();
        
        switch (elementName) {
            case "save":
            case "header":
            case "version":
            case "attribute":
            case "children":
                // These elements don't change the stack, just discard them
                break;

            case "region":
                assert stack.size() == 0;
                assert currentRegion != null;
                assert currentRegion.regionName != null;
                currentRegion = null;
                break;

            case "node":
                stack.remove(stack.size() - 1);
                break;

            // Value nodes, processed in readElement()
            case "float2":
            case "float3":
            case "float4":
            case "mat2":
            case "mat3":
            case "mat4":
                break;

            default:
                throw new InvalidFormatException("Unknown element encountered: " + elementName);
        }
    }

    private void readInternal() throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        reader = factory.createXMLStreamReader(stream);
        
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    readElement();
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    readEndElement();
                }
            }
        } catch (Exception e) {
            lastLine = reader.getLocation().getLineNumber();
            lastColumn = reader.getLocation().getColumnNumber();
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public Resource read() {
        resource = new Resource();
        currentRegion = null;
        stack = new ArrayList<>();
        lastLine = lastColumn = 0;
        Resource resultResource = resource;

        try {
            readInternal();
        } catch (Exception e) {
            if (lastLine > 0) {
                throw new RuntimeException("Parsing error at or near line " + lastLine + ", column " + lastColumn + ":\n" + e.getMessage(), e);
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            resource = null;
            currentRegion = null;
            stack = null;
        }

        return resultResource;
    }
} 
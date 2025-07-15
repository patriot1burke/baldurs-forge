package org.baldurs.archivist.LS;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;
import java.util.UUID;

/**
 * Node attribute class with type handling and serialization
 * Converted from C# NodeAttribute.cs
 */
public class NodeAttribute {
    
    private final AttributeType type;
    private Object value;
    public Integer line = null;
    
    public NodeAttribute(AttributeType type) {
        this.type = type;
    }
    
    public AttributeType getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException("toString() is not safe to use anymore, use asString(settings) instead");
    }
    
    public static UUID byteSwapGuid(UUID g) {
        byte[] bytes = new byte[16];
        long msb = g.getMostSignificantBits();
        long lsb = g.getLeastSignificantBits();
        
        // Convert to byte array
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >> (8 * (7 - i)));
            bytes[i + 8] = (byte) (lsb >> (8 * (7 - i)));
        }
        
        // Swap bytes 8-15 in pairs
        for (int i = 8; i < 16; i += 2) {
            byte temp = bytes[i];
            bytes[i] = bytes[i + 1];
            bytes[i + 1] = temp;
        }
        
        // Convert back to UUID
        long newMsb = 0;
        long newLsb = 0;
        for (int i = 0; i < 8; i++) {
            newMsb = (newMsb << 8) | (bytes[i] & 0xff);
            newLsb = (newLsb << 8) | (bytes[i + 8] & 0xff);
        }
        
        return new UUID(newMsb, newLsb);
    }
    
    public String asString(NodeSerializationSettings settings) {
        switch (type) {
            case ScratchBuffer:
                // ScratchBuffer is a special case, as its stored as byte[] and ToString() doesn't really do what we want
                return Base64.getEncoder().encodeToString((byte[]) value);
                
            case IVec2:
            case IVec3:
            case IVec4:
                return Arrays.stream((int[]) value)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" "));
                
            case Vec2:
            case Vec3:
            case Vec4:
                float[] floatArray = (float[]) value;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < floatArray.length; i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(floatArray[i]);
                }
                return sb.toString();
                
            case UUID:
                if (settings.byteSwapGuids) {
                    return byteSwapGuid((UUID) value).toString();
                } else {
                    return value.toString();
                }
                
            default:
                return value.toString();
        }
    }
    
    public UUID asGuid(NodeSerializationSettings settings) {
        return asGuid(settings.byteSwapGuids);
    }
    
    public UUID asGuid() {
        return asGuid(true);
    }
    
    public UUID asGuid(boolean byteSwapGuids) {
        switch (type) {
            case UUID:
                return (UUID) value;
                
            case String:
            case FixedString:
            case LSString:
                if (byteSwapGuids) {
                    return byteSwapGuid(UUID.fromString((String) value));
                } else {
                    return UUID.fromString((String) value);
                }
                
            default:
                throw new UnsupportedOperationException("Type not convertible to GUID");
        }
    }
    
    public void fromString(String str, NodeSerializationSettings settings) {
        value = parseFromString(str, type, settings.byteSwapGuids);
    }
    
    public static Object parseFromString(String str, AttributeType type, boolean byteSwapGuids) {
        if (type.isNumeric()) {
            // Workaround: Some XML files use empty strings, instead of "0" for zero values.
            if (str.isEmpty()) {
                str = "0";
            }
            // Handle hexadecimal integers in XML files
            else if (str.length() > 2 && str.startsWith("0x")) {
                str = String.valueOf(Long.parseUnsignedLong(str.substring(2), 16));
            }
        }
        
        switch (type) {
            case None:
                // This is a null type, cannot have a value
                return null;
                
            case Byte:
                return Byte.parseByte(str);
                
            case Short:
                return Short.parseShort(str);
                
            case UShort:
                return Integer.parseUnsignedInt(str);
                
            case Int:
                return Integer.parseInt(str);
                
            case UInt:
                return Long.parseUnsignedLong(str);
                
            case Float:
                return Float.parseFloat(str);
                
            case Double:
                return Double.parseDouble(str);
                
            case IVec2:
            case IVec3:
            case IVec4:
                String[] nums = str.split(" ");
                int length = type.getColumns();
                if (length != nums.length) {
                    throw new IllegalArgumentException(String.format("A vector of length %d was expected, got %d", length, nums.length));
                }
                
                int[] vec = new int[length];
                for (int i = 0; i < length; i++) {
                    vec[i] = Integer.parseInt(nums[i]);
                }
                return vec;
                
            case Vec2:
            case Vec3:
            case Vec4:
                String[] nums2 = str.split(" ");
                int length2 = type.getColumns();
                if (length2 != nums2.length) {
                    throw new IllegalArgumentException(String.format("A vector of length %d was expected, got %d", length2, nums2.length));
                }
                
                float[] vec2 = new float[length2];
                for (int i = 0; i < length2; i++) {
                    vec2[i] = Float.parseFloat(nums2[i]);
                }
                return vec2;
                
            case Mat2:
            case Mat3:
            case Mat3x4:
            case Mat4x3:
            case Mat4:
                Matrix mat = Matrix.parse(str);
                if (mat.cols != type.getColumns() || mat.rows != type.getRows()) {
                    throw new IllegalArgumentException("Invalid column/row count for matrix");
                }
                return mat;
                
            case Bool:
                if (str.equals("0")) return false;
                else if (str.equals("1")) return true;
                else return Boolean.parseBoolean(str);
                
            case String:
            case Path:
            case FixedString:
            case LSString:
            case WString:
            case LSWString:
                return str;
                
            case TranslatedString:
                // We'll only set the value part of the translated string, not the TranslatedStringKey / Handle part
                // That can be changed separately via attribute.Value.Handle
                TranslatedString value = new TranslatedString();
                value.value = str;
                return value;
                
            case TranslatedFSString:
                // We'll only set the value part of the translated string, not the TranslatedStringKey / Handle part
                // That can be changed separately via attribute.Value.Handle
                TranslatedFSString value2 = new TranslatedFSString();
                value2.value = str;
                return value2;
                
            case ULongLong:
                return Long.parseUnsignedLong(str);
                
            case ScratchBuffer:
                return Base64.getDecoder().decode(str);
                
            case Long:
            case Int64:
                return Long.parseLong(str);
                
            case Int8:
                return Byte.parseByte(str);
                
            case UUID:
                if (byteSwapGuids) {
                    return byteSwapGuid(UUID.fromString(str));
                } else {
                    return UUID.fromString(str);
                }
                
            default:
                // This should not happen!
                throw new UnsupportedOperationException(String.format("fromString() not implemented for type %s", type));
        }
    }
} 
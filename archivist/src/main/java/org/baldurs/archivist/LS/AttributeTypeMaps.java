package org.baldurs.archivist.LS;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute type mapping utilities
 */
public class AttributeTypeMaps {
    public static final Map<String, AttributeType> TYPE_TO_ID = new HashMap<>();
    public static final Map<AttributeType, String> ID_TO_TYPE = new HashMap<>();

    static {
        TYPE_TO_ID.put("None", AttributeType.None);
        TYPE_TO_ID.put("uint8", AttributeType.Byte);
        TYPE_TO_ID.put("int16", AttributeType.Short);
        TYPE_TO_ID.put("uint16", AttributeType.UShort);
        TYPE_TO_ID.put("int32", AttributeType.Int);
        TYPE_TO_ID.put("uint32", AttributeType.UInt);
        TYPE_TO_ID.put("float", AttributeType.Float);
        TYPE_TO_ID.put("double", AttributeType.Double);
        TYPE_TO_ID.put("ivec2", AttributeType.IVec2);
        TYPE_TO_ID.put("ivec3", AttributeType.IVec3);
        TYPE_TO_ID.put("ivec4", AttributeType.IVec4);
        TYPE_TO_ID.put("fvec2", AttributeType.Vec2);
        TYPE_TO_ID.put("fvec3", AttributeType.Vec3);
        TYPE_TO_ID.put("fvec4", AttributeType.Vec4);
        TYPE_TO_ID.put("mat2x2", AttributeType.Mat2);
        TYPE_TO_ID.put("mat3x3", AttributeType.Mat3);
        TYPE_TO_ID.put("mat3x4", AttributeType.Mat3x4);
        TYPE_TO_ID.put("mat4x3", AttributeType.Mat4x3);
        TYPE_TO_ID.put("mat4x4", AttributeType.Mat4);
        TYPE_TO_ID.put("bool", AttributeType.Bool);
        TYPE_TO_ID.put("string", AttributeType.String);
        TYPE_TO_ID.put("path", AttributeType.Path);
        TYPE_TO_ID.put("FixedString", AttributeType.FixedString);
        TYPE_TO_ID.put("LSString", AttributeType.LSString);
        TYPE_TO_ID.put("uint64", AttributeType.ULongLong);
        TYPE_TO_ID.put("ScratchBuffer", AttributeType.ScratchBuffer);
        TYPE_TO_ID.put("old_int64", AttributeType.Long);
        TYPE_TO_ID.put("int8", AttributeType.Int8);
        TYPE_TO_ID.put("TranslatedString", AttributeType.TranslatedString);
        TYPE_TO_ID.put("WString", AttributeType.WString);
        TYPE_TO_ID.put("LSWString", AttributeType.LSWString);
        TYPE_TO_ID.put("guid", AttributeType.UUID);
        TYPE_TO_ID.put("int64", AttributeType.Int64);
        TYPE_TO_ID.put("TranslatedFSString", AttributeType.TranslatedFSString);

        ID_TO_TYPE.put(AttributeType.None, "None");
        ID_TO_TYPE.put(AttributeType.Byte, "uint8");
        ID_TO_TYPE.put(AttributeType.Short, "int16");
        ID_TO_TYPE.put(AttributeType.UShort, "uint16");
        ID_TO_TYPE.put(AttributeType.Int, "int32");
        ID_TO_TYPE.put(AttributeType.UInt, "uint32");
        ID_TO_TYPE.put(AttributeType.Float, "float");
        ID_TO_TYPE.put(AttributeType.Double, "double");
        ID_TO_TYPE.put(AttributeType.IVec2, "ivec2");
        ID_TO_TYPE.put(AttributeType.IVec3, "ivec3");
        ID_TO_TYPE.put(AttributeType.IVec4, "ivec4");
        ID_TO_TYPE.put(AttributeType.Vec2, "fvec2");
        ID_TO_TYPE.put(AttributeType.Vec3, "fvec3");
        ID_TO_TYPE.put(AttributeType.Vec4, "fvec4");
        ID_TO_TYPE.put(AttributeType.Mat2, "mat2x2");
        ID_TO_TYPE.put(AttributeType.Mat3, "mat3x3");
        ID_TO_TYPE.put(AttributeType.Mat3x4, "mat3x4");
        ID_TO_TYPE.put(AttributeType.Mat4x3, "mat4x3");
        ID_TO_TYPE.put(AttributeType.Mat4, "mat4x4");
        ID_TO_TYPE.put(AttributeType.Bool, "bool");
        ID_TO_TYPE.put(AttributeType.String, "string");
        ID_TO_TYPE.put(AttributeType.Path, "path");
        ID_TO_TYPE.put(AttributeType.FixedString, "FixedString");
        ID_TO_TYPE.put(AttributeType.LSString, "LSString");
        ID_TO_TYPE.put(AttributeType.ULongLong, "uint64");
        ID_TO_TYPE.put(AttributeType.ScratchBuffer, "ScratchBuffer");
        ID_TO_TYPE.put(AttributeType.Long, "old_int64");
        ID_TO_TYPE.put(AttributeType.Int8, "int8");
        ID_TO_TYPE.put(AttributeType.TranslatedString, "TranslatedString");
        ID_TO_TYPE.put(AttributeType.WString, "WString");
        ID_TO_TYPE.put(AttributeType.LSWString, "LSWString");
        ID_TO_TYPE.put(AttributeType.UUID, "guid");
        ID_TO_TYPE.put(AttributeType.Int64, "int64");
        ID_TO_TYPE.put(AttributeType.TranslatedFSString, "TranslatedFSString");
    }
} 
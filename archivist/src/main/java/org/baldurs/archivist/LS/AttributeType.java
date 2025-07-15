package org.baldurs.archivist.LS;

/**
 * Attribute type enumeration
 */
public enum AttributeType {
    None(0),
    Byte(1),
    Short(2),
    UShort(3),
    Int(4),
    UInt(5),
    Float(6),
    Double(7),
    IVec2(8),
    IVec3(9),
    IVec4(10),
    Vec2(11),
    Vec3(12),
    Vec4(13),
    Mat2(14),
    Mat3(15),
    Mat3x4(16),
    Mat4x3(17),
    Mat4(18),
    Bool(19),
    String(20),
    Path(21),
    FixedString(22),
    LSString(23),
    ULongLong(24),
    ScratchBuffer(25),
    // Seems to be unused?
    Long(26),
    Int8(27),
    TranslatedString(28),
    WString(29),
    LSWString(30),
    UUID(31),
    Int64(32),
    TranslatedFSString(33);
    
    private final int value;
    
    AttributeType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static AttributeType fromInt(int value) {
        for (AttributeType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return None;
    }
    
    public int getRows() {
        switch (this) {
            case IVec2:
            case IVec3:
            case IVec4:
            case Vec2:
            case Vec3:
            case Vec4:
                return 1;
                
            case Mat2:
                return 2;
                
            case Mat3:
            case Mat3x4:
                return 3;
                
            case Mat4x3:
            case Mat4:
                return 4;
                
            default:
                throw new UnsupportedOperationException("Data type does not have rows");
        }
    }
    
    public int getColumns() {
        switch (this) {
            case IVec2:
            case Vec2:
            case Mat2:
                return 2;
                
            case IVec3:
            case Vec3:
            case Mat3:
            case Mat4x3:
                return 3;
                
            case IVec4:
            case Vec4:
            case Mat3x4:
            case Mat4:
                return 4;
                
            default:
                throw new UnsupportedOperationException("Data type does not have columns");
        }
    }
    
    public boolean isNumeric() {
        return this == Byte
            || this == Short
            || this == UShort
            || this == Int
            || this == UInt
            || this == Float
            || this == Double
            || this == ULongLong
            || this == Long
            || this == Int8;
    }
} 
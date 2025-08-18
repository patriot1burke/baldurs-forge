package org.baldurs.archivist.LS;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * Binary utilities for reading/writing binary data
 * Ported from C# BinUtils.cs
 */
public class BinUtils {
    
    /**
     * Read-only substream that provides a view into a portion of another stream
     */
    public static class ReadOnlySubstream extends InputStream {
        private final InputStream sourceStream;
        private final long fileOffset;
        private final long size;
        private long curPosition = 0;
        
        public ReadOnlySubstream(InputStream sourceStream, long offset, long size) {
            this.sourceStream = sourceStream;
            this.fileOffset = offset;
            this.size = size;
        }
        
        @Override
        public int read() throws IOException {
            if (curPosition >= size) {
                return -1;
            }
            
            // For single byte reads, we need to seek and read
            if (sourceStream instanceof FileInputStream) {
                FileInputStream fis = (FileInputStream) sourceStream;
                FileChannel channel = fis.getChannel();
                channel.position(fileOffset + curPosition);
            }
            
            int result = sourceStream.read();
            if (result != -1) {
                curPosition++;
            }
            return result;
        }
        
        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            if (curPosition >= size) {
                return -1;
            }
            
            long readable = size - curPosition;
            int bytesToRead = (readable < count) ? (int) readable : count;
            
            // Seek to the correct position
            if (sourceStream instanceof FileInputStream) {
                FileInputStream fis = (FileInputStream) sourceStream;
                FileChannel channel = fis.getChannel();
                channel.position(fileOffset + curPosition);
            }
            
            int read = sourceStream.read(buffer, offset, bytesToRead);
            if (read > 0) {
                curPosition += read;
            }
            return read;
        }
        
        @Override
        public long skip(long n) throws IOException {
            long skipAmount = Math.min(n, size - curPosition);
            curPosition += skipAmount;
            return skipAmount;
        }
        
        @Override
        public int available() throws IOException {
            return (int) Math.min(size - curPosition, Integer.MAX_VALUE);
        }
        
        @Override
        public void close() throws IOException {
            // Don't close the source stream
        }
        
        public long getLength() {
            return size;
        }
        
        public long getPosition() {
            return curPosition;
        }
    }
    
    /**
     * Convert null-terminated bytes to string
     */
    public static String nullTerminatedBytesToString(byte[] b) {
        int len;
        for (len = 0; len < b.length && b[len] != 0; len++) {}
        return new String(b, 0, len, StandardCharsets.UTF_8);
    }
    
    /**
     * Convert string to null-terminated bytes
     */
    public static byte[] stringToNullTerminatedBytes(String s, int length) {
        byte[] b = new byte[length];
        byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(stringBytes.length, length);
        System.arraycopy(stringBytes, 0, b, 0, len);
        // Clear the rest of the array (already zeroed by default)
        return b;
    }
    
    /**
     * Read an attribute from a BinaryReader
     */
    public static NodeAttribute readAttribute(AttributeType type, ByteBuffer reader) throws IOException {
        NodeAttribute attr = new NodeAttribute(type);
        
        switch (type) {
            case None:
                break;
                
            case Byte:
                attr.setValue(reader.get());
                break;
                
            case Short:
                attr.setValue(reader.getShort());
                break;
                
            case UShort:
                attr.setValue(reader.getShort());
                break;
                
            case Int:
                attr.setValue(reader.getInt());
                break;
                
            case UInt:
                attr.setValue(reader.getInt()); // Read as unsigned
                break;
                
            case Float:
                attr.setValue(reader.getFloat());
                break;
                
            case Double:
                attr.setValue(reader.getDouble());
                break;
                
            case IVec2:
            case IVec3:
            case IVec4:
                {
                    int columns = attr.getType().getColumns();
                    int[] vec = new int[columns];
                    for (int i = 0; i < columns; i++) {
                        vec[i] = reader.getInt();
                    }
                    attr.setValue(vec);
                    break;
                }
                
            case Vec2:
            case Vec3:
            case Vec4:
                {
                    int columns = attr.getType().getColumns();
                    float[] vec = new float[columns];
                    for (int i = 0; i < columns; i++) {
                        vec[i] = reader.getFloat();
                    }
                    attr.setValue(vec);
                    break;
                }
                
            case Mat2:
            case Mat3:
            case Mat3x4:
            case Mat4x3:
            case Mat4:
                {
                    int columns = attr.getType().getColumns();
                    int rows = attr.getType().getRows();
                    Matrix mat = new Matrix(rows, columns);
                    attr.setValue(mat);
                    
                    for (int col = 0; col < columns; col++) {
                        for (int row = 0; row < rows; row++) {
                            mat.data[row * columns + col] = reader.getFloat();
                        }
                    }
                    break;
                }
                
            case Bool:
                attr.setValue(reader.get() != 0);
                break;
                
            case ULongLong:
                throw new InvalidFormatException("ULongLong is not supported");
                // attr.setValue(reader.getLong());
                // break;
                
            case Long:
            case Int64:
                attr.setValue(reader.getLong());
                break;
                
            case Int8:
                attr.setValue(reader.get());
                break;
                
            case UUID:
                byte[] uuidBytes = new byte[16];
                reader.get(uuidBytes);
                ByteBuffer bb = ByteBuffer.wrap(uuidBytes);
                long mostSigBits = bb.getLong();
                long leastSigBits = bb.getLong();
                attr.setValue(new UUID(mostSigBits, leastSigBits));
                break;
                
            default:
                // Strings are serialized differently for each file format and should be
                // handled by the format-specific ReadAttribute()
                throw new InvalidFormatException(String.format("ReadAttribute() not implemented for type %s", type));
        }
        
        return attr;
    }
    
    /**
     * Write an attribute to a BinaryWriter
     */
    public static void writeAttribute(DataOutput writer, NodeAttribute attr) throws IOException {
        switch (attr.getType()) {
            case None:
                break;
                
            case Byte:
                writer.writeByte((Byte) attr.getValue());
                break;
                
            case Short:
                writer.writeShort((Short) attr.getValue());
                break;
                
            case UShort:
                writer.writeShort((Short) attr.getValue());
                break;
                
            case Int:
                writer.writeInt((Integer) attr.getValue());
                break;
                
            case UInt:
                writer.writeInt((Integer) attr.getValue());
                break;
                
            case Float:
                writer.writeFloat((Float) attr.getValue());
                break;
                
            case Double:
                writer.writeDouble((Double) attr.getValue());
                break;
                
            case IVec2:
            case IVec3:
            case IVec4:
                for (int item : (int[]) attr.getValue()) {
                    writer.writeInt(item);
                }
                break;
                
            case Vec2:
            case Vec3:
            case Vec4:
                for (float item : (float[]) attr.getValue()) {
                    writer.writeFloat(item);
                }
                break;
                
            case Mat2:
            case Mat3:
            case Mat3x4:
            case Mat4x3:
            case Mat4:
                {
                    Matrix mat = (Matrix) attr.getValue();
                    for (int col = 0; col < mat.cols; col++) {
                        for (int row = 0; row < mat.rows; row++) {
                            writer.writeFloat(mat.data[row * mat.cols + col]);
                        }
                    }
                    break;
                }
                
            case Bool:
                writer.writeByte((Boolean) attr.getValue() ? 1 : 0);
                break;
                
            case ULongLong:
                throw new InvalidFormatException("ULongLong is not supported");
                
            case Long:
            case Int64:
                writer.writeLong((Long) attr.getValue());
                break;
                
            case Int8:
                writer.writeByte((Byte) attr.getValue());
                break;
                
            case UUID:
                UUID uuid = (UUID) attr.getValue();
                ByteBuffer bb = ByteBuffer.allocate(16);
                bb.putLong(uuid.getMostSignificantBits());
                bb.putLong(uuid.getLeastSignificantBits());
                writer.write(bb.array());
                break;
                
            default:
                throw new InvalidFormatException(String.format("WriteAttribute() not implemented for type %s", attr.getType()));
        }
    }
} 
package org.baldurs.archivist;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

public class PackageReader {

    private static final int SIGNATURE = 0x4B50534C;
    private int fileListOffset;

    public static class PackageHeader {

        public int Version;
        public long FileListOffset;
        public int FileListSize;
        public byte Flags;
        public byte Priority;
        public byte[] Md5 = new byte[16];

    
        public short NumParts;

        public static final int SIZE = 36;

        PackageHeader(MappedByteBuffer map, int offset) {
            Version = map.getInt(offset + 0);
            FileListOffset = map.getLong(offset + 4);
            FileListSize = map.getInt(offset + 12);
            Flags = map.get(offset + 16);
            Priority = map.get(offset + 17);
            for (int i = 0; i < Md5.length; i++) {
                Md5[i] = map.get(offset + 18 + i);
            }
            NumParts = map.getShort(offset + 34);
        }
        public String toString() {
            return "Header(Version: " + Version + ", FileListOffset: " + FileListOffset + ", FileListSize: " + FileListSize + ", Flags: " + Flags + ", Priority: " + Priority + ", Md5: " + Arrays.toString(Md5) + ", NumParts: " + NumParts + ")";
        }
    }

    public static class FileEntry {
        /*
[MarshalAs(UnmanagedType.ByValArray, SizeConst = 256)]
    public byte[] Name;

    public UInt32 OffsetInFile1;
    public UInt16 OffsetInFile2;
    public Byte ArchivePart;
    public Byte Flags;
    public UInt32 SizeOnDisk;
    public UInt32 UncompressedSize;         */

        public String Name;
        public int OffsetInFile1;
        public short OffsetInFile2;
        public byte ArchivePart;
        public byte Flags;
        public int SizeOnDisk;
        public int UncompressedSize;
        public long offsetInFile;

        public FileEntry() {
        }

        public FileEntry(ByteBuffer buffer) {
            byte[] nameBuffer = new byte[256];
            buffer.get(nameBuffer);
            int i;
            for (i = 0; i < nameBuffer.length; i++) {
                if (nameBuffer[i] == 0) {
                    break;
                }
            }

             Name = new String(nameBuffer, 0, i, StandardCharsets.UTF_8);
            
            
            OffsetInFile1 = buffer.getInt();
            OffsetInFile2 = buffer.getShort();
            ArchivePart = buffer.get();
            Flags = buffer.get();
            SizeOnDisk = buffer.getInt();
            UncompressedSize = buffer.getInt();
            offsetInFile = OffsetInFile1 | (OffsetInFile2 << 32);
        }

        public String toString() {
            return "FileEntry(Name: " + Name + ", OffsetInFile1: " + OffsetInFile1 + ", OffsetInFile2: " + OffsetInFile2 + ", ArchivePart: " + ArchivePart + ", Flags: " + Flags + ", SizeOnDisk: " + SizeOnDisk + ", UncompressedSize: " + UncompressedSize + ")";
        }

        public static final int SIZE = 256 + 4 + 2 + 1 + 1 + 4 + 4;

        public static final int MethodNone = 0;
        public static final int MethodZlib = 1;
        public static final int MethodLZ4 = 2;
        public static final int MethodZstd = 3;
        public static final int FastCompress = 0x10;
        public static final int DefaultCompress = 0x20;
        public static final int MaxCompress = 0x40;
    }

    private Path pakPath;
    private PackageHeader header;
    private FileEntry[] fileList;


    public PackageReader(Path pakPath) {
        this.pakPath = pakPath;
        try {
            FileChannel channel = FileChannel.open(pakPath, StandardOpenOption.READ);
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            map.order(ByteOrder.LITTLE_ENDIAN);
            //System.out.println("Channel size: " + channel.size());
            int signature = map.getInt(0);
            
            if (signature != 0x4B50534C) {
                System.err.printf("Signature: 0x%08X%n", signature);
                throw new RuntimeException("Invalid signature");
            }
            int version = map.getInt(4);
            //System.out.println("Version: " + version);
            header = new PackageHeader(map, (int)4);
            System.out.println("Header: " + header);
            fileListOffset = (int)header.FileListOffset;
            LZ4Factory factory = LZ4Factory.fastestInstance();
            LZ4SafeDecompressor decompressor = factory.safeDecompressor();

            //System.out.println("Reading compressed file list");
            int numFiles = map.getInt(fileListOffset);
            //System.out.println("Num files: " + numFiles);
            int compressedSize = map.getInt(fileListOffset + 4);
            //System.out.println("Compressed filelist size: " + compressedSize);
            byte[] compressedFileList = new byte[compressedSize];
            for (int i = 0; i < compressedFileList.length; i++) {
                int index = fileListOffset + 8 + i;
                compressedFileList[i] = map.get(index);
            }
            byte[] decompressedFileList = new byte[FileEntry.SIZE * numFiles];
            decompressor.decompress(compressedFileList, 0, compressedFileList.length, decompressedFileList, 0, decompressedFileList.length);
            ByteBuffer fileListBuf = ByteBuffer.wrap(decompressedFileList);
            fileListBuf.order(ByteOrder.LITTLE_ENDIAN);
            fileList = new FileEntry[numFiles];
            for (int i = 0; i < numFiles; i++) {
                FileEntry fileEntry = new FileEntry(fileListBuf);
                //System.out.println("File entry: " + fileEntry.Name);
                fileList[i] = fileEntry;
            }
            channel.close();
        } catch (Exception e) {   
           throw new RuntimeException(e);
        }
    }

    public void extract(Path outputDir) throws Exception {
        FileChannel channel = FileChannel.open(pakPath, StandardOpenOption.READ);
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        map.order(ByteOrder.LITTLE_ENDIAN);
        LZ4Factory factory = LZ4Factory.fastestInstance();
        for (FileEntry fileEntry : fileList) {
            //System.out.println("Extracting: " + fileEntry.Name);
            //System.out.println("Offset in file 1: " + fileEntry.OffsetInFile1);
            //System.out.println("Offset in file 2: " + fileEntry.OffsetInFile2);
            //System.out.println("Archive part: " + fileEntry.ArchivePart);
            //System.out.printf("Flags: 0x%08X%n", fileEntry.Flags);
            //System.out.println("Size on disk: " + fileEntry.SizeOnDisk);
            //System.out.println("Uncompressed size: " + fileEntry.UncompressedSize);
            //System.out.println("Offset: " + fileEntry.offsetInFile);

            if (fileEntry.SizeOnDisk > channel.size()) {
                throw new RuntimeException("File is larger than the archive");
            }
            byte[] compressedData = new byte[fileEntry.SizeOnDisk];
            map.position((int)fileEntry.offsetInFile);
            map.get(compressedData, 0, fileEntry.SizeOnDisk);
            byte[] uncompressedData = new byte[fileEntry.UncompressedSize];
            LZ4SafeDecompressor decompressor = factory.safeDecompressor();
            decompressor.decompress(compressedData, 0, compressedData.length, uncompressedData, 0, uncompressedData.length);
            Path outputPath = outputDir.resolve(fileEntry.Name);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, uncompressedData);
        }
    }
}
    
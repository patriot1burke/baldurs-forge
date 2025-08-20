package org.baldurs.archivist;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import net.jpountz.lz4.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PackageWriter {
    static class FileEntry {
        String name;
        byte[] data;
        int uncompressedSize;
        int compressedSize;
        int offset;
    }
    List<FileEntry> fileEntries = new ArrayList<>();
    int offset = 40;
    public void archive(Path inputDir, Path outputPak) throws Exception {
        walkFiles(inputDir);
        long fileListOffset = offset;
        byte[] fileList = new byte[fileEntries.size() * PackageReader.FileEntry.SIZE];
        ByteBuffer fileListBuf = ByteBuffer.wrap(fileList);
        fileListBuf.order(ByteOrder.LITTLE_ENDIAN);
        for (FileEntry entry : fileEntries) {
            byte[] nameBytes = entry.name.getBytes(StandardCharsets.UTF_8);
            byte[] nameBuffer = new byte[256]; 
            
            Arrays.fill(nameBuffer, (byte) 0);
            
            System.arraycopy(nameBytes, 0, nameBuffer, 0, nameBytes.length);
            fileListBuf.put(nameBuffer);
            fileListBuf.putInt(entry.offset);
            fileListBuf.putShort((short)0);
            fileListBuf.put((byte)0);
            fileListBuf.put((byte)0x12);
            fileListBuf.putInt(entry.compressedSize);
            fileListBuf.putInt(entry.uncompressedSize);
        }
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(fileList.length);
        byte[] compressedFileList = new byte[maxCompressedLength];
        int compressedFileListSize = compressor.compress(fileList, 0, fileList.length, compressedFileList, 0, maxCompressedLength);
        System.out.println("File list size: " + fileEntries.size());
        System.out.println("File list offset: " + fileListOffset);
        System.out.println("Compressed file list size: " + compressedFileListSize);

        byte[] header = new byte[40];
        ByteBuffer headerBuf = ByteBuffer.wrap(header);
        headerBuf.order(ByteOrder.LITTLE_ENDIAN);
        headerBuf.putInt(0x4B50534C);
        headerBuf.putInt(18);
        headerBuf.putLong(fileListOffset);
        headerBuf.putInt(compressedFileListSize);
        headerBuf.put((byte)0);
        headerBuf.put((byte)30);
        for (int i = 0; i < 16; i++) {
            headerBuf.put((byte)0);
        }
        headerBuf.putShort((short)1);

        FileChannel channel = FileChannel.open(outputPak, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        channel.write(ByteBuffer.wrap(header));
        for (FileEntry entry : fileEntries) {
            ByteBuffer wrap = ByteBuffer.wrap(entry.data, 0, entry.compressedSize);
            channel.write(wrap);
        }
        ByteBuffer fileListHeader = ByteBuffer.allocate(8);
        fileListHeader.order(ByteOrder.LITTLE_ENDIAN);
        fileListHeader.putInt(fileEntries.size());
        fileListHeader.putInt(compressedFileListSize);
        fileListHeader.flip();
        channel.write(fileListHeader);
        channel.write(ByteBuffer.wrap(compressedFileList, 0, compressedFileListSize));
        channel.close();
    }

    public void walkFiles(Path dir) throws IOException {
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                  .forEach(path -> {
                      String rel = dir.relativize(path).toString().replace("\\", "/");
                      try {
                        addEntry(rel, path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                  });
        }
    }

    void addEntry(String name, Path path) throws IOException {
        System.out.println("Adding entry: " + name);
        FileEntry entry = new FileEntry();
        entry.name = name;

        byte[] data = Files.readAllBytes(path);
        entry.uncompressedSize = data.length;
        
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(data.length);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedSize = compressor.compress(data, 0, data.length, compressed, 0, maxCompressedLength);
        entry.compressedSize = compressedSize;
        System.out.println("Compressed size: " + compressedSize);
        entry.data = compressed;
        entry.offset = offset;
        offset += compressedSize;
        fileEntries.add(entry);
    }
}

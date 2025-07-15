package org.baldurs.archivist.LS;

/**
 * Packed version structure for version handling
 */
public class PackedVersion {
    public long major;
    public long minor;
    public long revision;
    public long build;

    public static PackedVersion fromInt64(long packed) {
        PackedVersion version = new PackedVersion();
        version.major = (packed >> 55) & 0x7f;
        version.minor = (packed >> 47) & 0xff;
        version.revision = (packed >> 31) & 0xffff;
        version.build = packed & 0x7fffffff;
        return version;
    }

    public static PackedVersion fromInt32(int packed) {
        PackedVersion version = new PackedVersion();
        version.major = (packed >> 28) & 0x0f;
        version.minor = (packed >> 24) & 0x0f;
        version.revision = (packed >> 16) & 0xff;
        version.build = packed & 0xffff;
        return version;
    }

    public int toVersion32() {
        return (int) (((major & 0x0f) << 28) |
                ((minor & 0x0f) << 24) |
                ((revision & 0xff) << 16) |
                ((build & 0xffff) << 0));
    }

    public long toVersion64() {
        return (((long) major & 0x7f) << 55) |
                (((long) minor & 0xff) << 47) |
                (((long) revision & 0xffff) << 31) |
                (((long) build & 0x7fffffff) << 0);
    }
} 
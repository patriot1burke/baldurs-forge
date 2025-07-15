package org.baldurs.archivist.LS.Enums;

public enum PackageVersion {
    V7(7),   // D:OS 1
    V9(9),   // D:OS 1 EE
    V10(10), // D:OS 2
    V13(13), // D:OS 2 DE
    V15(15), // BG3 EA
    V16(16), // BG3 EA Patch4
    V18(18); // BG3 Release

    private final int value;

    PackageVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean hasCrc() {
        return this.value >= V10.value && this.value <= V16.value;
    }

    public long maxPackageSize() {
        if (this.value <= V15.value) {
            return 0x40000000L;
        } else {
            return 0x100000000L;
        }
    }

    public int paddingSize() {
        if (this.value <= V9.value) {
            return 0x1000;
        } else {
            return 0x40;
        }
    }
} 
package org.baldurs.archivist.LS.Enums;

public enum Game {
    DIVINITY_ORIGINAL_SIN(0),
    DIVINITY_ORIGINAL_SIN_EE(1),
    DIVINITY_ORIGINAL_SIN2(2),
    DIVINITY_ORIGINAL_SIN2_DE(3),
    BALDURS_GATE3(4),
    UNSET(5);

    private final int value;

    Game(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isFW3() {
        return this != DIVINITY_ORIGINAL_SIN && this != DIVINITY_ORIGINAL_SIN_EE;
    }

    public PackageVersion getPAKVersion() {
        switch (this) {
            case DIVINITY_ORIGINAL_SIN:
                return PackageVersion.V7;
            case DIVINITY_ORIGINAL_SIN_EE:
                return PackageVersion.V9;
            case DIVINITY_ORIGINAL_SIN2:
                return PackageVersion.V10;
            case DIVINITY_ORIGINAL_SIN2_DE:
                return PackageVersion.V13;
            case BALDURS_GATE3:
                return PackageVersion.V18;
            default:
                return PackageVersion.V18;
        }
    }

    public LSFVersion getLSFVersion() {
        switch (this) {
            case DIVINITY_ORIGINAL_SIN:
            case DIVINITY_ORIGINAL_SIN_EE:
                return LSFVersion.VER_CHUNKED_COMPRESS;
            case DIVINITY_ORIGINAL_SIN2:
            case DIVINITY_ORIGINAL_SIN2_DE:
                return LSFVersion.VER_EXTENDED_NODES;
            case BALDURS_GATE3:
                return LSFVersion.VER_BG3_PATCH3;
            default:
                return LSFVersion.VER_BG3_PATCH3;
        }
    }

    public LSXVersion getLSXVersion() {
        switch (this) {
            case DIVINITY_ORIGINAL_SIN:
            case DIVINITY_ORIGINAL_SIN_EE:
            case DIVINITY_ORIGINAL_SIN2:
            case DIVINITY_ORIGINAL_SIN2_DE:
                return LSXVersion.V3;
            case BALDURS_GATE3:
                return LSXVersion.V4;
            default:
                return LSXVersion.V4;
        }
    }
} 
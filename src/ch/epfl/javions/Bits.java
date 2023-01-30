package ch.epfl.javions;

import java.util.Objects;

public final class Bits {
    private Bits() {}

    public static int extractUInt(long value, int startBit, int length) {
        Preconditions.checkArgument(0 < length && length < Integer.SIZE);
        Objects.checkFromIndexSize(startBit, length, Long.SIZE);

        var unusedBits = Long.SIZE - length;
        return (int) ((value << (unusedBits - startBit)) >>> unusedBits);
    }

    public static boolean testBit(long value, int bit) {
        Objects.checkIndex(bit, Long.SIZE);
        return (value & (1L << bit)) != 0;
    }
}

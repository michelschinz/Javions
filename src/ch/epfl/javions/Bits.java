package ch.epfl.javions;

import java.util.Objects;

public final class Bits {
    private Bits() {}

    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(0 < size && size < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Long.SIZE);

        var unusedBits = Long.SIZE - size;
        return (int) ((value << (unusedBits - start)) >>> unusedBits);
    }

    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        return (value & (1L << index)) != 0;
    }
}

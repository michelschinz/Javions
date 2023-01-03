package ch.epfl.javions;

public final class Bits {
    public static int extractUInt(long value, int startBit, int length) {
        // TODO add more preconditions
        //  (in particular, disallow length == 0, as the code below probably doesn't work then).
        Preconditions.checkArgument(length < Integer.SIZE);

        var unusedBits = Long.SIZE - length;
        return (int) ((value << (unusedBits - startBit)) >>> unusedBits);
    }

    public static int extractBit(long value, int bit) {
        return (int) ((value >> bit) & 1);
    }
}

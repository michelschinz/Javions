package ch.epfl.javions;

public final class Bits {
    public static int extractUInt(long value, int startBit, int length) {
        // TODO add more preconditions
        //  (in particular, disallow length == 0, as the code below probably doesn't work then).
        Preconditions.checkArgument(length < Integer.SIZE);

        var unusedBits = Long.SIZE - length;
        return (int) ((value << (unusedBits - startBit)) >>> unusedBits);
    }

    public static boolean testBit(long value, int bit) {
        return ((value >> bit) & 1) != 0;
    }
}

package ch.epfl.javions;

public final class BitUnpacker<T extends Enum<T>> {
    public record Field<T>(T label, int size) {
        public Field {
            Preconditions.checkArgument(size > 0);
        }
    }

    public static <T> Field<T> field(T label, int size) {
        return new Field<>(label, size);
    }

    private final byte[] firstBit;

    @SafeVarargs
    public BitUnpacker(Field<T>... fields) {
        var firstBit = new byte[fields.length + 1];

        var start = 0;
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            if (field.label().ordinal() != i) throw new IllegalArgumentException();
            firstBit[i] = (byte) start;
            start += field.size();
            if (start > Long.SIZE) throw new IllegalArgumentException();
        }
        firstBit[fields.length] = (byte) start;

        this.firstBit = firstBit;
    }

    public int unpack(T field, long v) {
        var fieldIndex = field.ordinal();
        var start = this.firstBit[fieldIndex];
        var length = this.firstBit[fieldIndex + 1] - start;
        return Bits.extractUInt(v, start, length);
    }
}

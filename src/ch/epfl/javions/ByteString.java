package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;

/**
 * An immutable byte string.
 */
public final class ByteString {
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    private final byte[] bytes;

    public static ByteString ofHexadecimalString(CharSequence charSequence,
                                                 int fromIndex,
                                                 int toIndex) {
        return new ByteString(HEX_FORMAT.parseHex(charSequence, fromIndex, toIndex));
    }

    public static ByteString ofHexadecimalString(CharSequence charSequence) {
        return new ByteString(HEX_FORMAT.parseHex(charSequence));
    }

    public static ByteString ofBytes(byte[] bytes) {
        return new ByteString(bytes.clone());
    }

    public static ByteString ofBytes(byte[] bytes, int fromIndex, int toIndex) {
        return new ByteString(Arrays.copyOfRange(bytes, fromIndex, toIndex));
    }

    private ByteString(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes.length;
    }

    public int byteAt(int index) {
        return Byte.toUnsignedInt(bytes[index]);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ByteString that) && Arrays.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return HEX_FORMAT.formatHex(bytes);
    }
}

package ch.epfl.javions;

public final class Crc24 {
    public static final int GENERATOR = 0xff_f4_09;

    private static final int CRC_BITS = 24;
    private static final int TOP_BYTE_START_BIT = CRC_BITS - Byte.SIZE;

    private final int[] table;

    public Crc24(int generator) {
        this.table = buildTable(generator);
    }

    private static int[] buildTable(int generator) {
        var table = new int[1 << Byte.SIZE];
        for (var b = 0; b < table.length; b += 1) {
            var crc = b << TOP_BYTE_START_BIT;
            for (var i = 0; i < Byte.SIZE; i += 1) {
                crc <<= 1;
                if (Bits.testBit(crc, CRC_BITS)) crc ^= generator;
            }
            // Keeping only the low 24 bits is not strictly necessary, but cleaner.
            table[b] = Bits.extractUInt(crc, 0, CRC_BITS);
        }
        return table;
    }

    public int crc(byte[] bytes) {
        var crc = 0;
        for (var b : bytes) {
            var topByte = Bits.extractUInt(crc, TOP_BYTE_START_BIT, Byte.SIZE);
            crc = (crc << Byte.SIZE) ^ table[topByte ^ Byte.toUnsignedInt(b)];
        }
        return Bits.extractUInt(crc, 0, CRC_BITS);
    }
}

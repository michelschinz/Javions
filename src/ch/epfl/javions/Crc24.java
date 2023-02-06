package ch.epfl.javions;

public final class Crc24 {
    public static final int GENERATOR = 0xff_f4_09;

    private static final int CRC_BITS = 24;

    private final int[] table;

    public Crc24(int generator) {
        this.table = buildTable(generator);
    }

    private static int[] buildTable(int generator) {
        var table = new int[1 << Byte.SIZE];
        for (var i = 0; i < table.length; i++)
            table[i] = crc_bitwise(generator, new byte[]{(byte) i});
        return table;
    }

    private static int crc_bitwise(int generator, byte[] bytes) {
        var table = new int[]{0, generator};
        var crc = 0;
        for (var b : bytes) {
            for (var i = Byte.SIZE - 1; i >= 0; i -= 1) {
                var topBit = Bits.extractUInt(crc, CRC_BITS - 1, 1);
                crc = ((crc << 1) | Bits.extractUInt(b, i, 1)) ^ table[topBit];
            }
        }
        for (var i = 0; i < CRC_BITS; i += 1) {
            var topBit = Bits.extractUInt(crc, CRC_BITS - 1, 1);
            crc = (crc << 1) ^ table[topBit];
        }
        return Bits.extractUInt(crc, 0, CRC_BITS);
    }

    public int crc(byte[] bytes) {
        var crc = 0;
        for (var b : bytes) {
            var topByte = Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE);
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(b)) ^ table[topByte];
        }
        for (var i = 0; i < CRC_BITS / Byte.SIZE; i += 1) {
            var topByte = Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE);
            crc = (crc << Byte.SIZE) ^ table[topByte];
        }
        return Bits.extractUInt(crc, 0, CRC_BITS);
    }
}

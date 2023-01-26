package ch.epfl.javions;

public final class Crc24 {
    public static final int GENERATOR = 0xff_f4_09;

    private static final int MASK_BIT_24 = (1 << 24);
    private static final int MASK_LSBS_24 = 0xFF_FF_FF;

    private final int[] table;

    public Crc24(int generator) {
        this.table = buildTable(generator);
    }

    private static int[] buildTable(int generator) {
        var table = new int[0x100];
        for (var b = 0; b < table.length; b += 1) {
            var crc = b << 16;
            for (var i = 0; i < 8; i += 1) {
                crc <<= 1;
                if ((crc & MASK_BIT_24) != 0) crc ^= generator;
            }
            table[b] = crc & MASK_LSBS_24;
        }
        return table;
    }

    public int crc(byte[] bytes) {
        var crc = 0;
        for (var b : bytes) crc = (crc << 8) ^ table[((crc >> 16) ^ b) & 0xFF];
        return crc & MASK_LSBS_24;
    }
}

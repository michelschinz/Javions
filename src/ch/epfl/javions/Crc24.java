package ch.epfl.javions;

import java.util.Arrays;
import java.util.Comparator;

public final class Crc24 {
    public static final int GENERATOR = 0xff_f4_09;

    private static final int MASK_BIT_24 = (1 << 24);
    private static final int MASK_LSBS_24 = 0xFF_FF_FF;

    private final int[] table;
    private final int[][] errorTable;

    public Crc24(int generator) {
        var crcTable = buildTable(generator);
        var errorTable = buildErrorTable(crcTable);

        this.table = crcTable;
        this.errorTable = errorTable;
    }

    private static int[] buildTable(int generator) {
        var table = new int[0x100];
        for (var b = 0; b < 0x100; b += 1) {
            var crc = b << 16;
            for (var i = 0; i < 8; i += 1) {
                crc <<= 1;
                if ((crc & MASK_BIT_24) != 0) crc ^= generator;
            }
            table[b] = crc & MASK_LSBS_24;
        }
        return table;
    }

    private static int[][] buildErrorTable(int[] crcTable) {
        record CrcAndIndex(int crc, int index) { }

        var msg = new byte[112 / Byte.SIZE];
        var table = new CrcAndIndex[msg.length * Byte.SIZE];
        var i = 0;
        for (var byteIndex = 0; byteIndex < msg.length; byteIndex += 1) {
            for (var mask = 1 << 7; mask != 0; mask >>= 1) {
                msg[byteIndex] ^= mask;
                table[i] = new CrcAndIndex(crc(crcTable, msg), i);
                i += 1;
                msg[byteIndex] = 0;
            }
        }
        Arrays.sort(table, Comparator.comparingInt(c -> c.crc));

        var tables = new int[2][table.length];
        for (int j = 0; j < table.length; j += 1) {
            tables[0][j] = table[j].crc;
            tables[1][j] = table[j].index;
        }
        return tables;
    }

    private static int crc(int[] table, byte[] message) {
        var crc = 0;
        for (var b : message) crc = (crc << 8) ^ table[((crc >> 16) ^ b) & 0xFF];
        return crc & MASK_LSBS_24;
    }

    public int crc(byte[] message) {
        return crc(table, message);
    }

    public int findOneBitError(int invalidCrc) {
        var i = Arrays.binarySearch(errorTable[0], invalidCrc);
        return i >= 0 ? errorTable[1][i] : -1;
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

public record RawMessage(long timeStampNs, ByteString bytes) {
    private static final int DF_EXTENDED_SQUITTER = 17;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);

    public static int downLinkFormat(int dfAndCa) {
        return Bits.extractUInt(dfAndCa, 3, 5);
    }

    public static int size(int firstByte) {
        return downLinkFormat(firstByte) == DF_EXTENDED_SQUITTER ? 14 : 0;
    }

    public static boolean isValid(byte[] bytes) {
        return bytes.length == 14
               && downLinkFormat(bytes[0]) == DF_EXTENDED_SQUITTER
               && CRC_24.crc(bytes) == 0;
    }

    public static RawMessage of(long timeStampNs, ByteString bytes) {
        return new RawMessage(timeStampNs, bytes);
    }

    public int downLinkFormat() {
        return downLinkFormat(bytes().byteAt(0));
    }

    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HEX_FORMAT.toHexDigits(bytes.bytesInRange(1, 4), 6));
    }

    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    public int typeCode() {
        return Bits.extractUInt(payload(), 51, 5);
    }
}

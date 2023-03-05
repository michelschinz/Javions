package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14;

    // Bytes of the message.
    private static final int DF_CA_BYTE = 0;
    private static final int ICAO_ADDRESS_START = DF_CA_BYTE + 1;
    private static final int ICAO_ADDRESS_LENGTH = 3;
    private static final int ICAO_ADDRESS_END = ICAO_ADDRESS_START + ICAO_ADDRESS_LENGTH;
    private static final int PAYLOAD_START = ICAO_ADDRESS_END;
    private static final int PAYLOAD_END = PAYLOAD_START + 7;

    // Bits of the first byte (DF and CA).
    private static final int CA_START = 0;
    private static final int CA_SIZE = 3;
    private static final int DF_START = CA_START + CA_SIZE;
    private static final int DF_SIZE = 5;

    // Known DF values.
    private static final int DF_EXTENDED_SQUITTER = 17;

    // Bits of the payload (ME).
    private static final int TC_START = 51;
    private static final int TC_SIZE = 5;

    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);

    public static int size(int byte0) {
        return downLinkFormat(byte0) == DF_EXTENDED_SQUITTER ? LENGTH : 0;
    }

    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(bytes.length == LENGTH);
        return CRC_24.crc(bytes) == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    public static int downLinkFormat(int dfAndCa) {
        return Bits.extractUInt(dfAndCa, DF_START, DF_SIZE);
    }

    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, TC_START, TC_SIZE);
    }

    public int downLinkFormat() {
        return downLinkFormat(bytes().byteAt(DF_CA_BYTE));
    }

    public IcaoAddress icaoAddress() {
        var address = bytes.bytesInRange(ICAO_ADDRESS_START, ICAO_ADDRESS_END);
        return new IcaoAddress(HEX_FORMAT.toHexDigits(address, 2 * ICAO_ADDRESS_LENGTH));
    }

    public long payload() {
        return bytes.bytesInRange(PAYLOAD_START, PAYLOAD_END);
    }

    public int typeCode() {
        return typeCode(payload());
    }
}

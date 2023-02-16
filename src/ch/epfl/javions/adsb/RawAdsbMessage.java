package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

public record RawAdsbMessage(long timeStampNs,
                             int dfAndCa,
                             IcaoAddress icaoAddress,
                             long payload) {
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    public static RawAdsbMessage of(long timeStampNs, ByteString bytes) {
        Preconditions.checkArgument(bytes.size() == 14);
        var icaoString = HEX_FORMAT.toHexDigits(bytes.bytesBetween(1, 4), 6);
        return new RawAdsbMessage(
                timeStampNs,
                bytes.byteAt(0),
                new IcaoAddress(icaoString),
                bytes.bytesBetween(4, 11));
    }

    public int downLinkFormat() {
        return Bits.extractUInt(dfAndCa, 3, 5);
    }

    public int typeCode() {
        return Bits.extractUInt(payload, 51, 5);
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

public record RawAdsbMessage(long timeStamp,
                             int dfAndCa,
                             IcaoAddress icaoAddress,
                             long payload) {
    public static RawAdsbMessage of(long timeStamp, ByteString bytes) {
        Preconditions.checkArgument(bytes.size() == 14);
        return new RawAdsbMessage(
                timeStamp,
                bytes.byteAt(0),
                new IcaoAddress("%06X".formatted((int) bytes.bytesBetween(1, 4))),
                bytes.bytesBetween(4, 11));
    }

    public int downLinkFormat() {
        return Bits.extractUInt(dfAndCa, 3, 5);
    }

    public int typeCode() {
        return Bits.extractUInt(payload, 51, 5);
    }
}

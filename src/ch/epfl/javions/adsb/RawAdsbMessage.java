package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;

public record RawAdsbMessage(long timeStamp, ByteString bytes) {
    public int downLinkFormat() {
        return Bits.extractUInt(bytes.byteAt(0), 3, 5);
    }

    public int capability() {
        return Bits.extractUInt(bytes.byteAt(4), 0, 3);
    }

    public int typeCode() {
        return bytes.byteAt(4) >> 3;
    }

    public long payload() {
        return bytes.bytesBetween(4, 11);
    }

    public IcaoAddress icaoAddress() {
        return new IcaoAddress("%06X".formatted((int) bytes.bytesBetween(1, 4)));
    }
}

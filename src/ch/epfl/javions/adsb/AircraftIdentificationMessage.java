package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AircraftIdentificationMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        int category,
        String callSign
) implements Message {
    private static final int CALLSIGN_LENGTH = 8;
    private static final int CALLSIGN_CHAR_BITS = 6;

    private static int category(int typeCode, int capability) {
        return (0xE - typeCode) << 4 | capability;
    }

    private static String callSign(long payload) {
        var callSignChars = new char[CALLSIGN_LENGTH];
        for (var i = 0; i < CALLSIGN_LENGTH; i += 1) {
            var startBitI = (CALLSIGN_LENGTH - 1 - i) * CALLSIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALLSIGN_CHAR_BITS);
            callSignChars[i] = (char) ((n < 32 ? 0b0100_0000 : 0) | n);
        }
        return new String(callSignChars).trim();
    }

    public static AircraftIdentificationMessage of(long timeStamp, ByteString bytes) {
        return new AircraftIdentificationMessage(
                timeStamp,
                Message.icaoAddress(bytes),
                category(Message.typeCode(bytes), Message.capability(bytes)),
                callSign(Message.payload(bytes)));
    }
}

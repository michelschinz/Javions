package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.IcaoAddress;

public record AircraftIdentificationMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        int category,
        String callSign
) implements Message {
    private static final int CALLSIGN_LENGTH = 8;
    private static final int CALLSIGN_CHAR_BITS = 6;

    public static int category(int typeCode, int capability) {
        return (0xE - typeCode) << 4 | capability;
    }

    public static String callSign(long payload) {
        var callSignChars = new char[CALLSIGN_LENGTH];
        for (int i = 0; i < CALLSIGN_LENGTH; i += 1) {
            var startBitI = (CALLSIGN_LENGTH - 1 - i) * CALLSIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALLSIGN_CHAR_BITS);
            callSignChars[i] = (char) ((n < 32 ? 0b0100_0000 : 0) | n);
        }
        return new String(callSignChars).trim();
    }

    public static AircraftIdentificationMessage of(long timeStamp, ByteString messageData) {
        var icao = Message.icaoAddress(messageData);
        var typeCode = Message.rawTypeCode(messageData);
        var capability = Message.rawCapability(messageData);
        var category = category(typeCode, capability);
        var callSign = callSign(Message.payload(messageData));
        return new AircraftIdentificationMessage(timeStamp, icao, category, callSign);
    }
}

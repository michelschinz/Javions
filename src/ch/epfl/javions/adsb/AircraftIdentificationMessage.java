package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AircraftIdentificationMessage(long timeStampNs,
                                            IcaoAddress icaoAddress,
                                            int category,
                                            CallSign callSign) implements Message {
    private static final int CALLSIGN_LENGTH = 8;
    private static final int CALLSIGN_CHAR_BITS = 6;

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        var payload = rawMessage.payload();
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                category(payload),
                callSign(payload));
    }

    private static int category(long payload) {
        var category = Bits.extractUInt(payload, 48, 3);
        return (0xE - RawMessage.typeCode(payload)) << 4 | category;
    }

    private static CallSign callSign(long payload) {
        var callSignChars = new char[CALLSIGN_LENGTH];
        for (var i = 0; i < CALLSIGN_LENGTH; i += 1) {
            var startBitI = (CALLSIGN_LENGTH - 1 - i) * CALLSIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALLSIGN_CHAR_BITS);
            callSignChars[i] = (char) ((n < 32 ? 0b0100_0000 : 0) | n);
        }
        return new CallSign(new String(callSignChars).trim());
    }
}

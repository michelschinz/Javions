package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;

public final class AircraftIdentificationMessage extends Message {
    private static final int CALLSIGN_LENGTH = 8;
    private static final int CALLSIGN_CHAR_BITS = 6;

    public int category() {
        return (0xE - rawMessage.typeCode()) << 4 | rawMessage.capability();
    }

    public String callSign() {
        var payload = rawMessage.payload();
        var callSignChars = new char[CALLSIGN_LENGTH];
        for (var i = 0; i < CALLSIGN_LENGTH; i += 1) {
            var startBitI = (CALLSIGN_LENGTH - 1 - i) * CALLSIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALLSIGN_CHAR_BITS);
            callSignChars[i] = (char) ((n < 32 ? 0b0100_0000 : 0) | n);
        }
        return new String(callSignChars).trim();
    }

    public AircraftIdentificationMessage(RawAdsbMessage rawMessage) {
        super(rawMessage);
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AircraftIdentificationMessage(long timeStampNs,
                                            IcaoAddress icaoAddress,
                                            int category,
                                            CallSign callSign) implements Message {
    private static final int CALL_SIGN_LENGTH = 8;
    private static final int CALL_SIGN_CHAR_BITS = 6;
    private static final String CALL_SIGN_ALPHABET =
            "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789??????";

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        var payload = rawMessage.payload();
        var callSign = callSign(payload);
        return callSign == null
                ? null
                : new AircraftIdentificationMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                category(payload),
                callSign);
    }

    private static int category(long payload) {
        var category = Bits.extractUInt(payload, 48, 3);
        return (0xE - RawMessage.typeCode(payload)) << 4 | category;
    }

    private static CallSign callSign(long payload) {
        var callSignChars = new char[CALL_SIGN_LENGTH];
        for (var i = 0; i < CALL_SIGN_LENGTH; i += 1) {
            var startBitI = (CALL_SIGN_LENGTH - 1 - i) * CALL_SIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALL_SIGN_CHAR_BITS);
            callSignChars[i] = CALL_SIGN_ALPHABET.charAt(n);
        }
        var callSign = new String(callSignChars).trim();
        return callSign.contains("?") ? null : new CallSign(callSign);
    }
}

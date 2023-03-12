package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AircraftIdentificationMessage(long timeStampNs,
                                            IcaoAddress icaoAddress,
                                            int category,
                                            CallSign callSign) implements Message {
    private static final int CALL_SIGN_LENGTH = 8;
    private static final int CALL_SIGN_CHAR_SIZE = 6;
    private static final String CALL_SIGN_ALPHABET =
            "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789??????";

    private static final int CATEGORY_START = CALL_SIGN_LENGTH * CALL_SIGN_CHAR_SIZE;
    private static final int CATEGORY_SIZE = 3;

    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
    }

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
        var category = Bits.extractUInt(payload, CATEGORY_START, CATEGORY_SIZE);
        return (0xE - RawMessage.typeCode(payload)) << 4 | category;
    }

    private static CallSign callSign(long payload) {
        var callSignB = new StringBuilder(CALL_SIGN_LENGTH);
        for (var i = 0; i < CALL_SIGN_LENGTH * CALL_SIGN_CHAR_SIZE; i += CALL_SIGN_CHAR_SIZE) {
            var n = Bits.extractUInt(payload, i, CALL_SIGN_CHAR_SIZE);
            var c = CALL_SIGN_ALPHABET.charAt(n);
            if (callSignB.isEmpty() && c == ' ') continue; // ignore trailing spaces
            callSignB.append(c);
        }
        var callSign = callSignB.reverse().toString();
        return callSign.contains("?") ? null : new CallSign(callSign);
    }
}

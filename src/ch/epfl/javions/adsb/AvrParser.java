package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;

/**
 * Parser for (long) ADSB frames in AVR format.
 *
 * Example line:
 * *8D45D06458B51698A180E204E532;
 * 0         1         2
 * 012345678901234567890123456789
 *
 */
public final class AvrParser {
    public static ByteString parseAVR(String s) {
        return switch (s.length()) {
            case 30 -> ByteString.ofHexadecimalString(s, 1, 29);
            default -> throw new Error("invalid AVR frame: %s".formatted(s));
        };
    }
}

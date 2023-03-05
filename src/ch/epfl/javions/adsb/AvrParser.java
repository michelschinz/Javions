package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;

import java.util.HexFormat;

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
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public static byte[] parseAVR(String s) {
        if (s.length() != 30)
            throw new Error("invalid AVR frame: %s".formatted(s));
        return HEX_FORMAT.parseHex(s, 1, 29);
    }
}

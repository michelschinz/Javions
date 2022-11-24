package ch.epfl.javions;

import java.util.Optional;

import static ch.epfl.javions.Math2.TAU;
import static java.lang.Math.*;

public final class CprDecoder {
    // References:
    // [1] 1090-WP-14-09R1

    private static final double TAU_3_4 = TAU * 3d / 4d;

    private static final int LATITUDE_ZONE_COUNT = 15; // NZ in [1]
    private static final double D_LAT_E = TAU / (4d * LATITUDE_ZONE_COUNT);
    private static final double D_LAT_O = TAU / (4d * LATITUDE_ZONE_COUNT - 1d);

    private static final double NL_DENOMINATOR = 1 - cos(PI / (2d * LATITUDE_ZONE_COUNT));

    // Called "NL(x)" in [1]
    private static int lonZones(double latitude) {
        var cosLat = cos(latitude);
        var nl = floor(TAU / acos(1 - NL_DENOMINATOR / (cosLat * cosLat)));
        return Double.isNaN(nl) ? 1 : (int) nl;
    }

    public static Optional<GeoPos> decodePosition(int intLonCprE,
                                                  int intLatCprE,
                                                  int intLonCprO,
                                                  int intLatCprO,
                                                  boolean mostRecentIsE) {
        var lonCprE = Math.scalb(intLonCprE, -17);
        var latCprE = Math.scalb(intLatCprE, -17);
        var lonCprO = Math.scalb(intLonCprO, -17);
        var latCprO = Math.scalb(intLatCprO, -17);

        // TODO name constants for 60 (which is 4*NZ), and 59 (which is 4*NZ-1)
        var j = (int) floor(59 * latCprE - 60 * latCprO + 0.5);

        var latE = normalizeLat(D_LAT_E * (floorMod(j, 60) + latCprE));
        var latO = normalizeLat(D_LAT_O * (floorMod(j, 59) + latCprO));

        if (lonZones(latE) != lonZones(latO)) return Optional.empty();

        if (mostRecentIsE) {
            var nl = lonZones(latE);
            var m = (int) floor(lonCprE * (nl - 1) - lonCprO * nl + 0.5);
            var lonE = normalizeLon(TAU / nl * (floorMod(m, nl) + lonCprE));
            return Optional.of(new GeoPos(lonE, latE));
        } else {
            var nl = lonZones(latO);
            var nl1 = max(nl - 1, 1);
            var m = (int) floor(lonCprE * (nl - 1) - lonCprO * nl + 0.5);
            var lonO = normalizeLon(TAU / nl1 * (floorMod(m, nl1) + lonCprO));
            return Optional.of(new GeoPos(lonO, latO));
        }
    }

    private static double normalizeLon(double lon) {
        return lon >= PI ? lon - TAU : lon;
    }

    private static double normalizeLat(double lat) {
        return lat >= TAU_3_4 ? lat - TAU : lat;
    }
}

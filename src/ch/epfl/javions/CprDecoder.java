package ch.epfl.javions;

import java.util.Optional;

import static ch.epfl.javions.Math2.TAU;
import static java.lang.Math.*;

public final class CprDecoder {
    // References:
    // [1] 1090-WP-14-09R1

    private static final double TAU_3_4 = TAU * 3d / 4d;

    private static final double ZONES_E = 60;
    private static final double ZONES_O = ZONES_E - 1;

    private static final double D_LAT_E = TAU / ZONES_E;
    private static final double D_LAT_O = TAU / ZONES_O;

    private static final double NL_DENOMINATOR = 1 - cos(TAU / ZONES_E);

    // Called "NL(x)" in [1]
    private static int lonZones(double latitude) {
        var cosLat = cos(latitude);
        var nl = floor(TAU / acos(1 - NL_DENOMINATOR / (cosLat * cosLat)));
        return Double.isNaN(nl) ? 1 : (int) nl;
    }

    public static Optional<GeoPos> decodePosition(int lonCprE,
                                                  int latCprE,
                                                  int lonCprO,
                                                  int latCprO,
                                                  boolean mostRecentIsE) {
        var latZIn = floor(scalb(ZONES_O * latCprE - ZONES_E * latCprO + scalb(1d, 16), -17));
        var latE = D_LAT_E * (latZIn - ZONES_E * floor(latZIn / ZONES_E) + scalb(latCprE, -17));
        var latO = D_LAT_O * (latZIn - ZONES_O * floor(latZIn / ZONES_O) + scalb(latCprO, -17));

        if (lonZones(latE) != lonZones(latO)) return Optional.empty();
        var nl = lonZones(latE);

        if (nl == 1) {
            return mostRecentIsE
                    ? geoPos(TAU * scalb(lonCprE, -17), latE)
                    : geoPos(TAU * scalb(lonCprO, -17), latO);
        } else {
            var lonZIn = floor(scalb((nl - 1d) * lonCprE - nl * lonCprO + scalb(1d, 16), -17));
            if (!mostRecentIsE) nl -= 1;
            var v = lonZIn - nl * floor(lonZIn / nl);
            return mostRecentIsE
                    ? geoPos(TAU / nl * (v + scalb(lonCprE, -17)), latE)
                    : geoPos(TAU / nl * (v + scalb(lonCprO, -17)), latO);
        }
    }

    private static Optional<GeoPos> geoPos(double lon, double lat) {
        var normalizedLon = lon < PI ? lon : lon - TAU;
        var normalizedLat = lat < TAU_3_4 ? lat : lat - TAU;
        return Optional.of(new GeoPos(normalizedLon, normalizedLat));
    }
}

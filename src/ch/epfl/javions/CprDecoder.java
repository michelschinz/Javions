package ch.epfl.javions;

import java.util.Optional;

import static ch.epfl.javions.Math2.TAU;
import static ch.epfl.javions.Math2.floorMod;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.scalb;

public final class CprDecoder {
    private static final int CPR_BITS = 17;
    private static final double CPR_ONE_HALF = scalb(0.5d, CPR_BITS);

    private static final double LAT_ZONES_E = 60;
    private static final double LAT_ZONES_O = LAT_ZONES_E - 1;

    private static final double D_LAT_E = 1d / LAT_ZONES_E;
    private static final double D_LAT_O = 1d / LAT_ZONES_O;

    private static final double LON_ZONES_NUMERATOR = 1 - cos(D_LAT_E * Units.Angle.TURN);

    private static int lonZones(double latitude) {
        var cosLat = cos(latitude * Units.Angle.TURN);
        var nl = floor(TAU / acos(1 - LON_ZONES_NUMERATOR / (cosLat * cosLat)));
        return Double.isNaN(nl) ? 1 : (int) nl;
    }

    public static Optional<GeoPos> decodePosition(int lonCprE,
                                                  int latCprE,
                                                  int lonCprO,
                                                  int latCprO,
                                                  boolean mostRecentIsE) {
        var latZIn = floor(scalb(LAT_ZONES_O * latCprE - LAT_ZONES_E * latCprO + CPR_ONE_HALF, -CPR_BITS));
        var latE = D_LAT_E * (floorMod(latZIn, LAT_ZONES_E) + scalb(latCprE, -CPR_BITS));
        var latO = D_LAT_O * (floorMod(latZIn, LAT_ZONES_O) + scalb(latCprO, -CPR_BITS));

        var lonZonesE = lonZones(latE);
        if (lonZonesE != lonZones(latO)) return Optional.empty();

        if (lonZonesE == 1) {
            return mostRecentIsE
                    ? geoPos(scalb(lonCprE, -CPR_BITS), latE)
                    : geoPos(scalb(lonCprO, -CPR_BITS), latO);
        } else {
            var lonZonesO = lonZonesE - 1;
            var lonZIn = floor(scalb(lonZonesO * lonCprE - lonZonesE * lonCprO + CPR_ONE_HALF, -CPR_BITS));
            return mostRecentIsE
                    ? geoPos(floorMod(lonZIn, lonZonesE) + scalb(lonCprE, -CPR_BITS) / lonZonesE, latE)
                    : geoPos(floorMod(lonZIn, lonZonesO) + scalb(lonCprO, -CPR_BITS) / lonZonesO, latO);
        }
    }

    private static Optional<GeoPos> geoPos(double lon, double lat) {
        var normalizedLon = (int) scalb(lon < 0.5 ? lon : lon - 1, 32);
        var normalizedLat = (int) scalb(lat < 0.75 ? lat : lat - 1, 32);
        return Optional.of(new GeoPos(normalizedLon, normalizedLat));
    }
}

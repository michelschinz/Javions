package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import static java.lang.Math.*;

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
        var nl = floor(Units.Angle.TURN / acos(1 - LON_ZONES_NUMERATOR / (cosLat * cosLat)));
        return Double.isNaN(nl) ? 1 : (int) nl;
    }

    public static GeoPos decodePosition(int lonCprE,
                                        int latCprE,
                                        int lonCprO,
                                        int latCprO,
                                        boolean mostRecentIsE) {
        var latZIn = floor(scalb(LAT_ZONES_O * latCprE - LAT_ZONES_E * latCprO + CPR_ONE_HALF, -CPR_BITS));
        var latE = D_LAT_E * (normalizeZoneIndex(latZIn, LAT_ZONES_E) + scalb(latCprE, -CPR_BITS));
        var latO = D_LAT_O * (normalizeZoneIndex(latZIn, LAT_ZONES_O) + scalb(latCprO, -CPR_BITS));

        var lonZonesE = lonZones(latE);
        if (lonZonesE != lonZones(latO)) return null;

        if (lonZonesE == 1) {
            return mostRecentIsE
                    ? geoPos(scalb(lonCprE, -CPR_BITS), latE)
                    : geoPos(scalb(lonCprO, -CPR_BITS), latO);
        } else {
            var lonZonesO = lonZonesE - 1;
            var lonZIn = floor(scalb(lonZonesO * lonCprE - lonZonesE * lonCprO + CPR_ONE_HALF, -CPR_BITS));
            return mostRecentIsE
                    ? geoPos(normalizeZoneIndex(lonZIn, lonZonesE) + scalb(lonCprE, -CPR_BITS) / lonZonesE, latE)
                    : geoPos(normalizeZoneIndex(lonZIn, lonZonesO) + scalb(lonCprO, -CPR_BITS) / lonZonesO, latO);
        }
    }

    private static double normalizeZoneIndex(double zIn, double zonesCount) {
        return zIn < 0 ? zIn + zonesCount : zIn;
    }

    private static GeoPos geoPos(double lon, double lat) {
        var normalizedLon = (int) scalb(lon < 0.5 ? lon : lon - 1, Integer.SIZE);
        var normalizedLat = (int) scalb(lat < 0.75 ? lat : lat - 1, Integer.SIZE);
        return GeoPos.isValidLatitudeT32(normalizedLat)
                ? new GeoPos(normalizedLon, normalizedLat)
                : null;
    }
}

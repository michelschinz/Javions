package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import static java.lang.Math.*;

public final class CprDecoder {
    private static final int CPR_BITS = 17;

    private static final int LAT_ZONES_E = 60;
    private static final int LAT_ZONES_O = LAT_ZONES_E - 1;

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
        var latZIn = (int) rint(cprToDouble(LAT_ZONES_O * latCprE - LAT_ZONES_E * latCprO));
        var latE = D_LAT_E * (normalizeZoneIndex(latZIn, LAT_ZONES_E) + cprToDouble(latCprE));
        var latO = D_LAT_O * (normalizeZoneIndex(latZIn, LAT_ZONES_O) + cprToDouble(latCprO));

        var lonZonesE = lonZones(latE);
        if (lonZonesE != lonZones(latO)) return null;

        if (lonZonesE == 1) {
            return mostRecentIsE
                    ? geoPos(cprToDouble(lonCprE), latE)
                    : geoPos(cprToDouble(lonCprO), latO);
        } else {
            var lonZonesO = lonZonesE - 1;
            var lonZIn = (int) rint(cprToDouble(lonZonesO * lonCprE - lonZonesE * lonCprO));
            return mostRecentIsE
                    ? geoPos(normalizeZoneIndex(lonZIn, lonZonesE) + cprToDouble(lonCprE) / lonZonesE, latE)
                    : geoPos(normalizeZoneIndex(lonZIn, lonZonesO) + cprToDouble(lonCprO) / lonZonesO, latO);
        }
    }

    private static double cprToDouble(int x) {
        return scalb((double) x, -CPR_BITS);
    }

    private static int normalizeZoneIndex(int zIn, int zonesCount) {
        return zIn < 0 ? zIn + zonesCount : zIn;
    }

    private static int turnToT32(double angleTurn) {
        var centeredAngleTurn = angleTurn < 0.5 ? angleTurn : angleTurn - 1;
        return (int) rint(Units.convert(centeredAngleTurn, Units.Angle.TURN, Units.Angle.T32));
    }

    private static GeoPos geoPos(double lonTurn, double latTurn) {
        var lonT32 = turnToT32(lonTurn);
        var latT32 = turnToT32(latTurn);
        return GeoPos.isValidLatitudeT32(latT32)
                ? new GeoPos(lonT32, latT32)
                : null;
    }
}

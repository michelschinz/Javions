package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import static java.lang.Math.*;

public final class CprDecoder {
    private static final int CPR_BITS = 17;

    private static final int LAT_ZONES_0 = 60;
    private static final int LAT_ZONES_1 = LAT_ZONES_0 - 1;

    private static final double D_LAT_0_TURN = 1d / LAT_ZONES_0;
    private static final double D_LAT_1_TURN = 1d / LAT_ZONES_1;

    private static final double LON_ZONES_NUMERATOR = 1 - cos(D_LAT_0_TURN * Units.Angle.TURN);

    private static int lonZones(double latTurn) {
        var cosLat = cos(latTurn * Units.Angle.TURN);
        var nl = floor(Units.Angle.TURN / acos(1 - LON_ZONES_NUMERATOR / (cosLat * cosLat)));
        return Double.isNaN(nl) ? 1 : (int) nl;
    }

    public static GeoPos decodePosition(int x0, int y0, int x1, int y1, boolean mostRecentIs0) {
        var latZIn = (int) rint(cprToDouble(LAT_ZONES_1 * y0 - LAT_ZONES_0 * y1));
        var lat0Turn = D_LAT_0_TURN * (normalizeZoneIndex(latZIn, LAT_ZONES_0) + cprToDouble(y0));
        var lat1Turn = D_LAT_1_TURN * (normalizeZoneIndex(latZIn, LAT_ZONES_1) + cprToDouble(y1));

        var lonZones0 = lonZones(lat0Turn);
        if (lonZones0 != lonZones(lat1Turn)) return null;

        if (lonZones0 == 1) {
            return mostRecentIs0
                    ? geoPos(cprToDouble(x0), lat0Turn)
                    : geoPos(cprToDouble(x1), lat1Turn);
        } else {
            var lonZones1 = lonZones0 - 1;
            var lonZIn = (int) rint(cprToDouble(lonZones1 * x0 - lonZones0 * x1));
            return mostRecentIs0
                    ? geoPos(normalizeZoneIndex(lonZIn, lonZones0) + cprToDouble(x0) / lonZones0, lat0Turn)
                    : geoPos(normalizeZoneIndex(lonZIn, lonZones1) + cprToDouble(x1) / lonZones1, lat1Turn);
        }
    }

    private static double cprToDouble(int cpr) {
        return scalb((double) cpr, -CPR_BITS);
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

package ch.epfl.javions;

import static ch.epfl.javions.Math2.asinh;
import static java.lang.Math.scalb;
import static java.lang.Math.tan;

public final class WebMercator {
    private WebMercator() {}

    public static double x(int zoomLevel, double lon) {
        return scalb(lon * (1d / Units.Angle.TURN) + 0.5, shiftFor(zoomLevel));
    }

    public static double y(int zoomLevel, double lat) {
        return scalb(0.5 - asinh(tan(lat)) * (1d / Units.Angle.TURN), shiftFor(zoomLevel));
    }

    private static int shiftFor(int zoomLevel) {
        return 8 + zoomLevel;
    }
}

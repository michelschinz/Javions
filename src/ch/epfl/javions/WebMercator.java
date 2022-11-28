package ch.epfl.javions;

import static ch.epfl.javions.Math2.TAU;
import static ch.epfl.javions.Math2.asinh;
import static java.lang.Math.*;

public final class WebMercator {
    private WebMercator() {}

    private static final double ONE_OVER_TAU = 1d / TAU;

    public static double x(int zoomLevel, double lon) {
        return scalb(ONE_OVER_TAU * (lon + PI), shiftFor(zoomLevel));
    }

    public static double y(int zoomLevel, double lat) {
        return scalb(ONE_OVER_TAU * (PI - asinh(tan(lat))), shiftFor(zoomLevel));
    }

    private static int shiftFor(int zoomLevel) {
        return 8 + zoomLevel;
    }
}

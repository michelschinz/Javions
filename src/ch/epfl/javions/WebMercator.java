package ch.epfl.javions;

import static ch.epfl.javions.Math2.TAU;
import static ch.epfl.javions.Math2.asinh;
import static java.lang.Math.*;

public final class WebMercator {
    private WebMercator() {}

    private static final double ONE_OVER_TAU = 1d / TAU;

    public static double x(double lon) {
        return ONE_OVER_TAU * (lon + PI);
    }

    public static double y(double lat) {
        return ONE_OVER_TAU * (PI - asinh(tan(lat)));
    }

    public static double lon(double x) {
        return TAU * x - PI;
    }

    public static double lat(double y) {
        return atan(sinh(PI - TAU * y));
    }

    private static int shiftFor(int zoomLevel) {
        return 8 + zoomLevel;
    }

    public static double xAtZoomLevel(int zoomLevel, double x) {
        return scalb(x, shiftFor(zoomLevel));
    }

    public static double yAtZoomLevel(int zoomLevel, double y) {
        return scalb(y, shiftFor(zoomLevel));
    }
}

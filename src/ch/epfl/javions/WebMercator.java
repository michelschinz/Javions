package ch.epfl.javions;

import static ch.epfl.javions.Math2.asinh;
import static java.lang.Math.scalb;
import static java.lang.Math.tan;

public final class WebMercator {
    private WebMercator() {}

    public static double x(int zoomLevel, double lon) {
        var x = 0.5 + lon * (1d / Units.Angle.TURN);
        return atZoomLevel(zoomLevel, x);
    }

    public static double y(int zoomLevel, double lat) {
        var y = 0.5 - asinh(tan(lat)) * (1d / Units.Angle.TURN);
        return atZoomLevel(zoomLevel, y);
    }

    private static double atZoomLevel(int z, double v) {
        return scalb(v, 8 + z);
    }
}

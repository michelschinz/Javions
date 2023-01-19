package ch.epfl.javions;

import static ch.epfl.javions.Math2.asinh;
import static java.lang.Math.*;

public final class WebMercator {
    private WebMercator() {}

    public static double x(int zoomLevel, double lon) {
        return scalb((lon + PI) * (1d / Units.Angle.TURN), shiftFor(zoomLevel));
    }

    public static double y(int zoomLevel, double lat) {
        return scalb((PI - asinh(tan(lat))) * (1d / Units.Angle.TURN), shiftFor(zoomLevel));
    }

    private static int shiftFor(int zoomLevel) {
        return 8 + zoomLevel;
    }
}

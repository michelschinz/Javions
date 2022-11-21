package ch.epfl.javions;

import static java.lang.Math.scalb;

public record PointWebMercator(double x, double y) {
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(
                scalb(x, -shiftFor(zoomLevel)),
                scalb(y, -shiftFor(zoomLevel)));
    }

    public PointWebMercator {
        Preconditions.checkArgument((0 <= x && x <= 1) && (0 <= y && y <= 1));
    }

    private static int shiftFor(int zoomLevel) {
        return 8 + zoomLevel;
    }

    public double xAtZoomLevel(int zoomLevel) {
        return scalb(x, shiftFor(zoomLevel));
    }

    public double yAtZoomLevel(int zoomLevel) {
        return scalb(y, shiftFor(zoomLevel));
    }

    public double lon() {
        return WebMercator.lon(x);
    }

    public double lat() {
        return WebMercator.lat(y);
    }
}

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

    public static double xAtZoomLevel(int zoomLevel, double x) {
        return scalb(x, shiftFor(zoomLevel));
    }

    public static double yAtZoomLevel(int zoomLevel, double y) {
        return scalb(y, shiftFor(zoomLevel));
    }

    public double xAtZoomLevel(int zoomLevel) {
        return xAtZoomLevel(zoomLevel, x);
    }

    public double yAtZoomLevel(int zoomLevel) {
        return yAtZoomLevel(zoomLevel, y);
    }

    public double lon() {
        return WebMercator.lon(x);
    }

    public double lat() {
        return WebMercator.lat(y);
    }
}

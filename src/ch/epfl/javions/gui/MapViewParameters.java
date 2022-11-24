package ch.epfl.javions.gui;

import ch.epfl.javions.WebMercator;
import javafx.geometry.Point2D;

public record MapViewParameters(int zoomLevel, double minX, double minY) {
    public MapViewParameters(int zoomLevel, Point2D topLeft) {
        this(zoomLevel, topLeft.getX(), topLeft.getY());
    }

    public Point2D topLeft() {
        return new Point2D(minX, minY);
    }

    public MapViewParameters withMinXY(double minX, double minY) {
        return new MapViewParameters(zoomLevel, minX, minY);
    }

    public double viewX(double x) {
        return WebMercator.xAtZoomLevel(zoomLevel, x) - minX;
    }

    public double viewY(double y) {
        return WebMercator.yAtZoomLevel(zoomLevel, y) - minY;
    }
}

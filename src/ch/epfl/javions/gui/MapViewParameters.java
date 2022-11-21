package ch.epfl.javions.gui;

import ch.epfl.javions.PointWebMercator;
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

    public PointWebMercator pointAt(double viewX, double viewY) {
        return PointWebMercator.of(zoomLevel, minX + viewX, minY + viewY);
    }

    public double viewX(PointWebMercator pointWM) {
        return pointWM.xAtZoomLevel(zoomLevel) - minX;
    }

    public double viewY(PointWebMercator pointWM) {
        return pointWM.yAtZoomLevel(zoomLevel) - minY;
    }
}

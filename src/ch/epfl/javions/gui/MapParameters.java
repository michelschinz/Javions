package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.beans.property.*;
import javafx.geometry.Point2D;

import static java.lang.Math.scalb;

// TODO also have min/max zoom values
public final class MapParameters {
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;
    private final IntegerProperty zoomProperty;

    public MapParameters(double initialMinX, double initialMinY, int initialZoom) {
        minXProperty = new SimpleDoubleProperty(initialMinX);
        minYProperty = new SimpleDoubleProperty(initialMinY);
        zoomProperty = new SimpleIntegerProperty(initialZoom);
    }

    public ReadOnlyDoubleProperty minXProperty() {
        return minXProperty;
    }

    public double getMinX() {
        return minXProperty.get();
    }

    public ReadOnlyDoubleProperty minYProperty() {
        return minYProperty;
    }

    public double getMinY() {
        return minYProperty.get();
    }

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoomProperty;
    }

    public int getZoom() {
        return zoomProperty.get();
    }

    public double viewX(GeoPos pos) {
        return WebMercator.x(getZoom(), pos.longitude()) - getMinX();
    }

    public double viewY(GeoPos pos) {
        return WebMercator.y(getZoom(), pos.latitude()) - getMinY();
    }

    public void scroll(double dX, double dY) {
        // TODO clamp? if yes, with a margin?
        minXProperty.set(getMinX() + dX);
        minYProperty.set(getMinY() + dY);
    }

    public void changeZoomLevel(int zoomDelta, double centerViewX, double centerViewY) {
        // TODO clamp zoomDelta

        var newZoomLevel = getZoom() + zoomDelta;
        var newTopLeft = new Point2D(getMinX(), getMinY())
                .add(centerViewX, centerViewY)
                .multiply(scalb(1d, zoomDelta))
                .subtract(centerViewX, centerViewY);

        minXProperty.set(newTopLeft.getX());
        minYProperty.set(newTopLeft.getY());
        zoomProperty.set(newZoomLevel);
    }
}

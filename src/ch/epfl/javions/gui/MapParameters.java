package ch.epfl.javions.gui;

import javafx.beans.property.*;

import static ch.epfl.javions.Math2.clamp;
import static java.lang.Math.scalb;

public final class MapParameters {
    private static final int MIN_ZOOM_LEVEL = 1;
    private static final int MAX_ZOOM_LEVEL = 19;

    private final IntegerProperty zoomProperty;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;

    public MapParameters(int initialZoom, double initialMinX, double initialMinY) {
        zoomProperty = new SimpleIntegerProperty(initialZoom);
        minXProperty = new SimpleDoubleProperty(initialMinX);
        minYProperty = new SimpleDoubleProperty(initialMinY);
    }

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoomProperty;
    }

    public int getZoom() {
        return zoomProperty.get();
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

    public void scroll(double dX, double dY) {
        minXProperty.set(getMinX() + dX);
        minYProperty.set(getMinY() + dY);
    }

    public void changeZoomLevel(int zoomDelta) {
        var newZoom = clamp(MIN_ZOOM_LEVEL, getZoom() + zoomDelta, MAX_ZOOM_LEVEL);
        if (newZoom != getZoom()) {
            minXProperty.set(scalb(getMinX(), zoomDelta));
            minYProperty.set(scalb(getMinY(), zoomDelta));
            zoomProperty.set(newZoom);
        }
    }
}

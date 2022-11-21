package ch.epfl.javions.gui;

import ch.epfl.javions.gui.TileManager.TileId;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.io.IOException;

import static ch.epfl.javions.Math2.clamp;
import static ch.epfl.javions.gui.TileManager.TILE_SIZE;
import static java.lang.Math.max;
import static java.lang.Math.scalb;

public final class BaseMapManager {
    private static final int MIN_ZOOM = 8;
    private static final int MAX_ZOOM = 19;

    private final TileManager tileManager;
    private final ObjectProperty<MapViewParameters> mapViewParametersProperty;

    private final Canvas canvas;
    private final Pane pane;

    private boolean redrawNeeded;

    public BaseMapManager(TileManager tileManager,
                          ObjectProperty<MapViewParameters> mapViewParametersProperty) {
        var canvas = new Canvas();
        var pane = new Pane(canvas);

        this.tileManager = tileManager;
        this.mapViewParametersProperty = mapViewParametersProperty;
        this.canvas = canvas;
        this.pane = pane;

        installHandlers();
        installBindings();
        installListeners();
    }

    private void installHandlers() {
        // Zooming
        var minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d) return;
            var currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            var zoomDelta = (int) Math.signum(e.getDeltaY());
            var mapViewParameters = mapViewParametersProperty.get();

            var newZoomLevel = clamp(MIN_ZOOM, mapViewParameters.zoomLevel() + zoomDelta, MAX_ZOOM);
            var scaleFactor = scalb(1, newZoomLevel - mapViewParameters.zoomLevel());

            var localPoint = new Point2D(e.getX(), e.getY());
            var pointUnderMouse = mapViewParameters.topLeft().add(localPoint);
            var newPointUnderMouse = pointUnderMouse.multiply(scaleFactor);
            var newTopLeft = newPointUnderMouse.subtract(localPoint);

            mapViewParametersProperty.set(new MapViewParameters(newZoomLevel, newTopLeft));
        });

        // Scrolling
        var lastDragPointP = new SimpleObjectProperty<Point2D>();
        pane.setOnMousePressed(e -> lastDragPointP.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseDragged(e -> {
            var mapViewParameters = mapViewParametersProperty.get();
            var newTopLeft = mapViewParameters.topLeft()
                    .add(lastDragPointP.get())
                    .subtract(e.getX(), e.getY());
            var newViewParameters = mapViewParameters
                    .withMinXY(max(0, newTopLeft.getX()), max(0, newTopLeft.getY()));

            mapViewParametersProperty.set(newViewParameters);
            lastDragPointP.set(new Point2D(e.getX(), e.getY()));
        });
        pane.setOnMouseReleased(e -> lastDragPointP.set(null));
    }

    private void installBindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    private void installListeners() {
        canvas.sceneProperty().addListener((p, oldScene, newScene) -> {
            assert oldScene == null;
            newScene.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        mapViewParametersProperty.addListener(o -> redrawOnNextPulse());
        canvas.widthProperty().addListener(o -> redrawOnNextPulse());
        canvas.heightProperty().addListener(o -> redrawOnNextPulse());
    }

    public Pane pane() {
        return pane;
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        var mapViewParameters = mapViewParametersProperty.get();
        int zoom = mapViewParameters.zoomLevel();
        var minX = mapViewParameters.minX();
        var minY = mapViewParameters.minY();
        var minTileIndexX = (int) (minX / TILE_SIZE);
        var maxTileIndexX = (int) ((minX + canvas.getWidth()) / TILE_SIZE);
        var minTileIndexY = (int) (minY / TILE_SIZE);
        var maxTileIndexY = (int) ((minY + canvas.getHeight()) / TILE_SIZE);

        var ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (var tileX = minTileIndexX; tileX <= maxTileIndexX; tileX += 1) {
            var x = tileX * TILE_SIZE - minX;
            for (var tileY = minTileIndexY; tileY <= maxTileIndexY; tileY += 1) {
                var y = tileY * TILE_SIZE - minY;
                if (TileId.isValid(zoom, tileX, tileY)) {
                    try {
                        var tile = tileManager.imageForTileAt(new TileId(zoom, tileX, tileY));
                        ctx.drawImage(tile, x, y);
                    } catch (IOException e) {
                        // Do nothing (the tile just won't be drawn)
                    }
                }
            }
        }
    }
}

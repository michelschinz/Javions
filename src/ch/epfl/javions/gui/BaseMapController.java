package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.gui.TileManager.TileId;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

import static ch.epfl.javions.gui.TileManager.TILE_SIZE;

public final class BaseMapController {
    private final TileManager tileManager;
    private final MapParameters mapParameters;

    private final Canvas canvas;
    private final Pane pane;

    private boolean redrawNeeded;

    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        var canvas = new Canvas();
        var pane = new Pane(canvas);

        this.tileManager = Objects.requireNonNull(tileManager);
        this.mapParameters = Objects.requireNonNull(mapParameters);
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
            mapParameters.changeZoomLevel((int) Math.signum(e.getDeltaY()), e.getX(), e.getY());
        });

        // Scrolling
        var lastDragPointP = new SimpleObjectProperty<Point2D>();
        pane.setOnMousePressed(e -> lastDragPointP.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseDragged(e -> {
            var currentDragPoint = new Point2D(e.getX(), e.getY());
            var scrollVector = lastDragPointP.get().subtract(currentDragPoint);
            mapParameters.scroll(scrollVector.getX(), scrollVector.getY());
            lastDragPointP.set(currentDragPoint);
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

        mapParameters.minXProperty().addListener(o -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(o -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(o -> redrawOnNextPulse());
        canvas.widthProperty().addListener(o -> redrawOnNextPulse());
        canvas.heightProperty().addListener(o -> redrawOnNextPulse());
    }

    public Pane pane() {
        return pane;
    }

    public void centerOn(GeoPos centerPosition) {
        var currentCenterX = mapParameters.getMinX() + canvas.getWidth() / 2d;
        var currentCenterY = mapParameters.getMinY() + canvas.getHeight() / 2d;
        var newCenterX = WebMercator.x(mapParameters.getZoom(), centerPosition.longitude());
        var newCenterY = WebMercator.y(mapParameters.getZoom(), centerPosition.latitude());
        mapParameters.scroll(newCenterX - currentCenterX, newCenterY - currentCenterY);
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        var zoom = mapParameters.getZoom();
        var minX = mapParameters.getMinX();
        var minY = mapParameters.getMinY();
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

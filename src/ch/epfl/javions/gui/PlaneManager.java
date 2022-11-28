package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.Units.Angle;
import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class PlaneManager {
    private static final String AIRLINER = "M.01 14.75c-.26 0-.74-.71-.86-1.41l-3.33.86L-4.5 14.29l.08-1.41.11-.07c1.13-.68 2.68-1.64 3.2-2-.37-1.06-.51-3.92-.43-8.52v0L-4.5 2.31C-7.13 3.12-11.3 4.39-11.5 4.5a.5.5 0 01-.21 0 .52.52 0 01-.49-.45 1 1 0 01.52-1l1.74-.91c1.36-.71 3.22-1.69 4.66-2.43a4 4 0 010-.52c0-.69 0-1 0-1.14l.25-.13H-5.34A1.07 1.07 0 01-4.26-3.27 1.12 1.12 0 01-3.44-3a1.46 1.46 0 01.26.87L-3.42-2h.25c0 .14 0 .31 0 .58l1.52-.84c0-1.48 0-7.06 1.1-8.25a.74.74 0 011.13 0c1.15 1.19 1.13 6.78 1.1 8.25l1.52.84c0-.32 0-.48 0-.58l.25-.13H3.2A1.46 1.46 0 013.5-3a1.11 1.11 0 01.82-.28 1.06 1.06 0 011.08 1.16V-2c0 .19 0 .48 0 1.17a4 4 0 010 .52c1.75.9 4.4 2.29 5.67 3l.73.38a.9.9 0 01.5 1 .55.55 0 01-.5.47h0l-.11 0c-.28-.11-4.81-1.49-7.16-2.2H1.56v0c.09 4.6-.06 7.46-.43 8.52.52.33 2.07 1.29 3.2 2l.11.07L4.5 14.29l-.33-.09-3.33-.86c-.12.7-.6 1.41-.86 1.41h0Z";

    private final MapParameters mapParameters;
    private final ObservableMap<IcaoAddress, ObservablePlaneState> planeStates;
    private final ObjectProperty<IcaoAddress> selectedPlaneProperty;
    private final Pane pane;

    public PlaneManager(MapParameters mapParameters,
                        ObservableMap<IcaoAddress, ObservablePlaneState> planeStates,
                        ObjectProperty<IcaoAddress> selectedPlaneProperty) {
        assert planeStates.isEmpty(); // TODO should we instead create the initial nodes?

        var pane = new Pane();
        pane.getStylesheets().add("planes.css");
        pane.setPickOnBounds(false);

        this.mapParameters = mapParameters;
        this.planeStates = planeStates;
        this.selectedPlaneProperty = selectedPlaneProperty;
        this.pane = pane;

        installHandlers();
    }

    private void installHandlers() {
        planeStates.addListener((MapChangeListener<IcaoAddress, ObservablePlaneState>) change -> {
            if (change.wasRemoved()) {
                assert !change.wasAdded();
                var idToRemove = change.getKey().toString();
                pane.getChildren().removeIf(n -> n.getId().equals(idToRemove));
            }
            if (change.wasAdded()) {
                assert !change.wasRemoved();
                pane.getChildren().add(groupForPlane(change.getKey(), change.getValueAdded()));
            }
        });
    }

    private Node groupForPlane(IcaoAddress address, ObservablePlaneState planeState) {
        var planeNode = nodeForPlane(address, planeState);
        var trajectoryPath = polyLineForPlaneTrajectory(address, planeState);
        return new Group(trajectoryPath, planeNode);
    }

    private Node nodeForPlane(IcaoAddress address, ObservablePlaneState planeState) {
        var planePath = new SVGPath();
        planePath.setContent(AIRLINER);
        planePath.getStyleClass().add("plane");
        planePath.setId(address.toString());

        Tooltip tip = new Tooltip();
        tip.textProperty().bind(Bindings.when(planeState.callSignProperty().isEmpty())
                .then("(%s)".formatted(address.toString()))
                .otherwise(planeState.callSignProperty()));
        tip.setShowDelay(Duration.ZERO);
        tip.setHideDelay(Duration.seconds(1));
        Tooltip.install(planePath, tip);

        planePath.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    var pos = planeState.getPosition();
                    return pos != null
                            ? WebMercator.x(mapParameters.getZoom(), pos.longitude()) - mapParameters.getMinX()
                            : Double.NaN;
                },
                planeState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));
        planePath.layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    var pos = planeState.getPosition();
                    return pos != null
                            ? WebMercator.y(mapParameters.getZoom(), pos.latitude()) - mapParameters.getMinY()
                            : Double.NaN;
                },
                planeState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        planePath.rotateProperty().bind(planeState.trackOrHeadingProperty().multiply(Angle.RADIAN / Angle.DEGREE));

        planePath.setOnMouseClicked(e -> selectedPlaneProperty.set(address));

        return planePath;
    }

    private Polyline polyLineForPlaneTrajectory(IcaoAddress address, ObservablePlaneState planeState) {
        var l = new Polyline();

        planeState.trajectory().addListener((ListChangeListener<GeoPos>) c ->
                l.getPoints().setAll(trajectory(mapParameters.getZoom(), planeState.trajectory())));
        mapParameters.zoomProperty().addListener((p, o, n) ->
                l.getPoints().setAll(trajectory(n.intValue(), planeState.trajectory())));

        l.layoutXProperty().bind(mapParameters.minXProperty().negate());
        l.layoutYProperty().bind(mapParameters.minYProperty().negate());

        l.visibleProperty().bind(selectedPlaneProperty.isEqualTo(address));

        l.getStyleClass().add("trajectory");

        return l;
    }

    private static ArrayList<Double> trajectory(int zoomLevel, List<GeoPos> trajectory) {
        var points = new ArrayList<Double>(2 * trajectory.size());
        for (var p : trajectory) {
            points.add(WebMercator.x(zoomLevel, p.longitude()));
            points.add(WebMercator.y(zoomLevel, p.latitude()));
        }
        return points;
    }

    public Pane pane() {
        return pane;
    }
}

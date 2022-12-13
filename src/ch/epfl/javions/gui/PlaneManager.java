package ch.epfl.javions.gui;

import ch.epfl.javions.*;
import ch.epfl.javions.Units.Angle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class PlaneManager {
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservablePlaneState> selectedPlaneProperty;
    private final Pane pane;

    public PlaneManager(MapParameters mapParameters,
                        ObservableSet<ObservablePlaneState> planeStates,
                        ObjectProperty<ObservablePlaneState> selectedPlaneProperty) {
        assert planeStates.isEmpty(); // TODO should we instead create the initial nodes?

        var pane = new Pane();
        pane.getStylesheets().add("planes.css");
        pane.setPickOnBounds(false);

        this.mapParameters = mapParameters;
        this.selectedPlaneProperty = selectedPlaneProperty;
        this.pane = pane;

        installHandlers(planeStates);
    }

    private void installHandlers(ObservableSet<ObservablePlaneState> planeStates) {
        planeStates.addListener((SetChangeListener<ObservablePlaneState>) change -> {
            if (change.wasRemoved()) {
                var idToRemove = change.getElementRemoved().address().toString();
                pane.getChildren().removeIf(n -> n.getId().equals(idToRemove));
            }
            if (change.wasAdded())
                pane.getChildren().add(groupForPlane(change.getElementAdded()));
        });
    }

    private Node groupForPlane(ObservablePlaneState planeState) {
        var address = planeState.address();
        var planeNode = nodeForPlane(planeState);
        var trajectoryPath = polyLineForPlaneTrajectory(address, planeState);
        return new Group(trajectoryPath, planeNode);
    }

    private Node nodeForPlane(ObservablePlaneState planeState) {
        var address = planeState.address();
        var planePath = new SVGPath();
        planePath.getStyleClass().add("plane");
        planePath.setId(address.toString());

        planePath.setContent(iconFor(planeState.getTypeDesignator(), planeState.getTypeDescription()).svgPath());

        planePath.fillProperty().bind(Bindings.createObjectBinding(() ->
                        ColorRamp.PLASMA.at(planeState.getAltitude() / (11_000 * Units.Distance.METER)),
                planeState.altitudeProperty()));

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

        planePath.setOnMouseClicked(e -> selectedPlaneProperty.set(planeState));

        return planePath;
    }

    private AircraftIcon iconFor(String typeDesignator, String typeDescription) {
        // TODO should the designator be valid (i.e. non-empty)?
        var maybeIcon = IconTables.TYPE_DESIGNATOR_TABLE.getOrDefault(typeDesignator, AircraftIcon.UNKNOWN);
        if (maybeIcon == AircraftIcon.UNKNOWN)
            maybeIcon = IconTables.TYPE_DESCRIPTION_TABLE.getOrDefault(typeDescription, AircraftIcon.UNKNOWN);
        return maybeIcon;
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

package ch.epfl.javions.gui;

import ch.epfl.javions.*;
import ch.epfl.javions.Units.Angle;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import ch.epfl.javions.gui.ObservablePlaneState.GeoPosWithAltitude;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
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
                pane.getChildren().removeIf(n -> idToRemove.equals(n.getId()));
            }
            if (change.wasAdded())
                pane.getChildren().add(groupForPlane(change.getElementAdded()));
        });
    }

    private Node groupForPlane(ObservablePlaneState planeState) {
        var group = new Group(lineGroupForPlaneTrajectory(planeState), nodeForPlane(planeState));
        group.setId(planeState.address().toString());
        group.viewOrderProperty().bind(planeState.altitudeProperty().negate());
        return group;
    }

    private Node nodeForPlane(ObservablePlaneState planeState) {
        var address = planeState.address();
        var planePath = new SVGPath();
        planePath.getStyleClass().add("plane");

        var data = planeState.getFixedData();
        planePath.setContent(iconFor(data.typeDesignator(), data.description(), planeState.getCategory(), data.wakeTurbulenceCategory()).svgPath());

        planePath.fillProperty().bind(Bindings.createObjectBinding(
                () -> colorForAltitude(planeState.getAltitude()),
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

    private static Color colorForAltitude(double altitude) {
        // FIXME improve (and avoid the arbitrary constant)
        var scaledAltitude = altitude / (11_000d * Units.Distance.METER);
        return ColorRamp.PLASMA.at(Math.pow(scaledAltitude, 1d / 3d));
    }

    private AircraftIcon iconFor(AircraftTypeDesignator typeDesignator,
                                 AircraftDescription typeDescription,
                                 int category,
                                 WakeTurbulenceCategory wtc) {
        // TODO should the designator be valid (i.e. non-empty)?
        return IconTables.iconFor(typeDesignator, typeDescription, category, wtc);
    }

    private Group lineGroupForPlaneTrajectory(ObservablePlaneState planeState) {
        var lineGroup = new Group();

        planeState.trajectory().addListener((InvalidationListener) c ->
                rebuildTrajectory(lineGroup, mapParameters.getZoom(), planeState.trajectory()));
        mapParameters.zoomProperty().addListener((p, o, n) ->
                rebuildTrajectory(lineGroup, n.intValue(), planeState.trajectory()));

        lineGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        lineGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        lineGroup.visibleProperty().bind(selectedPlaneProperty.isEqualTo(planeState));

        lineGroup.getStyleClass().add("trajectory");

        return lineGroup;
    }

    private static void rebuildTrajectory(Group group, int zoomLevel, List<GeoPosWithAltitude> trajectory) {
        if (trajectory.size() < 2) {
            group.getChildren().clear();
            return;
        }

        var segments = new ArrayList<Line>(trajectory.size() - 1);

        var posAndAlt1 = trajectory.get(0);
        var p1 = projectedPos(zoomLevel, posAndAlt1.position());
        var c1 = colorForAltitude(posAndAlt1.altitude());

        for (var posAndAlt2 : trajectory.subList(1, trajectory.size())) {
            var p2 = projectedPos(zoomLevel, posAndAlt2.position());
            var c2 = colorForAltitude(posAndAlt2.altitude());

            var line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            line.setStroke(c1.equals(c2) ? c1 : gradient(c1, c2));

            segments.add(line);

            p1 = p2;
            c1 = c2;
        }

        group.getChildren().setAll(segments);
    }

    private static LinearGradient gradient(Color c1, Color c2) {
        var s1 = new Stop(0, c1);
        var s2 = new Stop(1, c2);
        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, s1, s2);
    }

    private static Point2D projectedPos(int zoomLevel, GeoPos position) {
        var x = WebMercator.x(zoomLevel, position.longitude());
        var y = WebMercator.y(zoomLevel, position.latitude());
        return new Point2D(x, y);
    }

    public Pane pane() {
        return pane;
    }
}

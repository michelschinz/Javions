package ch.epfl.javions.gui;

import ch.epfl.javions.*;
import ch.epfl.javions.Units.Angle;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import ch.epfl.javions.gui.ObservableAircraftState.GeoPosWithAltitude;
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

public final class AircraftManager {
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftProperty;
    private final Pane pane;

    public AircraftManager(MapParameters mapParameters,
                           ObservableSet<ObservableAircraftState> aircraftStates,
                           ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        assert aircraftStates.isEmpty(); // TODO should we instead create the initial nodes?

        var pane = new Pane();
        pane.getStylesheets().add("aircraft.css");
        pane.setPickOnBounds(false);

        this.mapParameters = mapParameters;
        this.selectedAircraftProperty = selectedAircraftProperty;
        this.pane = pane;

        installHandlers(aircraftStates);
    }

    private void installHandlers(ObservableSet<ObservableAircraftState> aircraftStates) {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasRemoved()) {
                var idToRemove = change.getElementRemoved().address().toString();
                pane.getChildren().removeIf(n -> idToRemove.equals(n.getId()));
            }
            if (change.wasAdded())
                pane.getChildren().add(groupForAircraft(change.getElementAdded()));
        });
    }

    private Node groupForAircraft(ObservableAircraftState aircraftState) {
        var group = new Group(lineGroupForAircraftTrajectory(aircraftState), nodeForAircraft(aircraftState));
        group.setId(aircraftState.address().toString());
        group.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        return group;
    }

    private Node nodeForAircraft(ObservableAircraftState aircraftState) {
        var address = aircraftState.address();
        var aircraftPath = new SVGPath();
        aircraftPath.getStyleClass().add("aircraft");

        var data = aircraftState.getFixedData();
        aircraftPath.setContent(iconFor(data.typeDesignator(), data.description(), aircraftState.getCategory(), data.wakeTurbulenceCategory()).svgPath());

        aircraftPath.fillProperty().bind(Bindings.createObjectBinding(
                () -> colorForAltitude(aircraftState.getAltitude()),
                aircraftState.altitudeProperty()));

        Tooltip tip = new Tooltip();
        tip.textProperty().bind(Bindings.when(aircraftState.callSignProperty().isEmpty())
                .then("(%s)".formatted(address.toString()))
                .otherwise(aircraftState.callSignProperty()));
        tip.setShowDelay(Duration.ZERO);
        tip.setHideDelay(Duration.seconds(1));
        Tooltip.install(aircraftPath, tip);

        aircraftPath.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    var pos = aircraftState.getPosition();
                    return pos != null
                            ? WebMercator.x(mapParameters.getZoom(), pos.longitude()) - mapParameters.getMinX()
                            : Double.NaN;
                },
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));
        aircraftPath.layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    var pos = aircraftState.getPosition();
                    return pos != null
                            ? WebMercator.y(mapParameters.getZoom(), pos.latitude()) - mapParameters.getMinY()
                            : Double.NaN;
                },
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        aircraftPath.rotateProperty().bind(aircraftState.trackOrHeadingProperty().multiply(Angle.RADIAN / Angle.DEGREE));

        aircraftPath.setOnMouseClicked(e -> selectedAircraftProperty.set(aircraftState));

        return aircraftPath;
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

    private Group lineGroupForAircraftTrajectory(ObservableAircraftState aircraftState) {
        var lineGroup = new Group();

        aircraftState.trajectory().addListener((InvalidationListener) c ->
                rebuildTrajectory(lineGroup, mapParameters.getZoom(), aircraftState.trajectory()));
        mapParameters.zoomProperty().addListener((p, o, n) ->
                rebuildTrajectory(lineGroup, n.intValue(), aircraftState.trajectory()));

        lineGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        lineGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        lineGroup.visibleProperty().bind(selectedAircraftProperty.isEqualTo(aircraftState));

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

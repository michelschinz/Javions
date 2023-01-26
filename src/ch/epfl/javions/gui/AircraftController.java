package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.Units.Angle;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.gui.ObservableAircraftState.GeoPosWithAltitude;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class AircraftController {
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftProperty;
    private final Pane pane;

    public AircraftController(MapParameters mapParameters,
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
        var layoutX = Bindings.createDoubleBinding(() ->
                {
                    if (aircraftState.getPosition() != null) {
                        var lon = aircraftState.getPosition().longitude();
                        return WebMercator.x(mapParameters.getZoom(), lon) - mapParameters.getMinX();
                    } else {
                        return Double.NaN;
                    }
                },
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty());
        var layoutY = Bindings.createDoubleBinding(() ->
                {
                    if (aircraftState.getPosition() != null) {
                        var lat = aircraftState.getPosition().latitude();
                        return WebMercator.y(mapParameters.getZoom(), lat) - mapParameters.getMinY();
                    } else {
                        return Double.NaN;
                    }
                },
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty());

        var group = new Group(
                trajectory(aircraftState),
                label(aircraftState, layoutX, layoutY),
                icon(aircraftState, layoutX, layoutY));
        group.setId(aircraftState.address().toString());
        group.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        return group;
    }

    private Node icon(ObservableAircraftState aircraftState,
                      DoubleBinding layoutX,
                      DoubleBinding layoutY) {
        var aircraftPath = new SVGPath();
        aircraftPath.getStyleClass().add("aircraft");

        // TODO the category can change, we should observe it!
        // TODO should the designator be valid (i.e. non-empty)?
        var fixedData = aircraftState.getFixedData();
        var aircraftIcon = fixedData != null
                ? AircraftIcon.iconFor(
                fixedData.typeDesignator(),
                fixedData.description(),
                aircraftState.getCategory(),
                fixedData.wakeTurbulenceCategory())
                : AircraftIcon.UNKNOWN;
        aircraftPath.setContent(aircraftIcon.svgPath());

        aircraftPath.fillProperty().bind(Bindings.createObjectBinding(
                () -> colorForAltitude(aircraftState.getAltitude()),
                aircraftState.altitudeProperty()));

        aircraftPath.layoutXProperty().bind(layoutX);
        aircraftPath.layoutYProperty().bind(layoutY);

        aircraftPath.rotateProperty().bind(aircraftState.trackOrHeadingProperty().multiply(Angle.RADIAN / Angle.DEGREE));

        aircraftPath.setOnMouseClicked(e -> selectedAircraftProperty.set(aircraftState));

        return aircraftPath;
    }

    private StringBinding optionalNumericString(DoubleExpression expression, double factor, String suffix) {
        return Bindings.createStringBinding(() -> {
            var value = expression.get();
            return Double.isNaN(value)
                    ? "? " + suffix
                    : String.format("%.0f %s", value * factor, suffix);
        }, expression);
    }

    private Node label(ObservableAircraftState aircraftState,
                       DoubleBinding layoutX,
                       DoubleBinding layoutY) {
        var fixedData = aircraftState.getFixedData();
        var name = (Object) null;
        if (fixedData != null) {
            name = fixedData.registration();
        } else {
            var callSign = Bindings.convert(aircraftState.callSignProperty());
            var icao24 = Bindings.createStringBinding(aircraftState.address()::toString);
            name = Bindings.when(callSign.isNotEmpty()).then(callSign).otherwise(icao24);
        }

        var velocity = optionalNumericString(aircraftState.velocityProperty(),
                1d / Units.Speed.KILOMETERS_PER_HOUR,
                "km/h");
        var altitude = optionalNumericString(aircraftState.altitudeProperty(),
                1d / Units.Distance.METER,
                "m");

        var label = new Text();
        label.textProperty().bind(Bindings.format("%s\n%s %s", name, velocity, altitude));

        var background = new Rectangle();
        background.widthProperty().bind(label.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        background.heightProperty().bind(label.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        var group = new Group(background, label);
        group.getStyleClass().add("label");
        group.layoutXProperty().bind(layoutX);
        group.layoutYProperty().bind(layoutY);

        return group;
    }

    private static Color colorForAltitude(double altitude) {
        // FIXME improve (and avoid the arbitrary constant)
        var scaledAltitude = altitude / (11_000d * Units.Distance.METER);
        return ColorRamp.PLASMA.at(Math.pow(scaledAltitude, 1d / 3d));
    }

    private Group trajectory(ObservableAircraftState aircraftState) {
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
package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.Units.Angle;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import ch.epfl.javions.gui.ObservableAircraftState.AirbornePos;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private static final double MAX_FLYING_ALTITUDE = 12_000;
    private static final AircraftTypeDesignator EMPTY_TYPE_DESIGNATOR = new AircraftTypeDesignator("");
    private static final AircraftDescription EMPTY_DESCRIPTION = new AircraftDescription("");

    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftProperty;
    private final Pane pane;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        Preconditions.checkArgument(aircraftStates.isEmpty());

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
                var idToRemove = change.getElementRemoved().address().string();
                pane.getChildren().removeIf(n -> idToRemove.equals(n.getId()));
            }
            if (change.wasAdded())
                pane.getChildren().add(groupForAircraft(change.getElementAdded()));
        });
    }

    private Node groupForAircraft(ObservableAircraftState aircraftState) {
        var layoutX = Bindings.createDoubleBinding(() -> {
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
        var layoutY = Bindings.createDoubleBinding(() -> {
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
        group.setId(aircraftState.address().string());
        group.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        return group;
    }

    private Node icon(ObservableAircraftState aircraftState,
                      DoubleBinding layoutX,
                      DoubleBinding layoutY) {
        var aircraftPath = new SVGPath();
        aircraftPath.getStyleClass().add("aircraft");

        var iconProperty = new SimpleObjectProperty<AircraftIcon>();
        var fixedData = aircraftState.aircraftData();
        var typeDesignator = fixedData != null ? fixedData.typeDesignator() : EMPTY_TYPE_DESIGNATOR;
        var description = fixedData != null ? fixedData.description() : EMPTY_DESCRIPTION;
        var wtc = fixedData != null ? fixedData.wakeTurbulenceCategory() : WakeTurbulenceCategory.UNKNOWN;
        iconProperty.bind(Bindings.createObjectBinding(() ->
                        AircraftIcon.iconFor(typeDesignator, description, aircraftState.getCategory(), wtc),
                aircraftState.categoryProperty()));

        aircraftPath.contentProperty().bind(iconProperty.map(AircraftIcon::svgPath));
        aircraftPath.fillProperty().bind(Bindings.createObjectBinding(
                () -> colorForAltitude(aircraftState.getAltitude()),
                aircraftState.altitudeProperty()));

        aircraftPath.layoutXProperty().bind(layoutX);
        aircraftPath.layoutYProperty().bind(layoutY);

        aircraftPath.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                        iconProperty.get().canRotate()
                                ? Units.convertTo(aircraftState.getTrackOrHeading(), Angle.DEGREE)
                                : 0d,
                iconProperty,
                aircraftState.trackOrHeadingProperty()));

        aircraftPath.setOnMouseClicked(e -> selectedAircraftProperty.set(aircraftState));

        return aircraftPath;
    }

    private Node label(ObservableAircraftState aircraftState,
                       DoubleBinding layoutX,
                       DoubleBinding layoutY) {
        var name = aircraftState.aircraftData() != null
                ? aircraftState.aircraftData().registration().string()
                : Bindings.when(aircraftState.callSignProperty().isNotNull())
                .then(Bindings.convert(aircraftState.callSignProperty().map(CallSign::string)))
                .otherwise(aircraftState.address().string());

        var velocity = optionalNumericString(aircraftState.velocityProperty(),
                Units.Speed.KILOMETER_PER_HOUR,
                "km/h");
        var altitude = optionalNumericString(aircraftState.altitudeProperty(),
                Units.Length.METER,
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

        group.visibleProperty().bind(
                mapParameters.zoomProperty().greaterThan(10)
                        .or(selectedAircraftProperty.isEqualTo(aircraftState)));

        return group;
    }

    private StringBinding optionalNumericString(DoubleExpression expression, double unit, String suffix) {
        return Bindings.createStringBinding(() ->
                Double.isNaN(expression.doubleValue())
                        ? "? " + suffix
                        : "%.0f %s".formatted(Units.convertTo(expression.doubleValue(), unit), suffix),
                expression);
    }

    private static Color colorForAltitude(double altitude) {
        return ColorRamp.PLASMA.at(Math.pow(altitude / MAX_FLYING_ALTITUDE, 1d / 3d));
    }

    private Node trajectory(ObservableAircraftState aircraftState) {
        var lineGroup = new Group();
        lineGroup.getStyleClass().add("trajectory");

        lineGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        lineGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        lineGroup.visibleProperty().bind(selectedAircraftProperty.isEqualTo(aircraftState));

        aircraftState.trajectory().addListener((InvalidationListener) c ->
                rebuildTrajectory(lineGroup, mapParameters.getZoom(), aircraftState.trajectory()));
        mapParameters.zoomProperty().addListener((p, o, n) ->
                rebuildTrajectory(lineGroup, n.intValue(), aircraftState.trajectory()));

        return lineGroup;
    }

    private static void rebuildTrajectory(Group group, int zoomLevel, List<AirbornePos> trajectory) {
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

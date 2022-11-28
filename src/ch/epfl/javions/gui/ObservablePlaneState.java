package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.PlaneStateSetter;
import ch.epfl.javions.adsb.WakeVortexCategory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ObservablePlaneState implements PlaneStateSetter {
    private final ObjectProperty<WakeVortexCategory> categoryProperty = new SimpleObjectProperty<>();
    private final StringProperty callSignProperty = new SimpleStringProperty("");
    private final ObjectProperty<GeoPos> positionProperty = new SimpleObjectProperty<>();
    private final ObservableList<GeoPos> trajectory = FXCollections.observableArrayList();
    private final DoubleProperty altitudeProperty = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocityProperty = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeadingProperty = new SimpleDoubleProperty(Double.NaN);

    private final ObservableList<GeoPos> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);

    public ObjectProperty<WakeVortexCategory> categoryProperty() {
        return categoryProperty;
    }

    public WakeVortexCategory getCategory() {
        return categoryProperty.get();
    }

    @Override
    public void setCategory(WakeVortexCategory category) {
        categoryProperty.set(category);
    }

    public StringProperty callSignProperty() {
        return callSignProperty;
    }

    public String getCallSign() {
        return callSignProperty.get();
    }

    @Override
    public void setCallSign(String callSign) {
        callSignProperty.set(callSign);
    }

    public ObjectProperty<GeoPos> positionProperty() {
        return positionProperty;
    }

    public GeoPos getPosition() {
        return positionProperty.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        positionProperty.set(position);
        trajectory.add(position);
    }

    public ObservableList<GeoPos> trajectory() {
        return unmodifiableTrajectory;
    }

    public DoubleProperty altitudeProperty() {
        return altitudeProperty;
    }

    public double getAltitude() {
        return altitudeProperty.get();
    }

    @Override
    public void setAltitude(double altitude) {
        altitudeProperty.set(altitude);
    }

    public DoubleProperty velocityProperty() {
        return velocityProperty;
    }

    public double getVelocity() {
        return velocityProperty.get();
    }

    @Override
    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }

    public DoubleProperty trackOrHeadingProperty() {
        return trackOrHeadingProperty;
    }

    public double getTrackOrHeading() {
        return trackOrHeadingProperty.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        trackOrHeadingProperty.set(trackOrHeading);
    }
}

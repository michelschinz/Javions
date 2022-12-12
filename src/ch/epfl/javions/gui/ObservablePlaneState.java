package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneStateSetter;
import ch.epfl.javions.adsb.WakeVortexCategory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ObservablePlaneState implements PlaneStateSetter {
    private final IcaoAddress address;
    private final ObjectProperty<WakeVortexCategory> categoryProperty;
    private final StringProperty callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<GeoPos> trajectory;
    private final ObservableList<GeoPos> unmodifiableTrajectory;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;

    public ObservablePlaneState(IcaoAddress address) {
        var trajectory = FXCollections.<GeoPos>observableArrayList();

        this.address = address;
        this.categoryProperty = new SimpleObjectProperty<>();
        this.callSignProperty = new SimpleStringProperty("");
        this.positionProperty = new SimpleObjectProperty<>();
        this.trajectory = trajectory;
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
        this.altitudeProperty = new SimpleDoubleProperty(Double.NaN);
        this.velocityProperty = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeadingProperty = new SimpleDoubleProperty(Double.NaN);
    }

    public IcaoAddress address() {
        return address;
    }

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

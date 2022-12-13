package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneStateSetter;
import ch.epfl.javions.adsb.WakeVortexCategory;
import ch.epfl.javions.db.AircraftDatabase;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ObservablePlaneState implements PlaneStateSetter {
    private final IcaoAddress address;
    private final LongProperty lastMessageTimeStampNsProperty;
    private final ObjectProperty<WakeVortexCategory> categoryProperty;
    private final StringProperty callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<GeoPos> trajectory;
    private final ObservableList<GeoPos> unmodifiableTrajectory;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;
    private final AircraftDatabase.AircraftData maybeAircraftData;

    public ObservablePlaneState(IcaoAddress address, AircraftDatabase.AircraftData maybeAircraftData) {
        var trajectory = FXCollections.<GeoPos>observableArrayList();

        this.address = address;
        this.lastMessageTimeStampNsProperty = new SimpleLongProperty();
        this.categoryProperty = new SimpleObjectProperty<>();
        this.callSignProperty = new SimpleStringProperty("");
        this.positionProperty = new SimpleObjectProperty<>();
        this.trajectory = trajectory;
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
        this.altitudeProperty = new SimpleDoubleProperty(Double.NaN);
        this.velocityProperty = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeadingProperty = new SimpleDoubleProperty(Double.NaN);
        this.maybeAircraftData = maybeAircraftData;
    }

    public IcaoAddress address() {
        return address;
    }

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNsProperty;
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNsProperty.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNsProperty.set(timeStampNs);
    }

    public String getRegistration() {
        // TODO return null or ""? or even Optional.empty()?
        return maybeAircraftData != null ? maybeAircraftData.registration() : "";
    }

    public String getTypeDesignator() {
        // TODO return null or ""? or even Optional.empty()?
        return maybeAircraftData != null ? maybeAircraftData.typeDesignator() : "";
    }

    public String getTypeDescription() {
        // TODO return null or ""? or even Optional.empty()?
        return maybeAircraftData != null ? maybeAircraftData.typeDescription() : "";
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

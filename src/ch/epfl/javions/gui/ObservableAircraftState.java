package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ObservableAircraftState implements AircraftStateSetter {
    public record GeoPosWithAltitude(GeoPos position, double altitude) {}

    private final IcaoAddress address;
    private final LongProperty lastMessageTimeStampNsProperty;
    private final IntegerProperty categoryProperty;
    private final ObjectProperty<CallSign> callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<GeoPosWithAltitude> trajectory;
    private final ObservableList<GeoPosWithAltitude> unmodifiableTrajectory;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;
    private final AircraftData maybeAircraftData;

    public ObservableAircraftState(IcaoAddress address, AircraftData maybeAircraftData) {
        var trajectory = FXCollections.<GeoPosWithAltitude>observableArrayList();

        this.address = address;
        this.lastMessageTimeStampNsProperty = new SimpleLongProperty();
        this.categoryProperty = new SimpleIntegerProperty();
        this.callSignProperty = new SimpleObjectProperty<>();
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

    public AircraftData getFixedData() {
        return maybeAircraftData;
    }

    public IntegerProperty categoryProperty() {
        return categoryProperty;
    }

    public int getCategory() {
        return categoryProperty.get();
    }

    @Override
    public void setCategory(int category) {
        categoryProperty.set(category);
    }

    public ObjectProperty<CallSign> callSignProperty() {
        return callSignProperty;
    }

    public CallSign getCallSign() {
        return callSignProperty.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
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
        trajectory.add(new GeoPosWithAltitude(position, getAltitude()));
    }

    public ObservableList<GeoPosWithAltitude> trajectory() {
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

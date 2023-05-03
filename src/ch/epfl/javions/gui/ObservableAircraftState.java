package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public final class ObservableAircraftState implements AircraftStateSetter {
    public record AirbornePos(GeoPos position, double altitude) {}

    private final IcaoAddress address;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNsProperty;
    private final IntegerProperty categoryProperty;
    private final ObjectProperty<CallSign> callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<AirbornePos> trajectory;
    private final ObservableList<AirbornePos> unmodifiableTrajectory;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;
    private long lastPositionTimeStampNs;

    public ObservableAircraftState(IcaoAddress address, AircraftData aircraftData) {
        var trajectory = FXCollections.<AirbornePos>observableArrayList();

        this.address = Objects.requireNonNull(address);
        this.aircraftData = aircraftData;
        this.lastMessageTimeStampNsProperty = new SimpleLongProperty();
        this.categoryProperty = new SimpleIntegerProperty();
        this.callSignProperty = new SimpleObjectProperty<>();
        this.positionProperty = new SimpleObjectProperty<>();
        this.trajectory = trajectory;
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
        this.altitudeProperty = new SimpleDoubleProperty(Double.NaN);
        this.velocityProperty = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeadingProperty = new SimpleDoubleProperty(Double.NaN);
        this.lastPositionTimeStampNs = -1L;
    }

    public IcaoAddress address() {
        return address;
    }

    public AircraftData aircraftData() {
        return aircraftData;
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

    public ReadOnlyIntegerProperty categoryProperty() {
        return categoryProperty;
    }

    public int getCategory() {
        return categoryProperty.get();
    }

    @Override
    public void setCategory(int category) {
        categoryProperty.set(category);
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSignProperty;
    }

    public CallSign getCallSign() {
        return callSignProperty.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        callSignProperty.set(callSign);
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return positionProperty;
    }

    public GeoPos getPosition() {
        return positionProperty.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        positionProperty.set(position);

        if (Double.isNaN(getAltitude())) return;
        trajectory.add(new AirbornePos(position, getAltitude()));
        lastPositionTimeStampNs = getLastMessageTimeStampNs();
    }

    public ObservableList<AirbornePos> trajectory() {
        return unmodifiableTrajectory;
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitudeProperty;
    }

    public double getAltitude() {
        return altitudeProperty.get();
    }

    @Override
    public void setAltitude(double altitude) {
        altitudeProperty.set(altitude);

        if (getPosition() == null) return;
        if (trajectory.isEmpty()) {
            trajectory.add(new AirbornePos(getPosition(), altitude));
            lastPositionTimeStampNs = getLastMessageTimeStampNs();
        } else if (lastPositionTimeStampNs == getLastMessageTimeStampNs()) {
            trajectory.set(trajectory.size() - 1, new AirbornePos(getPosition(), altitude));
        }
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocityProperty;
    }

    public double getVelocity() {
        return velocityProperty.get();
    }

    @Override
    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
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

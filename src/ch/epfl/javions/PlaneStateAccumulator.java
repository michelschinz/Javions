package ch.epfl.javions;

import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AirborneVelocityMessage;
import ch.epfl.javions.adsb.AircraftIdentificationMessage;
import ch.epfl.javions.adsb.Message;

import java.time.Duration;

public final class PlaneStateAccumulator {
    private static final long MAX_INTER_MESSAGE_NS =
            Duration.ofSeconds(10).toNanos();

    private AirbornePositionMessage lastPositionMessage = null;
    private final PlaneState.Builder stateBuilder = new PlaneState.Builder();

    public void update(Message message) {
        switch (message) {
            case AirborneVelocityMessage m -> stateBuilder
                    .setVelocity(m.velocity())
                    .setTrackOrHeading(m.trackOrHeading());

            case AirbornePositionMessage m -> {
                stateBuilder.setAltitude(m.altitude());
                if (isValidMessagePair(lastPositionMessage, m)) {
                    var messageE = m.isEven() ? m : lastPositionMessage;
                    var messageO = m.isEven() ? lastPositionMessage : m;
                    CprDecoder.decodePosition(
                                    Math.scalb(messageE.cprLon(), -17), Math.scalb(messageE.cprLat(), -17),
                                    Math.scalb(messageO.cprLon(), -17), Math.scalb(messageO.cprLat(), -17),
                                    m.isEven())
                            .ifPresent(stateBuilder::setPosition);
                }
                lastPositionMessage = m;
            }

            case AircraftIdentificationMessage m -> stateBuilder
                    .setCategory(m.category())
                    .setCallSign(m.callSign());
        }
    }

    private static boolean isValidMessagePair(AirbornePositionMessage m1, AirbornePositionMessage m2) {
        return m1 != null
               && m2 != null
               && m1.isEven() != m2.isEven()
               && Math.abs(m1.timeStamp() - m2.timeStamp()) <= MAX_INTER_MESSAGE_NS;
    }

    public PlaneState currentState() {
        return stateBuilder.build();
    }

    @Override
    public String toString() {
        return currentState().toString();
    }
}

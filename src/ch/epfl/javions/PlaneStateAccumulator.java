package ch.epfl.javions;

import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AirborneVelocityMessage;
import ch.epfl.javions.adsb.AircraftIdentificationMessage;
import ch.epfl.javions.adsb.Message;

public final class PlaneStateAccumulator {
    private AirbornePositionMessage lastPositionMessage = null;
    private final PlaneState.Builder stateBuilder = new PlaneState.Builder();

    public void update(Message message) {
        switch (message) {
            case AirborneVelocityMessage m -> stateBuilder
                    .setVelocity(m.velocity())
                    .setTrackOrHeading(m.trackOrHeading());

            case AirbornePositionMessage m -> {
                // TODO also check that the messages are not too far apart (10 s ?)
                if (lastPositionMessage != null && lastPositionMessage.isEven() != m.isEven()) {
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

    public PlaneState currentState() {
        return stateBuilder.build();
    }

    @Override
    public String toString() {
        return currentState().toString();
    }
}

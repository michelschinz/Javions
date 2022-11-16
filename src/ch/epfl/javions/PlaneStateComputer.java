package ch.epfl.javions;

import ch.epfl.javions.adsb.*;

public final class PlaneStateComputer {
    public static PlaneState update(PlaneState state, Message message) {
        return switch (message) {
            case AircraftIdentificationMessage m -> state
                    .withCategory(m.category())
                    .withCallSign(m.callSign());
            case AirborneVelocityMessage m -> state
                    .withVelocity(m.velocity())
                    .withTrackOrHeading(m.trackOrHeading());
            case AirbornePositionMessage m -> state
                    .withAltitude(m.altitude());
        };
    }
}

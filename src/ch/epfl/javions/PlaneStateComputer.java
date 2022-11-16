package ch.epfl.javions;

import ch.epfl.javions.adsb.*;

public final class PlaneStateComputer {
    public static PlaneState update(PlaneState state, Message message) {
        var builder = new PlaneState.Builder(state);
        switch (message) {
            case AircraftIdentificationMessage m -> builder
                    .setCategory(m.category())
                    .setCallSign(m.callSign());

            case AirbornePositionMessage m -> builder
                    .setAltitude(m.altitude());

            case AirborneVelocityMessage m -> builder
                    .setVelocity(m.velocity())
                    .setTrackOrHeading(m.trackOrHeading());
        }
        return builder.build();
    }
}

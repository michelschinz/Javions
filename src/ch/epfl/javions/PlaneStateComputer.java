package ch.epfl.javions;

import ch.epfl.javions.adsb.MessageDispatcher;
import ch.epfl.javions.adsb.MessageHandler;
import ch.epfl.javions.adsb.WakeVortexCategory;

public final class PlaneStateComputer {
    public static PlaneState update(PlaneState state, ByteString message) {
        return MessageDispatcher.dispatch(message, new MessageHandler<>() {
            public PlaneState onAircraftIdentification(WakeVortexCategory category, String callSign) {
                return state.withCategory(category).withCallSign(callSign);
            }

            public PlaneState onAirborneVelocity() {
                return state;
            }
        });
    }
}

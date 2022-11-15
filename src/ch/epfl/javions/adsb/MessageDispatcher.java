package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;

public final class MessageDispatcher {
    public static <R> R dispatch(ByteString message, MessageHandler<R> handler) {
        var rawTypeCode = MessageParser.rawTypeCode(message);
        var payload = MessageParser.rawPayload(message);

        return switch (MessageParser.typeCode(rawTypeCode)) {
            case AIRCRAFT_IDENTIFICATION -> {
                var category = AircraftIdentificationParser.parseCategory(
                        rawTypeCode,
                        MessageParser.rawCapability(message));
                var callSign = AircraftIdentificationParser.parseCallSign(payload);
                yield handler.onAircraftIdentification(category, callSign);
            }
            case AIRBORNE_VELOCITIES -> handler.onAirborneVelocity();
            default -> throw new Error("");
        };
    }
}

package ch.epfl.javions.adsb;

public final class MessageParser {
    private MessageParser() {}

    public static Message parse(RawMessage rawMessage) {
        return switch (rawMessage.typeCode()) {
            case 1, 2, 3, 4 ->
                    AircraftIdentificationMessage.of(rawMessage);
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 ->
                    AirbornePositionMessage.of(rawMessage);
            case 19 ->
                    AirborneVelocityMessage.of(rawMessage);
            default ->
                    null;
        };
    }
}

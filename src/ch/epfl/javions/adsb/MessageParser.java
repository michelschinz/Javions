package ch.epfl.javions.adsb;

public final class MessageParser {
    public static Message parse(RawAdsbMessage rawMessage) {
        return switch (rawMessage.typeCode()) {
            case 1, 2, 3, 4 -> new AircraftIdentificationMessage(rawMessage);
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 ->
                    new AirbornePositionMessage(rawMessage);
            case 19 -> new AirborneVelocityMessage(rawMessage);
            default -> null;
        };
    }
}

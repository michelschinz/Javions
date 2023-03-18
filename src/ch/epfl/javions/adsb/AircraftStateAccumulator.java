package ch.epfl.javions.adsb;

public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private static final long MAX_INTER_MESSAGE_NS = 10_000_000_000L;

    private final T stateSetter;
    private final AirbornePositionMessage[] lastPositionMessage = {null, null};

    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
        switch (message) {
            case AircraftIdentificationMessage m -> {
                stateSetter.setCategory(m.category());
                stateSetter.setCallSign(m.callSign());
            }

            case AirbornePositionMessage m -> {
                stateSetter.setAltitude(m.altitude());
                lastPositionMessage[m.parity()] = m;
                if (isValidMessagePair(lastPositionMessage[0], lastPositionMessage[1])) {
                    var x0 = lastPositionMessage[0].x();
                    var y0 = lastPositionMessage[0].y();
                    var x1 = lastPositionMessage[1].x();
                    var y1 = lastPositionMessage[1].y();
                    var maybePos = CprDecoder.decodePosition(x0, y0, x1, y1, m.parity());
                    if (maybePos != null) stateSetter.setPosition(maybePos);
                }
            }

            case AirborneVelocityMessage m -> {
                stateSetter.setVelocity(m.speed());
                stateSetter.setTrackOrHeading(m.trackOrHeading());
            }

            default -> throw new Error();
        }
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
    }

    private static boolean isValidMessagePair(AirbornePositionMessage m1, AirbornePositionMessage m2) {
        return m1 != null
                && m2 != null
                && Math.abs(m2.timeStampNs() - m1.timeStampNs()) <= MAX_INTER_MESSAGE_NS;
    }
}

package ch.epfl.javions.adsb;

import java.time.Duration;

import static java.lang.Math.scalb;

public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private static final long MAX_INTER_MESSAGE_NS =
            Duration.ofSeconds(10).toNanos();

    private final T stateSetter;
    private AirbornePositionMessage lastPositionMessage = null;

    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
        switch (message) {
            case AirborneVelocityMessage m -> {
                stateSetter.setVelocity(m.speed());
                stateSetter.setTrackOrHeading(m.trackOrHeading());
            }

            case AirbornePositionMessage m -> {
                stateSetter.setAltitude(m.altitude());
                if (isValidMessagePair(lastPositionMessage, m)) {
                    var message0 = m.parity() == 0 ? m : lastPositionMessage;
                    var message1 = m.parity() == 0 ? lastPositionMessage : m;
                    var maybePos = CprDecoder.decodePosition(
                            message0.x(), message0.y(), message1.x(), message1.y(), m.parity());
                    if (maybePos != null) stateSetter.setPosition(maybePos);
                }
                lastPositionMessage = m;
            }

            case AircraftIdentificationMessage m -> {
                stateSetter.setCategory(m.category());
                stateSetter.setCallSign(m.callSign());
            }

            default -> throw new Error();
        }
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
    }

    private static boolean isValidMessagePair(AirbornePositionMessage m1, AirbornePositionMessage m2) {
        return m1 != null
               && m2 != null
               && m1.parity() != m2.parity()
               && Math.abs(m1.timeStampNs() - m2.timeStampNs()) <= MAX_INTER_MESSAGE_NS;
    }
}

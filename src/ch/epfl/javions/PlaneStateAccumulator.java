package ch.epfl.javions;

import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AirborneVelocityMessage;
import ch.epfl.javions.adsb.AircraftIdentificationMessage;
import ch.epfl.javions.adsb.Message;

import java.time.Duration;

public final class PlaneStateAccumulator {
    private static final long MAX_INTER_MESSAGE_NS =
            Duration.ofSeconds(10).toNanos();

    private final PlaneStateSetter stateSetter;
    private AirbornePositionMessage lastPositionMessage = null;

    public PlaneStateAccumulator(PlaneStateSetter stateSetter) {
        this.stateSetter = stateSetter;
    }

    public void update(Message message) {
        switch (message) {
            case AirborneVelocityMessage m -> {
                stateSetter.setVelocity(m.velocity());
                stateSetter.setTrackOrHeading(m.trackOrHeading());
            }

            case AirbornePositionMessage m -> {
                stateSetter.setAltitude(m.altitude());
                if (isValidMessagePair(lastPositionMessage, m)) {
                    var messageE = m.isEven() ? m : lastPositionMessage;
                    var messageO = m.isEven() ? lastPositionMessage : m;
                    CprDecoder.decodePosition(
                                    messageE.cprLon(), messageE.cprLat(),
                                    messageO.cprLon(), messageO.cprLat(),
                                    m.isEven())
                            .ifPresent(stateSetter::setPosition);
                }
                lastPositionMessage = m;
            }

            case AircraftIdentificationMessage m -> {
                stateSetter.setCategory(m.category());
                stateSetter.setCallSign(m.callSign());
            }
        }
        stateSetter.setLastMessageTimeStampNs(message.timeStamp());
    }

    private static boolean isValidMessagePair(AirbornePositionMessage m1, AirbornePositionMessage m2) {
        return m1 != null
               && m2 != null
               && m1.isEven() != m2.isEven()
               && Math.abs(m1.timeStamp() - m2.timeStamp()) <= MAX_INTER_MESSAGE_NS;
    }
}

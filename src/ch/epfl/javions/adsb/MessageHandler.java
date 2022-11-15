package ch.epfl.javions.adsb;

public interface MessageHandler<R> {
    R onAircraftIdentification(WakeVortexCategory category, String callSign);

    R onAirborneVelocity();
}

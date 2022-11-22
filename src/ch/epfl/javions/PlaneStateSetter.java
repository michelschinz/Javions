package ch.epfl.javions;

import ch.epfl.javions.adsb.WakeVortexCategory;

public interface PlaneStateSetter {
    void setCategory(WakeVortexCategory category);
    void setCallSign(String callSign);
    void setPosition(GeoPos position);
    void setAltitude(double altitude);
    void setVelocity(double velocity);
    void setTrackOrHeading(double trackOrHeading);
}

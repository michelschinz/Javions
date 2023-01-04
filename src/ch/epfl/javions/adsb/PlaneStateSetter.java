package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

public interface PlaneStateSetter {
    void setLastMessageTimeStampNs(long timeStampNs);
    void setCategory(int category);
    void setCallSign(String callSign);
    void setPosition(GeoPos position);
    void setAltitude(double altitude);
    void setVelocity(double velocity);
    void setTrackOrHeading(double trackOrHeading);
}

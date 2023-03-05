package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

public interface Message {
    long timeStampNs();
    IcaoAddress icaoAddress();
}

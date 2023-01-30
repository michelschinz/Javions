package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

public abstract class Message {
    protected RawAdsbMessage rawMessage;

    public Message(RawAdsbMessage rawMessage) {
        this.rawMessage = rawMessage;
    }

    public long timeStampNs() {
        return rawMessage.timeStampNs();
    }

    public IcaoAddress icaoAddress() {
        return rawMessage.icaoAddress();
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

public abstract class Message {
    protected RawMessage rawMessage;

    public Message(RawMessage rawMessage) {
        this.rawMessage = rawMessage;
    }

    public long timeStampNs() {
        return rawMessage.timeStampNs();
    }

    public IcaoAddress icaoAddress() {
        return rawMessage.icaoAddress();
    }
}

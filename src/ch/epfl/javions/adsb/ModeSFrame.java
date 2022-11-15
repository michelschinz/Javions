package ch.epfl.javions.adsb;

import ch.epfl.javions.*;

// TODO replace with a class hierarchy, with one class per message type?
public final class ModeSFrame {
    public static final int BITS_LONG = 112;
    public static final int BYTES_LONG = BITS_LONG / Byte.SIZE;

    private static final int CPR_POSITION_BITS = 17;

    private static final int ALTITUDE_Q_BIT_INDEX = 5;
    private static final int ALTITUDE_Q_BIT_UPPER_MASK = ~0 << ALTITUDE_Q_BIT_INDEX;
    private static final int ALTITUDE_Q_BIT_LOWER_MASK = (1 << ALTITUDE_Q_BIT_INDEX) - 1;
    private static final double ALTITUDE_STEP = 25 * Units.Distance.FOOT;
    private static final double ALTITUDE_ORIGIN = -1000 * Units.Distance.FOOT;

    private final ByteString bytes;

    public ModeSFrame(ByteString bytes) {
        Preconditions.checkArgument(bytes.size() == BYTES_LONG);
        this.bytes = bytes;
    }

    public CprPosition cprPosition() {
        // FIXME throw some other exception
//        assert messageType() == MessageType.AIRBORNE_POSITION;

        var message = 0L; // FIXME
        var format = CprPosition.Format.values()[Bits.extractBit(message, 2 * CPR_POSITION_BITS)];
        var lon = Math.scalb(Bits.extractUInt(message, 0, CPR_POSITION_BITS), -CPR_POSITION_BITS);
        var lat = Math.scalb(Bits.extractUInt(message, CPR_POSITION_BITS, CPR_POSITION_BITS), -CPR_POSITION_BITS);
        return new CprPosition(format, lon, lat);
    }

    public double altitude() {
        // FIXME throw some other exception
//        assert messageType() == MessageType.AIRBORNE_POSITION;

        var message = 0L; // FIXME
        var encAltitude = Bits.extractUInt(message, 36, 12);
        var q = Bits.extractBit(encAltitude, 4);
        if (q == 0) {
            throw new Error("TODO");
        } else {
            var altitude = (encAltitude & ALTITUDE_Q_BIT_UPPER_MASK) >> 1
                           | (encAltitude & ALTITUDE_Q_BIT_LOWER_MASK);
            return ALTITUDE_ORIGIN + altitude * ALTITUDE_STEP;
        }
    }

    @Override
    public String toString() {
        return bytes.toString();
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.CprPosition;
import ch.epfl.javions.Units;

public record AirbornePositionMessage(
        long timeStamp,
        int icao,
        double altitude
) implements Message {
    private static final int CPR_POSITION_BITS = 17;

    private static final int ALTITUDE_Q_BIT_INDEX = 4;
    private static final int ALTITUDE_Q_BIT_UPPER_MASK = ~0 << (ALTITUDE_Q_BIT_INDEX + 1);
    private static final int ALTITUDE_Q_BIT_LOWER_MASK = (1 << ALTITUDE_Q_BIT_INDEX) - 1;
    private static final double ALTITUDE_UNIT = 25 * Units.Distance.FOOT;
    private static final double ALTITUDE_ORIGIN = -1000 * Units.Distance.FOOT;

    private static CprPosition cprPosition(long payload) {
        var format = CprPosition.Format.values()[Bits.extractBit(payload, 2 * CPR_POSITION_BITS)];
        var lon = Math.scalb(Bits.extractUInt(payload, 0, CPR_POSITION_BITS), -CPR_POSITION_BITS);
        var lat = Math.scalb(Bits.extractUInt(payload, CPR_POSITION_BITS, CPR_POSITION_BITS), -CPR_POSITION_BITS);
        return new CprPosition(format, lon, lat);
    }

    private static double altitude(long payload) {
        var encAltitude = Bits.extractUInt(payload, 36, 12);
        var q = Bits.extractBit(encAltitude, ALTITUDE_Q_BIT_INDEX);
        if (q == 0) {
            // FIXME implement
            return Double.NaN;
        } else {
            var altitude = (encAltitude & ALTITUDE_Q_BIT_UPPER_MASK) >> 1
                    | (encAltitude & ALTITUDE_Q_BIT_LOWER_MASK);
            return ALTITUDE_ORIGIN + altitude * ALTITUDE_UNIT;
        }
    }

    public static AirbornePositionMessage of(long timeStamp, ByteString bytes) {
        var icao = Message.icao(bytes);
        var altitude = altitude(Message.payload(bytes));
        return new AirbornePositionMessage(icao, altitude);
    }
}

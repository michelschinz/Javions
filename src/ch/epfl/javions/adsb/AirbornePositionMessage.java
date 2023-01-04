package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.Units;

public record AirbornePositionMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        boolean isEven,
        int cprLon,
        int cprLat,
        double altitude
) implements Message {
    private static final int CPR_POSITION_BITS = 17;

    private static final int ALTITUDE_Q_BIT_INDEX = 4;
    private static final int ALTITUDE_Q_BIT_UPPER_MASK = ~0 << (ALTITUDE_Q_BIT_INDEX + 1);
    private static final int ALTITUDE_Q_BIT_LOWER_MASK = (1 << ALTITUDE_Q_BIT_INDEX) - 1;
    private static final double ALTITUDE_UNIT = 25 * Units.Distance.FOOT;
    private static final double ALTITUDE_ORIGIN = -1000 * Units.Distance.FOOT;

    private static int cprLon(long payload) {
        return Bits.extractUInt(payload, 0, CPR_POSITION_BITS);
    }

    private static int cprLat(long payload) {
        return Bits.extractUInt(payload, CPR_POSITION_BITS, CPR_POSITION_BITS);
    }

    private static int cprFormat(long payload) {
        return Bits.extractBit(payload, 2 * CPR_POSITION_BITS);
    }

    private static double altitude(long payload) {
        var encAltitude = Bits.extractUInt(payload, 36, 12);
        var q = Bits.extractBit(encAltitude, ALTITUDE_Q_BIT_INDEX);
        if (q == 0) {
            // FIXME implement (see https://www.wikiwand.com/en/Gillham_code and http://www.ccsinfo.com/forum/viewtopic.php?p=140960#140960)
            return Double.NaN;
        } else {
            var altitude = (encAltitude & ALTITUDE_Q_BIT_UPPER_MASK) >> 1
                    | (encAltitude & ALTITUDE_Q_BIT_LOWER_MASK);
            return ALTITUDE_ORIGIN + altitude * ALTITUDE_UNIT;
        }
    }

    public static AirbornePositionMessage of(long timeStamp, ByteString bytes) {
        var icao = Message.icaoAddress(bytes);
        var payload = Message.payload(bytes);
        var isEven = cprFormat(payload) == 0;
        var cprLon = cprLon(payload);
        var cprLat = cprLat(payload);
        var altitude = altitude(payload);
        return new AirbornePositionMessage(timeStamp, icao, isEven, cprLon, cprLat, altitude);
    }
}

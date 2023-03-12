package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AirbornePositionMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double altitude,
                                      int parity,
                                      double x,
                                      double y) implements Message {
    private static final int CPR_BITS = 17;

    private static final int LON_CPR_START = 0;
    private static final int LON_CPR_SIZE = CPR_BITS;
    private static final int LAT_CPR_START = LON_CPR_START + LON_CPR_SIZE;
    private static final int LAT_CPR_SIZE = CPR_BITS;
    private static final int FORMAT_START = LAT_CPR_START + LAT_CPR_SIZE;
    private static final int FORMAT_SIZE = 1;
    private static final int TIME_START = FORMAT_START + FORMAT_SIZE;
    private static final int TIME_SIZE = 1;
    private static final int ALT_START = TIME_START + TIME_SIZE;
    private static final int ALT_SIZE = 12;

    private static final int ALTITUDE_Q_BIT_INDEX = 4;
    private static final int ALTITUDE_Q_BIT_UPPER_MASK = ~0 << (ALTITUDE_Q_BIT_INDEX + 1);
    private static final int ALTITUDE_Q_BIT_LOWER_MASK = (1 << ALTITUDE_Q_BIT_INDEX) - 1;

    public static AirbornePositionMessage of(RawMessage rawMessage) {
        var payload = rawMessage.payload();
        return new AirbornePositionMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                altitude(payload),
                Bits.extractUInt(payload, FORMAT_START, FORMAT_SIZE),
                normalizeCpr(Bits.extractUInt(payload, LON_CPR_START, LON_CPR_SIZE)),
                normalizeCpr(Bits.extractUInt(payload, LAT_CPR_START, LAT_CPR_SIZE)));
    }

    private static double normalizeCpr(int cpr) {
        return Math.scalb((double) cpr, -CPR_BITS);
    }

    private static double altitude(long payload) {
        var encAltitude = Bits.extractUInt(payload, ALT_START, ALT_SIZE);
        if (Bits.testBit(encAltitude, ALTITUDE_Q_BIT_INDEX)) {
            var ft25 = (encAltitude & ALTITUDE_Q_BIT_UPPER_MASK) >> 1
                    | (encAltitude & ALTITUDE_Q_BIT_LOWER_MASK);
            return Units.convertFrom(-1000 + ft25 * 25, Units.Length.FOOT);
        } else {
            return decodeGillhamAltitude(permuteGillham(encAltitude));
        }
    }

    //         11 10  9  8  7  6  5  4  3  2  1  0
    //  Input: C1 A1 C2 A2 C4 A4 B1 D1 B2 D2 B4 D4
    // Output: D1 D2 D4 A1 A2 A4 B1 B2 B4 C1 C2 C4
    private static int permuteGillham(int shuffled) {
        var unshuffled = 0;
        for (var i : new int[] {4, 10, 5, 11}) {
            for (var j = 0; j < 5; j += 2) {
                var bit = (shuffled >> (i - j)) & 1;
                unshuffled = (unshuffled << 1) | bit;
            }
        }
        return unshuffled;
    }

    private static double decodeGillhamAltitude(int encAltitude) {
        // Algorithm taken from http://www.ccsinfo.com/forum/viewtopic.php?p=140960#140960
        var ft100 = gray16ToBinary(Bits.extractUInt(encAltitude, 0, 3));
        if (ft100 == 0 || ft100 == 5 || ft100 == 6) return Double.NaN;
        if (ft100 == 7) ft100 = 5;

        var ft500 = gray16ToBinary(Bits.extractUInt(encAltitude, 3, 9));
        if ((ft500 & 1) == 1) ft100 = 6 - ft100;
        return Units.convertFrom(-1300 + ft100 * 100 + ft500 * 500, Units.Length.FOOT);
    }

    private static int gray16ToBinary(int gray) {
        assert 0 <= gray && gray < (1 << 16);
        var binary = gray;
        for (var i = 8; i > 0; i >>= 1) binary ^= binary >> i;
        return binary;
    }
}

package ch.epfl.javions.adsb;

import ch.epfl.javions.BitUnpacker;
import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.Units;

import static ch.epfl.javions.BitUnpacker.field;

public record AirbornePositionMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        boolean isEven,
        int cprLon,
        int cprLat,
        double altitude
) implements Message {
    // TODO remaining fields
    private enum Field {LONGITUDE, LATITUDE, FORMAT, TIME, ALT}

    private static final BitUnpacker<Field> UNPACKER = new BitUnpacker<>(
            field(Field.LONGITUDE, 17),
            field(Field.LATITUDE, 17),
            field(Field.FORMAT, 1),
            field(Field.TIME, 1),
            field(Field.ALT, 12)
    );

    private static final int ALTITUDE_Q_BIT_INDEX = 4;
    private static final int ALTITUDE_Q_BIT_UPPER_MASK = ~0 << (ALTITUDE_Q_BIT_INDEX + 1);
    private static final int ALTITUDE_Q_BIT_LOWER_MASK = (1 << ALTITUDE_Q_BIT_INDEX) - 1;
    private static final double ALTITUDE_UNIT = 25 * Units.Distance.FOOT;
    private static final double ALTITUDE_ORIGIN = -1000 * Units.Distance.FOOT;

    private static double altitude(long payload) {
        var encAltitude = UNPACKER.unpack(Field.ALT, payload);
        if (Bits.testBit(encAltitude, ALTITUDE_Q_BIT_INDEX)) {
            var altitude = (encAltitude & ALTITUDE_Q_BIT_UPPER_MASK) >> 1
                    | (encAltitude & ALTITUDE_Q_BIT_LOWER_MASK);
            return ALTITUDE_ORIGIN + altitude * ALTITUDE_UNIT;
        } else {
            // FIXME implement (see https://www.wikiwand.com/en/Gillham_code and http://www.ccsinfo.com/forum/viewtopic.php?p=140960#140960)
            return Double.NaN;
        }
    }

    public static AirbornePositionMessage of(long timeStamp, ByteString bytes) {
        var payload = Message.payload(bytes);
        return new AirbornePositionMessage(
                timeStamp,
                Message.icaoAddress(bytes),
                UNPACKER.unpack(Field.FORMAT, payload) == 0,
                UNPACKER.unpack(Field.LONGITUDE, payload),
                UNPACKER.unpack(Field.LATITUDE, payload),
                altitude(payload));
    }
}

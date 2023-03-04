package ch.epfl.javions.adsb;

import ch.epfl.javions.BitUnpacker;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import static ch.epfl.javions.BitUnpacker.field;

public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      VelocityType velocityType,
                                      double velocity,
                                      double trackOrHeading) implements Message {
    public enum VelocityType {GROUND, AIR}

    private enum Field {
        GNSS_BAROMETER_DIFFERENCE,
        GNSS_BAROMETER_DIFFERENCE_SIGN,
        RESERVED,
        VERTICAL_RATE,
        VERTICAL_RATE_SIGN,
        VERTICAL_RATE_SOURCE,
        SPECIFIC_1,
        SPECIFIC_2,
        SPECIFIC_3,
        SPECIFIC_4,
        NAVIGATION_UNCERTAINTY,
        IFR_CAPABLE,
        INTENT_CHANGE,
        SUB_TYPE
    }

    // Specific fields for subtypes 1 and 2
    private static final Field FIELD_NS_VELOCITY = Field.SPECIFIC_1;
    private static final Field FIELD_NS_VELOCITY_DIRECTION = Field.SPECIFIC_2;
    private static final Field FIELD_EW_VELOCITY = Field.SPECIFIC_3;
    private static final Field FIELD_EW_VELOCITY_DIRECTION = Field.SPECIFIC_4;

    // Specific fields for subtypes 3 and 4
    private static final Field FIELD_AIRSPEED = Field.SPECIFIC_1;
    private static final Field FIELD_AIRSPEED_TYPE = Field.SPECIFIC_2;
    private static final Field FIELD_HEADING = Field.SPECIFIC_3;
    private static final Field FIELD_HEADING_STATUS = Field.SPECIFIC_4;

    private static final BitUnpacker<Field> UNPACKER = new BitUnpacker<>(
            field(Field.GNSS_BAROMETER_DIFFERENCE, 7),
            field(Field.GNSS_BAROMETER_DIFFERENCE_SIGN, 1),
            field(Field.RESERVED, 2),
            field(Field.VERTICAL_RATE, 9),
            field(Field.VERTICAL_RATE_SIGN, 1),
            field(Field.VERTICAL_RATE_SOURCE, 1),
            field(Field.SPECIFIC_1, 10),
            field(Field.SPECIFIC_2, 1),
            field(Field.SPECIFIC_3, 10),
            field(Field.SPECIFIC_4, 1),
            field(Field.NAVIGATION_UNCERTAINTY, 3),
            field(Field.IFR_CAPABLE, 1),
            field(Field.INTENT_CHANGE, 1),
            field(Field.SUB_TYPE, 3));

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        var payload = rawMessage.payload();
        return new AirborneVelocityMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                velocityType(payload),
                velocity(payload),
                trackOrHeading(payload));
    }

    private static int subType(long payload) {
        return UNPACKER.unpack(Field.SUB_TYPE, payload);
    }

    private static double unit(long payload) {
        return switch (subType(payload)) {
            case 1, 3 -> Units.Speed.KNOT;
            case 2, 4 -> Units.Speed.KNOT * 4;
            default -> Double.NaN; // TODO do something else?
        };
    }

    private static boolean hasVelocity(long payload) {
        var subType = subType(payload);
        return 1 <= subType && subType <= 4 && velocityEW(payload) != 0 && velocityNS(payload) != 0;
    }

    private static VelocityType velocityType(long payload) {
        return switch (subType(payload)) {
            case 1, 2 -> VelocityType.GROUND;
            case 3, 4 -> VelocityType.AIR;
            default -> null; // TODO do something else?
        };
    }

    private static int velocityEW(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var ew = UNPACKER.unpack(FIELD_EW_VELOCITY, payload) - 1;
        // TODO handle the case when there is no data (ew == 0)
        return UNPACKER.unpack(FIELD_EW_VELOCITY_DIRECTION, payload) == 0 ? ew : -ew;
    }

    private static int velocityNS(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var ns = UNPACKER.unpack(FIELD_NS_VELOCITY, payload) - 1;
        // TODO handle the case when there is no data (ns == 0)
        return UNPACKER.unpack(FIELD_NS_VELOCITY_DIRECTION, payload) == 0 ? ns : -ns;
    }

    private static double track(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var signedTrack = Math.atan2(velocityEW(payload), velocityNS(payload));
        return signedTrack < 0 ? signedTrack + Units.Angle.TURN : signedTrack;
    }

    private static double heading(long payload) {
        assert velocityType(payload) == VelocityType.AIR;
        return Units.convertFrom(Math.scalb(UNPACKER.unpack(FIELD_HEADING, payload), -10), Units.Angle.TURN);
    }

    private static double velocity(long payload) {
        var value = switch (velocityType(payload)) {
            case GROUND -> Math.hypot(velocityNS(payload), velocityEW(payload));
            case AIR -> UNPACKER.unpack(FIELD_AIRSPEED, payload) - 1;
        };
        return Units.convertFrom(value, unit(payload));
    }

    private static double trackOrHeading(long payload) {
        return switch (velocityType(payload)) {
            case GROUND -> track(payload);
            case AIR -> heading(payload);
        };
    }
}

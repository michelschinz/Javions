package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      VelocityType velocityType,
                                      double velocity,
                                      double trackOrHeading) implements Message {
    public enum VelocityType {GROUND, AIR}

    private static final int GNSS_BAROMETER_DIFFERENCE_START = 0;
    private static final int GNSS_BAROMETER_DIFFERENCE_SIZE = 8;
    private static final int RESERVED_START = GNSS_BAROMETER_DIFFERENCE_START + GNSS_BAROMETER_DIFFERENCE_SIZE;
    private static final int RESERVED_SIZE = 2;
    private static final int VERTICAL_RATE_START = RESERVED_START + RESERVED_SIZE;
    private static final int VERTICAL_RATE_SIZE = 11;
    private static final int SPECIFIC_START = VERTICAL_RATE_START + VERTICAL_RATE_SIZE;
    private static final int SPECIFIC_SIZE = 22;
    private static final int NAVIGATION_UNCERTAINTY_START = SPECIFIC_START + SPECIFIC_SIZE;
    private static final int NAVIGATION_UNCERTAINTY_SIZE = 3;
    private static final int IFR_CAPABLE_START = NAVIGATION_UNCERTAINTY_START + NAVIGATION_UNCERTAINTY_SIZE;
    private static final int IFR_CAPABLE_SIZE = 1;
    private static final int INTENT_CHANGE_START = IFR_CAPABLE_START + IFR_CAPABLE_SIZE;
    private static final int INTENT_CHANGE_SIZE = 1;
    private static final int SUB_TYPE_START = INTENT_CHANGE_START + INTENT_CHANGE_SIZE;
    private static final int SUB_TYPE_SIZE = 3;

    // Specific fields for subtypes 1 and 2
    private static final int NS_VELOCITY_START = SPECIFIC_START;
    private static final int NS_VELOCITY_SIZE = 10;
    private static final int NS_VELOCITY_DIRECTION_START = NS_VELOCITY_START + NS_VELOCITY_SIZE;
    private static final int NS_VELOCITY_DIRECTION_SIZE = 1;
    private static final int EW_VELOCITY_START = NS_VELOCITY_DIRECTION_START + NS_VELOCITY_DIRECTION_SIZE;
    private static final int EW_VELOCITY_SIZE = 10;
    private static final int EW_VELOCITY_DIRECTION_START = EW_VELOCITY_START + EW_VELOCITY_SIZE;
    private static final int EW_VELOCITY_DIRECTION_SIZE = 1;

    // Specific fields for subtypes 3 and 4
    private static final int AIRSPEED_START = SPECIFIC_START;
    private static final int AIRSPEED_SIZE = 10;
    private static final int AIRSPEED_TYPE_START = AIRSPEED_START + AIRSPEED_SIZE;
    private static final int AIRSPEED_TYPE_SIZE = 1;
    private static final int HEADING_START = AIRSPEED_TYPE_START + AIRSPEED_TYPE_SIZE;
    private static final int HEADING_SIZE = 10;
    private static final int HEADING_STATUS_START = HEADING_START + HEADING_SIZE;
    private static final int HEADING_STATUS_SIZE = 1;

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
        return Bits.extractUInt(payload, SUB_TYPE_START, SUB_TYPE_SIZE);
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
        var ew = Bits.extractUInt(payload, EW_VELOCITY_START, EW_VELOCITY_SIZE) - 1;
        // TODO handle the case when there is no data (ew == 0)
        return Bits.testBit(payload, EW_VELOCITY_DIRECTION_START) ? -ew : ew;
    }

    private static int velocityNS(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var ns = Bits.extractUInt(payload, NS_VELOCITY_START, NS_VELOCITY_SIZE) - 1;
        // TODO handle the case when there is no data (ew == 0)
        return Bits.testBit(payload, NS_VELOCITY_DIRECTION_START) ? -ns : ns;
    }

    private static double track(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var signedTrack = Math.atan2(velocityEW(payload), velocityNS(payload));
        return signedTrack < 0 ? signedTrack + Units.Angle.TURN : signedTrack;
    }

    private static double heading(long payload) {
        assert velocityType(payload) == VelocityType.AIR;
        var headingField = Bits.extractUInt(payload, HEADING_START, HEADING_SIZE);
        return Units.convertFrom(Math.scalb(headingField, -HEADING_SIZE), Units.Angle.TURN);
    }

    private static double velocity(long payload) {
        var value = switch (velocityType(payload)) {
            case GROUND -> Math.hypot(velocityNS(payload), velocityEW(payload));
            case AIR -> Bits.extractUInt(payload, AIRSPEED_START, AIRSPEED_SIZE) - 1;
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

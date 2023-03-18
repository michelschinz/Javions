package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double speed,
                                      double trackOrHeading) implements Message {
    // Payload contents
    private static final int SUBTYPE_SPECIFIC_START = 21;
    private static final int SUBTYPE_SPECIFIC_SIZE = 22;
    private static final int SUBTYPE_START = 48;
    private static final int SUBTYPE_SIZE = 3;

    // Specific fields for subtypes 1 and 2
    private static final int MAGNITUDE_SIZE = 10;
    private static final int SIGN_SIZE = 1;
    private static final int NS_SPEED_START = 0;
    private static final int EW_SPEED_START = NS_SPEED_START + MAGNITUDE_SIZE + SIGN_SIZE;

    // Specific fields for subtypes 3 and 4
    private static final int AIRSPEED_START = 0;
    private static final int AIRSPEED_SIZE = 10;
    private static final int AIRSPEED_TYPE_START = AIRSPEED_START + AIRSPEED_SIZE;
    private static final int AIRSPEED_TYPE_SIZE = 1;
    private static final int HEADING_START = AIRSPEED_TYPE_START + AIRSPEED_TYPE_SIZE;
    private static final int HEADING_SIZE = 10;
    private static final int HEADING_STATUS = HEADING_START + HEADING_SIZE;

    private static final int SCALE_SUBSONIC = 0;
    private static final int SCALE_SUPERSONIC = 2;

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        var payload = rawMessage.payload();
        var subType = Bits.extractUInt(payload, SUBTYPE_START, SUBTYPE_SIZE);
        var data = Bits.extractUInt(payload, SUBTYPE_SPECIFIC_START, SUBTYPE_SPECIFIC_SIZE);
        return switch (subType) {
            case 1 -> groundSpeed(rawMessage, data, SCALE_SUBSONIC);
            case 2 -> groundSpeed(rawMessage, data, SCALE_SUPERSONIC);
            case 3 -> airSpeed(rawMessage, data, SCALE_SUBSONIC);
            case 4 -> airSpeed(rawMessage, data, SCALE_SUPERSONIC);
            default -> null;
        };
    }

    public AirborneVelocityMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(speed >= 0);
        Preconditions.checkArgument(trackOrHeading >= 0);
    }

    private static AirborneVelocityMessage of(RawMessage rawMessage, double speed, double trackOrHeading) {
        var timeStampNs = rawMessage.timeStampNs();
        var icaoAddress = rawMessage.icaoAddress();
        return new AirborneVelocityMessage(timeStampNs, icaoAddress, speed, trackOrHeading);
    }

    private static AirborneVelocityMessage groundSpeed(RawMessage rawMessage, int data, int speedScale) {
        var vX = speedComponent(data, EW_SPEED_START);
        var vY = speedComponent(data, NS_SPEED_START);
        var speed = convertSpeed(Math.hypot(vX, vY), speedScale);
        var track = Math.atan2(vX, vY);
        if (track < 0) track += Units.Angle.TURN;
        return of(rawMessage, speed, track);
    }

    private static double speedComponent(int data, int startBit) {
        var magnitude = Bits.extractUInt(data, startBit, MAGNITUDE_SIZE);
        var isNegative = Bits.testBit(data, startBit + MAGNITUDE_SIZE);
        if (magnitude == 0) return Double.NaN;
        else if (isNegative) return -(magnitude - 1);
        else return magnitude - 1;
    }

    private static AirborneVelocityMessage airSpeed(RawMessage rawMessage, int data, int speedScale) {
        if (!Bits.testBit(data, HEADING_STATUS)) return null;

        var speedKnot = Bits.extractUInt(data, AIRSPEED_START, AIRSPEED_SIZE) - 1;
        if (speedKnot == -1) return null;

        var headingBits = Bits.extractUInt(data, HEADING_START, HEADING_SIZE);
        var heading = Units.convertFrom(Math.scalb(headingBits, -HEADING_SIZE), Units.Angle.TURN);

        var speed = convertSpeed(speedKnot, speedScale);
        return of(rawMessage, speed, heading);
    }

    private static double convertSpeed(double speedKnot, int speedScale) {
        return Math.scalb(Units.convertFrom(speedKnot, Units.Speed.KNOT), speedScale);
    }
}

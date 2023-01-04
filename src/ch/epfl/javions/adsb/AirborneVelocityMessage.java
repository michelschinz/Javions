package ch.epfl.javions.adsb;

import ch.epfl.javions.*;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AirborneVelocityMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        VelocityType velocityType,
        double velocity,
        double trackOrHeading
) implements Message {

    public enum VelocityType {GROUND, AIR}

    private static int subType(long payload) {
        return Bits.extractUInt(payload, 48, 3);
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

    public static VelocityType velocityType(long payload) {
        return switch (subType(payload)) {
            case 1, 2 -> VelocityType.GROUND;
            case 3, 4 -> VelocityType.AIR;
            default -> null; // TODO do something else?
        };
    }

    private static int velocityEW(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var ew = Bits.extractUInt(payload, 21 + 11, 10) - 1;
        // TODO handle the case when there is no data (ew == 0)
        if (Bits.extractBit(payload, 21 + 21) == 1) ew = -ew;
        return ew;
    }

    private static int velocityNS(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var ns = Bits.extractUInt(payload, 21 + 0, 10) - 1;
        // TODO handle the case when there is no data (ns == 0)
        if (Bits.extractBit(payload, 21 + 10) == 1) ns = -ns;
        return ns;
    }

    private static double track(long payload) {
        assert velocityType(payload) == VelocityType.GROUND;
        var signedTrack = Math.atan2(velocityEW(payload), velocityNS(payload));
        return signedTrack < 0 ? signedTrack + Math2.TAU : signedTrack;
    }

    private static double heading(long payload) {
        assert velocityType(payload) == VelocityType.AIR;
        return Math2.TAU * Math.scalb(Bits.extractUInt(payload, 21 + 11, 10), -10);
    }

    public static double velocity(long payload) {
        return switch (velocityType(payload)) {
            case GROUND -> Math.hypot(velocityNS(payload), velocityEW(payload));
            case AIR -> (Bits.extractUInt(payload, 21 + 0, 10) - 1);
        } * unit(payload);
    }

    public static double trackOrHeading(long payload) {
        return switch (velocityType(payload)) {
            case GROUND -> track(payload);
            case AIR -> heading(payload);
        };
    }

    public static boolean isValid(ByteString message) {
        var subType = subType(Message.payload(message));
        return 1 <= subType && subType <= 4;
    }

    public static AirborneVelocityMessage of(long timeStamp, ByteString message) {
        var payload = Message.payload(message);
        var subType = subType(payload);
        // TODO messages with invalid subtype probably contain other data nevertheless!
        //   => do not ignore them, but make "velocity" return NaN for example.
        if (!(1 <= subType && subType <= 4)) return null;

        var icao = Message.icaoAddress(message);
        var type = velocityType(payload);
        var velocity = velocity(payload);
        var trackOrHeading = trackOrHeading(payload);
        return new AirborneVelocityMessage(timeStamp, icao, type, velocity, trackOrHeading);
    }
}

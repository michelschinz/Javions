package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;

public final class AirborneVelocityParser {
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

    public VelocityType velocityType(long payload) {
        return switch (subType(payload)) {
            case 1, 2 -> VelocityType.GROUND;
            case 3, 4 -> VelocityType.AIR;
            default -> null; // TODO do something else?
        };
    }

    public static double velocity(long payload) {
        var unit = unit(payload);
        var ns = Bits.extractSInt(payload, 0, 11) * unit;
        var ew = Bits.extractSInt(payload, 11, 11) * unit;
        return Math.hypot(ns, ew);
    }
}

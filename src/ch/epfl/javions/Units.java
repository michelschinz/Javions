package ch.epfl.javions;

import java.util.function.DoubleUnaryOperator;

public final class Units {
    /*
     Rules:
     1. to convert a value to the base unit, multiply it by its unit, e.g.:
          var length = 6 * Distance.FOOT;  // length = 1.8288 (6 ft in m)
     2. to convert a value from the base unit to another unit, divide by its unit,
        but using a multiplication, e.g.:
          var lengthInFeet = length * (Distance.METER / Distance.FOOT);
    */

    public static DoubleUnaryOperator converter(double toUnit) {
        return v -> v * (1d / toUnit);
    }

    // SI prefixes
    private static final double CENTI = 1e-2;
    private static final double KILO = 1e3;

    public static final class Angle {
        public static final double RADIAN = 1;
        public static final double TURN = 2 * Math.PI * RADIAN;
        public static final double DEGREE = TURN / 360;
        public static final double T32 = Math.scalb(TURN, -32);
    }

    public static final class Time {
        public static final double SECOND = 1;
        public static final double MINUTE = 60 * SECOND;
        public static final double HOUR = 60 * MINUTE;
    }

    public static class Distance {
        public static final double METER = 1;
        public static final double CENTIMETER = CENTI * METER;
        public static final double KILOMETER = KILO * METER;
        public static final double INCH = 2.54 * CENTIMETER;
        public static final double FOOT = 12 * INCH;
        public static final double NAUTICAL_MILE = 1852 * METER;
    }

    public static final class Speed {
        public static final double KNOT = Distance.NAUTICAL_MILE / Time.HOUR;
        public static final double KILOMETERS_PER_HOUR = Distance.KILOMETER / Time.HOUR;
    }
}

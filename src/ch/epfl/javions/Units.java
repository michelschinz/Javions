package ch.epfl.javions;

public final class Units {
    public static final class Angle {
        public static final double RADIAN = 1;
        public static final double TURN = 2 * Math.PI * RADIAN;
        public static final double DEGREE = TURN / 360;
    }

    public static final class Time {
        public static final double SECOND = 1;
        public static final double MINUTE = 60 * SECOND;
        public static final double HOUR = 60 * MINUTE;
    }

    public static class Distance {
        public static final double METER = 1;
        public static final double CENTIMETER = METER / 100;
        public static final double INCH = 2.54 * CENTIMETER;
        public static final double FOOT = 12 * INCH;
        public static final double NAUTICAL_MILE = 1852 * METER;
    }

    public static final class Speed {
        public static final double METERS_PER_SECOND = Distance.METER / Time.SECOND;
        public static final double FEET_PER_MINUTE = Distance.FOOT / Time.MINUTE;
        public static final double KNOT = Distance.NAUTICAL_MILE / Time.HOUR;
    }
}

package ch.epfl.javions;

public final class Units {
    private Units() {}

    // SI prefixes
    public static final double CENTI = 1e-2;
    public static final double KILO = 1e3;

    public static final class Angle {
        private Angle() {}

        public static final double RADIAN = 1;
        public static final double TURN = Math.scalb(Math.PI, 1) * RADIAN;
        public static final double DEGREE = TURN / 360;
        public static final double T32 = Math.scalb(TURN, -32);
    }

    public static class Length {
        private Length() {}

        public static final double METER = 1;
        public static final double CENTIMETER = CENTI * METER;
        public static final double KILOMETER = KILO * METER;
        public static final double INCH = 2.54 * CENTIMETER;
        public static final double FOOT = 12 * INCH;
        public static final double NAUTICAL_MILE = 1852 * METER;
    }

    public static final class Time {
        private Time() {}

        public static final double SECOND = 1;
        public static final double MINUTE = 60 * SECOND;
        public static final double HOUR = 60 * MINUTE;
    }

    public static final class Speed {
        private Speed() {}

        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        public static final double KILOMETERS_PER_HOUR = Length.KILOMETER / Time.HOUR;
    }

    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    public static double convertTo(double value, double toUnit) {
        return value * (1d / toUnit);
    }
}

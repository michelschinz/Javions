package ch.epfl.javions;

public record GeoPos(int intLon, int intLat) {
    private static final int INT_90_DEGREES = 1 << (Integer.SIZE - 2);

    public static boolean isValid(int intLon, int intLat) {
        return -INT_90_DEGREES <= intLat && intLat <= INT_90_DEGREES;
    }

    public GeoPos {
        Preconditions.checkArgument(isValid(intLon, intLat));
    }

    public double longitude() {
        return intLon * Units.Angle.T32;
    }

    public double latitude() {
        return intLat * Units.Angle.T32;
    }

    @Override
    public String toString() {
        var lonDeg = intLon * (Units.Angle.T32 / Units.Angle.DEGREE);
        var latDeg = intLat * (Units.Angle.T32 / Units.Angle.DEGREE);
        return "(%.5f°, %.5f°)".formatted(lonDeg, latDeg);
    }
}

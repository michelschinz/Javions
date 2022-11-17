package ch.epfl.javions;

public record GeoPos(int intLon, int intLat) {
    private static final int INT_90_DEGREES = 1 << 30;

    private static double checkLon(double lon) {
        Preconditions.checkArgument(-Math.PI <= lon && lon < Math.PI);
        return lon;
    }

    private static double checkLat(double lat) {
        Preconditions.checkArgument(-Math.PI / 2 <= lat && lat <= Math.PI / 2);
        return lat;
    }

    private static int encode(double angle) {
        return (int) Math.scalb(angle / Units.Angle.TURN, Integer.SIZE);
    }

    private static double decode(int angle) {
        // Warning: the cast is necessary to call the correct variant of scalb
        //   and avoid losing precision.
        return Math.scalb((double) angle, -Integer.SIZE) * Units.Angle.TURN;
    }

    public GeoPos {
        Preconditions.checkArgument(-INT_90_DEGREES <= intLat && intLat <= INT_90_DEGREES);
    }

    public GeoPos(double lon, double lat) {
        this(encode(checkLon(lon)), encode(checkLat(lat)));
    }

    public double longitude() {
        return decode(intLon);
    }

    public double latitude() {
        return decode(intLat);
    }

    @Override
    public String toString() {
        return "(%.5f°, %.5f°)".formatted(Math.toDegrees(longitude()), Math.toDegrees(latitude()));
    }
}

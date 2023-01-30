package ch.epfl.javions;

public record GeoPos(int longitudeT32, int latitudeT32) {
    private static final int MAX_ABSOLUTE_LATITUDE_T32 = 1 << (Integer.SIZE - 2);

    public static boolean isValidLatitudeT32(int latitudeT32) {
        return -MAX_ABSOLUTE_LATITUDE_T32 <= latitudeT32 && latitudeT32 <= MAX_ABSOLUTE_LATITUDE_T32;
    }

    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    public double longitude() {
        return longitudeT32 * Units.Angle.T32;
    }

    public double latitude() {
        return latitudeT32 * Units.Angle.T32;
    }

    @Override
    public String toString() {
        var longitudeDeg = longitudeT32 * (Units.Angle.T32 / Units.Angle.DEGREE);
        var latitudeDeg = latitudeT32 * (Units.Angle.T32 / Units.Angle.DEGREE);
        return "(%.5f°, %.5f°)".formatted(longitudeDeg, latitudeDeg);
    }
}

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
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    @Override
    public String toString() {
        var longitudeDeg = Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE);
        var latitudeDeg = Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE);
        return "(" + longitudeDeg + "°, " + latitudeDeg + "°)";
    }
}

package ch.epfl.javions;

import static java.lang.Math.PI;

public record GeoPos(double longitude, double latitude, float altitude) {
    public GeoPos {
        Preconditions.checkArgument(-PI <= longitude && longitude <= PI);
        Preconditions.checkArgument(-PI / 2d <= latitude && latitude <= PI / 2d);
    }

    @Override
    public String toString() {
        return "(%.5f°, %.5f°, %.1fm)".formatted(
                Math.toDegrees(longitude),
                Math.toDegrees(latitude),
                altitude);
    }
}

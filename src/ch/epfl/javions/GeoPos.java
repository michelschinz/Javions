package ch.epfl.javions;

import static java.lang.Math.PI;

public record GeoPos(float longitude, float latitude) {
    public GeoPos {
        Preconditions.checkArgument(-PI <= longitude && longitude <= PI);
        Preconditions.checkArgument(-PI / 2d <= latitude && latitude <= PI / 2d);
    }

    @Override
    public String toString() {
        return "(%.5f°, %.5f°)".formatted(Math.toDegrees(longitude), Math.toDegrees(latitude));
    }
}

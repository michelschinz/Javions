package ch.epfl.javions;

import ch.epfl.javions.adsb.WakeVortexCategory;

public record PlaneState(
        WakeVortexCategory category,
        String callSign,
        double altitude,
        double velocity,
        double trackOrHeading) {
    public static final PlaneState EMPTY = new PlaneState();

    public PlaneState() {
        this(WakeVortexCategory.UNKNOWN, "", Double.NaN, Double.NaN, Double.NaN);
    }

    public PlaneState withCategory(WakeVortexCategory newCategory) {
        return newCategory != category
                ? new PlaneState(newCategory, callSign, altitude, velocity, trackOrHeading)
                : this;
    }

    public PlaneState withCallSign(String newCallSign) {
        return !newCallSign.equals(callSign)
                ? new PlaneState(category, newCallSign, altitude, velocity, trackOrHeading)
                : this;
    }

    public PlaneState withAltitude(double newAltitude) {
        return new PlaneState(category, callSign, newAltitude, velocity, trackOrHeading);
    }

    public PlaneState withVelocity(double newVelocity) {
        return !(newVelocity == velocity) // TODO beware: NaN always compares as false
                ? new PlaneState(category, callSign, altitude, newVelocity, trackOrHeading)
                : this;
    }

    public PlaneState withTrackOrHeading(double newTrackOrHeading) {
        // TODO return this if no change?
        return new PlaneState(category, callSign, altitude, velocity, newTrackOrHeading);
    }
}

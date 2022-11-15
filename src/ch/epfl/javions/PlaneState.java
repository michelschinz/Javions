package ch.epfl.javions;

import ch.epfl.javions.adsb.WakeVortexCategory;

public record PlaneState(
        WakeVortexCategory category,
        String callSign) {
    public static final PlaneState EMPTY = new PlaneState();

    public PlaneState() {
        this(WakeVortexCategory.UNKNOWN, "");
    }

    public PlaneState withCategory(WakeVortexCategory newCategory) {
        return newCategory != category
                ? new PlaneState(newCategory, callSign)
                : this;
    }

    public PlaneState withCallSign(String newCallSign) {
        return !newCallSign.equals(callSign)
                ? new PlaneState(category, newCallSign)
                : this;
    }
}

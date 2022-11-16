package ch.epfl.javions;

import ch.epfl.javions.adsb.WakeVortexCategory;

public record PlaneState(
        WakeVortexCategory category,
        String callSign,
        double altitude,
        double velocity,
        double trackOrHeading) {
    public static final PlaneState EMPTY = new Builder().build();

    public static final class Builder {
        private WakeVortexCategory category;
        private String callSign;
        private double altitude;
        private double velocity;
        private double trackOrHeading;

        public Builder(WakeVortexCategory initialCategory,
                       String initialCallSign,
                       double initialAltitude,
                       double initialVelocity,
                       double initialTrackOrHeading) {
            this.category = initialCategory;
            this.callSign = initialCallSign;
            this.altitude = initialAltitude;
            this.velocity = initialVelocity;
            this.trackOrHeading = initialTrackOrHeading;
        }

        public Builder() {
            this(WakeVortexCategory.UNKNOWN, "", Double.NaN, Double.NaN, Double.NaN);
        }

        public Builder(PlaneState initialState) {
            this(initialState.category,
                    initialState.callSign(),
                    initialState.altitude(),
                    initialState.velocity(),
                    initialState.trackOrHeading());
        }

        public WakeVortexCategory category() {
            return category;
        }

        public Builder setCategory(WakeVortexCategory category) {
            this.category = category;
            return this;
        }

        public String callSign() {
            return callSign;
        }

        public Builder setCallSign(String callSign) {
            this.callSign = callSign;
            return this;
        }

        public double altitude() {
            return altitude;
        }

        public Builder setAltitude(double altitude) {
            this.altitude = altitude;
            return this;
        }

        public double velocity() {
            return velocity;
        }

        public Builder setVelocity(double velocity) {
            this.velocity = velocity;
            return this;
        }

        public double trackOrHeading() {
            return trackOrHeading;
        }

        public Builder setTrackOrHeading(double trackOrHeading) {
            this.trackOrHeading = trackOrHeading;
            return this;
        }

        public PlaneState build() {
            return new PlaneState(category, callSign, altitude, velocity, trackOrHeading);
        }
    }
}

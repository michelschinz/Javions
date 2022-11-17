package ch.epfl.javions;

import ch.epfl.javions.adsb.WakeVortexCategory;

public record PlaneState(
        WakeVortexCategory category,
        String callSign,
        GeoPos position,
        double altitude,
        double velocity,
        double trackOrHeading) {
    public static final PlaneState EMPTY = new Builder().build();

    public static final class Builder {
        private WakeVortexCategory category = WakeVortexCategory.UNKNOWN;
        private String callSign = "";
        private GeoPos position = null;
        private double altitude = Double.NaN;
        private double velocity = Double.NaN;
        private double trackOrHeading = Double.NaN;

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

        public GeoPos position() {
            return position;
        }

        public void setPosition(GeoPos position) {
            this.position = position;
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
            return new PlaneState(category(), callSign(), position(), altitude(), velocity(), trackOrHeading());
        }
    }
}

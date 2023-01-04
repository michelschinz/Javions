package ch.epfl.javions.aircraft;

public record AircraftData(AircraftRegistration registration,
                           AircraftTypeDesignator typeDesignator,
                           String model,
                           AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {
    public static final AircraftData EMPTY = new AircraftData(
            AircraftRegistration.EMPTY,
            AircraftTypeDesignator.EMPTY,
            "",
            AircraftDescription.EMPTY,
            WakeTurbulenceCategory.NONE);
}

package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class AircraftRegistration extends ConstrainedString {
    private static final Pattern VALID_REGISTRATION =
            Pattern.compile("[A-Z0-9]+(-[A-Z0-9]+)?|");

    public static final AircraftRegistration EMPTY = new AircraftRegistration("");

    public AircraftRegistration(String registration) {
        super(VALID_REGISTRATION, registration);
    }
}

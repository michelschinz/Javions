package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class AircraftRegistration extends ConstrainedString {
    private static final Pattern VALID_REGISTRATION =
            Pattern.compile("[A-Z0-9]+(-[A-Z0-9]+)?");

    public AircraftRegistration(String registration) {
        super(false, VALID_REGISTRATION, registration);
    }
}

package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class AircraftTypeDesignator extends ConstrainedString {
    private static final Pattern VALID_DESIGNATOR =
            Pattern.compile("[A-Z0-9]{2,4}|");

    public static final AircraftTypeDesignator EMPTY = new AircraftTypeDesignator("");

    public AircraftTypeDesignator(String designator) {
        super(VALID_DESIGNATOR, designator);
    }
}

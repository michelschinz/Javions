package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class AircraftTypeDesignator extends ConstrainedString {
    private static final Pattern VALID_DESIGNATOR =
            Pattern.compile("[A-Z0-9]{2,4}");

    public AircraftTypeDesignator(String designator) {
        super(true, VALID_DESIGNATOR, designator);
    }

    @Override
    public boolean equals(Object thatO) {
        return thatO instanceof AircraftTypeDesignator that && equalsConstrainedString(that);
    }
}

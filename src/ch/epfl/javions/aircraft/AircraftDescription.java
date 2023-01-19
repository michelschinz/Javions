package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class AircraftDescription extends ConstrainedString {
    private static final Pattern VALID_DESCRIPTION =
            Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    public AircraftDescription(String description) {
        super(true, VALID_DESCRIPTION, description);
    }

    @Override
    public boolean equals(Object thatO) {
        return thatO instanceof AircraftDescription that && equalsConstrainedString(that);
    }
}

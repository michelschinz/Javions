package ch.epfl.javions;

import java.util.regex.Pattern;

public final class AircraftDescription extends ConstrainedString {
    private static final Pattern VALID_DESCRIPTION =
            Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]|");

    public static final AircraftDescription EMPTY = new AircraftDescription("");

    public AircraftDescription(String description) {
        super(VALID_DESCRIPTION, description);
    }
}

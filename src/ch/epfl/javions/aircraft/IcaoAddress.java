package ch.epfl.javions.aircraft;

import ch.epfl.javions.ConstrainedString;

import java.util.regex.Pattern;

public final class IcaoAddress extends ConstrainedString {
    private static final Pattern VALID = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress(String address) {
        super(false, VALID, address);
    }
}

package ch.epfl.javions;

import java.util.regex.Pattern;

public final class IcaoAddress extends ConstrainedString {
    private static final Pattern VALID = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress(String address) {
        super(VALID, address);
    }
}

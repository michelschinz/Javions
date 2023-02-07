package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record CallSign(String string) {
    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9 ]{0,8}");

    public CallSign {
        if (!PATTERN.matcher(string).matches())
            System.out.printf("invalid call sign: %s\n", string);
        Preconditions.checkArgument(PATTERN.matcher(string).matches());
    }
}

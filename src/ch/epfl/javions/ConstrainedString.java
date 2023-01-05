package ch.epfl.javions;

import java.util.Objects;
import java.util.regex.Pattern;

public abstract class ConstrainedString {
    private final String string;

    protected ConstrainedString(boolean allowEmpty, Pattern pattern, String string) {
        Preconditions.checkArgument((allowEmpty && string.isEmpty()) || pattern.matcher(string).matches());
        this.string = string.intern();
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(Object thatO) {
        return thatO instanceof ConstrainedString that && Objects.equals(this.string, that.string);
    }

    @Override
    public String toString() {
        return string;
    }
}

package ch.epfl.javions;

public final class Preconditions {
    public static void checkArgument(boolean mustBeTrue) {
        if (!mustBeTrue) throw new IllegalArgumentException();
    }
}

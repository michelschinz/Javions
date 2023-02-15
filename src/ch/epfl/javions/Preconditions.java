package ch.epfl.javions;

public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument(boolean mustBeTrue) {
        if (!mustBeTrue) throw new IllegalArgumentException();
    }
}

package ch.epfl.javions;

import static java.lang.Math.*;

public final class Math2 {
    private Math2() {}

    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return max(min, min(v, max));
    }

    public static double asinh(double x) {
        return log(x + hypot(1, x));
    }
}

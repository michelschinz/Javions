package ch.epfl.javions;

import static java.lang.Math.*;

public final class Math2 {
    private Math2() {}

    public static final double TAU = Math.scalb(Math.PI, 1);

    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return max(min, min(v, max));
    }

    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        return max(min, min(v, max));
    }

    public static double asinh(double x) {
        return log(x + hypot(1, x));
    }

    public static double floorMod(double x, double y) {
        return x - y * floor(x / y);
    }
}

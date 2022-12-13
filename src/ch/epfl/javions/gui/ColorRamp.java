package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.US_ASCII;

public final class ColorRamp {
    public static final ColorRamp PLASMA = loadColorRamp("plasma-colors.txt");

    private static ColorRamp loadColorRamp(String fileName) {
        try {
            var resource = ColorRamp.class.getResourceAsStream("/" + fileName);
            assert resource != null;
            try (var s = new BufferedReader(new InputStreamReader(resource, US_ASCII))) {
                return new ColorRamp(s.lines()
                        .map(Color::valueOf)
                        .toArray(Color[]::new));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Color[] steps;
    private final double stepSize;

    public ColorRamp(Color... steps) {
        Preconditions.checkArgument(steps.length >= 2);

        this.steps = steps.clone();
        this.stepSize = 1d / (steps.length - 1);
    }

    public Color at(double x) {
        var i = x / stepSize;
        var iL = (int) Math.floor(i);
        var iR = iL + 1;

        if (iR <= 0)
            return steps[0];
        else if (iL >= steps.length - 1)
            return steps[steps.length - 1];
        else {
            var colorL = steps[iL];
            var colorR = steps[iR];
            return colorL.interpolate(colorR, i - iL);
        }
    }
}

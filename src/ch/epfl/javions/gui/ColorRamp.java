package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

public final class ColorRamp {
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.rgb(13, 8, 135),
            Color.rgb(34, 6, 144),
            Color.rgb(50, 5, 151),
            Color.rgb(64, 4, 157),
            Color.rgb(78, 2, 162),
            Color.rgb(91, 1, 165),
            Color.rgb(104, 0, 168),
            Color.rgb(117, 1, 168),
            Color.rgb(129, 4, 167),
            Color.rgb(141, 11, 165),
            Color.rgb(152, 20, 160),
            Color.rgb(163, 29, 154),
            Color.rgb(173, 38, 147),
            Color.rgb(182, 48, 139),
            Color.rgb(191, 57, 132),
            Color.rgb(199, 66, 124),
            Color.rgb(207, 76, 116),
            Color.rgb(214, 85, 109),
            Color.rgb(221, 94, 102),
            Color.rgb(227, 104, 95),
            Color.rgb(233, 114, 88),
            Color.rgb(238, 124, 81),
            Color.rgb(243, 135, 74),
            Color.rgb(247, 146, 67),
            Color.rgb(250, 157, 59),
            Color.rgb(252, 169, 53),
            Color.rgb(253, 181, 46),
            Color.rgb(253, 194, 41),
            Color.rgb(252, 207, 37),
            Color.rgb(249, 221, 36),
            Color.rgb(245, 235, 39),
            Color.rgb(240, 249, 33)
    );

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

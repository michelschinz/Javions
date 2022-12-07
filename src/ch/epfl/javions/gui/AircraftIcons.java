package ch.epfl.javions.gui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public enum AircraftIcons {
    AIRLINER,
    BALLOON,
    CESSNA,
    HEAVY_2E,
    HEAVY_4E,
    HELICOPTER,
    HI_PERF,
    JET_NONSWEPT,
    JET_SWEPT,
    TWIN_LARGE,
    TWIN_SMALL,
    UNKNOWN;

    public static AircraftIcons iconFor(String typeDesignator, String typeDescription) {
        var icon = IconTables.TYPE_DESIGNATOR_TABLE.get(typeDesignator);
        return icon == null
                ? UNKNOWN
                : icon;
    }

    private static String loadSvgPath(String name) {
        try (var s = PlaneManager.class.getResourceAsStream(name)) {
            if (s == null) throw new Error("Cannot find resource " + name);
            return new String(s.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final String svgPath;

    AircraftIcons() {
        var resourceName = "/icons/%s.svgpath".formatted(name().toLowerCase());
        svgPath = loadSvgPath(resourceName);
    }

    public String svgPath() {
        return svgPath;
    }
}

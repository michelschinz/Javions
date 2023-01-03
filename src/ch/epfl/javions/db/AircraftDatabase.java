package ch.epfl.javions.db;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.WakeTurbulenceCategory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public final class AircraftDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    public record AircraftData(String registration,
                               String typeDesignator,
                               String model,
                               String typeDescription,
                               WakeTurbulenceCategory wakeTurbulenceCategory) {
        public static final AircraftData EMPTY =
                new AircraftData("", "", "", "", WakeTurbulenceCategory.NONE);
    }

    private final File dbFile;

    public AircraftDatabase(File dbFile) {
        this.dbFile = dbFile;
    }

    public AircraftData get(IcaoAddress address) {
        var addressString = address.toString();
        try {
            try (var zipFile = new ZipFile(dbFile)) {
                var entryName = addressString.substring(4, 6) + ".csv";
                try (var s = zipFile.getInputStream(zipFile.getEntry(entryName))) {
                    var reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
                    return reader.lines()
                            .filter(l -> l.startsWith(addressString))
                            .map(l -> l.split(SEPARATOR))
                            .map(l -> new AircraftData(l[1], l[2], l[3], l[4], WakeTurbulenceCategory.of(l[5])))
                            .findFirst()
                            .orElse(AircraftData.EMPTY);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

package ch.epfl.javions.db;

import ch.epfl.javions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public final class AircraftDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    // Format: ICAO,Registration,Designator,Model,Description,WTC,Flags
    private static AircraftData parseLine(String line) {
        var columns = line.split(SEPARATOR);
        return new AircraftData(
                new AircraftRegistration(columns[1]),
                new AircraftTypeDesignator(columns[2]),
                columns[3],
                new AircraftDescription(columns[4]),
                WakeTurbulenceCategory.of(columns[5]));
    }

    public record AircraftData(AircraftRegistration registration,
                               AircraftTypeDesignator typeDesignator,
                               String model,
                               AircraftDescription description,
                               WakeTurbulenceCategory wakeTurbulenceCategory) {
        public static final AircraftData EMPTY = new AircraftData(
                AircraftRegistration.EMPTY,
                AircraftTypeDesignator.EMPTY,
                "",
                AircraftDescription.EMPTY,
                WakeTurbulenceCategory.NONE);
    }

    private final File dbFile;

    public AircraftDatabase(File dbFile) {
        this.dbFile = dbFile;
    }

    public AircraftData get(IcaoAddress address) {
        try {
            try (var zipFile = new ZipFile(dbFile)) {
                try (var s = zipFile.getInputStream(zipFile.getEntry(entryName(address)))) {
                    var reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
                    var addressString = address.toString();
                    return reader.lines()
                            .dropWhile(l -> addressString.compareTo(l) > 0)
                            .findFirst()
                            .map(AircraftDatabase::parseLine)
                            .orElse(AircraftData.EMPTY);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String entryName(IcaoAddress address) {
        return address.toString().substring(4, 6) + ".csv";
    }
}

package ch.epfl.javions.aircraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class AircraftDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    private final String fileName;

    public AircraftDatabase(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    public AircraftData get(IcaoAddress address) throws IOException {
        var addressString = address.string();
        var entryName = addressString.substring(4) + ".csv";

        try (var zipFile = new ZipFile(fileName);
             var entryStream = zipFile.getInputStream(zipFile.getEntry(entryName));
             var reader = new BufferedReader(new InputStreamReader(entryStream, UTF_8))) {
            while (true) {
                var line = reader.readLine();
                if (line == null) return null;
                if (line.compareTo(addressString) < 0) continue;
                if (!line.startsWith(addressString)) return null;

                // Format: ICAO,Registration,Designator,Model,Description,WTC
                var columns = line.split(SEPARATOR, -1);
                assert columns[0].equals(addressString);
                return new AircraftData(
                        new AircraftRegistration(columns[1]),
                        new AircraftTypeDesignator(columns[2]),
                        columns[3],
                        new AircraftDescription(columns[4]),
                        WakeTurbulenceCategory.of(columns[5]));
            }
        }
    }
}

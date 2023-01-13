package ch.epfl.javions.aircraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public final class AircraftDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    private final File dbFile;

    public AircraftDatabase(File dbFile) {
        this.dbFile = dbFile;
    }

    public AircraftData get(IcaoAddress address) {
        try {
            try (var zipFile = new ZipFile(dbFile);
                 var entryStream = zipFile.getInputStream(zipFile.getEntry(entryName(address)));
                 var reader = new BufferedReader(new InputStreamReader(entryStream, StandardCharsets.UTF_8))) {

                var addressString = address.toString();
                while (true) {
                    var line = reader.readLine();
                    if (line == null) return null;
                    if (line.compareTo(addressString) < 0) continue;
                    if (!line.startsWith(addressString)) return null;

                    // Format: ICAO,Registration,Designator,Model,Description,WTC,Flags
                    var columns = line.split(SEPARATOR);
                    assert columns[0].equals(addressString);
                    return new AircraftData(
                            new AircraftRegistration(columns[1]),
                            new AircraftTypeDesignator(columns[2]),
                            columns[3],
                            new AircraftDescription(columns[4]),
                            WakeTurbulenceCategory.of(columns[5]));
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

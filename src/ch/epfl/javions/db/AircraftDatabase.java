package ch.epfl.javions.db;

import ch.epfl.javions.IcaoAddress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AircraftDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    public record AircraftData(String registration,
                               String typeDesignator,
                               String model,
                               String description) {
        public AircraftData {
            typeDesignator = typeDesignator.intern();
            model = model.intern();
            description = description.intern();
        }
    }

    private final Map<IcaoAddress, AircraftData> aircraft;

    static Map<IcaoAddress, AircraftData> readAircraftDatabase(Path filePath) throws IOException {
        try (var s = Files.newBufferedReader(filePath)) {
            return s.lines()
                    .skip(1)
                    .map(l -> l.split(SEPARATOR))
                    .collect(Collectors.toMap(
                            l -> new IcaoAddress(Integer.parseInt(l[0], 16)),
                            l -> new AircraftData(l[1], l[2], l[3], l[4])));
        }
    }

    public AircraftDatabase(Path path) throws IOException {
        aircraft = readAircraftDatabase(path);
    }

    public AircraftData get(IcaoAddress address) {
        return aircraft.get(address);
    }
}

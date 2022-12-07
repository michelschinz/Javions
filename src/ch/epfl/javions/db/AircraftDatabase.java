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

    private final Map<IcaoAddress, Aircraft> aircraft;

    static Map<IcaoAddress, Aircraft> readAircraftDatabase(Path filePath) throws IOException {
        try (var s = Files.newBufferedReader(filePath)) {
            return s.lines()
                    .skip(1)
                    .map(l -> l.split(SEPARATOR))
                    .map(l -> new Aircraft(new IcaoAddress(Integer.parseInt(l[0], 16)), l[1], l[2], l[3], l[4]))
                    .collect(Collectors.toMap(Aircraft::address, Function.identity()));
        }
    }

    public AircraftDatabase(Path path) throws IOException {
        aircraft = readAircraftDatabase(path);
    }

    public Aircraft get(IcaoAddress address) {
        return aircraft.get(address);
    }
}

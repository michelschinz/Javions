package ch.epfl.javions.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class OperatorsDatabase {
    private static final String SEPARATOR = Pattern.quote(",");

    private final Map<String, Operator> operators;

    private static Map<String, Operator> readOperatorDatabase(Path filePath) throws IOException {
        try (var s = Files.newBufferedReader(filePath)) {
            return s.lines()
                    .skip(1)
                    .map(l -> l.split(SEPARATOR))
                    .map(l -> new Operator(l[0], l[1], l[2]))
                    .collect(Collectors.toMap(Operator::abbreviation, Function.identity()));
        }
    }

    public OperatorsDatabase(Path dbPath) throws IOException {
        operators = readOperatorDatabase(dbPath);
    }

    public Operator get(String abbreviation) {
        return operators.get(abbreviation);
    }
}

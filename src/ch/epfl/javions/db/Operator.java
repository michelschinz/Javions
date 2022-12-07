package ch.epfl.javions.db;

public record Operator(String abbreviation,
                       String name,
                       String country) {
    public Operator {
        country = country.intern();
    }
}

package ch.epfl.javions;

public enum WakeTurbulenceCategory {
    NONE, LIGHT, MEDIUM, HEAVY;

    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> NONE;
        };
    }
}
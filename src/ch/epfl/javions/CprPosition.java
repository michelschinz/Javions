package ch.epfl.javions;

public record CprPosition(Format format, float longitude, float latitude) {
    public enum Format { EVEN, ODD }
}

package ch.epfl.javions;

public record IcaoAddress(int address) {
    public IcaoAddress {
        Preconditions.checkArgument((address & 0xFF_FF_FF) == address);
    }

    public static IcaoAddress of(String s) {
        try {
            return new IcaoAddress(Integer.parseInt(s, 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return "%06X".formatted(address);
    }
}

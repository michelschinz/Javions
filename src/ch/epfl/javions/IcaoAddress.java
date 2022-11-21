package ch.epfl.javions;

public record IcaoAddress(int address) {
    public IcaoAddress {
        Preconditions.checkArgument((address & 0xFF_FF_FF) == address);
    }

    @Override
    public String toString() {
        return "%06x".formatted(address);
    }
}

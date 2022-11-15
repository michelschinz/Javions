package ch.epfl.javions;

import java.util.Objects;

public record TimeStamped<T>(long timeStamp, T value) {
    public TimeStamped {
        Preconditions.checkArgument(0 <= timeStamp);
        Objects.requireNonNull(value);
    }
}

package ch.epfl.javions.db;

import ch.epfl.javions.IcaoAddress;

public record Aircraft(IcaoAddress address,
                       String registration,
                       String typeDesignator,
                       String model,
                       String description) {
    public Aircraft {
        typeDesignator = typeDesignator.intern();
        model = model.intern();
        description = description.intern();
    }
}

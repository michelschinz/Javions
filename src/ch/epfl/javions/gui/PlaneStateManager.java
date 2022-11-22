package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneState;
import ch.epfl.javions.PlaneStateAccumulator;
import ch.epfl.javions.adsb.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.Map;

public final class PlaneStateManager {
    private final Map<IcaoAddress, PlaneStateAccumulator> accumulators =
            new HashMap<>();
    private final ObservableMap<IcaoAddress, PlaneState> states =
            FXCollections.observableHashMap();

    public ObservableMap<IcaoAddress, PlaneState> states() {
        return states;
    }

    public void updateWithMessage(Message message) {
        var accumulator = accumulators.computeIfAbsent(message.icaoAddress(), a -> new PlaneStateAccumulator());
        accumulator.update(message);
        states.put(message.icaoAddress(), accumulator.currentState());
    }

    // Remove planes for which we didn't get a message recently
    public void purge() {

    }
}

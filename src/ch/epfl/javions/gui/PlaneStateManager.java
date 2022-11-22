package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneStateAccumulator;
import ch.epfl.javions.adsb.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.Map;

public final class PlaneStateManager {
    private final ObservableMap<IcaoAddress, ObservablePlaneState> states =
            FXCollections.observableHashMap();
    private final Map<ObservablePlaneState, PlaneStateAccumulator> accumulators =
            new HashMap<>();

    public ObservableMap<IcaoAddress, ObservablePlaneState> states() {
        return states;
    }

    public void updateWithMessage(Message message) {
        if (!states.containsKey(message.icaoAddress())) {
            var state = new ObservablePlaneState();
            var accumulator = new PlaneStateAccumulator(state);
            accumulator.update(message);
            accumulators.put(state, accumulator);
            states.put(message.icaoAddress(), state);
        } else
            accumulators.get(states.get(message.icaoAddress())).update(message);
    }

    // Remove planes for which we didn't get a message recently
    public void purge() {

    }
}

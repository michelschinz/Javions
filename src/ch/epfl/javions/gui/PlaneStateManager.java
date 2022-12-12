package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneStateAccumulator;
import ch.epfl.javions.adsb.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.HashMap;
import java.util.Map;

public final class PlaneStateManager {
    private final ObservableSet<ObservablePlaneState> states =
            FXCollections.observableSet();
    private final Map<IcaoAddress, PlaneStateAccumulator> accumulators =
            new HashMap<>();

    public ObservableSet<ObservablePlaneState> states() {
        return states;
    }

    public void updateWithMessage(Message message) {
        var address = message.icaoAddress();

        if (!accumulators.containsKey(address)) {
            var state = new ObservablePlaneState(address);
            states.add(state);
            accumulators.put(address, new PlaneStateAccumulator(state));
        }

        accumulators.get(address).update(message);
    }

    // Remove planes for which we didn't get a message recently
    public void purge() {

    }
}

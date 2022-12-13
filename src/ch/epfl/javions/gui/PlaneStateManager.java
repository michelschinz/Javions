package ch.epfl.javions.gui;

import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.PlaneStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.db.AircraftDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.HashMap;
import java.util.Map;

public final class PlaneStateManager {
    private final AircraftDatabase aircraftDatabase;
    private final ObservableSet<ObservablePlaneState> states;
    private final Map<IcaoAddress, PlaneStateAccumulator> accumulators;

    public PlaneStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftDatabase = aircraftDatabase;
        this.states = FXCollections.observableSet();
        this.accumulators = new HashMap<>();
    }

    public ObservableSet<ObservablePlaneState> states() {
        return states;
    }

    public void updateWithMessage(Message message) {
        var address = message.icaoAddress();

        if (!accumulators.containsKey(address)) {
            var state = new ObservablePlaneState(address, aircraftDatabase.get(address));
            states.add(state);
            accumulators.put(address, new PlaneStateAccumulator(state));
        }

        accumulators.get(address).update(message);
    }

    // Remove planes for which we didn't get a message recently
    public void purge() {

    }
}

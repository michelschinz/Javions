package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.adsb.PlaneStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class PlaneStateManager {
    private static final long MAX_INTER_MESSAGE_DELAY =
            Duration.ofMinutes(1).toNanos();

    private final AircraftDatabase aircraftDatabase;
    private final ObservableSet<ObservablePlaneState> states;
    private final ObservableSet<ObservablePlaneState> unmodifiableStates;
    private final Map<IcaoAddress, PlaneStateAccumulator> accumulators;
    private final LongProperty lastMessageTimeStampNsProperty;

    public PlaneStateManager(AircraftDatabase aircraftDatabase) {
        var states = FXCollections.<ObservablePlaneState>observableSet();
        this.aircraftDatabase = aircraftDatabase;
        this.states = states;
        this.unmodifiableStates = FXCollections.unmodifiableObservableSet(states);
        this.accumulators = new HashMap<>();
        this.lastMessageTimeStampNsProperty = new SimpleLongProperty();
    }

    public ObservableSet<ObservablePlaneState> states() {
        return unmodifiableStates;
    }

    public void updateWithMessage(Message message) {
        var address = message.icaoAddress();

        if (!accumulators.containsKey(address)) {
            var state = new ObservablePlaneState(address, aircraftDatabase.get(address));
            states.add(state);
            accumulators.put(address, new PlaneStateAccumulator(state));
        }

        accumulators.get(address).update(message);
        lastMessageTimeStampNsProperty.set(message.timeStamp());
    }

    // Remove planes for which we didn't get a message recently
    public void purge() {
        var aircraftIt = states.iterator();
        while (aircraftIt.hasNext()) {
            var state = aircraftIt.next();
            var dT = lastMessageTimeStampNsProperty.get() - state.getLastMessageTimeStampNs();
            if (dT > MAX_INTER_MESSAGE_DELAY) {
                aircraftIt.remove();
                accumulators.remove(state.address());
            }
        }
    }
}

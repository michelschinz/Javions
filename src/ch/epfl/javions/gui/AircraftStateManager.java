package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AircraftStateManager {
    private static final long MAX_INTER_MESSAGE_DELAY =
            Duration.ofMinutes(1).toNanos();

    private final AircraftDatabase aircraftDatabase;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObservableSet<ObservableAircraftState> unmodifiableStates;
    private final Map<IcaoAddress, AircraftStateAccumulator> accumulators;
    private long lastMessageTimeStampNs;

    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        var states = FXCollections.<ObservableAircraftState>observableSet();
        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
        this.states = states;
        this.unmodifiableStates = FXCollections.unmodifiableObservableSet(states);
        this.accumulators = new HashMap<>();
        this.lastMessageTimeStampNs = 0L;
    }

    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableStates;
    }

    public void updateWithMessage(Message message) {
        var address = message.icaoAddress();

        if (!accumulators.containsKey(address)) {
            var state = new ObservableAircraftState(address, aircraftDatabase.get(address));
            states.add(state);
            accumulators.put(address, new AircraftStateAccumulator(state));
        }

        accumulators.get(address).update(message);
        lastMessageTimeStampNs = message.timeStamp();
    }

    // Remove aircraft for which we didn't get a message recently
    public void purge() {
        var aircraftIt = states.iterator();
        while (aircraftIt.hasNext()) {
            var state = aircraftIt.next();
            var dT = lastMessageTimeStampNs - state.getLastMessageTimeStampNs();
            if (dT > MAX_INTER_MESSAGE_DELAY) {
                aircraftIt.remove();
                accumulators.remove(state.address());
            }
        }
    }
}

package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
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
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> accumulators;
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

    public void updateWithMessage(Message message) throws IOException {
        var address = message.icaoAddress();

        var accumulator = accumulators.get(address);
        if (accumulator == null) {
            var state = new ObservableAircraftState(address, aircraftDatabase.get(address));
            accumulator = new AircraftStateAccumulator<>(state);
            accumulators.put(address, accumulator);
        }

        accumulator.update(message);
        var updatedState = accumulator.stateSetter();
        if (updatedState.getPosition() != null) states.add(updatedState);
        lastMessageTimeStampNs = message.timeStamp();
    }

    // Remove aircraft for which we didn't get a message recently
    public void purge() {
        var accumulatorsIt = accumulators.values().iterator();
        while (accumulatorsIt.hasNext()) {
            var accumulator = accumulatorsIt.next();
            var state = accumulator.stateSetter();
            if (lastMessageTimeStampNs - state.getLastMessageTimeStampNs() > MAX_INTER_MESSAGE_DELAY) {
                states.remove(state);
                accumulatorsIt.remove();
            }
        }
    }
}

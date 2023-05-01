package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Main extends Application {
    private static final String AIRCRAFT_DB_RESOURCE_NAME = "/aircraft.zip";
    private static final String OSM_TILE_SERVER = "tile.openstreetmap.org";
    private static final String OSM_TILE_CACHE_PATH = "tile-cache";

    private static final int INITIAL_ZOOM = 8;
    private static final int INITIAL_MIN_X = 33_530;
    private static final int INITIAL_MIN_Y = 23_070;

    private static final long PURGE_INTERVAL_NS = 1_000_000_000L;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var aircraftDbUrl = getClass().getResource(AIRCRAFT_DB_RESOURCE_NAME);
        assert aircraftDbUrl != null;
        var aircraftDbPath = Path.of(aircraftDbUrl.toURI());
        var aircraftDb = new AircraftDatabase(aircraftDbPath.toString());

        var aircraftStateManager = new AircraftStateManager(aircraftDb);
        var tileManager = new TileManager(Path.of(OSM_TILE_CACHE_PATH), OSM_TILE_SERVER);
        var mapParameters = new MapParameters(INITIAL_ZOOM, INITIAL_MIN_X, INITIAL_MIN_Y);
        var selectedAircraftProperty = new SimpleObjectProperty<ObservableAircraftState>();

        // Map
        var aircraftController = new AircraftController(mapParameters,
                aircraftStateManager.states(),
                selectedAircraftProperty);
        var baseMapController = new BaseMapController(tileManager, mapParameters);
        var mapPane = new StackPane(baseMapController.pane(), aircraftController.pane());

        // Status and table
        var statusLineController = new StatusLineController();
        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));

        var aircraftTableController = new AircraftTableController(aircraftStateManager.states(),
                selectedAircraftProperty);
        aircraftTableController.setOnDoubleClick(p -> {
            assert p.getPosition() != null;
            baseMapController.centerOn(p.getPosition());
        });
        var bottomPane = new BorderPane(aircraftTableController.pane());
        bottomPane.setTop(statusLineController.pane());

        var mainPane = new SplitPane(mapPane, bottomPane);
        mainPane.setOrientation(Orientation.VERTICAL);

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Javions");
        primaryStage.show();

        var messageQueue = new ConcurrentLinkedQueue<Message>();
        var programArguments = getParameters().getRaw();
        var messageThread = programArguments.size() == 1
                ? new BinMessageThread(programArguments.get(0), messageQueue)
                : new AirSpyMessageThread(messageQueue);
        messageThread.setDaemon(true);
        messageThread.start();

        var aircraftAnimationTimer = new AnimationTimer() {
            private long lastPurgeTime = 0;
            private final IntegerProperty messageCount = statusLineController.messageCountProperty();

            @Override
            public void handle(long now) {
                var messagesReceived = 0;
                while (!messageQueue.isEmpty()) {
                    try {
                        aircraftStateManager.updateWithMessage(messageQueue.remove());
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                    messagesReceived += 1;
                }
                messageCount.set(messageCount.get() + messagesReceived);
                if (now - lastPurgeTime > PURGE_INTERVAL_NS) {
                    aircraftStateManager.purge();
                    lastPurgeTime = now;
                }
            }
        };
        aircraftAnimationTimer.start();
    }

    private static final class AirSpyMessageThread extends Thread {
        private final ConcurrentLinkedQueue<Message> messages;

        public AirSpyMessageThread(ConcurrentLinkedQueue<Message> messages) {
            super("AirSpyMessageThread");
            this.messages = messages;
        }

        @Override
        public void run() {
            try {
                var demodulator = new AdsbDemodulator(System.in);
                while (true) {
                    var rawMessage = demodulator.nextMessage();
                    if (rawMessage == null) break;
                    var message = MessageParser.parse(rawMessage);
                    if (message != null) messages.add(message);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static final class BinMessageThread extends Thread {
        private final String messageFileName;
        private final ConcurrentLinkedQueue<Message> messageQueue;

        public BinMessageThread(String messageFileName, ConcurrentLinkedQueue<Message> messageQueue) {
            super("BinMessageThread");
            this.messageFileName = messageFileName;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            try (var s = new DataInputStream(new FileInputStream(messageFileName))) {
                var messageBuffer = new byte[RawMessage.LENGTH];
                var t0 = System.nanoTime();
                while (true) {
                    var timeStampNs = s.readLong();
                    var bytesRead = s.read(messageBuffer);
                    if (bytesRead != RawMessage.LENGTH)
                        throw new IOException("Unexpected end of file");

                    var rawMessage = RawMessage.of(timeStampNs, messageBuffer);
                    assert rawMessage != null;

                    var nsToWait = timeStampNs - (System.nanoTime() - t0);
                    if (nsToWait > 0) Thread.sleep(nsToWait / 1_000_000L);
                    var message = MessageParser.parse(rawMessage);
                    if (message != null) messageQueue.add(message);
                }
            } catch (EOFException e) {
                // nothing to do (messages exhausted)
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
    }
}

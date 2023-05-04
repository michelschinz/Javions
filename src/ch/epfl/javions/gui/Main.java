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
import java.util.function.Supplier;

public final class Main extends Application {
    private static final int WINDOW_MIN_WIDTH = 800;
    private static final int WINDOW_MIN_HEIGHT = 600;

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
        primaryStage.setMinWidth(WINDOW_MIN_WIDTH);
        primaryStage.setMinHeight(WINDOW_MIN_HEIGHT);
        primaryStage.setTitle("Javions");
        primaryStage.show();

        var messageQueue = new ConcurrentLinkedQueue<Message>();
        var programArguments = getParameters().getRaw();
        var dataStream = programArguments.size() >= 2
                ? new FileInputStream(programArguments.get(1))
                : System.in;
        var messageSupplier = supplier(programArguments.size() >= 1 ? programArguments.get(0) : "", dataStream);
        var messageThread = new Thread(() -> {
            while (true) {
                var message = messageSupplier.get();
                if (message == null)
                    break;
                messageQueue.add(message);
            }
        });
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

    private static Supplier<Message> supplier(String name, InputStream dataStream) throws IOException {
        return "airspy".equalsIgnoreCase(name)
                ? airSpyMessageSupplier(dataStream)
                : binMessageSupplier(dataStream);
    }

    private static Supplier<Message> airSpyMessageSupplier(InputStream samplesStream) throws IOException {
        var demodulator = new AdsbDemodulator(samplesStream);
        return () -> {
            try {
                while (true) {
                    var rawMessage = demodulator.nextMessage();
                    if (rawMessage == null) return null;
                    var message = MessageParser.parse(rawMessage);
                    if (message != null) return message;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Supplier<Message> binMessageSupplier(InputStream msgStream) {
        var dataStream = new DataInputStream(msgStream);
        var messageBuffer = new byte[RawMessage.LENGTH];
        var startTime = System.nanoTime();
        return () -> {
            try {
                while (true) {
                    var timeStampNs = dataStream.readLong();
                    var bytesRead = dataStream.read(messageBuffer);
                    if (bytesRead != RawMessage.LENGTH)
                        throw new IOException("Unexpected end of file");

                    var rawMessage = RawMessage.of(timeStampNs, messageBuffer);
                    assert rawMessage != null;

                    var nsToWait = timeStampNs - (System.nanoTime() - startTime);
                    if (nsToWait > 0) //noinspection BusyWait
                        Thread.sleep(nsToWait / 1_000_000L);
                    var message = MessageParser.parse(rawMessage);
                    if (message != null) return message;
                }
            } catch (EOFException e) {
                return null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        };
    }
}

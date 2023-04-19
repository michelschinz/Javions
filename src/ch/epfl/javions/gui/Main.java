package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Main extends Application {
    private static final String AIRCRAFT_DB_RESOURCE_NAME = "/aircraft.zip";
    private static final String OSM_TILE_SERVER = "tile.openstreetmap.org";
    private static final String OSM_TILE_CACHE_PATH = "/Users/michelschinz/local/ppo/23/javions/osm-cache";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var aircraftDbUrl = getClass().getResource(AIRCRAFT_DB_RESOURCE_NAME);
        assert aircraftDbUrl != null;
        var aircraftDbPath = Path.of(aircraftDbUrl.toURI());
        var aircraftDb = new AircraftDatabase(aircraftDbPath.toString());

        var tileManager = new TileManager(Path.of(OSM_TILE_CACHE_PATH), OSM_TILE_SERVER);
        var mapParameters = new MapParameters(12, 543_200, 370_650);

        var baseMapController = new BaseMapController(tileManager, mapParameters);

        var aircraftStateManager = new AircraftStateManager(aircraftDb);

        var selectedAircraftProperty = new SimpleObjectProperty<ObservableAircraftState>();
        var aircraftController = new AircraftController(mapParameters, aircraftStateManager.states(), selectedAircraftProperty);
        var aircraftTableController = new AircraftTableController(aircraftStateManager.states(), selectedAircraftProperty);
        aircraftTableController.setOnDoubleClick(p -> {
            if (p.getPosition() != null) baseMapController.centerOn(p.getPosition());
        });

        var mapPane = new StackPane(baseMapController.pane(), aircraftController.pane());

        var statusLineController = new StatusLineController();
        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));

        var bottomPane = new BorderPane(aircraftTableController.pane(), statusLineController.pane(), null, null, null);

        var mainPane = new SplitPane(mapPane, bottomPane);
        mainPane.setOrientation(Orientation.VERTICAL);

        var scene = new Scene(mainPane);
        primaryStage.setScene(scene);

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
                statusLineController.messageCountProperty().set(statusLineController.messageCountProperty().get() + messagesReceived);
                aircraftStateManager.purge();
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
                    messageQueue.add(MessageParser.parse(rawMessage));
                }
            } catch (EOFException e) {
                System.out.println("Messages exhausted, exiting.");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

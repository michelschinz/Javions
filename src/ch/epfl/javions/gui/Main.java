package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AvrParser;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawAdsbMessage;
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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Main extends Application {
    private static final String OSM_TILE_SERVER = "tile.openstreetmap.org";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var cacheBasePath = Path.of("/Users/michelschinz/local/ppo/23/javions/osm-cache");
        var dbResource = getClass().getResource("/aircraft.zip");
        assert dbResource != null;

        var aircraftDatabase = new AircraftDatabase(new File(dbResource.toURI()));

        var tileManager = new TileManager(cacheBasePath, OSM_TILE_SERVER);
        var mapParameters = new MapParameters(543_200, 370_650, 12);

        var baseMapManager = new BaseMapManager(tileManager, mapParameters);

        var aircraftStateManager = new AircraftStateManager(aircraftDatabase);

        var selectedAircraftProperty = new SimpleObjectProperty<ObservableAircraftState>();
        var aircraftManager = new AircraftManager(mapParameters, aircraftStateManager.states(), selectedAircraftProperty);
        var aircraftTableManager = new AircraftTableManager(aircraftStateManager.states(), selectedAircraftProperty);
        aircraftTableManager.setOnDoubleClick(p -> {
            if (p.getPosition() != null) baseMapManager.centerOn(p.getPosition());
        });

        var mapPane = new StackPane(baseMapManager.pane(), aircraftManager.pane());

        var statusManager = new StatusLineManager();
        statusManager.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));

        var bottomPane = new BorderPane(aircraftTableManager.pane(), statusManager.pane(), null, null, null);

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
                ? new AvrMessageThread(programArguments.get(0), messageQueue)
                : new AirSpyMessageThread(messageQueue);
        messageThread.setDaemon(true);
        messageThread.start();

        var aircraftAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                var messagesReceived = 0;
                while (!messageQueue.isEmpty()) {
                    aircraftStateManager.updateWithMessage(messageQueue.remove());
                    messagesReceived += 1;
                }
                statusManager.messageCountProperty().set(statusManager.messageCountProperty().get() + messagesReceived);
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

    private static final class AvrMessageThread extends Thread {
        private final String messageFileName;
        private final ConcurrentLinkedQueue<Message> messageQueue;
        private long fakeTimeStamp = 0L;

        public AvrMessageThread(String messageFileName, ConcurrentLinkedQueue<Message> messageQueue) {
            super("AvrMessageThread");
            this.messageFileName = messageFileName;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            try (var s = Files.newBufferedReader(Path.of(messageFileName))) {
                s.lines()
                        .map(AvrParser::parseAVR)
                        .map(m -> RawAdsbMessage.of(fakeTimeStamp++, m))
                        .filter(m -> m.downLinkFormat() == 17)
                        .map(MessageParser::parse)
                        .filter(Objects::nonNull)
                        .forEach(m -> {
                            try {
                                messageQueue.add(m);
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                throw new Error(e);
                            }
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

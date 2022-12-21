package ch.epfl.javions.gui;

import ch.epfl.javions.AvrParser;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.db.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.EOFException;
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
    public void start(Stage primaryStage) throws IOException {
        var basePath = Path.of("/Users/michelschinz/local/ppo/23/javions");
        var cacheBasePath = basePath.resolve("osm-cache");
        var dbBasePath = basePath.resolve("db");

        var aircraftDatabase = new AircraftDatabase(dbBasePath.resolve("aircraft.csv"));

        var tileManager = new TileManager(cacheBasePath, OSM_TILE_SERVER);
        var mapParameters = new MapParameters(543_200, 370_650, 12);

        var baseMapManager = new BaseMapManager(tileManager, mapParameters);

        var messageQueue = new ConcurrentLinkedQueue<Message>();
        var planeStateManager = new PlaneStateManager(aircraftDatabase);

        var selectedAircraftProperty = new SimpleObjectProperty<ObservablePlaneState>();
        var planeManager = new PlaneManager(mapParameters, planeStateManager.states(), selectedAircraftProperty);
        var planeTableManager = new PlaneTableManager(planeStateManager.states(), selectedAircraftProperty);
        planeTableManager.setOnDoubleClick(p -> {
            if (p.getPosition() != null) baseMapManager.centerOn(p.getPosition());
        });

        var mapPane = new StackPane(baseMapManager.pane(), planeManager.pane());
        var mainPane = new SplitPane(mapPane, planeTableManager.pane());
        mainPane.setOrientation(Orientation.VERTICAL);

        var scene = new Scene(mainPane);
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setTitle("Javions");
        primaryStage.show();

        var programArguments = getParameters().getRaw();
        var messageThread = programArguments.size() == 1
                ? new AvrMessageThread(programArguments.get(0), messageQueue)
                : new AirSpyMessageThread(messageQueue);
        messageThread.setDaemon(true);
        messageThread.start();

        var planeAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                while (!messageQueue.isEmpty())
                    planeStateManager.updateWithMessage(messageQueue.remove());
                planeStateManager.purge();
            }
        };
        planeAnimationTimer.start();
    }

    private static final class AirSpyMessageThread extends Thread {
        private final ConcurrentLinkedQueue<Message> messages;

        public AirSpyMessageThread(ConcurrentLinkedQueue<Message> messages) {
            this.messages = messages;
        }

        @Override
        public void run() {
            try {
                var demodulator = new AdsbDemodulator(System.in);
                while (true) demodulator.nextFrame().ifPresent(messages::add);
            } catch (EOFException e) {
                // Nothing to do.
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
            this.messageFileName = messageFileName;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            try (var s = Files.newBufferedReader(Path.of(messageFileName))) {
                s.lines()
                        .map(AvrParser::parseAVR)
                        .filter(m -> Message.rawDownLinkFormat(m) == 17)
                        .map(m -> Message.of(fakeTimeStamp++, m))
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

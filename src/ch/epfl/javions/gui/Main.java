package ch.epfl.javions.gui;

import ch.epfl.javions.AvrParser;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.adsb.DownlinkFormat;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public final class Main extends Application {
    private static final String OSM_TILE_SERVER = "tile.openstreetmap.org";
    private static final MapViewParameters INITIAL_MAP_VIEW_PARAMETERS =
            new MapViewParameters(12, 543_200, 370_650);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        var cacheBasePath = Path.of("/Users/michelschinz/local/ppo/23/javions/osm-cache");
        var tileManager = new TileManager(cacheBasePath, OSM_TILE_SERVER);
        var mapViewParametersProperty = new SimpleObjectProperty<>(INITIAL_MAP_VIEW_PARAMETERS);
        var baseMapManager = new BaseMapManager(tileManager, mapViewParametersProperty);

        var messageQueue = new ConcurrentLinkedQueue<Message>();
        var planeStateManager = new PlaneStateManager();

        var selectedAddressProperty = new SimpleObjectProperty<IcaoAddress>();
        var planeManager = new PlaneManager(mapViewParametersProperty, planeStateManager.states(), selectedAddressProperty);
        var planeTableManager = new PlaneTableManager(planeStateManager.states(), selectedAddressProperty);
        var mapPane = new StackPane(baseMapManager.pane(), planeManager.pane());
        var mainPane = new SplitPane(mapPane, planeTableManager.pane());

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

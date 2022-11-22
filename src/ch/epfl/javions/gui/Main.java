package ch.epfl.javions.gui;

import ch.epfl.javions.AvrParser;
import ch.epfl.javions.IcaoAddress;
import ch.epfl.javions.adsb.Message;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Main extends Application {
    private static final String OSM_TILE_SERVER = "tile.openstreetmap.org";
    private static final MapViewParameters INITIAL_MAP_VIEW_PARAMETERS =
            new MapViewParameters(12, 543_200, 370_650);

    public static void main(String[] args) {
        launch(args);
    }

    private static long fakeTimeStamp = 0L;

    @Override
    public void start(Stage primaryStage) {
        var cacheBasePath = Path.of("/Users/michelschinz/local/ppo/23/javions/osm-cache");
        var tileManager = new TileManager(cacheBasePath, OSM_TILE_SERVER);
        var mapViewParametersProperty = new SimpleObjectProperty<>(INITIAL_MAP_VIEW_PARAMETERS);
        var baseMapManager = new BaseMapManager(tileManager, mapViewParametersProperty);

        var messageQueue = new ConcurrentLinkedDeque<Message>();
        var planeStateManager = new PlaneStateManager();

        var observablePlaneStates = FXCollections.<ObservablePlaneState>observableArrayList();
        planeStateManager.states().addListener((MapChangeListener<IcaoAddress, ObservablePlaneState>) c -> {
            if (c.wasRemoved())
                observablePlaneStates.remove(c.getValueRemoved());
            if (c.wasAdded())
                observablePlaneStates.add(c.getValueAdded());
        });

        var planeManager = new PlaneManager(mapViewParametersProperty, planeStateManager.states());
        var planeTableManager = new PlaneTableManager(observablePlaneStates);
        var mapPane = new StackPane(baseMapManager.pane(), planeManager.pane());
        var mainPane = new SplitPane(mapPane, planeTableManager.pane());

        var scene = new Scene(mainPane);
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setTitle("Javions");
        primaryStage.show();

        var messageFileName = getParameters().getRaw().get(0);
        var messageHandlingThread = new Thread(() -> {
            try (var s = Files.newBufferedReader(Path.of(messageFileName))) {
                s.lines()
                        .map(AvrParser::parseAVR)
                        .filter(m -> Message.rawDownLinkFormat(m) == 17)
                        .map(m -> Message.of(fakeTimeStamp++, m))
                        .filter(Objects::nonNull)
                        .forEach(m -> {
                            try {
                                messageQueue.addFirst(m);
                                Thread.sleep(5);
                            } catch (InterruptedException e) {throw new Error(e);}});
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        messageHandlingThread.setDaemon(true);
        messageHandlingThread.start();

        var planeAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                while (!messageQueue.isEmpty())
                    planeStateManager.updateWithMessage(messageQueue.removeLast());
                planeStateManager.purge();
            }
        };
        planeAnimationTimer.start();
    }
}

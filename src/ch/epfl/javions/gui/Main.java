package ch.epfl.javions.gui;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

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

        var scene = new Scene(baseMapManager.pane());
        primaryStage.setScene(scene);

        primaryStage.setTitle("Javions");
        primaryStage.show();
    }
}

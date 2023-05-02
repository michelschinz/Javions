package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TileManager {
    public static final int TILE_SIZE = 256;

    private static final int MAX_CACHE_SIZE = 100;
    private static final float INITIAL_LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = (int) Math.ceil(MAX_CACHE_SIZE / INITIAL_LOAD_FACTOR);

    public record TileId(int zoom, int x, int y) {
        public static boolean isValid(int zoom, int x, int y) {
            var maxXY = 1 << zoom;
            return 0 <= x && x < maxXY && 0 <= y && y < maxXY;
        }

        public TileId {
            Preconditions.checkArgument(isValid(zoom, x, y));
        }

        private Path path(Path basePath) {
            return basePath
                    .resolve(Integer.toString(zoom))
                    .resolve(Integer.toString(x))
                    .resolve(y + ".png");
        }

        private URL url(String hostName) throws IOException {
            return new URL("https", hostName, "/" + zoom + "/" + x + "/" + y + ".png");
        }
    }

    private final String tileServerHost;
    private final Path cacheBasePath;
    private final Map<TileId, Image> cache;

    public TileManager(Path cacheBasePath, String tileServerHost) {
        this.cacheBasePath = Objects.requireNonNull(cacheBasePath);
        this.tileServerHost = Objects.requireNonNull(tileServerHost);
        this.cache = new LinkedHashMap<>(INITIAL_CAPACITY, INITIAL_LOAD_FACTOR, true);
    }

    public Image imageForTileAt(TileId tileId) throws IOException {
        if (!cache.containsKey(tileId)) {
            if (cache.size() == MAX_CACHE_SIZE)
                cache.remove(cache.keySet().iterator().next());
            cache.put(tileId, fetchTileImage(tileId));
        }
        return cache.get(tileId);
    }

    private Image fetchTileImage(TileId tileId) throws IOException {
        var imagePath = tileId.path(cacheBasePath);
        if (Files.exists(imagePath)) {
            try (var inStream = new FileInputStream(imagePath.toFile())) {
                return new Image(inStream);
            }
        } else {
            Files.createDirectories(imagePath.getParent());
            var connection = tileId.url(tileServerHost).openConnection();
            connection.setRequestProperty("User-Agent", "Javions");
            try (var inStream = connection.getInputStream()) {
                var imageBytes = inStream.readAllBytes();
                Files.write(imagePath, imageBytes);
                // Note: a ByteArrayInputStream does not have to be closed
                return new Image(new ByteArrayInputStream(imageBytes));
            }
        }
    }
}

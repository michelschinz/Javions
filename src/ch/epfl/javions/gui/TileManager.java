package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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
                    .resolve("%d".formatted(zoom))
                    .resolve("%d".formatted(x))
                    .resolve("%d.png".formatted(y));
        }

        private URL url(String hostName) throws IOException {
            return new URL("https", hostName, "/%d/%d/%d.png".formatted(zoom, x, y));
        }
    }

    private final String tileServerHost;
    private final Path cacheBasePath;
    private final Map<TileId, Image> cache;

    public TileManager(Path cacheBasePath, String tileServerHost) {
        this.cacheBasePath = Objects.requireNonNull(cacheBasePath);
        this.tileServerHost = Objects.requireNonNull(tileServerHost);
        this.cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true);
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
        if (!Files.exists(imagePath)) {
            Files.createDirectories(imagePath.getParent());
            var connection = tileId.url(tileServerHost).openConnection();
            connection.setRequestProperty("User-Agent", "Javions");
            try (var inStream = connection.getInputStream();
                 var outStream = new FileOutputStream(imagePath.toFile())) {
                inStream.transferTo(outStream);
            }
        }
        try (var inStream = new FileInputStream(imagePath.toFile())) {
            return new Image(inStream);
        }
    }
}

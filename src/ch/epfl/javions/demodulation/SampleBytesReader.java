package ch.epfl.javions.demodulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class SampleBytesReader {
    private final InputStream stream;
    private final int chunkSize;

    public SampleBytesReader(InputStream stream, int chunkSize) {
        assert chunkSize > 0;
        this.stream = stream;
        this.chunkSize = chunkSize;
    }

    public int readChunk(byte[] chunk) throws IOException {
        assert chunk.length == chunkSize;
        var bytesRead = stream.readNBytes(chunk, 0, chunkSize);
        if (bytesRead < chunkSize) Arrays.fill(chunk, bytesRead, chunkSize, (byte) 0);
        return bytesRead;
    }
}

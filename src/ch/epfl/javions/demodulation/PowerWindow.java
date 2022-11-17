package ch.epfl.javions.demodulation;

import java.io.IOException;
import java.io.InputStream;

public final class PowerWindow {
    private static final int CHUNK_SIZE = 1 << 16;

    private final int windowSize;
    private final PowerComputer powerComputer;
    private final int[][] chunks;
    private long position;
    private int headIndex; // Invariant: 0 <= headIndex < CHUNK_SIZE
    private int available;

    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        assert 0 < windowSize && windowSize <= CHUNK_SIZE;

        var powerComputer = new PowerComputer(stream, CHUNK_SIZE);
        var chunks = new int[2][CHUNK_SIZE];
        var initiallyAvailable = powerComputer.readChunk(chunks[0]);

        this.windowSize = windowSize;
        this.powerComputer = powerComputer;
        this.chunks = chunks;
        this.position = 0;
        this.headIndex = 0;
        this.available = initiallyAvailable;
    }

    public long position() {
        return position;
    }

    public int available() {
        return available;
    }

    public int get(int i) {
        assert 0 <= i && i < windowSize;
        var j = headIndex + i;
        return chunks[j / CHUNK_SIZE][j % CHUNK_SIZE];
    }

    public void advance() throws IOException {
        position += 1;
        headIndex += 1;
        available -= 1;

        if (headIndex + windowSize - 1 == CHUNK_SIZE) {
            // Window overlaps with second chunk, load it.
            var newlyAvailable = powerComputer.readChunk(chunks[1]);
            available += newlyAvailable;
        } else if (headIndex == CHUNK_SIZE) {
            // Window doesn't overlap with first chunk anymore, swap chunks
            var b0 = chunks[0];
            chunks[0] = chunks[1];
            chunks[1] = b0;

            headIndex = 0;
        }
    }

    public void advanceBy(int offset) throws IOException {
        assert 0 <= offset;
        for (int i = 0; i < offset; i += 1) advance();
    }
}

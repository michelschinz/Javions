package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class PowerWindow {
    private static final int BATCH_SIZE = 1 << 16;

    private final int windowSize;
    private final PowerComputer powerComputer;
    private int[] batch0, batch1;
    private long position;
    private int headIndex; // Invariant: 0 <= headIndex < BATCH_SIZE
    private int available;

    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= BATCH_SIZE);

        var powerComputer = new PowerComputer(stream, BATCH_SIZE);
        var batchEven = new int[BATCH_SIZE];
        var initiallyAvailable = powerComputer.readBatch(batchEven);
        var batchOdd = new int[BATCH_SIZE];

        this.windowSize = windowSize;
        this.powerComputer = powerComputer;
        this.batch0 = batchEven;
        this.batch1 = batchOdd;
        this.position = 0;
        this.headIndex = 0;
        this.available = initiallyAvailable;
    }

    public int size() {
        return windowSize;
    }

    public long position() {
        return position;
    }

    public boolean isFull() {
        return available >= windowSize;
    }

    public int get(int i) {
        var j = headIndex + Objects.checkIndex(i, windowSize);
        return j < BATCH_SIZE ? batch0[j] : batch1[j - BATCH_SIZE];
    }

    public void advance() throws IOException {
        position += 1;
        headIndex += 1;
        available -= 1;

        if (headIndex + windowSize - 1 == BATCH_SIZE) {
            // Window overlaps with second batch, load it.
            var newlyAvailable = powerComputer.readBatch(batch1);
            available += newlyAvailable;
        } else if (headIndex == BATCH_SIZE) {
            // Window doesn't overlap with first batch anymore, swap batches
            var b0 = batch0;
            batch0 = batch1;
            batch1 = b0;

            headIndex = 0;
        }
    }

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (var i = 0; i < offset; i += 1) advance();
    }
}

package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public final class PowerComputer {
    private static final int FILTER_SIZE = 4;

    private final int chunkSize;
    private final short[] samplesChunk;
    private final SamplesDecoder samplesDecoder;

    private final int[] iWindow;
    private final int[] qWindow;
    private int iSum, qSum;

    // chunkSize must be a multiple of FILTER_SIZE (4)
    public PowerComputer(InputStream stream, int chunkSize) {
        Preconditions.checkArgument(chunkSize > 0 && chunkSize % FILTER_SIZE == 0);
        var samplesChunkSize = 2 * chunkSize;

        this.chunkSize = chunkSize;
        this.samplesChunk = new short[samplesChunkSize];
        this.samplesDecoder = new SamplesDecoder(stream, samplesChunkSize);
        this.iWindow = new int[FILTER_SIZE];
        this.qWindow = new int[FILTER_SIZE];
    }

    public int readChunk(int[] chunk) throws IOException {
        Preconditions.checkArgument(chunk.length == chunkSize);

        var samplesRead = samplesDecoder.readChunk(samplesChunk);
        var i = 0;
        while (i < chunkSize) {
            iSum += sampleDelta(iWindow, i % FILTER_SIZE, samplesChunk[2 * i]);
            qSum += sampleDelta(qWindow, i % FILTER_SIZE, samplesChunk[2 * i + 1]);
            chunk[i++] = power(iSum, qSum);

            iSum -= sampleDelta(iWindow, i % FILTER_SIZE, samplesChunk[2 * i]);
            qSum -= sampleDelta(qWindow, i % FILTER_SIZE, samplesChunk[2 * i + 1]);
            chunk[i++] = power(iSum, qSum);
        }
        return samplesRead / 2;
    }

    private static int power(int iSum, int qSum) {
        return iSum * iSum + qSum * qSum;
    }

    private static int sampleDelta(int[] window, int index, int newSample) {
        var oldSample = window[index];
        window[index] = newSample;
        return newSample - oldSample;
    }
}

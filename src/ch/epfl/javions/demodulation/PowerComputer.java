package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public final class PowerComputer {
    private static final int FILTER_SIZE = 8;

    private final int batchSize;
    private final short[] samples;
    private final SamplesDecoder samplesDecoder;

    private final int[] window;
    private int iSum, qSum;

    // batchSize must be a multiple of FILTER_SIZE (8)
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % FILTER_SIZE == 0);

        this.batchSize = batchSize;
        this.samples = new short[batchSize * 2];
        this.samplesDecoder = new SamplesDecoder(stream, samples.length);
        this.window = new int[FILTER_SIZE];
    }

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        var samplesRead = samplesDecoder.readBatch(samples);

        var powerI = 0;
        var sampleI = 0;
        while (sampleI < samplesRead) {
            iSum += sampleDelta(window, 0, samples[sampleI++]);
            qSum += sampleDelta(window, 1, samples[sampleI++]);
            batch[powerI++] = power(iSum, qSum);

            iSum -= sampleDelta(window, 2, samples[sampleI++]);
            qSum -= sampleDelta(window, 3, samples[sampleI++]);
            batch[powerI++] = power(iSum, qSum);

            iSum += sampleDelta(window, 4, samples[sampleI++]);
            qSum += sampleDelta(window, 5, samples[sampleI++]);
            batch[powerI++] = power(iSum, qSum);

            iSum -= sampleDelta(window, 6, samples[sampleI++]);
            qSum -= sampleDelta(window, 7, samples[sampleI++]);
            batch[powerI++] = power(iSum, qSum);
        }

        return powerI;
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

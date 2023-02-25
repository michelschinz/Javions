package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class SamplesDecoder {
    private static final int BIAS = 1 << 11;

    private final int batchSize;
    private final byte[] bytes;
    private final InputStream stream;

    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);

        this.batchSize = batchSize;
        this.bytes = new byte[batchSize * Short.BYTES];
        this.stream = Objects.requireNonNull(stream);
    }

    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        var bytesRead = stream.readNBytes(bytes, 0, bytes.length);
        var samplesRead = bytesRead / Short.BYTES;

        for (var i = 0; i < samplesRead; i += 1) {
            var lsb = Byte.toUnsignedInt(bytes[i * Short.BYTES]);
            var msb = Byte.toUnsignedInt(bytes[i * Short.BYTES + 1]);
            batch[i] = (short) ((msb << Byte.SIZE | lsb) - BIAS);
        }

        return samplesRead;
    }
}

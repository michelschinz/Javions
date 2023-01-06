package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class SamplesDecoder {
    private final int chunkSize;
    private final byte[] byteChunk;
    private final InputStream stream;

    public SamplesDecoder(InputStream stream, int chunkSize) {
        Preconditions.checkArgument(chunkSize > 0);

        var byteChunkSize = chunkSize * Short.BYTES;
        this.chunkSize = chunkSize;
        this.byteChunk = new byte[byteChunkSize];
        this.stream = stream;
    }

    public int readChunk(short[] chunk) throws IOException {
        Preconditions.checkArgument(chunk.length == chunkSize);

        var bytesRead = stream.readNBytes(byteChunk, 0, byteChunk.length);
        var samplesRead = bytesRead / Short.BYTES;

        for (var i = 0; i < samplesRead; i += 1) {
            var lsb = Byte.toUnsignedInt(byteChunk[i * Short.BYTES]);
            var msb = Byte.toUnsignedInt(byteChunk[i * Short.BYTES + 1]);
            chunk[i] = (short) ((msb << Byte.SIZE | lsb) - (1 << 11));
        }

        return samplesRead;
    }
}

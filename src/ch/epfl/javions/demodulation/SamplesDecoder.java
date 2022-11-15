package ch.epfl.javions.demodulation;

import java.io.IOException;
import java.io.InputStream;

public final class SamplesDecoder {
    private final int chunkSize;
    private final byte[] byteChunk;
    private final SampleBytesReader bytesReader;

    public SamplesDecoder(InputStream stream, int chunkSize) {
        var byteChunkSize = chunkSize * Short.BYTES;
        this.chunkSize = chunkSize;
        this.byteChunk = new byte[byteChunkSize];
        this.bytesReader = new SampleBytesReader(stream, byteChunkSize);
    }

    public int readChunk(short[] chunk) throws IOException {
        assert chunk.length == chunkSize;
        var bytesRead = bytesReader.readChunk(byteChunk);
        for (int i = 0; i < chunkSize; i += 1) {
            var lsb = Byte.toUnsignedInt(byteChunk[i * Short.BYTES]);
            var msb = Byte.toUnsignedInt(byteChunk[i * Short.BYTES + 1]);
            chunk[i] = (short) ((msb << Byte.SIZE | lsb) - (1 << 11));
        }
        return bytesRead / Short.BYTES;
    }
}

package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {
    private static final int MESSAGE_BYTES = 14;

    // Number of samples per pulse (half bit, i.e. half a microsecond).
    // (The AirSpy samples at 20MHz, we compute the signal power at 10MHz).
    private static final int PULSE_WIDTH = 5;
    private static final int BIT_WIDTH = 2 * PULSE_WIDTH;
    private static final int PREAMBLE_WIDTH = 8 * BIT_WIDTH;
    private static final int BYTE_WIDTH = Byte.SIZE * BIT_WIDTH;
    private static final int LONG_MESSAGE_WIDTH = PREAMBLE_WIDTH + MESSAGE_BYTES * BYTE_WIDTH;

    private static final long NANOSECONDS_PER_SAMPLE = 100;

    private final PowerWindow window;
    private final byte[] messageBuffer = new byte[MESSAGE_BYTES];

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.window = new PowerWindow(samplesStream, LONG_MESSAGE_WIDTH);
    }

    public RawMessage nextMessage() throws IOException {
        for (int pPrev = 0, pCurr = 0, pNext;
             window.isFull();
             window.advance(), pPrev = pCurr, pCurr = pNext) {
            pNext = nextPeakPower();
            if (!(pPrev < pCurr && pCurr > pNext)) continue;

            var vCurr = currentValleyPower();
            if (pCurr < 2 * vCurr) continue;

            var firstByte = getByte(0);
            if (RawMessage.size(firstByte) != MESSAGE_BYTES) continue;

            messageBuffer[0] = (byte) firstByte;
            for (var i = 1; i < MESSAGE_BYTES; i += 1)
                messageBuffer[i] = (byte) getByte(i);

            if (RawMessage.isValid(messageBuffer)) {
                var message = RawMessage.of(timeStampNs(), new ByteString(messageBuffer));
                window.advanceBy(LONG_MESSAGE_WIDTH);
                return message;
            }
        }
        return null;
    }

    private int nextPeakPower() {
        return window.get(1 + 0 * PULSE_WIDTH)
                + window.get(1 + 2 * PULSE_WIDTH)
                + window.get(1 + 7 * PULSE_WIDTH)
                + window.get(1 + 9 * PULSE_WIDTH);
    }

    private int currentValleyPower() {
        return window.get(PULSE_WIDTH)
                + window.get(3 * PULSE_WIDTH)
                + window.get(4 * PULSE_WIDTH)
                + window.get(5 * PULSE_WIDTH)
                + window.get(6 * PULSE_WIDTH)
                + window.get(8 * PULSE_WIDTH);
    }

    private long timeStampNs() {
        return window.position() * NANOSECONDS_PER_SAMPLE;
    }

    private int getByte(int i) {
        var b = 0;
        for (var j = 0; j < Byte.SIZE; j += 1)
            b = (b << 1) | getBit(i * Byte.SIZE + j);
        return b;
    }

    private int getBit(int i) {
        var base = PREAMBLE_WIDTH + i * BIT_WIDTH;
        var p1 = window.get(base);
        var p2 = window.get(base + PULSE_WIDTH);
        return p1 < p2 ? 0 : 1;
    }

}

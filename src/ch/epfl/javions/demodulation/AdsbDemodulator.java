package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.adsb.Message;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {
    // Number of samples per pulse (half bit, i.e. half a microsecond).
    // (The AirSpy samples at 20MHz, we compute the signal power at 10MHz).
    private static final int PULSE_WIDTH = 5;
    private static final int BIT_WIDTH = 2 * PULSE_WIDTH;
    private static final int PREAMBLE_WIDTH = 8 * BIT_WIDTH;
    private static final int BYTE_WIDTH = 8 * BIT_WIDTH;
    private static final int LONG_MESSAGE_WIDTH = PREAMBLE_WIDTH + 14 * BYTE_WIDTH;

    private static final long NANOSECONDS_PER_SAMPLE = 100;

    private static final int[] PREAMBLE_PEAKS = {0, 2, 7, 9};
    private static final int[] PREAMBLE_VALLEYS = {1, 3, 4, 5, 6, 8};

    private static final int FIRST_BIT_OFFSET = 1 + PREAMBLE_WIDTH;

    private static final Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);

    private final PowerWindow window;

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.window = new PowerWindow(samplesStream, LONG_MESSAGE_WIDTH);
    }

    public Message nextMessage() throws IOException {
        if (window.available() < 0) throw new EOFException();

        var message = (Message) null;
        while ((message = currentMessage()) == null) window.advance();
        window.advanceBy(LONG_MESSAGE_WIDTH);
        return message;
    }

    /**
     * Returns the message at the current position of the window, or null if there is no message.
     */
    private Message currentMessage() {
        var p0 = totalPower(0, PREAMBLE_PEAKS);
        var p1 = totalPower(1, PREAMBLE_PEAKS);
        var p2 = totalPower(2, PREAMBLE_PEAKS);

        if (!(p0 < p1 && p1 > p2)) return null;

        // Check signal/noise ratio
        var v1 = totalPower(0, PREAMBLE_VALLEYS);
        if (p1 < 2 * v1) return null;

        // Extract first byte, to obtain length
        var firstByte = getByte(0);
        var df = Message.rawDownLinkFormat(firstByte);
        if (df != 17) return null;

        // Check CRC, fixing one-bit errors
        var frameBytes = new byte[Message.BYTES_LONG];
        frameBytes[0] = (byte) firstByte;
        for (var i = 1; i < frameBytes.length; i += 1)
            frameBytes[i] = (byte) getByte(i);

        return CRC_24.crc(frameBytes) == 0
                ? Message.of(timeStamp(), ByteString.ofBytes(frameBytes))
                : null;
    }

    private long timeStamp() {
        return window.position() * NANOSECONDS_PER_SAMPLE;
    }

    private int getByte(int i) {
        var b = 0;
        for (var j = 0; j < Byte.SIZE; j += 1)
            b = (b << 1) | getBit(i * Byte.SIZE + j);
        return b;
    }

    private int getBit(int i) {
        var p1 = window.get(FIRST_BIT_OFFSET + i * BIT_WIDTH);
        var p2 = window.get(FIRST_BIT_OFFSET + i * BIT_WIDTH + PULSE_WIDTH);
        return p1 < p2 ? 0 : 1;
    }

    private int totalPower(int base, int[] offsets) {
        var power = 0;
        for (var o : offsets) power += window.get(base + o * PULSE_WIDTH);
        return power;
    }
}

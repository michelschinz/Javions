package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

public sealed interface Message permits AirbornePositionMessage, AirborneVelocityMessage, AircraftIdentificationMessage {
    int BITS_LONG = 112;
    int BYTES_LONG = BITS_LONG / Byte.SIZE;

    static boolean isExtendedSquitter(int firstByte) {
        return Bits.extractUInt(firstByte, 3, 5) == 17;
    }

    static boolean isExtendedSquitter(ByteString message) {
        return isExtendedSquitter(message.byteAt(0));
    }

    static int capability(ByteString msg) {
        return Bits.extractUInt(msg.byteAt(4), 0, 3);
    }

    static int typeCode(ByteString msg) {
        return msg.byteAt(4) >> 3;
    }

    static long payload(ByteString message) {
        return message.bytesBetween(4, 11);
    }

    static IcaoAddress icaoAddress(ByteString msg) {
        return new IcaoAddress("%06X".formatted((int) msg.bytesBetween(1, 4)));
    }

    static Message of(long timeStamp, ByteString bytes) {
        Preconditions.checkArgument(isExtendedSquitter(bytes));

        return switch (typeCode(bytes)) {
            case 1, 2, 3, 4 -> AircraftIdentificationMessage.of(timeStamp, bytes);
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 -> AirbornePositionMessage.of(timeStamp, bytes);
            case 19 -> AirborneVelocityMessage.of(timeStamp, bytes);
            default -> null;
        };
    }

    long timeStamp();
    IcaoAddress icaoAddress();
}

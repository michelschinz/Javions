package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;

public sealed interface Message permits AirbornePositionMessage, AirborneVelocityMessage, AircraftIdentificationMessage {
    int BITS_LONG = 112;
    int BYTES_LONG = BITS_LONG / Byte.SIZE;

    static int rawDownLinkFormat(int firstByte) {
        return Bits.extractUInt(firstByte, 3, 5);
    }
    static int rawDownLinkFormat(ByteString message) {
        return rawDownLinkFormat(message.byteAt(0));
    }

    static int rawCapability(ByteString msg) {
        return Bits.extractUInt(msg.byteAt(4), 0, 3);
    }

    static int rawTypeCode(ByteString msg) {
        return msg.byteAt(4) >> 3;
    }

    static long payload(ByteString message) {
        var payload = 0L;
        for (int i = 0; i < 7; i += 1) payload = (payload << 8) | message.byteAt(4 + i);
        return payload;
    }

    static DownlinkFormat downlinkFormat(ByteString message) {
        return switch (rawDownLinkFormat(message)) {
            case 17 -> DownlinkFormat.EXTENDED_SQUITTER;
            default -> throw new Error();
        };
    }

    static MessageType typeCode(int rawTypeCode) {
        return switch (rawTypeCode) {
            case 1, 2, 3, 4 -> MessageType.AIRCRAFT_IDENTIFICATION;
            case 5, 6, 7, 8 -> MessageType.SURFACE_POSITION;
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 -> MessageType.AIRBORNE_POSITION;
            case 19 -> MessageType.AIRBORNE_VELOCITIES;
            case 20, 21, 22 -> MessageType.AIRBORNE_POSITION;
            case 28 -> MessageType.AIRCRAFT_STATUS;
            case 29 -> MessageType.TARGET_STATE;
            case 31 -> MessageType.AIRCRAFT_OPERATION_STATUS;
            default -> MessageType.UNKNOWN;
        };
    }

    static MessageType typeCode(ByteString msg) {
        return typeCode(rawTypeCode(msg));
    }

    static int rawIcaoAddress(ByteString msg) {
        return msg.byteAt(1) << 16
               | msg.byteAt(2) << 8
               | msg.byteAt(3);
    }

    static IcaoAddress icaoAddress(ByteString msg) {
        return new IcaoAddress("%06X".formatted(rawIcaoAddress(msg)));
    }

    static Message of(long timeStamp, ByteString bytes) {
        if (rawDownLinkFormat(bytes) != 17) return null; // FIXME clean-up

        return switch (typeCode(bytes)) {
            case AIRCRAFT_IDENTIFICATION -> AircraftIdentificationMessage.of(timeStamp, bytes);
            case AIRBORNE_VELOCITIES -> AirborneVelocityMessage.of(timeStamp, bytes);
            case AIRBORNE_POSITION -> AirbornePositionMessage.of(timeStamp, bytes);
            default -> null;
        };
    }

    long timeStamp();
    IcaoAddress icaoAddress();
}

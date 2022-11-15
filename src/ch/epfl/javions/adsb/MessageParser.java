package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.MessageType;

public final class MessageParser {
    public static int rawDownLinkFormat(int firstByte) {
        return Bits.extractUInt(firstByte, 3, 5);
    }
    public static int rawDownLinkFormat(ByteString message) {
        return rawDownLinkFormat(message.byteAt(0));
    }

    public static int rawCapability(ByteString msg) {
        return Bits.extractUInt(msg.byteAt(0), 0, 3);
    }

    public static int rawTypeCode(ByteString msg) {
        return msg.byteAt(4) >> 3;
    }

    public static long rawPayload(ByteString message) {
        var payload = 0L;
        for (int i = 0; i < 7; i += 1) payload = (payload << 8) | message.byteAt(4 + i);
        return payload;
    }

    public static DownlinkFormat downlinkFormat(ByteString message) {
        return switch (rawDownLinkFormat(message)) {
            case 17 -> DownlinkFormat.EXTENDED_SQUITTER;
            default -> throw new Error();
        };
    }

    public static MessageType typeCode(int rawTypeCode) {
        return switch (rawTypeCode) {
            case 1, 2, 3, 4 -> MessageType.AIRCRAFT_IDENTIFICATION;
            case 5, 6, 7, 8 -> MessageType.SURFACE_POSITION;
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 -> MessageType.AIRBORNE_VELOCITIES;
            case 20, 21, 22 -> MessageType.AIRBORNE_POSITION;
            case 28 -> MessageType.AIRCRAFT_STATUS;
            case 29 -> MessageType.TARGET_STATE;
            case 31 -> MessageType.AIRCRAFT_OPERATION_STATUS;
            default -> throw new Error();
        };
    }

    public static MessageType typeCode(ByteString msg) {
        return typeCode(rawTypeCode(msg));
    }

    public static int icaoAddress(ByteString msg) {
        return msg.byteAt(1) << 16
                | msg.byteAt(2) << 8
                | msg.byteAt(3);
    }
}

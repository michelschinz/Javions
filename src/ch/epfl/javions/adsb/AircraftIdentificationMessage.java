package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.IcaoAddress;

import static ch.epfl.javions.adsb.WakeVortexCategory.*;

public record AircraftIdentificationMessage(
        long timeStamp,
        IcaoAddress icaoAddress,
        WakeVortexCategory category,
        String callSign
) implements Message {
    private static final int CALLSIGN_LENGTH = 8;
    private static final int CALLSIGN_CHAR_BITS = 6;

    public static WakeVortexCategory category(int typeCode, int capability) {
        return switch ((typeCode << 4) | capability) {
            case 0x2_1 -> SURFACE_EMERGENCY_VEHICLE;
            case 0x2_3 -> SURFACE_SERVICE_VEHICLE;
            case 0x2_4, 0x2_5, 0x2_6, 0x2_7 -> GROUND_OBSTRUCTION;
            case 0x3_1 -> GLIDER;
            case 0x3_2 -> LIGHTER_THAN_AIR;
            case 0x3_3 -> PARACHUTIST;
            case 0x3_4 -> ULTRALIGHT;
            case 0x3_6 -> UNMANNED_AERIAL_VEHICLE;
            case 0x3_7 -> SPACE_OR_TRANSATMOSPHERIC_VEHICLE;
            case 0x4_1 -> LIGHT;
            case 0x4_2 -> MEDIUM_1;
            case 0x4_3 -> MEDIUM_2;
            case 0x4_4 -> HIGH_VORTEX_AIRCRAFT;
            case 0x4_5 -> HEAVY;
            case 0x4_6 -> HIGH_PERFORMANCE_HIGH_SPEED;
            case 0x4_7 -> ROTORCRAFT;
            default -> UNKNOWN;
        };
    }

    public static String callSign(long payload) {
        var callSignChars = new char[CALLSIGN_LENGTH];
        for (int i = 0; i < CALLSIGN_LENGTH; i += 1) {
            var startBitI = (CALLSIGN_LENGTH - 1 - i) * CALLSIGN_CHAR_BITS;
            var n = Bits.extractUInt(payload, startBitI, CALLSIGN_CHAR_BITS);
            callSignChars[i] = (char) ((n < 32 ? 0b0100_0000 : 0) | n);
        }
        return new String(callSignChars).trim();
    }

    public static AircraftIdentificationMessage of(long timeStamp, ByteString messageData) {
        var icao = Message.icaoAddress(messageData);
        var typeCode = Message.rawTypeCode(messageData);
        var capability = Message.rawCapability(messageData);
        var category = category(typeCode, capability);
        var callSign = callSign(Message.payload(messageData));
        return new AircraftIdentificationMessage(timeStamp, icao, category, callSign);
    }
}

package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;

import java.util.HashMap;
import java.util.Map;

import static ch.epfl.javions.gui.AircraftIcon.*;

public final class IconTables {
    private IconTables() {}

    public static final Map<AircraftTypeDesignator, AircraftIcon> TYPE_DESIGNATOR_TABLE = createTypeDesignatorTable();

    private static Map<AircraftTypeDesignator, AircraftIcon> createTypeDesignatorTable() {
        // Note: we don't use Map.ofEntries here, as IntelliJ becomes slow if we do.
        var map = new HashMap<AircraftTypeDesignator, AircraftIcon>();
        map.put(new AircraftTypeDesignator("A10"), HI_PERF);
        map.put(new AircraftTypeDesignator("A148"), HI_PERF);
        map.put(new AircraftTypeDesignator("A225"), HEAVY_4E);
        map.put(new AircraftTypeDesignator("A3"), HI_PERF);
        map.put(new AircraftTypeDesignator("A37"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("A5"), CESSNA);
        map.put(new AircraftTypeDesignator("A6"), HI_PERF);
        map.put(new AircraftTypeDesignator("A700"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("AC80"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("AC90"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("AC95"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("AJ27"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("AJET"), HI_PERF);
        map.put(new AircraftTypeDesignator("AN28"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("ARCE"), HI_PERF);
        map.put(new AircraftTypeDesignator("AT3"), HI_PERF);
        map.put(new AircraftTypeDesignator("ATG1"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("B18T"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("B190"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("B25"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("B350"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("B52"), HEAVY_4E);
        map.put(new AircraftTypeDesignator("B712"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("B721"), AIRLINER);
        map.put(new AircraftTypeDesignator("B722"), AIRLINER);
        map.put(new AircraftTypeDesignator("BALL"), BALLOON);
        map.put(new AircraftTypeDesignator("BE10"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE20"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE30"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE32"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE40"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("BE99"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE9L"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BE9T"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BN2T"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("BPOD"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("BU20"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C08T"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("C125"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C212"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C21T"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C22J"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C25A"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C25B"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C25C"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C25M"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C425"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C441"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("C46"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("C500"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C501"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C510"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C525"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C526"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C550"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C551"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C55B"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C560"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C56X"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C650"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("C680"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C68A"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("C750"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("C82"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("CKUO"), HI_PERF);
        map.put(new AircraftTypeDesignator("CL30"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CL35"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CL60"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CRJ1"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CRJ2"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CRJ7"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CRJ9"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CRJX"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("CVLP"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("D228"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("DA36"), HI_PERF);
        map.put(new AircraftTypeDesignator("DA50"), AIRLINER);
        map.put(new AircraftTypeDesignator("DC10"), HEAVY_2E);
        map.put(new AircraftTypeDesignator("DC3"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("DC3S"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("DHA3"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("DHC4"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("DHC6"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("DLH2"), HI_PERF);
        map.put(new AircraftTypeDesignator("E110"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("E135"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("E145"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("E29E"), HI_PERF);
        map.put(new AircraftTypeDesignator("E45X"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("E500"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("E50P"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("E545"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("E55P"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("EA50"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("EFAN"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("EFUS"), HI_PERF);
        map.put(new AircraftTypeDesignator("ELIT"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("EUFI"), HI_PERF);
        map.put(new AircraftTypeDesignator("F1"), HI_PERF);
        map.put(new AircraftTypeDesignator("F100"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("F111"), HI_PERF);
        map.put(new AircraftTypeDesignator("F117"), HI_PERF);
        map.put(new AircraftTypeDesignator("F14"), HI_PERF);
        map.put(new AircraftTypeDesignator("F15"), HI_PERF);
        map.put(new AircraftTypeDesignator("F22"), HI_PERF);
        map.put(new AircraftTypeDesignator("F2TH"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("F4"), HI_PERF);
        map.put(new AircraftTypeDesignator("F406"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("F5"), HI_PERF);
        map.put(new AircraftTypeDesignator("F900"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("FA50"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("FA5X"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("FA7X"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("FA8X"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("FJ10"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("FOUG"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("FURY"), HI_PERF);
        map.put(new AircraftTypeDesignator("G150"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("G3"), AIRLINER);
        map.put(new AircraftTypeDesignator("GENI"), HI_PERF);
        map.put(new AircraftTypeDesignator("GL5T"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLEX"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLF2"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLF3"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLF4"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLF5"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GLF6"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("GSPN"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("H25A"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("H25B"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("H25C"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("HA4T"), AIRLINER);
        map.put(new AircraftTypeDesignator("HDJT"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("HERN"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("J8A"), HI_PERF);
        map.put(new AircraftTypeDesignator("J8B"), HI_PERF);
        map.put(new AircraftTypeDesignator("JH7"), HI_PERF);
        map.put(new AircraftTypeDesignator("JS31"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("JS32"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("JU52"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("L101"), HEAVY_2E);
        map.put(new AircraftTypeDesignator("LAE1"), HI_PERF);
        map.put(new AircraftTypeDesignator("LEOP"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ23"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ24"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ25"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ28"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ31"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ35"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ40"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ45"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ55"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ60"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ70"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ75"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LJ85"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("LTNG"), HI_PERF);
        map.put(new AircraftTypeDesignator("M28"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("MD11"), HEAVY_2E);
        map.put(new AircraftTypeDesignator("MD81"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("MD82"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("MD83"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("MD87"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("MD88"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("MD90"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("ME62"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("METR"), HI_PERF);
        map.put(new AircraftTypeDesignator("MG19"), HI_PERF);
        map.put(new AircraftTypeDesignator("MG25"), HI_PERF);
        map.put(new AircraftTypeDesignator("MG29"), HI_PERF);
        map.put(new AircraftTypeDesignator("MG31"), HI_PERF);
        map.put(new AircraftTypeDesignator("MG44"), HI_PERF);
        map.put(new AircraftTypeDesignator("MH02"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("MS76"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("MT2"), HI_PERF);
        map.put(new AircraftTypeDesignator("MU2"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("P180"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("P2"), TWIN_LARGE);
        map.put(new AircraftTypeDesignator("P68T"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PA47"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("PAT4"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PAY1"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PAY2"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PAY3"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PAY4"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("PIAE"), HI_PERF);
        map.put(new AircraftTypeDesignator("PIT4"), HI_PERF);
        map.put(new AircraftTypeDesignator("PITE"), HI_PERF);
        map.put(new AircraftTypeDesignator("PRM1"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("PRTS"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("Q5"), HI_PERF);
        map.put(new AircraftTypeDesignator("R721"), AIRLINER);
        map.put(new AircraftTypeDesignator("R722"), AIRLINER);
        map.put(new AircraftTypeDesignator("RFAL"), HI_PERF);
        map.put(new AircraftTypeDesignator("ROAR"), HI_PERF);
        map.put(new AircraftTypeDesignator("S3"), HI_PERF);
        map.put(new AircraftTypeDesignator("S32E"), HI_PERF);
        map.put(new AircraftTypeDesignator("S37"), HI_PERF);
        map.put(new AircraftTypeDesignator("S601"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SATA"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SB05"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SC7"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("SF50"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SJ30"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SLCH"), HEAVY_4E);
        map.put(new AircraftTypeDesignator("SM60"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("SOL1"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("SOL2"), JET_SWEPT);
        map.put(new AircraftTypeDesignator("SP33"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("SR71"), HI_PERF);
        map.put(new AircraftTypeDesignator("SS2"), HI_PERF);
        map.put(new AircraftTypeDesignator("SU15"), HI_PERF);
        map.put(new AircraftTypeDesignator("SU24"), HI_PERF);
        map.put(new AircraftTypeDesignator("SU25"), HI_PERF);
        map.put(new AircraftTypeDesignator("SU27"), HI_PERF);
        map.put(new AircraftTypeDesignator("SW2"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("SW3"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("SW4"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("T154"), AIRLINER);
        map.put(new AircraftTypeDesignator("T2"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("T22M"), HI_PERF);
        map.put(new AircraftTypeDesignator("T37"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("T38"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("T4"), HI_PERF);
        map.put(new AircraftTypeDesignator("TJET"), JET_NONSWEPT);
        map.put(new AircraftTypeDesignator("TOR"), HI_PERF);
        map.put(new AircraftTypeDesignator("TRIM"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("TRIS"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("TRMA"), TWIN_SMALL);
        map.put(new AircraftTypeDesignator("TU22"), HI_PERF);
        map.put(new AircraftTypeDesignator("VAUT"), HI_PERF);
        map.put(new AircraftTypeDesignator("Y130"), HI_PERF);
        map.put(new AircraftTypeDesignator("Y141"), AIRLINER);
        map.put(new AircraftTypeDesignator("YK28"), HI_PERF);
        map.put(new AircraftTypeDesignator("YK38"), AIRLINER);
        map.put(new AircraftTypeDesignator("YK40"), AIRLINER);
        map.put(new AircraftTypeDesignator("YK42"), AIRLINER);
        map.put(new AircraftTypeDesignator("YURO"), HI_PERF);
        return Map.copyOf(map);
    }

    public static AircraftIcon iconFor(AircraftTypeDesignator typeDesignator,
                                       AircraftDescription typeDescription,
                                       int category,
                                       WakeTurbulenceCategory wakeTurbulenceCategory) {
        var maybeIcon = TYPE_DESIGNATOR_TABLE.get(typeDesignator);
        if (maybeIcon != null) return maybeIcon;

        var description = typeDescription.toString();
        if (description.startsWith("H")) return HELICOPTER;

        switch (description) {
            case "L1P", "L1T" -> {
                return CESSNA;
            }
            case "L1J" -> {
                return HI_PERF;
            }
            case "L2P" -> {
                return TWIN_SMALL;
            }
            case "L2T" -> {
                return TWIN_LARGE;
            }
            case "L2J" -> {
                switch (wakeTurbulenceCategory) {
                    case LIGHT -> {
                        return JET_SWEPT;
                    }
                    case MEDIUM -> {
                        return AIRLINER;
                    }
                    case HEAVY -> {
                        return HEAVY_2E;
                    }
                }
            }
            case "L4T" -> {
                return HEAVY_4E;
            }
            case "L4J" -> {
                if (wakeTurbulenceCategory == WakeTurbulenceCategory.HEAVY) return HEAVY_4E;
            }
        }

        return switch (category) {
            case 0xA1, 0xB1, 0xB4 -> CESSNA;
            case 0xA2 -> JET_NONSWEPT;
            case 0xA3 -> AIRLINER;
            case 0xA4 -> HEAVY_2E;
            case 0xA5 -> HEAVY_4E;
            case 0xA6 -> HI_PERF;
            case 0xA7 -> HELICOPTER;
            case 0xB2 -> BALLOON;
            default -> UNKNOWN;
        };
    }
}

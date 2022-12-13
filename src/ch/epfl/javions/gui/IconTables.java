package ch.epfl.javions.gui;

import java.util.HashMap;
import java.util.Map;

import static ch.epfl.javions.gui.AircraftIcon.*;
import static java.util.Map.entry;

public final class IconTables {
    public static final Map<String, AircraftIcon> TYPE_DESIGNATOR_TABLE = createTypeDesignatorTable();
    public static final Map<String, AircraftIcon> TYPE_DESCRIPTION_TABLE = createTypeDescriptionTable();

    private static Map<String, AircraftIcon> createTypeDescriptionTable() {
        return Map.ofEntries(
                entry("H", HELICOPTER),
                entry("L1P", CESSNA),
                entry("L1T", CESSNA),
                entry("L1J", HI_PERF),
                entry("L2P", TWIN_SMALL),
                entry("L2T", TWIN_LARGE),
                entry("L2J-L", JET_SWEPT),
                entry("L2J-M", AIRLINER),
                entry("L2J-H", HEAVY_2E),
                entry("L4T", HEAVY_4E),
                entry("L4J-H", HEAVY_4E));
    }

    private static Map<String, AircraftIcon> createTypeDesignatorTable() {
        var map = new HashMap<String, AircraftIcon>();
        map.put("A10", HI_PERF);
        map.put("A148", HI_PERF);
        map.put("A225", HEAVY_4E);
        map.put("A3", HI_PERF);
        map.put("A37", JET_NONSWEPT);
        map.put("A5", CESSNA);
        map.put("A6", HI_PERF);
        map.put("A700", JET_NONSWEPT);
        map.put("AC80", TWIN_SMALL);
        map.put("AC90", TWIN_SMALL);
        map.put("AC95", TWIN_SMALL);
        map.put("AJ27", JET_NONSWEPT);
        map.put("AJET", HI_PERF);
        map.put("AN28", TWIN_SMALL);
        map.put("ARCE", HI_PERF);
        map.put("AT3", HI_PERF);
        map.put("ATG1", JET_NONSWEPT);
        map.put("B18T", TWIN_SMALL);
        map.put("B190", TWIN_SMALL);
        map.put("B25", TWIN_LARGE);
        map.put("B350", TWIN_SMALL);
        map.put("B52", HEAVY_4E);
        map.put("B712", JET_SWEPT);
        map.put("B721", AIRLINER);
        map.put("B722", AIRLINER);
        map.put("BALL", BALLOON);
        map.put("BE10", TWIN_SMALL);
        map.put("BE20", TWIN_SMALL);
        map.put("BE30", TWIN_SMALL);
        map.put("BE32", TWIN_SMALL);
        map.put("BE40", JET_NONSWEPT);
        map.put("BE99", TWIN_SMALL);
        map.put("BE9L", TWIN_SMALL);
        map.put("BE9T", TWIN_SMALL);
        map.put("BN2T", TWIN_SMALL);
        map.put("BPOD", JET_SWEPT);
        map.put("BU20", TWIN_SMALL);
        map.put("C08T", JET_SWEPT);
        map.put("C125", TWIN_SMALL);
        map.put("C212", TWIN_SMALL);
        map.put("C21T", TWIN_SMALL);
        map.put("C22J", JET_NONSWEPT);
        map.put("C25A", JET_NONSWEPT);
        map.put("C25B", JET_NONSWEPT);
        map.put("C25C", JET_NONSWEPT);
        map.put("C25M", JET_NONSWEPT);
        map.put("C425", TWIN_SMALL);
        map.put("C441", TWIN_SMALL);
        map.put("C46", TWIN_LARGE);
        map.put("C500", JET_NONSWEPT);
        map.put("C501", JET_NONSWEPT);
        map.put("C510", JET_NONSWEPT);
        map.put("C525", JET_NONSWEPT);
        map.put("C526", JET_NONSWEPT);
        map.put("C550", JET_NONSWEPT);
        map.put("C551", JET_NONSWEPT);
        map.put("C55B", JET_NONSWEPT);
        map.put("C560", JET_NONSWEPT);
        map.put("C56X", JET_NONSWEPT);
        map.put("C650", JET_SWEPT);
        map.put("C680", JET_NONSWEPT);
        map.put("C68A", JET_NONSWEPT);
        map.put("C750", JET_SWEPT);
        map.put("C82", TWIN_LARGE);
        map.put("CKUO", HI_PERF);
        map.put("CL30", JET_SWEPT);
        map.put("CL35", JET_SWEPT);
        map.put("CL60", JET_SWEPT);
        map.put("CRJ1", JET_SWEPT);
        map.put("CRJ2", JET_SWEPT);
        map.put("CRJ7", JET_SWEPT);
        map.put("CRJ9", JET_SWEPT);
        map.put("CRJX", JET_SWEPT);
        map.put("CVLP", TWIN_LARGE);
        map.put("D228", TWIN_SMALL);
        map.put("DA36", HI_PERF);
        map.put("DA50", AIRLINER);
        map.put("DC10", HEAVY_2E);
        map.put("DC3", TWIN_LARGE);
        map.put("DC3S", TWIN_LARGE);
        map.put("DHA3", TWIN_SMALL);
        map.put("DHC4", TWIN_LARGE);
        map.put("DHC6", TWIN_SMALL);
        map.put("DLH2", HI_PERF);
        map.put("E110", TWIN_SMALL);
        map.put("E135", JET_SWEPT);
        map.put("E145", JET_SWEPT);
        map.put("E29E", HI_PERF);
        map.put("E45X", JET_SWEPT);
        map.put("E500", JET_NONSWEPT);
        map.put("E50P", JET_NONSWEPT);
        map.put("E545", JET_SWEPT);
        map.put("E55P", JET_NONSWEPT);
        map.put("EA50", JET_NONSWEPT);
        map.put("EFAN", JET_NONSWEPT);
        map.put("EFUS", HI_PERF);
        map.put("ELIT", JET_NONSWEPT);
        map.put("EUFI", HI_PERF);
        map.put("F1", HI_PERF);
        map.put("F100", JET_SWEPT);
        map.put("F111", HI_PERF);
        map.put("F117", HI_PERF);
        map.put("F14", HI_PERF);
        map.put("F15", HI_PERF);
        map.put("F22", HI_PERF);
        map.put("F2TH", JET_SWEPT);
        map.put("F4", HI_PERF);
        map.put("F406", TWIN_SMALL);
        map.put("F5", HI_PERF);
        map.put("F900", JET_SWEPT);
        map.put("FA50", JET_SWEPT);
        map.put("FA5X", JET_SWEPT);
        map.put("FA7X", JET_SWEPT);
        map.put("FA8X", JET_SWEPT);
        map.put("FJ10", JET_NONSWEPT);
        map.put("FOUG", JET_NONSWEPT);
        map.put("FURY", HI_PERF);
        map.put("G150", JET_SWEPT);
        map.put("G3", AIRLINER);
        map.put("GENI", HI_PERF);
        map.put("GL5T", JET_SWEPT);
        map.put("GLEX", JET_SWEPT);
        map.put("GLF2", JET_SWEPT);
        map.put("GLF3", JET_SWEPT);
        map.put("GLF4", JET_SWEPT);
        map.put("GLF5", JET_SWEPT);
        map.put("GLF6", JET_SWEPT);
        map.put("GSPN", JET_NONSWEPT);
        map.put("H25A", JET_SWEPT);
        map.put("H25B", JET_SWEPT);
        map.put("H25C", JET_SWEPT);
        map.put("HA4T", AIRLINER);
        map.put("HDJT", JET_NONSWEPT);
        map.put("HERN", JET_SWEPT);
        map.put("J8A", HI_PERF);
        map.put("J8B", HI_PERF);
        map.put("JH7", HI_PERF);
        map.put("JS31", TWIN_SMALL);
        map.put("JS32", TWIN_SMALL);
        map.put("JU52", TWIN_SMALL);
        map.put("L101", HEAVY_2E);
        map.put("LAE1", HI_PERF);
        map.put("LEOP", JET_NONSWEPT);
        map.put("LJ23", JET_NONSWEPT);
        map.put("LJ24", JET_NONSWEPT);
        map.put("LJ25", JET_NONSWEPT);
        map.put("LJ28", JET_NONSWEPT);
        map.put("LJ31", JET_NONSWEPT);
        map.put("LJ35", JET_NONSWEPT);
        map.put("LJ40", JET_NONSWEPT);
        map.put("LJ45", JET_NONSWEPT);
        map.put("LJ55", JET_NONSWEPT);
        map.put("LJ60", JET_NONSWEPT);
        map.put("LJ70", JET_NONSWEPT);
        map.put("LJ75", JET_NONSWEPT);
        map.put("LJ85", JET_NONSWEPT);
        map.put("LTNG", HI_PERF);
        map.put("M28", TWIN_SMALL);
        map.put("MD11", HEAVY_2E);
        map.put("MD81", JET_SWEPT);
        map.put("MD82", JET_SWEPT);
        map.put("MD83", JET_SWEPT);
        map.put("MD87", JET_SWEPT);
        map.put("MD88", JET_SWEPT);
        map.put("MD90", JET_SWEPT);
        map.put("ME62", JET_NONSWEPT);
        map.put("METR", HI_PERF);
        map.put("MG19", HI_PERF);
        map.put("MG25", HI_PERF);
        map.put("MG29", HI_PERF);
        map.put("MG31", HI_PERF);
        map.put("MG44", HI_PERF);
        map.put("MH02", JET_NONSWEPT);
        map.put("MS76", JET_NONSWEPT);
        map.put("MT2", HI_PERF);
        map.put("MU2", TWIN_SMALL);
        map.put("P180", TWIN_SMALL);
        map.put("P2", TWIN_LARGE);
        map.put("P68T", TWIN_SMALL);
        map.put("PA47", JET_NONSWEPT);
        map.put("PAT4", TWIN_SMALL);
        map.put("PAY1", TWIN_SMALL);
        map.put("PAY2", TWIN_SMALL);
        map.put("PAY3", TWIN_SMALL);
        map.put("PAY4", TWIN_SMALL);
        map.put("PIAE", HI_PERF);
        map.put("PIT4", HI_PERF);
        map.put("PITE", HI_PERF);
        map.put("PRM1", JET_NONSWEPT);
        map.put("PRTS", JET_NONSWEPT);
        map.put("Q5", HI_PERF);
        map.put("R721", AIRLINER);
        map.put("R722", AIRLINER);
        map.put("RFAL", HI_PERF);
        map.put("ROAR", HI_PERF);
        map.put("S3", HI_PERF);
        map.put("S32E", HI_PERF);
        map.put("S37", HI_PERF);
        map.put("S601", JET_NONSWEPT);
        map.put("SATA", JET_NONSWEPT);
        map.put("SB05", JET_NONSWEPT);
        map.put("SC7", TWIN_SMALL);
        map.put("SF50", JET_NONSWEPT);
        map.put("SJ30", JET_NONSWEPT);
        map.put("SLCH", HEAVY_4E);
        map.put("SM60", TWIN_SMALL);
        map.put("SOL1", JET_SWEPT);
        map.put("SOL2", JET_SWEPT);
        map.put("SP33", JET_NONSWEPT);
        map.put("SR71", HI_PERF);
        map.put("SS2", HI_PERF);
        map.put("SU15", HI_PERF);
        map.put("SU24", HI_PERF);
        map.put("SU25", HI_PERF);
        map.put("SU27", HI_PERF);
        map.put("SW2", TWIN_SMALL);
        map.put("SW3", TWIN_SMALL);
        map.put("SW4", TWIN_SMALL);
        map.put("T154", AIRLINER);
        map.put("T2", JET_NONSWEPT);
        map.put("T22M", HI_PERF);
        map.put("T37", JET_NONSWEPT);
        map.put("T38", JET_NONSWEPT);
        map.put("T4", HI_PERF);
        map.put("TJET", JET_NONSWEPT);
        map.put("TOR", HI_PERF);
        map.put("TRIM", TWIN_SMALL);
        map.put("TRIS", TWIN_SMALL);
        map.put("TRMA", TWIN_SMALL);
        map.put("TU22", HI_PERF);
        map.put("VAUT", HI_PERF);
        map.put("Y130", HI_PERF);
        map.put("Y141", AIRLINER);
        map.put("YK28", HI_PERF);
        map.put("YK38", AIRLINER);
        map.put("YK40", AIRLINER);
        map.put("YK42", AIRLINER);
        map.put("YURO", HI_PERF);
        return map;
    }
}

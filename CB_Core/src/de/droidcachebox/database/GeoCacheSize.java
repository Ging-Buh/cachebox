package de.droidcachebox.database;

public enum GeoCacheSize {
    other, // 0
    micro, // 1
    small, // 2
    regular, // 3
    large // 4
    // virtual, // 0 - Achtung, hier habe ich einen neuen Wert eingefügt
    // other // 6 - Achtung, hier habe ich einen neuen Wert eingefügt
    ;

    public static GeoCacheSize CacheSizesFromInt(int value) {
        switch (value) {
            case 1:
                return GeoCacheSize.micro;
            case 2:
                return GeoCacheSize.small;
            case 3:
                return GeoCacheSize.regular;
            case 4:
                return GeoCacheSize.large;
            default:
                return GeoCacheSize.other;
        }
    }

    public static GeoCacheSize parseString(String text) {
        // Groundspeak
        if (text.equalsIgnoreCase("micro")) {
            return GeoCacheSize.micro;
        }
        if (text.equalsIgnoreCase("small")) {
            return GeoCacheSize.small;
        }
        if (text.equalsIgnoreCase("regular")) {
            return GeoCacheSize.regular;
        }
        if (text.equalsIgnoreCase("large")) {
            return GeoCacheSize.large;
        }
        if (text.equalsIgnoreCase("not chosen")) {
            return GeoCacheSize.other;
        }
        if (text.equalsIgnoreCase("virtual")) {
            return GeoCacheSize.other;
        }
        if (text.equalsIgnoreCase("other")) {
            return GeoCacheSize.other;
        }
        // GCTour
        if (text.equalsIgnoreCase("none")) {
            return GeoCacheSize.other;
        }
        if (text.equalsIgnoreCase("very large")) {
            return GeoCacheSize.large;
        }
        return GeoCacheSize.other;
    }

    public static String toShortString(Cache cache) {
        if (cache.geoCacheSize == null) return "?";

        switch (cache.geoCacheSize) {
            case large:
                return "L";
            case micro:
                return "M";
            case other:
                return "O";
            case regular:
                return "R";
            case small:
                return "S";
            default:
                break;
        }
        return "?";
    }

    @Override
    public String toString() {
        switch (this) {
            case large:
                return "Large";
            case micro:
                return "Micro";
            case other:
                return "Other";
            case regular:
                return "Regular";
            case small:
                return "Small";
            default:
                break;

        }

        return super.toString();
    }

}

package CB_Core;

import CB_Core.Types.Cache;

public enum CacheSizes {
    other, // 0
    micro, // 1
    small, // 2
    regular, // 3
    large // 4
    // virtual, // 0 - Achtung, hier habe ich einen neuen Wert eingefügt
    // other // 6 - Achtung, hier habe ich einen neuen Wert eingefügt
    ;

    public static CacheSizes parseInt(int value) {
        switch (value) {
            case 1:
                return CacheSizes.micro;
            case 2:
                return CacheSizes.small;
            case 3:
                return CacheSizes.regular;
            case 4:
                return CacheSizes.large;
            default:
                return CacheSizes.other;
        }
    }

    public static CacheSizes parseString(String text) {
        // Groundspeak
        if (text.equalsIgnoreCase("micro")) {
            return CacheSizes.micro;
        }
        if (text.equalsIgnoreCase("small")) {
            return CacheSizes.small;
        }
        if (text.equalsIgnoreCase("regular")) {
            return CacheSizes.regular;
        }
        if (text.equalsIgnoreCase("large")) {
            return CacheSizes.large;
        }
        if (text.equalsIgnoreCase("not chosen")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("virtual")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("other")) {
            return CacheSizes.other;
        }
        // GCTour
        if (text.equalsIgnoreCase("none")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("very large")) {
            return CacheSizes.large;
        }
        return CacheSizes.other;
    }

    public static String toShortString(Cache cache) {
        if (cache.Size == null) return "?";

        switch (cache.Size) {
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

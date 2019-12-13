package de.droidcachebox.database;

import java.util.ArrayList;

/*
from API https://api.groundspeak.com/documentation#type
2	Traditional
3	Multi-Cache
4	Virtual
5	Letterbox Hybrid
6	Event
8	Mystery/Unknown
9	Project A.P.E.
11	Webcam
12	Locationless (Reverse) Cache
13	Cache In Trash Out Event
137	Earthcache
453	Mega-Event
1304	GPS Adventures Exhibit
1858	Wherigo
3653	Community Celebration Event
3773	Geocaching HQ
3774	Geocaching HQ Celebration
4738	Geocaching HQ Block Party
7005	Giga-Event
 */

public enum GeoCacheType {
    Traditional(true), // = 0,
    Multi(true), // = 1,
    Mystery(true), // = 2,
    Camera(true), // = 3,
    Earth(true), // = 4,
    Event(true), // = 5,
    MegaEvent(true), // = 6,
    CITO(true), // = 7,
    Virtual(true), // = 8,
    Letterbox(true), // = 9,
    Wherigo(true), // = 10,
    ReferencePoint(false), // = 11,
    Wikipedia(true), // = 12,
    Undefined(true), // = 13,
    MultiStage(false), // = 14,
    MultiQuestion(false), // = 15,
    Trailhead(false), // = 16,
    ParkingArea(false), // = 17,
    Final(false), // = 18, !!! 18 used in CacheListDAO
    Cache(true), // = 19,
    MyParking(true), // = 20
    Munzee(true), // 21
    Giga(true), // 22
    Lab(true),
    APE(true),
    AdventuresExhibit(true),
    CelebrationEvent(true),
    HQ(true),
    HQCelebration(true),
    HQBlockParty(true),
    ;

    public boolean isCache;

    GeoCacheType(boolean isCache) {
        this.isCache = isCache;
    }

    public static GeoCacheType[] caches() {
        ArrayList<GeoCacheType> result = new ArrayList<>();
        for (GeoCacheType c : GeoCacheType.values()) {
            if (c.isCache) {
                result.add(c);
            }
        }
        return result.toArray(new GeoCacheType[0]);
    }

    public static GeoCacheType parseString(String string) {
        if (string.toLowerCase().contains("virtual cache")) {
            return Virtual;
        } else if (string.equalsIgnoreCase("Cache In Trash Out Event")) {
            return CITO;
        } else {
            // Remove trailing " cache" or " hybrid" fragments
            if (string.contains(" "))
                string = string.substring(0, string.indexOf(" "));
            // Remove trailing "-cache" fragments
            if (string.contains("-"))
                string = string.substring(0, string.indexOf("-"));

            // Replace some opencaching.de / geotoad cache types
            if (string.toLowerCase().contains("unknown"))
                return Mystery;
            if (string.toLowerCase().contains("multicache"))
                return Multi;
            if (string.toLowerCase().contains("whereigo"))
                return Wherigo; // note the additional "e"
            if (string.toLowerCase().contains("other"))
                return Mystery;
            if (string.toLowerCase().contains("earthcache"))
                return Earth;
            if (string.toLowerCase().contains("webcam"))
                return Camera;
            if (string.toLowerCase().contains("question"))
                return MultiQuestion;
            if (string.toLowerCase().contains("reference"))
                return ReferencePoint;
            if (string.toLowerCase().contains("referenzpunkt"))
                return ReferencePoint;
            if (string.toLowerCase().contains("parking"))
                return ParkingArea;
            if (string.toLowerCase().contains("stages"))
                return MultiStage;
            if (string.toLowerCase().contains("munzee"))
                return Munzee;
            if (string.toLowerCase().contains("mega"))
                return MegaEvent;
            if (string.toLowerCase().contains("virtual"))
                return MultiQuestion; // Import Virtual Stage as Question of a Multi
            if (string.toLowerCase().contains("physical"))
                return MultiStage; // Import Physical Stage as a Multi Stage
            if (string.toLowerCase().contains("lab")) // Lab Cache
                return Lab;
            //
            if (string.length() == 0)
                return Undefined;
        }

        try {
            return valueOf(string);
        } catch (Exception ex) {
            GeoCacheType cacheType = Undefined;
            boolean blnCacheTypeFound = false;
            for (GeoCacheType ct : GeoCacheType.values()) {
                if (ct.toString().toLowerCase().equals(string.toLowerCase())) {
                    cacheType = ct;
                    blnCacheTypeFound = true;
                    break;
                }
            }
            if (!blnCacheTypeFound) {
                System.out.println("Handle cache type: " + string);
            }
            return cacheType;
        }
    }

    public static String toShortString(Cache cache) {
        if (cache.getType() == null) return " ";

        switch (cache.getType()) {
            case CITO:
            case Event:
            case Giga:
            case MegaEvent:
                return "X";
            case Cache:
                return "C";
            case Camera:
                return "W";
            case Earth:
                return "E";
            case Letterbox:
                return "L";
            case Multi:
                return "M";
            case Munzee:
                return "Z";
            case Mystery:
                return "U";
            case Traditional:
                return "T";
            case Virtual:
                return "V";
            case Wherigo:
                return "G";
            case Wikipedia:
                return "?";
            default:
                break;
        }
        return " ";
    }

    @Override
    public String toString() {
        switch (this) {
            case CITO:
                return "Cache In Trash Out Event";
            case Earth:
                return "Earthcache";
            case Event:
                return "Event Cache";
            case Final:
                return "Final Location";
            case Multi:
                return "Multi-cache";
            case MultiQuestion:
                return "Virtual Stage"; // "Question to Answer";
            case MultiStage:
                return "Physical Stage"; //"Stages of a Multicache";
            case Mystery:
                return "Unknown Cache";
            case ParkingArea:
                return "Parking Area";
            case ReferencePoint:
                return "Reference Point";
            case Traditional:
                return "Traditional Cache";
            default:
                break;
        }
        return super.toString();
    }
}
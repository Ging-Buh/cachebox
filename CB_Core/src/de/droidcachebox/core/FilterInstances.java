package de.droidcachebox.core;

import de.droidcachebox.database.CacheTypes;

import java.util.Arrays;

public class FilterInstances {

    // All Caches 0 (no where clause)
    public final static FilterProperties ALL = new FilterProperties();

    //alle aktiven : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='<user>') and Difficulty <= 5.0 and Terrain <= 5.0 and Type in (0,3,4,21)
    public final static FilterProperties ACTIVE = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //"
            "}");
    //schnell      : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='<user>') and Difficulty <= 5.0 and Terrain <= 5.0 and Type in (1,4,5,22)
    public final static FilterProperties QUICK = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,0\"," + //
            "\"types\":\"" + sQuickCacheTypes() + "\"," + //
            "}");
    //Anfaenger    : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='arbor95') and Difficulty <= 4.0 and Terrain <= 4.0 and Size >= 2.0 and Type in (1)
    public final static FilterProperties BEGINNER = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.0,1.0,2.0,2.0,4.0,0.0,5.0,0\"," + //
            "\"types\":\"" + sBEGINNERCacheTypes() + "\"," + //
            "}");
    //with TB      : Available=1 and Archived=0 and NumTravelbugs > 0 and Difficulty <= 6.0 and Terrain <= 6.0 and Type in (1,2,3,6,7,8,10,11,23)
    public final static FilterProperties WITHTB = new FilterProperties("{" + //
            "\"caches\":\"0,-1,-1,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,0\"," + //
            "\"types\":\"" + sTBsCacheTypes() + "\"," + //
            "}");
    //place TB     : Available=1 and Archived=0 and Difficulty <= 6.0 and Terrain <= 6.0 and Size >= 2.0 and Type in (1,2,3,6,7,8,10,11,23)
    public final static FilterProperties DROPTB = new FilterProperties("{" + //
            "\"caches\":\"0,-1,-1,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,0\"," + //
            "\"types\":\"" + sTBsCacheTypes() + "\"," + //
            "}");
    //Highlights   : (Found=0 or Found is null) and Available=1 and Archived=0 and Rating >= 350.0
    public final static FilterProperties HIGHLIGHTS = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0\"," + //
            "}");
    // Favoriten   : Favorit=1
    public final static FilterProperties FAVORITES = new FilterProperties("{" + //
            "\"caches\":\"0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "}");
    // to archive  : Archived=0 and (not Owner='<user>') and (Favorit=0 or Favorit is null) and (ListingChanged=0 or ListingChanged is null) and (HasUserData = 0 or HasUserData is null)
    public final static FilterProperties TOARCHIVE = new FilterProperties("{" + //
            "\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "}");
    //Listing Changed : ListingChanged=1
    public final static FilterProperties LISTINGCHANGED = new FilterProperties("{" + //
            "\"caches\":\"0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "}");
    public final static FilterProperties UserDefinedSQL = new FilterProperties(true);
    public static FilterProperties HISTORY = new FilterProperties(); // == ALL, isHistory wird vor Verwendung gesetzt daher nicht final
    private static FilterProperties mLastFilter = null;

    // Quick Cache 2
    private static String sQuickCacheTypes() {
        // true,false,false,true,true,-false,-false,-false,-false,-false,-false,true,false
        // Traditional,-Multi,-Mystery,Camera,Earth,-Event,-MegaEvent,-CITO,-Virtual,-Letterbox,-Wherigo,
        //-ReferencePoint,-Wikipedia,-Undefined,-MultiStage,-MultiQuestion, -Trailhead,-ParkingArea,-Final,-Cache,-MyParking, Munzee,-Giga,
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        mCacheTypes[CacheTypes.Camera.ordinal()] = true;
        mCacheTypes[CacheTypes.Earth.ordinal()] = true;
        mCacheTypes[CacheTypes.Munzee.ordinal()] = true;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < mCacheTypes.length; i++) {
            tmp.append(",").append(mCacheTypes[i]);
        }
        return tmp.toString();
    }

    // Quick Cache 2
    private static String sBEGINNERCacheTypes() {
        // true,false,false,true,true,-false,-false,-false,-false,-false,-false,true,false
        // Traditional,-Multi,-Mystery,Camera,Earth,-Event,-MegaEvent,-CITO,-Virtual,-Letterbox,-Wherigo,
        //-ReferencePoint,-Wikipedia,-Undefined,-MultiStage,-MultiQuestion, -Trailhead,-ParkingArea,-Final,-Cache,-MyParking, Munzee,-Giga,
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < mCacheTypes.length; i++) {
            tmp.append(",").append(mCacheTypes[i]);
        }
        return tmp.toString();
    }

    // Travelbugs 4
    private static String sTBsCacheTypes() {
        // "\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\"," + //
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        mCacheTypes[CacheTypes.CITO.ordinal()] = true;
        mCacheTypes[CacheTypes.Event.ordinal()] = true;
        mCacheTypes[CacheTypes.Giga.ordinal()] = true;
        mCacheTypes[CacheTypes.Letterbox.ordinal()] = true;
        mCacheTypes[CacheTypes.MegaEvent.ordinal()] = true;
        mCacheTypes[CacheTypes.Multi.ordinal()] = true;
        mCacheTypes[CacheTypes.Mystery.ordinal()] = true;
        mCacheTypes[CacheTypes.Wherigo.ordinal()] = true;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < mCacheTypes.length; i++) {
            tmp.append(",").append(mCacheTypes[i]);
        }
        return tmp.toString();
    }

    public static FilterProperties getLastFilter() {
        return mLastFilter;
    }

    public static void setLastFilter(FilterProperties lastFilter) {
        mLastFilter = lastFilter;
    }

    public static boolean isLastFilterSet() {
        return mLastFilter != null && !mLastFilter.toString().equals("") && !ALL.equals(mLastFilter);
    }

}

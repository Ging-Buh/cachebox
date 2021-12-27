package de.droidcachebox.core;

import static de.droidcachebox.dataclasses.GeoCacheType.CITO;
import static de.droidcachebox.dataclasses.GeoCacheType.Camera;
import static de.droidcachebox.dataclasses.GeoCacheType.Earth;
import static de.droidcachebox.dataclasses.GeoCacheType.Event;
import static de.droidcachebox.dataclasses.GeoCacheType.Giga;
import static de.droidcachebox.dataclasses.GeoCacheType.Letterbox;
import static de.droidcachebox.dataclasses.GeoCacheType.MegaEvent;
import static de.droidcachebox.dataclasses.GeoCacheType.Multi;
import static de.droidcachebox.dataclasses.GeoCacheType.Munzee;
import static de.droidcachebox.dataclasses.GeoCacheType.Mystery;
import static de.droidcachebox.dataclasses.GeoCacheType.Traditional;
import static de.droidcachebox.dataclasses.GeoCacheType.Wherigo;

import java.util.Arrays;
import java.util.List;

import de.droidcachebox.dataclasses.GeoCacheType;

public class FilterInstances {

    // All Caches 0 (no where clause)
    public final static FilterProperties ALL = new FilterProperties();

    //alle aktiven : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='<user>') and Difficulty <= 5.0 and Terrain <= 5.0 and Type in (0,3,4,21)
    public final static FilterProperties ACTIVE = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"" + //"
            "}");
    //schnell      : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='<user>') and Difficulty <= 5.0 and Terrain <= 5.0 and Type in (1,4,5,22)
    public final static FilterProperties QUICK = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,0\"" + //
            ",\"CacheTypes\":\"" + getCacheTypes(Arrays.asList(Traditional, Camera, Earth, Munzee)) + "\"" + //
            "}");
    //Anfaenger    : (Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='arbor95') and Difficulty <= 4.0 and Terrain <= 4.0 and Size >= 2.0 and Type in (1)
    public final static FilterProperties BEGINNER = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.0,1.0,2.0,2.0,4.0,0.0,5.0,0\"" + //
            ",\"CacheTypes\":\"" + Traditional.ordinal() + "\"" + //
            "}");
    //with TB      : Available=1 and Archived=0 and NumTravelbugs > 0 and Difficulty <= 6.0 and Terrain <= 6.0 and Type in (1,2,3,6,7,8,10,11,23)
    public final static FilterProperties WITHTB = new FilterProperties("{" + //
            "\"caches\":\"0,-1,-1,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,0\"" + //
            ",\"CacheTypes\":\"" + getTBCacheTypes() + "\"" + //
            "}");
    //place TB     : Available=1 and Archived=0 and Difficulty <= 6.0 and Terrain <= 6.0 and Size >= 2.0 and Type in (1,2,3,6,7,8,10,11,23)
    public final static FilterProperties DROPTB = new FilterProperties("{" + //
            "\"caches\":\"0,-1,-1,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,0\"" + //
            ",\"CacheTypes\":\"" + getTBCacheTypes() + "\"" + //
            "}");
    //Highlights   : (Found=0 or Found is null) and Available=1 and Archived=0 and Rating >= 350.0
    public final static FilterProperties HIGHLIGHTS = new FilterProperties("{" + //
            "\"caches\":\"-1,-1,-1,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0\"" + //
            "}");
    // Favoriten   : Favorit=1
    public final static FilterProperties FAVORITES = new FilterProperties("{" + //
            "\"caches\":\"0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"" + //
            "}");
    // to archive  : Archived=0 and (not Owner='<user>') and (Favorit=0 or Favorit is null) and (ListingChanged=0 or ListingChanged is null) and (HasUserData = 0 or HasUserData is null)
    public final static FilterProperties TOARCHIVE = new FilterProperties("{" + //
            "\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"" + //
            "}");
    //Listing Changed : ListingChanged=1
    public final static FilterProperties LISTINGCHANGED = new FilterProperties("{" + //
            "\"caches\":\"0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"" + //
            "}");
    public static FilterProperties HISTORY = new FilterProperties(); // == ALL, isHistory wird vor Verwendung gesetzt daher nicht final
    private static FilterProperties mLastFilter = null;

    private static String getCacheTypes(List<GeoCacheType> geoCacheTypes) {
        String tmp = "";
        for (GeoCacheType ct : geoCacheTypes) {
            tmp = tmp + "," + ct.ordinal();
        }
        return tmp.substring(1);
    }

    private static String getTBCacheTypes() {
        return getCacheTypes(Arrays.asList(Traditional, CITO, Event, Giga, Letterbox, MegaEvent, Multi, Mystery, Wherigo));
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

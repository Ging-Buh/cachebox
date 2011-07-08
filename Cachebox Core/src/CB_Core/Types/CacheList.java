package CB_Core.Types;

import java.util.ArrayList;
import java.util.Collections;

import CB_Core.GlobalCore;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class CacheList extends ArrayList<Cache> {
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayList<MysterySolution> MysterySolutions;

	public CacheList() {
		
	}
/*
    public void LoadCaches(String where)
    {
    	SortedMap<Long, ArrayList<Waypoint>> waypoints;
    	waypoints = new TreeMap<Long, ArrayList<Waypoint>>();
        MysterySolutions = new ArrayList<MysterySolution>();
        // zuerst alle Waypoints einlesen
        ArrayList<Waypoint> wpList = new ArrayList<Waypoint>();
        long aktCacheID = -1;

        Cursor reader = Database.Data.myDB.rawQuery("select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint order by CacheId", null);
    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
            Waypoint wp = Database.getWaypoint(reader);
            if (wp.CacheId != aktCacheID)
            {
                aktCacheID = wp.CacheId;
                wpList = new ArrayList<Waypoint>();
                waypoints.put(aktCacheID, wpList);
            }
            wpList.add(wp);
            reader.moveToNext();
            
        }
        reader.close();


        try
        {
        	reader = Database.Data.myDB.rawQuery("select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates from Caches " + ((where.length() > 0) ? "where " + where : where), null);
    	
        }
        catch(Exception e)
        {
        	Logger.Error("CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
        }
    	reader.moveToFirst();
    	
        while(reader.isAfterLast() == false)
        {
            Cache cache = Database.getCache(reader);
            
            this.add(cache);
            if (waypoints.containsKey(cache.Id))
            {
            	cache.waypoints = waypoints.get(cache.Id);
                waypoints.remove(cache.Id);
                if (cache.Type == CacheTypes.Multi || cache.Type == CacheTypes.Mystery || cache.Type == CacheTypes.Wherigo)
                {
                	for (Waypoint wp : cache.waypoints)
                	{
	                    if (wp.Type == CacheTypes.Final)
	                    {
	                        MysterySolution solution = new MysterySolution();
	                        solution.Cache = cache;
	                        solution.Waypoint = wp;
	                        solution.Latitude = wp.Pos.Latitude;
	                        solution.Longitude = wp.Pos.Longitude;
	                        MysterySolutions.add(solution);
	                    }
                	}
                	
                }
            } else
            	cache.waypoints = new ArrayList<Waypoint>();

//            ++Global.CacheCount;
            reader.moveToNext();
            
        }

        reader.close();
//        Query.Sort();
        Collections.sort(this);
    }
*/	
    public Cache GetCacheByGcCode(String GcCode)
    {
    	for (Cache cache : this)
    	{
    		if (cache.GcCode.equalsIgnoreCase(GcCode))
    			return cache;
    	}
    	return null;
    }

    public Cache GetCacheById(long cacheId)
    {
    	for (Cache cache : this)
    	{
    		if (cache.Id == cacheId)
    			return cache;
    	}
    	return null;
    }

    public void Resort()
    {
        GlobalCore.ResortAtWork = true;
// Alle Distanzen aktualisieren
        for (Cache cache : this)
        {
            cache.Distance(true);
        }

        Collections.sort(this);

         // Nächsten Cache auswählen
         if (this.size() > 0)
         {
        	 Cache nextCache = this.get(0);
        	 // Wenn der nachste Cache ein Mystery mit Final Waypoint ist 
        	 // -> gleich den Final Waypoint auswahlen!!!
        	 // When the next Cache is a mystery with final waypoint
        	 // -> activate the final waypoint!!!
        	 Waypoint waypoint = nextCache.GetFinalWaypoint();
        	 GlobalCore.SelectedWaypoint(nextCache, waypoint);
        	 // Global.SelectedCache = Geocaching.Cache.Query[0];
        	 GlobalCore.NearestCache(nextCache);
         }

         CB_Core.Events.CachListChangedEventList.Call();

//        Cursor.Current = Cursors.Default;    	
         GlobalCore.ResortAtWork = false;
    }

}

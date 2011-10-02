package CB_Core.DAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Core.Types.CacheList;
import CB_Core.Types.MysterySolution;

public class CacheListDAO {
    public CacheList ReadCacheList(CacheList cacheList, String where)
    {
    	SortedMap<Long, ArrayList<Waypoint>> waypoints;
    	waypoints = new TreeMap<Long, ArrayList<Waypoint>>();
        cacheList.MysterySolutions = new ArrayList<MysterySolution>();
        // zuerst alle Waypoints einlesen
        ArrayList<Waypoint> wpList = new ArrayList<Waypoint>();
        long aktCacheID = -1;

        CoreCursor reader = Database.Data.rawQuery("select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint order by CacheId", null);
    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
        	WaypointDAO waypointDAO = new WaypointDAO();
            Waypoint wp = waypointDAO.getWaypoint(reader);
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
        	reader = Database.Data.rawQuery("select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus from Caches " + ((where.length() > 0) ? "where " + where : where), null);
    	
        }
        catch(Exception e)
        {
        	Logger.Error("CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
        }
    	reader.moveToFirst();
    	
    	CacheDAO cacheDAO = new CacheDAO();
        while(reader.isAfterLast() == false)
        {
            Cache cache = cacheDAO.ReadFromCursor(reader);
            
            cacheList.add(cache);
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
	                        cacheList.MysterySolutions.add(solution);
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
        try
        {
        	Collections.sort(cacheList);
        }
        catch(Exception e)
        {
        	Logger.Error("CacheListDAO.ReadCacheList()", "Sort ERROR", e);
        }
        
    	return cacheList;
    }
}

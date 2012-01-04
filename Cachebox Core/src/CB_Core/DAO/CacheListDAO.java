package CB_Core.DAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.Config;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.MysterySolution;
import CB_Core.Types.Waypoint;

public class CacheListDAO
{
	public CacheList ReadCacheList(CacheList cacheList, String where)
	{
		SortedMap<Long, ArrayList<Waypoint>> waypoints;
		waypoints = new TreeMap<Long, ArrayList<Waypoint>>();
		cacheList.MysterySolutions = new ArrayList<MysterySolution>();
		// zuerst alle Waypoints einlesen
		ArrayList<Waypoint> wpList = new ArrayList<Waypoint>();
		long aktCacheID = -1;

		CoreCursor reader = Database.Data
				.rawQuery(
						"select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint order by CacheId",
						null);
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
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
			reader = Database.Data
					.rawQuery(
							"select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint from Caches "
									+ ((where.length() > 0) ? "where " + where : where), null);

		}
		catch (Exception e)
		{
			Logger.Error("CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
		}
		reader.moveToFirst();

		CacheDAO cacheDAO = new CacheDAO();
		while (reader.isAfterLast() == false)
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
			}
			else
				cache.waypoints = new ArrayList<Waypoint>();

			// ++Global.CacheCount;
			reader.moveToNext();

		}

		reader.close();
		// Query.Sort();
		try
		{
			Collections.sort(cacheList);
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.ReadCacheList()", "Sort ERROR", e);
		}

		// add Parking Cache
		if (Config.settings.ParkingLatitude.getValue() != 0)
		{
			Cache cache = new Cache(Config.settings.ParkingLatitude.getValue(), Config.settings.ParkingLongitude.getValue(),
					"My Parking area", CacheTypes.MyParking, "CBPark");

			cacheList.add(0, cache);
		}

		return cacheList;
	}

	public long DelArchiv()
	{
		try
		{
			long ret = Database.Data.delete("Caches", "Archived=1", null);
			delCacheImages(getDelGcCodeList("Archived=1"));
			return ret;
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.DelArchiv()", "Archiv ERROR", e);
			return -1;
		}
	}

	public long DelFound()
	{
		try
		{
			long ret = Database.Data.delete("Caches", "Found=1", null);
			delCacheImages(getDelGcCodeList("Found=1"));
			return ret;
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.DelFound()", "Found ERROR", e);
			return -1;
		}
	}

	public long DelFilter(String Where)
	{
		try
		{
			long ret = Database.Data.delete("Caches", Where, null);
			delCacheImages(getDelGcCodeList(Where));
			return ret;
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.DelFilter()", "Filter ERROR", e);
			return -1;
		}
	}

	private ArrayList<String> getDelGcCodeList(String where)
	{
		CacheList list = new CacheList();
		ReadCacheList(list, where);
		ArrayList<String> StrList = new ArrayList<String>();

		for (Iterator<Cache> iterator = list.iterator(); iterator.hasNext();)
		{
			StrList.add(iterator.next().GcCode);
		}
		return StrList;
	}

	/**
	 * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
	 * 
	 * @param list
	 */
	private void delCacheImages(ArrayList<String> list)
	{
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();)
		{
			String gc = iterator.next();
			// hier müssen jetzt alle Images mit dem GC-Code anfangen gelöscht werden
		}
	}

}

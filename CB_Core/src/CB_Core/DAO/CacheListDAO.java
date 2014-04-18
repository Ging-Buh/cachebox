package CB_Core.DAO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.CacheListLite;
import CB_Core.Types.CacheLite;
import CB_Core.Types.Waypoint;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Logger;
import CB_Utils.Util.FileIO;

public class CacheListDAO
{

	public CacheList ReadCacheList(CacheList cacheList, String where)
	{
		return ReadCacheList(cacheList, "", where, false);
	}

	public CacheList ReadCacheList(CacheList cacheList, String join, String where)
	{
		return ReadCacheList(cacheList, join, where, false);
	}

	public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean withDescription)
	{
		if (cacheList == null) return null;

		// Clear List before read
		cacheList.clear();

		Logger.DEBUG("ReadCacheList 1.Waypoints");
		SortedMap<Long, CB_List<Waypoint>> waypoints;
		waypoints = new TreeMap<Long, CB_List<Waypoint>>();
		// zuerst alle Waypoints einlesen
		CB_List<Waypoint> wpList = new CB_List<Waypoint>();
		long aktCacheID = -1;

		CoreCursor reader = Database.Data
				.rawQuery(
						"select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title, isStart from Waypoint order by CacheId",
						null);
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			WaypointDAO waypointDAO = new WaypointDAO();
			Waypoint wp = waypointDAO.getWaypoint(reader);
			if (wp.CacheId != aktCacheID)
			{
				aktCacheID = wp.CacheId;
				wpList = new CB_List<Waypoint>();
				waypoints.put(aktCacheID, wpList);
			}
			wpList.add(wp);
			reader.moveToNext();

		}
		reader.close();

		Logger.DEBUG("ReadCacheList 2.Caches");
		try
		{
			String sql = "select c.Id, GcCode, Latitude, Longitude, c.Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint";
			if (withDescription)
			{
				sql += ", Description, Solver, Notes";
			}
			sql += " from Caches c " + join + " " + ((where.length() > 0) ? "where " + where : where);
			reader = Database.Data.rawQuery(sql, null);

		}
		catch (Exception e)
		{
			Logger.Error("CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
		}
		reader.moveToFirst();

		CacheDAO cacheDAO = new CacheDAO();
		while (!reader.isAfterLast())
		{
			Cache cache = cacheDAO.ReadFromCursor(reader, withDescription);

			cacheList.add(cache);
			cache.waypoints.clear();
			if (waypoints.containsKey(cache.Id))
			{
				CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.Id);

				for (int i = 0, n = tmpwaypoints.size(); i < n; i++)
				{
					cache.waypoints.add(tmpwaypoints.get(i));
				}

				waypoints.remove(cache.Id);
			}

			// ++Global.CacheCount;
			reader.moveToNext();

		}
		reader.close();

		// clear other never used WP`s from Mem
		waypoints.clear();
		waypoints = null;

		// do it manual (or automated after fix), got hanging app on startup
		// Logger.DEBUG("ReadCacheList 3.Sorting");
		try
		{
			// Collections.sort(cacheList);
		}
		catch (Exception e)
		{
			// Logger.Error("CacheListDAO.ReadCacheList()", "Sort ERROR", e);
		}
		// Logger.DEBUG("ReadCacheList 4. ready");
		return cacheList;
	}

	/**
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long DelArchiv(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal)
	{
		try
		{
			delCacheImages(getDelGcCodeList("Archived=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder,
					DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Archived=1", null);
			return ret;
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.DelArchiv()", "Archiv ERROR", e);
			return -1;
		}
	}

	/**
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long DelFound(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal)
	{
		try
		{
			delCacheImages(getDelGcCodeList("Found=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder,
					DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Found=1", null);
			return ret;
		}
		catch (Exception e)
		{
			Logger.Error("CacheListDAO.DelFound()", "Found ERROR", e);
			return -1;
		}
	}

	/**
	 * @param Where
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long DelFilter(String Where, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder,
			String DescriptionImageFolderLocal)
	{
		try
		{
			delCacheImages(getDelGcCodeList(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", Where, null);
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

		for (int i = 0, n = list.size(); i < n; i++)
		{
			StrList.add(list.get(i).getGcCode());
		}
		return StrList;
	}

	/**
	 * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
	 * 
	 * @param list
	 */

	/**
	 * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
	 * 
	 * @param list
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 */
	public void delCacheImages(ArrayList<String> list, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder,
			String DescriptionImageFolderLocal)
	{
		String spoilerpath = SpoilerFolder;
		if (SpoilerFolderLocal.length() > 0) spoilerpath = SpoilerFolderLocal;

		String imagespath = DescriptionImageFolder;
		if (DescriptionImageFolderLocal.length() > 0) imagespath = DescriptionImageFolderLocal;

		delCacheImagesByPath(spoilerpath, list);
		delCacheImagesByPath(imagespath, list);

		ImageDAO imageDAO = new ImageDAO();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();)
		{
			final String GcCode = iterator.next();
			imageDAO.deleteImagesForCache(GcCode);
		}
		imageDAO = null;
	}

	private void delCacheImagesByPath(String path, ArrayList<String> list)
	{
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();)
		{
			final String GcCode = iterator.next();
			String directory = path + "/" + GcCode.substring(0, 4);
			if (!FileIO.DirectoryExists(directory)) continue;
			File dir = new File(directory);
			FilenameFilter filter = new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String filename)
				{
					filename = filename.toLowerCase();
					return (filename.indexOf(GcCode.toLowerCase()) == 0);
				}
			};
			String[] files = dir.list(filter);
			for (int i = 0; i < files.length; i++)
			{
				String filename = dir + "/" + files[i];
				File file = new File(filename);
				if (file.exists())
				{
					if (!file.delete()) Logger.DEBUG("Error deleting : " + filename);
				}
			}
		}
	}

	public CacheListLite ReadCacheList(CacheListLite cacheList, String where)
	{
		return ReadCacheList(cacheList, "", where, false);
	}

	public CacheListLite ReadCacheList(CacheListLite cacheList, String join, String where)
	{
		return ReadCacheList(cacheList, join, where, false);
	}

	public CacheListLite ReadCacheList(CacheListLite cacheList, String join, String where, boolean withDescription)
	{
		if (cacheList == null) return null;

		// Clear List before read
		cacheList.clear();

		// zuerst alle Waypoints einlesen
		CB_List<Waypoint> wpList = new CB_List<Waypoint>();
		long aktCacheID = -1;

		SortedMap<Long, CB_List<Waypoint>> waypoints = new TreeMap<Long, CB_List<Waypoint>>();

		CoreCursor reader = Database.Data
				.rawQuery(
						"select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title, isStart from Waypoint order by CacheId",
						null);
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			WaypointDAO waypointDAO = new WaypointDAO();
			Waypoint wp = waypointDAO.getWaypoint(reader);
			if (wp.CacheId != aktCacheID)
			{
				aktCacheID = wp.CacheId;
				wpList = new CB_List<Waypoint>();
				waypoints.put(aktCacheID, wpList);
			}
			wpList.add(wp);
			reader.moveToNext();

		}
		reader.close();

		Logger.DEBUG("ReadCacheList 2.Caches");
		try
		{
			String sql = "select c.Id, GcCode, Latitude, Longitude, c.Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, Rating, Favorit, CorrectedCoordinates, Owner";
			if (withDescription)
			{
				sql += ", Description, Solver, Notes";
			}
			sql += " from Caches c " + join + " " + ((where.length() > 0) ? "where " + where : where);
			reader = Database.Data.rawQuery(sql, null);

		}
		catch (Exception e)
		{
			Logger.Error("CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
		}

		if (reader == null) return cacheList;

		reader.moveToFirst();

		CacheDAO cacheDAO = new CacheDAO();
		while (!reader.isAfterLast())
		{
			CacheLite cache = cacheDAO.ReadFromCursorLite(reader, withDescription);
			boolean hasStart = false;
			boolean hasFinal = false;
			if (waypoints.containsKey(cache.Id))
			{
				// set Final or start waypoint

				CB_List<Waypoint> cacheWaypoints = waypoints.get(cache.Id);

				for (int i = 0, n = cacheWaypoints.size(); i < n; i++)
				{
					Waypoint wp = cacheWaypoints.get(i);

					if ((wp.Type == CacheTypes.MultiStage) && (wp.IsStart))
					{
						cache.hasStartWaypoint = 1;
						cache.startWaypoint = wp;
						hasStart = true;
					}

					if (wp.Type == CacheTypes.Final)
					{
						// do not activate final waypoint with invalid coordinates
						if (!wp.Pos.isValid() || wp.Pos.isZero()) continue;
						cache.FinalWaypoint = wp;
						cache.hasFinalWaypoint = 1;
						hasFinal = true;
					}

				}

				cacheWaypoints.clear();
				waypoints.remove(cache.Id);
			}

			if (!hasFinal) cache.hasFinalWaypoint = 0;
			if (!hasStart) cache.hasStartWaypoint = 0;

			cacheList.add(cache);
			reader.moveToNext();

		}

		waypoints.clear();
		reader.close();

		return cacheList;
	}

}

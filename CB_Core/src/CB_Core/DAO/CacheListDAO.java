package CB_Core.DAO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Core.Util.FileIO;

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
		SortedMap<Long, ArrayList<Waypoint>> waypoints;
		waypoints = new TreeMap<Long, ArrayList<Waypoint>>();
		// zuerst alle Waypoints einlesen
		ArrayList<Waypoint> wpList = new ArrayList<Waypoint>();
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
				wpList = new ArrayList<Waypoint>();
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
				sql += ", Description";
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
			if (waypoints.containsKey(cache.Id))
			{
				cache.waypoints = waypoints.get(cache.Id);
				waypoints.remove(cache.Id);
			}
			else
				cache.waypoints = new ArrayList<Waypoint>();

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

}

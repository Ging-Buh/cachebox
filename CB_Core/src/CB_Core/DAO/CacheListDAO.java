/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_Core.DAO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.FilterProperties;
import CB_Core.Tag;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.FileIO;

import com.badlogic.gdx.Gdx;

/**
 * @author ging-buh
 * @author Longri
 */
public class CacheListDAO
{

	public CacheList ReadCacheList(CacheList cacheList, ArrayList<String> GC_Codes, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints)
	{
		ArrayList<String> orParts = new ArrayList<String>();

		for (String gcCode : GC_Codes)
		{
			orParts.add("GcCode like '%" + gcCode + "%'");
		}
		String where = FilterProperties.join(" or ", orParts);
		return ReadCacheList(cacheList, "", where, withDescription, fullDetails, loadAllWaypoints);
	}

	public CacheList ReadCacheList(CacheList cacheList, String where, boolean fullDetails, boolean loadAllWaypoints)
	{
		return ReadCacheList(cacheList, "", where, false, fullDetails, loadAllWaypoints);
	}

	// public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean fullDetails, boolean loadAllWaypoints)
	// {
	// return ReadCacheList(cacheList, join, where, false, fullDetails, loadAllWaypoints);
	// }

	public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints)
	{
		if (cacheList == null) return null;

		// Clear List before read
		cacheList.clear();

		Gdx.app.debug(Tag.TAG, "ReadCacheList 1.Waypoints");
		SortedMap<Long, CB_List<Waypoint>> waypoints;
		waypoints = new TreeMap<Long, CB_List<Waypoint>>();
		// zuerst alle Waypoints einlesen
		CB_List<Waypoint> wpList = new CB_List<Waypoint>();
		long aktCacheID = -1;

		String sql = fullDetails ? WaypointDAO.SQL_WP_FULL : WaypointDAO.SQL_WP;
		if (!((fullDetails || loadAllWaypoints)))
		{
			// when CacheList should be loaded without full details and without all Waypoints
			// do not load all waypoints from db!
			sql += " where IsStart=\"true\" or Type=18"; // StartWaypoint or Final
		}
		sql += " order by CacheId";
		CoreCursor reader = Database.Data.rawQuery(sql, null);
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			WaypointDAO waypointDAO = new WaypointDAO();
			Waypoint wp = waypointDAO.getWaypoint(reader, fullDetails);
			if (!(fullDetails || loadAllWaypoints))
			{
				// wenn keine FullDetails geladen werden sollen dann sollen nur die Finals und Start-Waypoints geladen werden
				if (!(wp.IsStart || wp.Type == CacheTypes.Final))
				{
					reader.moveToNext();
					continue;
				}
			}
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

		Gdx.app.debug(Tag.TAG, "ReadCacheList 2.Caches");
		try
		{
			if (fullDetails)
			{
				sql = CacheDAO.SQL_GET_CACHE + ", " + CacheDAO.SQL_DETAILS;
				if (withDescription)
				{
					// load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
					sql += "," + CacheDAO.SQL_GET_DETAIL_WITH_DESCRIPTION;
				}
			}
			else
			{
				sql = CacheDAO.SQL_GET_CACHE;

			}

			// if (withDescription)
			// {
			// sql += ", Description, Solver, Notes";
			// }
			sql += " from Caches c " + join + " " + ((where.length() > 0) ? "where " + where : where);
			reader = Database.Data.rawQuery(sql, null);

		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "CacheList.LoadCaches() reader = Database.Data.myDB.rawQuery(....", e);
		}
		reader.moveToFirst();

		CacheDAO cacheDAO = new CacheDAO();
		long start = System.currentTimeMillis();
		while (!reader.isAfterLast())
		{
			Cache cache = cacheDAO.ReadFromCursor(reader, fullDetails, withDescription);

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
		long end = System.currentTimeMillis();
		Gdx.app.log(Tag.TAG, "Load Cachelist at" + String.valueOf(end - start) + "ms");

		// clear other never used WP`s from Mem
		waypoints.clear();
		waypoints = null;

		// do it manual (or automated after fix), got hanging app on startup
		// Gdx.app.debug(Tag.TAG,"ReadCacheList 3.Sorting");
		try
		{
			// Collections.sort(cacheList);
		}
		catch (Exception e)
		{
			// Gdx.app.error(Tag.TAG,"CacheListDAO.ReadCacheList()", "Sort ERROR", e);
		}
		// Gdx.app.debug(Tag.TAG,"ReadCacheList 4. ready");
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
			delCacheImages(getGcCodeList("Archived=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Archived=1", null);
			return ret;
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "CacheListDAO.DelArchiv()", e);
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
			delCacheImages(getGcCodeList("Found=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Found=1", null);
			return ret;
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "CacheListDAO.DelFound()", e);
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
	public long DelFilter(String Where, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal)
	{
		try
		{
			delCacheImages(getGcCodeList(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", Where, null);
			return ret;
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "CacheListDAO.DelFilter()", e);
			return -1;
		}
	}

	private ArrayList<String> getGcCodeList(String where)
	{
		CacheList list = new CacheList();
		ReadCacheList(list, where, false, false);
		ArrayList<String> StrList = new ArrayList<String>();

		for (int i = 0, n = list.size(); i < n; i++)
		{
			StrList.add(list.get(i).getGcCode());
		}
		list.dispose();
		list = null;
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
	public void delCacheImages(ArrayList<String> list, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal)
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
					if (!file.delete()) Gdx.app.debug(Tag.TAG, "Error deleting : " + filename);
				}
			}
		}
	}
}

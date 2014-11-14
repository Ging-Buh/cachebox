package CB_Core.Import;

import java.io.IOException;
import java.util.HashMap;

import CB_Core.Tag;
import CB_Core.DB.Database;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.SDBM_Hash;

import com.badlogic.gdx.Gdx;

public class CacheInfoList
{

	/**
	 * Die Liste der Cache Infos, welche mit IndexDB() gef�llt und mit dispose() gel�scht wird.
	 */
	private static HashMap<String, CacheInfo> List = null;

	/**
	 * Mit dieser Methode wird die DB indexiert und die Klasse enth�lt dann eine Statiche Liste mit den Cache Informationen. Wenn die Liste
	 * nicht mehr ben�tigt wird, sollte sie mit dispose() gel�scht werden.
	 */
	public static void IndexDB()
	{
		List = new HashMap<String, CacheInfo>();

		CoreCursor reader = Database.Data.rawQuery("select GcCode, Id, ListingCheckSum, ImagesUpdated, DescriptionImagesUpdated, ListingChanged, Found, CorrectedCoordinates, Latitude, Longitude, GpxFilename_Id, Favorit from Caches", null);

		reader.moveToFirst();

		while (!reader.isAfterLast())
		{
			CacheInfo cacheInfo = new CacheInfo();

			cacheInfo.id = reader.getLong(1);

			if (reader.isNull(2))
			{
				cacheInfo.ListingCheckSum = 0;
			}
			else
			{
				cacheInfo.ListingCheckSum = reader.getInt(2);
			}

			if (reader.isNull(3))
			{
				cacheInfo.ImagesUpdated = false;
			}
			else
			{
				cacheInfo.ImagesUpdated = reader.getInt(3) != 0;
			}

			if (reader.isNull(4))
			{
				cacheInfo.DescriptionImagesUpdated = false;
			}
			else
			{
				cacheInfo.DescriptionImagesUpdated = reader.getInt(4) != 0;
			}

			if (reader.isNull(5))
			{
				cacheInfo.ListingChanged = false;
			}
			else
			{
				cacheInfo.ListingChanged = reader.getInt(5) != 0;
			}

			if (reader.isNull(6))
			{
				cacheInfo.Found = false;
			}
			else
			{
				cacheInfo.Found = reader.getInt(6) != 0;
			}

			if (reader.isNull(7))
			{
				cacheInfo.CorrectedCoordinates = false;
			}
			else
			{
				cacheInfo.CorrectedCoordinates = reader.getInt(7) != 0;
			}

			if (reader.isNull(8))
			{
				cacheInfo.Latitude = 361;
			}
			else
			{
				cacheInfo.Latitude = reader.getDouble(8);
			}
			if (reader.isNull(9))
			{
				cacheInfo.Longitude = 361;
			}
			else
			{
				cacheInfo.Longitude = reader.getDouble(9);
			}

			cacheInfo.GpxFilename_Id = reader.getInt(10);

			if (reader.isNull(11))
			{
				cacheInfo.favorite = false;
			}
			else
			{
				cacheInfo.favorite = reader.getInt(11) != 0;
			}

			List.put(reader.getString(0), cacheInfo);
			reader.moveToNext();
		}
		reader.close();

	}

	/**
	 * Die statische Liste der Cache Informationen wird mit diesem Aufruf gel�scht und der Speicher wieder frei gegeben.
	 */
	public static void dispose()
	{
		if (List == null) return;
		List.clear();
		List = null;
	}

	/**
	 * True wenn der Cache in der Liste Existiert
	 * 
	 * @param GcCode
	 * @return
	 */
	public static boolean ExistCache(String GcCode)
	{
		if (List == null) return false;
		return List.containsKey(GcCode);
	}

	public static boolean CacheIsFavoriteInDB(String GcCode)
	{
		if (List == null) return false;
		if (List.containsKey(GcCode))
		{
			CacheInfo ci = List.get(GcCode);
			return ci.favorite;
		}
		else
			return false;
	}

	public static boolean CacheIsFoundInDB(String GcCode)
	{
		if (List == null) return false;

		if (List.containsKey(GcCode))
		{
			CacheInfo ci = List.get(GcCode);
			return ci.Found;
		}
		else
			return false;
	}

	/**
	 * F�gt die CacheInfo in der Liste mit dem Infos des �bergebenen Caches zusammen und �ndert gegebenenfalls die Changed Attribute neu!
	 * 
	 * @param cache
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @throws IOException
	 */
	public static void mergeCacheInfo(Cache cache) throws IOException
	{
		CacheInfo info = List.get(cache.getGcCode());
		String GcCode = cache.getGcCode();
		if (info != null)
		{

			String stringForListingCheckSum = Database.GetDescription(cache);
			String recentOwnerLogString = "";

			CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
			cleanLogs = Database.Logs(cache);// cache.Logs();

			if (cleanLogs.size() > 0)
			{
				for (int i = 0, n = cleanLogs.size(); i < n; i++)
				{
					LogEntry entry = cleanLogs.get(i);
					String Comment = entry.Comment;
					String Finder = entry.Finder;

					if (Finder.equalsIgnoreCase(cache.getOwner()))
					{
						recentOwnerLogString += Comment;
						break;
					}
				}
			}

			int ListingCheckSum = (int) (SDBM_Hash.sdbm(stringForListingCheckSum) + SDBM_Hash.sdbm(recentOwnerLogString));

			boolean ListingChanged = info.ListingChanged;
			boolean ImagesUpdated = info.ImagesUpdated;
			boolean DescriptionImagesUpdated = info.DescriptionImagesUpdated;

			if (info.ListingCheckSum == 0)
			{
				ImagesUpdated = false;
				DescriptionImagesUpdated = false;
			}
			else if (ListingCheckSum != info.ListingCheckSum)
			{
				int oldStyleListingCheckSum = stringForListingCheckSum.hashCode() + recentOwnerLogString.hashCode();

				if (oldStyleListingCheckSum != info.ListingCheckSum)
				{
					ListingChanged = true;
					ImagesUpdated = false;
					DescriptionImagesUpdated = false;

					if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() > 0) CB_Core_Settings.DescriptionImageFolder.setValue(CB_Core_Settings.DescriptionImageFolderLocal.getValue());

					// 2014-06-21 - Ging-Buh - .changed files are no longer used. Only information in DB (ImagesUpdated and
					// DescriptionImagesUpdated) are used
					// CreateChangedListingFile(CB_Core_Settings.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4) + "/"
					// + GcCode + ".changed");

				}
				else
				{
					// old Style Hash codes must also be converted to sdbm, so force update Description Images but without creating changed
					// files.
					DescriptionImagesUpdated = false;
					ImagesUpdated = false;
				}
			}

			if (!info.Found)
			{
				// nur wenn der Cache nicht als gefunden markiert ist, wird der Wert aus dem GPX Import �bernommen!
				info.Found = cache.isFound();
			}

			// Schreibe info neu in die List(l�sche den Eintrag vorher)

			List.remove(GcCode);
			if (!info.ListingChanged) info.ListingChanged = ListingChanged; // Wenn das Flag schon gesetzt ist, dann nicht ausversehen
																			// wieder zur�cksetzen!

			info.ImagesUpdated = ImagesUpdated;
			info.DescriptionImagesUpdated = DescriptionImagesUpdated;
			info.ListingCheckSum = ListingCheckSum;

			List.put(GcCode, info);
		}

	}

	/**
	 * Schreibt die Liste der CacheInfos zur�ck in die DB
	 */
	public static void writeListToDB()
	{
		for (CacheInfo info : List.values())
		{
			Parameters args = new Parameters();

			// bei einem Update m�ssen nicht alle infos �berschrieben werden

			args.put("ListingCheckSum", info.ListingCheckSum);
			args.put("ListingChanged", info.ListingChanged ? 1 : 0);
			args.put("ImagesUpdated", info.ImagesUpdated ? 1 : 0);
			args.put("DescriptionImagesUpdated", info.DescriptionImagesUpdated ? 1 : 0);
			args.put("ListingCheckSum", info.ListingCheckSum);
			args.put("Found", info.Found ? 1 : 0);

			try
			{
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(info.id) });
			}
			catch (Exception exc)
			{
				Gdx.app.error(Tag.TAG, "CacheInfoList.writeListToDB()", exc);

			}
		}
	}

	// private static void CreateChangedListingFile(String changedFileString) throws IOException
	// {
	// File file = new File(changedFileString);
	//
	// if (!file.exists())
	// {
	// String changedFileDir = changedFileString.substring(0, changedFileString.lastIndexOf("/"));
	// File Directory = new File(changedFileDir);
	//
	// if (!Directory.exists())
	// {
	// Directory.mkdirs();
	// }
	//
	// PrintWriter writer = new PrintWriter(new FileWriter(file));
	//
	// writer.write("Listing Changed!");
	// writer.close();
	// }
	// }

	/**
	 * Packt eine neue CacheInfo des �bergebenen Caches in die Liste
	 * 
	 * @param cache
	 */
	public static void putNewInfo(Cache cache)
	{
		CacheInfo info = new CacheInfo(cache.Id, cache.GPXFilename_ID);
		String stringForListingCheckSum = Database.GetDescription(cache);
		String recentOwnerLogString = "";

		CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
		cleanLogs = Database.Logs(cache);// cache.Logs();

		if (cleanLogs.size() > 0)
		{
			for (int i = 0, n = cleanLogs.size(); i < n; i++)
			{
				LogEntry entry = cleanLogs.get(i);
				String Comment = entry.Comment;
				String Finder = entry.Finder;

				if (Finder.equalsIgnoreCase(cache.getOwner()))
				{
					recentOwnerLogString += Comment;
					break;
				}
			}
		}

		int ListingCheckSum = (int) (SDBM_Hash.sdbm(stringForListingCheckSum) + SDBM_Hash.sdbm(recentOwnerLogString));
		info.ListingCheckSum = ListingCheckSum;
		info.Latitude = cache.Latitude();
		info.Longitude = cache.Longitude();
		info.Found = cache.isFound();
		info.favorite = cache.isFavorite();
		info.CorrectedCoordinates = cache.CorrectedCoordiantesOrMysterySolved();

		if (List == null) List = new HashMap<String, CacheInfo>();

		List.put(cache.getGcCode(), info);

	}

	public static long getIDfromGcCode(String gccode)
	{
		CacheInfo info = List.get(gccode);
		if (info != null) return info.id;
		return 0;
	}

	public static void setImageUpdated(String GcCode)
	{
		CacheInfo info = List.get(GcCode);
		List.remove(GcCode);

		info.ImagesUpdated = true;
		info.DescriptionImagesUpdated = true;

		List.put(GcCode, info);
	}
}

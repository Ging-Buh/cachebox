package CB_Core.DAO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Api.SearchForGeocaches;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Coordinate;
import CB_Core.Types.DLong;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class CacheDAO
{
	protected static final String sqlgetFromDbByCacheId = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint from Caches where id = ?";
	protected static final String sqlgetFromDbByGcCode = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint from Caches where GCCode = ?";

	protected static final String sqlExistsCache = "select 1 from Caches where Id = ?";

	public Cache ReadFromCursor(CoreCursor reader)
	{
		try
		{
			Cache cache = new Cache();
			cache.Id = reader.getLong(0);
			cache.GcCode = reader.getString(1).trim();
			cache.Pos = new Coordinate(reader.getDouble(2), reader.getDouble(3));
			cache.Name = reader.getString(4).trim();
			cache.Size = CacheSizes.parseInt(reader.getInt(5));
			cache.Difficulty = ((float) reader.getShort(6)) / 2;
			cache.Terrain = ((float) reader.getShort(7)) / 2;
			cache.Archived = reader.getInt(8) != 0;
			cache.Available = reader.getInt(9) != 0;
			cache.Found = reader.getInt(10) != 0;
			cache.Type = CacheTypes.values()[reader.getShort(11)];
			cache.PlacedBy = reader.getString(12).trim();
			cache.Owner = reader.getString(13).trim();

			String sDate = reader.getString(14);
			DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try
			{
				cache.DateHidden = iso8601Format.parse(sDate);
			}
			catch (ParseException e)
			{
			}

			cache.Url = reader.getString(15).trim();
			cache.NumTravelbugs = reader.getInt(16);
			cache.GcId = reader.getString(17).trim();
			cache.Rating = ((float) reader.getShort(18)) / 100.0f;
			if (reader.getInt(19) > 0) cache.setFavorit(true);
			else
				cache.setFavorit(false);
			if (reader.getString(20) != null) cache.TourName = reader.getString(20).trim();
			else
				cache.TourName = "";

			if (reader.getString(21) != "") cache.GPXFilename_ID = reader.getLong(21);
			else
				cache.GPXFilename_ID = -1;

			if (reader.getInt(22) > 0) cache.hasUserData = true;
			else
				cache.hasUserData = false;

			if (reader.getInt(23) > 0) cache.listingChanged = true;
			else
				cache.listingChanged = false;

			if (reader.getInt(24) > 0) cache.setCorrectedCoordinates(true);
			else
				cache.setCorrectedCoordinates(false);

			if (reader.isNull(25)) cache.ApiStatus = 0;
			else
				cache.ApiStatus = (byte) reader.getInt(25);

			cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
			cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());

			cache.setAttributesPositive(new DLong(reader.getLong(27), reader.getLong(26)));
			cache.setAttributesNegative(new DLong(reader.getLong(29), reader.getLong(28)));

			if (reader.getString(30) != null) cache.hint = reader.getString(30).trim();
			else
				cache.hint = "";

			return cache;
		}
		catch (Exception exc)
		{
			Logger.Error("Read Cache", "", exc);
			return null;
		}
	}

	public void WriteToDatabase(Cache cache)
	{
		// int newCheckSum = createCheckSum(WP);
		// Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
		Parameters args = new Parameters();
		args.put("Id", cache.Id);
		args.put("GcCode", cache.GcCode);
		args.put("GcId", cache.GcId);
		args.put("Latitude", cache.Pos.getLatitude());
		args.put("Longitude", cache.Pos.getLongitude());
		args.put("Name", cache.Name);
		try
		{
			args.put("Size", cache.Size.ordinal());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		args.put("Difficulty", (int) (cache.Difficulty * 2));
		args.put("Terrain", (int) (cache.Terrain * 2));
		args.put("Archived", cache.Archived ? 1 : 0);
		args.put("Available", cache.Available ? 1 : 0);
		args.put("Found", cache.Found);
		args.put("Type", cache.Type.ordinal());
		args.put("PlacedBy", cache.PlacedBy);
		args.put("Owner", cache.Owner);
		args.put("Country", cache.Country);
		args.put("State", cache.State);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(cache.DateHidden);
		args.put("DateHidden", stimestamp);
		String firstimported = iso8601Format.format(new Date());
		args.put("FirstImported", firstimported);
		args.put("Hint", cache.hint);

		if ((cache.shortDescription != null) && (cache.shortDescription.length() > 0))
		{
			args.put("Description", cache.shortDescription + "<br /><hr /><br />");
		}

		if ((cache.longDescription != null) && (cache.longDescription.length() > 0))
		{
			if (args.containsKey("Description"))
			{
				args.put("Description", args.get("Description") + cache.longDescription);
			}
			else
			{
				args.put("Description", cache.longDescription);
			}
		}

		cache.longDescription = ""; // clear longDescription because this will
									// be loaded from database when used
		args.put("Url", cache.Url);
		args.put("NumTravelbugs", cache.NumTravelbugs);
		args.put("Rating", (int) (cache.Rating * 100));
		// args.put("Vote", cache.);
		// args.put("VotePending", cache.);
		// args.put("Notes", );
		// args.put("Solver", cache.);
		args.put("AttributesPositive", cache.getAttributesPositive().getLow());
		args.put("AttributesPositiveHigh", cache.getAttributesPositive().getHigh());
		args.put("AttributesNegative", cache.getAttributesNegative().getLow());
		args.put("AttributesNegativeHigh", cache.getAttributesNegative().getHigh());
		// args.put("ListingCheckSum", cache.);
		args.put("GPXFilename_Id", cache.GPXFilename_ID);
		args.put("ApiStatus", cache.ApiStatus);
		args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);
		args.put("TourName", cache.TourName);

		try
		{
			long ret = Database.Data.insert("Caches", args);

		}
		catch (Exception exc)
		{
			Logger.Error("Write Cache", "", exc);

		}
	}

	public void WriteToDatabase_Found(Cache cache)
	{
		Parameters args = new Parameters();
		args.put("found", cache.Found);
		try
		{
			Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(cache.Id) });
			Replication.FoundChanged(cache.Id, cache.Found);
		}
		catch (Exception exc)
		{
			Logger.Error("Write Cache Found", "", exc);
		}
	}

	public void UpdateDatabase(Cache cache)
	{

		Parameters args = new Parameters();

		// bei einem Update müssen nicht alle infos überschrieben werden

		// args.put("Id", cache.Id);
		// args.put("GcCode", cache.GcCode);
		// args.put("GcId", cache.GcId);
		args.put("Latitude", cache.Pos.getLatitude());
		args.put("Longitude", cache.Pos.getLongitude());
		args.put("Name", cache.Name);
		try
		{
			args.put("Size", cache.Size.ordinal());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		args.put("Difficulty", (int) (cache.Difficulty * 2));
		args.put("Terrain", (int) (cache.Terrain * 2));
		args.put("Archived", cache.Archived ? 1 : 0);
		args.put("Available", cache.Available ? 1 : 0);
		args.put("Found", cache.Found);
		args.put("Type", cache.Type.ordinal());
		args.put("PlacedBy", cache.PlacedBy);
		args.put("Owner", cache.Owner);
		args.put("Country", cache.Country);
		args.put("State", cache.State);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(cache.DateHidden);
		args.put("DateHidden", stimestamp);
		args.put("Hint", cache.hint);

		if ((cache.shortDescription != null) && (cache.shortDescription.length() > 0))
		{
			args.put("Description", cache.shortDescription + "<br /><hr /><br />");
		}

		if ((cache.longDescription != null) && (cache.longDescription.length() > 0))
		{
			if (args.containsKey("Description"))
			{
				args.put("Description", args.get("Description") + cache.longDescription);
			}
			else
			{
				args.put("Description", cache.longDescription);
			}
		}

		cache.longDescription = ""; // clear longDescription because this will
									// be loaded from database when used
		args.put("Url", cache.Url);
		args.put("NumTravelbugs", cache.NumTravelbugs);
		args.put("Rating", (int) (cache.Rating * 100));
		// args.put("Vote", cache.);
		// args.put("VotePending", cache.);
		// args.put("Notes", );
		// args.put("Solver", cache.);
		args.put("AttributesPositive", cache.getAttributesPositive().getLow());
		args.put("AttributesPositiveHigh", cache.getAttributesPositive().getHigh());
		args.put("AttributesNegative", cache.getAttributesNegative().getLow());
		args.put("AttributesNegativeHigh", cache.getAttributesNegative().getHigh());
		// args.put("ListingCheckSum", cache.);
		args.put("GPXFilename_Id", cache.GPXFilename_ID);
		args.put("Favorit", cache.Favorit() ? 1 : 0);
		args.put("ApiStatus", cache.ApiStatus);
		args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);
		args.put("TourName", cache.TourName);

		try
		{
			long ret = Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(cache.Id) });
		}
		catch (Exception exc)
		{
			Logger.Error("Update Cache", "", exc);

		}
	}

	public Cache getFromDbByCacheId(long CacheID)
	{
		CoreCursor reader = Database.Data.rawQuery(sqlgetFromDbByCacheId, new String[]
			{ String.valueOf(CacheID) });

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				Cache ret = ReadFromCursor(reader);

				reader.close();
				return ret;
			}
			else
			{
				if (reader != null) reader.close();
				return null;
			}
		}
		catch (Exception e)
		{
			if (reader != null) reader.close();
			e.printStackTrace();
			return null;
		}

	}

	public Cache getFromDbByGcCode(String GcCode)
	{
		CoreCursor reader = Database.Data.rawQuery(sqlgetFromDbByGcCode, new String[]
			{ GcCode });

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				Cache ret = ReadFromCursor(reader);

				reader.close();
				return ret;
			}
			else
			{
				if (reader != null) reader.close();
				return null;
			}
		}
		catch (Exception e)
		{
			if (reader != null) reader.close();
			e.printStackTrace();
			return null;
		}

	}

	public Boolean cacheExists(long CacheID)
	{

		CoreCursor reader = Database.Data.rawQuery(sqlExistsCache, new String[]
			{ String.valueOf(CacheID) });

		boolean exists = (reader.getCount() > 0);

		reader.close();

		return exists;

	}

	/**
	 * hier wird nur die Status Abfrage zurück geschrieben und gegebenen Falls die Replication Informationen geschrieben.
	 * 
	 * @param cache
	 */
	public boolean UpdateDatabaseCacheState(Cache cache)
	{

		// chk of changes
		boolean changed = false;
		Cache fromDB = getFromDbByCacheId(cache.Id);

		if (fromDB == null) return false; // nichts zum Updaten gefunden

		if (fromDB.Archived != cache.Archived)
		{
			changed = true;
			Replication.ArchivedChanged(cache.Id, cache.Archived);
		}
		if (fromDB.Available != cache.Available)
		{
			changed = true;
			Replication.AvailableChanged(cache.Id, cache.Available);
		}

		if (fromDB.NumTravelbugs != cache.NumTravelbugs)
		{
			changed = true;
			Replication.NumTravelbugsChanged(cache.Id, cache.NumTravelbugs);
		}

		if (changed) // Wir brauchen die DB nur Updaten, wenn sich auch etwas
						// geändert hat.
		{

			Parameters args = new Parameters();

			args.put("Archived", cache.Archived ? 1 : 0);
			args.put("Available", cache.Available ? 1 : 0);
			args.put("NumTravelbugs", cache.NumTravelbugs);

			try
			{
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(cache.Id) });
			}
			catch (Exception exc)
			{
				Logger.Error("Ubdate Cache", "", exc);

			}
		}

		return changed;
	}

	public Cache LoadApiDetails(Cache aktCache)
	{
		String accessToken = Config.GetAccessToken();

		Cache newCache = null;
		try
		{
			SearchForGeocaches.SearchGC search = new SearchForGeocaches.SearchGC();
			search.gcCode = aktCache.GcCode;

			ArrayList<Cache> apiCaches = new ArrayList<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
			CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, search, apiCaches, apiLogs, apiImages,
					aktCache.GPXFilename_ID);
			synchronized (Database.Data.Query)
			{
				if (apiCaches.size() == 1)
				{
					Database.Data.beginTransaction();
					newCache = apiCaches.get(0);
					Database.Data.Query.remove(aktCache);
					Database.Data.Query.add(newCache);
					newCache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, newCache.Longitude());
					newCache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, newCache.Latitude());

					UpdateDatabase(newCache);

					LogDAO logDAO = new LogDAO();
					for (LogEntry log : apiLogs)
					{
						if (log.CacheId != newCache.Id) continue;
						// Write Log to database

						logDAO.WriteToDatabase(log);
					}

					WaypointDAO waypointDAO = new WaypointDAO();
					for (Waypoint waypoint : newCache.waypoints)
					{

						waypointDAO.WriteToDatabase(waypoint);
					}

					ImageDAO imageDAO = new ImageDAO();
					for (ImageEntry image : apiImages)
					{
						if (image.CacheId != newCache.Id) continue;
						// Write Image to database

						imageDAO.WriteToDatabase(image, false);
					}

					Database.Data.setTransactionSuccessful();
					Database.Data.endTransaction();

					Database.Data.GPXFilenameUpdateCacheCount();
				}
			}
		}
		catch (Exception ex)
		{
			Logger.Error("DescriptionView", "Load CacheInfo by API", ex);
			return null;
		}

		return newCache;
	}

	public void WriteImports(Iterator<Cache> Caches, int CacheCount, ImporterProgress ip)
	{

		// Indexing DB
		CacheList IndexDB = new CacheList();
		CacheListDAO cacheListDAO = new CacheListDAO();
		IndexDB = cacheListDAO.ReadCacheList(IndexDB, "");

		ip.setJobMax("IndexingDB", IndexDB.size());
		ArrayList<String> index = new ArrayList<String>();
		for (Cache c : IndexDB)
		{
			ip.ProgressInkrement("IndexingDB", "index- " + c.GcCode, false);
			index.add(c.GcCode);
		}

		ip.setJobMax("WriteCachesToDB", CacheCount);
		while (Caches.hasNext())
		{
			Cache cache = Caches.next();

			if (index.contains(cache.GcCode))
			{
				ip.ProgressInkrement("WriteCachesToDB", "Update DB " + cache.GcCode, false);
				UpdateDatabase(cache);
			}
			else
			{
				ip.ProgressInkrement("WriteCachesToDB", "Write to DB " + cache.GcCode, false);
				WriteToDatabase(cache);
			}
		}
	}

	public ArrayList<String> getGcCodesFromMustLoadImages()
	{

		ArrayList<String> GcCodes = new ArrayList<String>();

		CoreCursor reader = Database.Data.rawQuery(
				"select GcCode from Caches where Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)", null);

		if (reader.getCount() > 0)
		{
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
			{
				String GcCode = reader.getString(0);
				GcCodes.add(GcCode);
				reader.moveToNext();
			}
		}
		reader.close();
		return GcCodes;
	}
}

package de.droidcachebox.DAO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Api.SearchForGeocaches;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.content.ContentValues;
import android.database.Cursor;
import de.droidcachebox.Database;
import de.droidcachebox.Replication.Replication;

public class CacheDAO {
	protected static String sqlReadCache = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus from Caches ";

	public Cache ReadFromCursor(Cursor reader) {
		try {
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
			DateFormat iso8601Format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			try {
				cache.DateHidden = iso8601Format.parse(sDate);
			} catch (ParseException e) {
			}

			cache.Url = reader.getString(15).trim();
			cache.NumTravelbugs = reader.getInt(16);
			cache.GcId = reader.getString(17).trim();
			cache.Rating = ((float) reader.getShort(18)) / 100.0f;
			if (reader.getInt(19) > 0)
				cache.setFavorit(true);
			else
				cache.setFavorit(false);
			if (reader.getString(20) != null)
				cache.TourName = reader.getString(20).trim();
			else
				cache.TourName = "";

			if (reader.getString(21) != "")
				cache.GPXFilename_ID = reader.getLong(21);
			else
				cache.GPXFilename_ID = -1;

			if (reader.getInt(22) > 0)
				cache.hasUserData = true;
			else
				cache.hasUserData = false;

			if (reader.getInt(23) > 0)
				cache.listingChanged = true;
			else
				cache.listingChanged = false;

			if (reader.getInt(24) > 0)
				cache.CorrectedCoordinates = true;
			else
				cache.CorrectedCoordinates = false;

			if (reader.isNull(25))
				cache.ApiStatus = 0;
			else
				cache.ApiStatus = (byte) reader.getInt(25);

			cache.MapX = 256.0 * Descriptor.LongitudeToTileX(
					Cache.MapZoomLevel, cache.Longitude());
			cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel,
					cache.Latitude());

			return cache;
		} catch (Exception exc) {
			Logger.Error("Read Cache", "", exc);
			return null;
		}
	}

	public void WriteToDatabase(Cache cache) {
		// int newCheckSum = createCheckSum(WP);
		// Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
		ContentValues args = new ContentValues();
		args.put("Id", cache.Id);
		args.put("GcCode", cache.GcCode);
		args.put("GcId", cache.GcId);
		args.put("Latitude", cache.Pos.Latitude);
		args.put("Longitude", cache.Pos.Longitude);
		args.put("Name", cache.Name);
		try {
			args.put("Size", cache.Size.ordinal());
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(cache.DateHidden);
		args.put("DateHidden", stimestamp);
		args.put("Hint", cache.hint);
		if ((cache.longDescription != null) && (cache.longDescription != ""))
			args.put("Description", cache.longDescription);
		cache.longDescription = ""; // clear longDescription because this will
									// be loaded from database when used
		args.put("Url", cache.Url);
		args.put("NumTravelbugs", cache.NumTravelbugs);
		args.put("Rating", (int) (cache.Rating * 100));
		// args.put("Vote", cache.);
		// args.put("VotePending", cache.);
		// args.put("Notes", );
		// args.put("Solver", cache.);
		args.put("AttributesPositive", cache.attributesPositive);
		args.put("AttributesNegative", cache.attributesNegative);
		// args.put("ListingCheckSum", cache.);
		args.put("GPXFilename_Id", cache.GPXFilename_ID);
		args.put("ApiStatus", cache.ApiStatus);
		args.put("CorrectedCoordinates", cache.CorrectedCoordinates ? 1 : 0);
		
		try {
			long anzahl = Database.Data.myDB.insert("Caches", null, args);
			// String s = anzahl + "";
			//
			// args = new ContentValues();
		} catch (Exception exc) {
			Logger.Error("Write Cache", "", exc);

		}
	}

	public void WriteToDatabase_Found(Cache cache) {
		ContentValues args = new ContentValues();
		args.put("found", cache.Found);
		try {
			Database.Data.myDB.update("Caches", args, "Id=" + cache.Id, null);
			Replication.FoundChanged(cache.Id, cache.Found);
		} catch (Exception exc) {
			Logger.Error("Write Cache Found", "", exc);
		}
	}

	public void UpdateDatabase(Cache cache) {

		ContentValues args = new ContentValues();

		// bei einem Update müssen nicht alle infos überschrieben werden

		// args.put("Id", cache.Id);
		// args.put("GcCode", cache.GcCode);
		// args.put("GcId", cache.GcId);
		args.put("Latitude", cache.Pos.Latitude);
		args.put("Longitude", cache.Pos.Longitude);
		args.put("Name", cache.Name);
		try {
			args.put("Size", cache.Size.ordinal());
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(cache.DateHidden);
		args.put("DateHidden", stimestamp);
		args.put("Hint", cache.hint);
		if ((cache.longDescription != null) && (cache.longDescription != ""))
			args.put("Description", cache.longDescription);
		cache.longDescription = ""; // clear longDescription because this will
									// be loaded from database when used
		args.put("Url", cache.Url);
		args.put("NumTravelbugs", cache.NumTravelbugs);
		args.put("Rating", (int) (cache.Rating * 100));
		// args.put("Vote", cache.);
		// args.put("VotePending", cache.);
		// args.put("Notes", );
		// args.put("Solver", cache.);
		args.put("AttributesPositive", cache.attributesPositive);
		args.put("AttributesNegative", cache.attributesNegative);
		// args.put("ListingCheckSum", cache.);
		args.put("GPXFilename_Id", cache.GPXFilename_ID);
		args.put("Favorit", cache.Favorit() ? 1 : 0);
		args.put("ApiStatus", cache.ApiStatus);
		args.put("CorrectedCoordinates", cache.CorrectedCoordinates ? 1 : 0);
		
		try {
			long anzahl = Database.Data.myDB.update("Caches", args, "Id="
					+ cache.Id, null);
			// String s = anzahl + "";
			//
			// args = new ContentValues();
		} catch (Exception exc) {
			Logger.Error("Ubdate Cache", "", exc);

		}
	}

	public Cache LoadApiDetails(Cache aktCache) {
		String accessToken = Config.GetString("GcAPI");
		String result = "";
		Cache newCache = null;
		try {
			SearchForGeocaches.SearchGC search = new SearchForGeocaches.SearchGC();
			search.gcCode = aktCache.GcCode;

			ArrayList<Cache> apiCaches = new ArrayList<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			result = CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(
					accessToken, search, apiCaches, apiLogs,
					aktCache.GPXFilename_ID);
			if (apiCaches.size() == 1) {
				Database.Data.myDB.beginTransaction();
				newCache = apiCaches.get(0);
				Database.Data.Query.remove(aktCache);
				Database.Data.Query.add(newCache);
				newCache.MapX = 256.0 * Descriptor.LongitudeToTileX(
						Cache.MapZoomLevel, newCache.Longitude());
				newCache.MapY = 256.0 * Descriptor.LatitudeToTileY(
						Cache.MapZoomLevel, newCache.Latitude());

				UpdateDatabase(newCache);
				for (LogEntry log : apiLogs) {
					if (log.CacheId != newCache.Id)
						continue;
					// Write Log to database
					LogDAO logDAO = new LogDAO();
					logDAO.WriteToDatabase(log);
				}
				for (Waypoint waypoint : newCache.waypoints) {
					WaypointDAO waypointDAO = new WaypointDAO();
					waypointDAO.WriteToDatabase(waypoint);
				}

				Database.Data.myDB.setTransactionSuccessful();
				Database.Data.myDB.endTransaction();

				Database.Data.GPXFilenameUpdateCacheCount();
			}
		} catch (Exception ex) {
			Logger.Error("DescriptionView", "Load CacheInfo by API", ex);
			return null;
		}

		return newCache;
	}

	public void WriteImports(Iterator<Cache> Caches, int CacheCount,
			ImporterProgress ip) {

		// Indexing DB
		CacheList IndexDB = new CacheList();
		CacheListDAO cacheListDAO = new CacheListDAO();
		IndexDB = cacheListDAO.ReadCacheList(IndexDB, "");

		ip.setJobMax("IndexingDB", IndexDB.size());
		ArrayList<String> index = new ArrayList<String>();
		for (Cache c : IndexDB) {
			ip.ProgressInkrement("IndexingDB", "index- " + c.GcCode);
			index.add(c.GcCode);
		}

		ip.setJobMax("WriteCachesToDB", CacheCount);
		while (Caches.hasNext()) {
			Cache cache = Caches.next();

			if (index.contains(cache.GcCode)) {
				ip.ProgressInkrement("WriteCachesToDB", "Update DB "
						+ cache.GcCode);
				UpdateDatabase(cache);
			} else {
				ip.ProgressInkrement("WriteCachesToDB", "Write to DB "
						+ cache.GcCode);
				WriteToDatabase(cache);
			}
		}

	}
}

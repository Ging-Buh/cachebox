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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Replication.Replication;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheDetail;
import CB_Core.Types.DLong;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;
import CB_Utils.Log.Logger;

public class CacheDAO
{
	protected static final String sqlgetFromDbByCacheId = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint from Caches where id = ?";
	protected static final String sqlgetFromDbByGcCode = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint from Caches where GCCode = ?";
	protected static final String sqlgetFromDbByGcCodeWithDiscription = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint, Description, Solver, Notes from Caches where GCCode = ?";

	protected static final String sqlExistsCache = "select 1 from Caches where Id = ?";

	public Cache ReadFromCursor(CoreCursor reader, boolean fullDetails)
	{
		return ReadFromCursor(reader, false, fullDetails);
	}

	public Cache ReadFromCursor(CoreCursor reader, boolean withDescription, boolean fullDetails)
	{
		try
		{
			Cache cache = new Cache(fullDetails);

			cache.Id = reader.getLong(0);
			cache.setGcCode(reader.getString(1).trim());
			cache.Pos = new Coordinate(reader.getDouble(2), reader.getDouble(3));
			cache.setName(reader.getString(4).trim());
			cache.Size = CacheSizes.parseInt(reader.getInt(5));
			cache.Difficulty = ((float) reader.getShort(6)) / 2;
			cache.Terrain = ((float) reader.getShort(7)) / 2;
			cache.setArchived(reader.getInt(8) != 0);
			cache.setAvailable(reader.getInt(9) != 0);
			cache.setFound(reader.getInt(10) != 0);
			cache.Type = CacheTypes.values()[reader.getShort(11)];
			cache.setOwner(reader.getString(13).trim());

			cache.NumTravelbugs = reader.getInt(16);
			cache.Rating = ((float) reader.getShort(18)) / 100.0f;
			if (reader.getInt(19) > 0) cache.setFavorit(true);
			else
				cache.setFavorit(false);

			if (reader.getInt(22) > 0) cache.setHasUserData(true);
			else
				cache.setHasUserData(false);

			if (reader.getInt(23) > 0) cache.setListingChanged(true);
			else
				cache.setListingChanged(false);

			if (reader.getInt(24) > 0) cache.setCorrectedCoordinates(true);
			else
				cache.setCorrectedCoordinates(false);

			cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
			cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());

			if (fullDetails)
			{
				readDetailFromCursor(reader, cache.detail);
			}
			if (withDescription)
			{
				cache.setLongDescription(reader.getString(31));
				cache.setTmpSolver(reader.getString(32));
				cache.setTmpNote(reader.getString(33));
			}
			return cache;
		}
		catch (Exception exc)
		{
			Logger.Error("Read Cache", "", exc);
			return null;
		}
	}

	public boolean readDetail(Cache cache)
	{
		if (cache.detail != null) return true;
		cache.detail = new CacheDetail();

		CoreCursor reader = Database.Data.rawQuery(sqlgetFromDbByCacheId, new String[]
			{ String.valueOf(cache.Id) });

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				readDetailFromCursor(reader, cache.detail);

				reader.close();
				return true;
			}
			else
			{
				if (reader != null) reader.close();
				return false;
			}
		}
		catch (Exception e)
		{
			if (reader != null) reader.close();
			e.printStackTrace();
			return false;
		}
	}

	private boolean readDetailFromCursor(CoreCursor reader, CacheDetail detail)
	{
		detail.setGcId(reader.getString(17).trim());
		detail.PlacedBy = reader.getString(12).trim();

		if (reader.isNull(25)) detail.ApiStatus = (byte) 0;
		else
			detail.ApiStatus = (byte) reader.getInt(25);

		String sDate = reader.getString(14);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			detail.DateHidden = iso8601Format.parse(sDate);
		}
		catch (ParseException e)
		{
		}

		detail.Url = reader.getString(15).trim();
		if (reader.getString(20) != null) detail.TourName = reader.getString(20).trim();
		else
			detail.TourName = "";
		if (reader.getString(21) != "") detail.GPXFilename_ID = reader.getLong(21);
		else
			detail.GPXFilename_ID = -1;
		detail.setAttributesPositive(new DLong(reader.getLong(27), reader.getLong(26)));
		detail.setAttributesNegative(new DLong(reader.getLong(29), reader.getLong(28)));

		if (reader.getString(30) != null) detail.setHint(reader.getString(30).trim());
		else
			detail.setHint("");

		return true;
	}

	public void WriteToDatabase(Cache cache)
	{
		// int newCheckSum = createCheckSum(WP);
		// Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
		Parameters args = new Parameters();
		args.put("Id", cache.Id);
		args.put("GcCode", cache.getGcCode());
		args.put("Latitude", cache.Pos.getLatitude());
		args.put("Longitude", cache.Pos.getLongitude());
		args.put("Name", cache.getName());
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
		args.put("Archived", cache.isArchived() ? 1 : 0);
		args.put("Available", cache.isAvailable() ? 1 : 0);
		args.put("Found", cache.isFound());
		args.put("Type", cache.Type.ordinal());
		args.put("Owner", cache.getOwner());
		args.put("Country", cache.getCountry());
		args.put("State", cache.getState());
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			String firstimported = iso8601Format.format(new Date());
			args.put("FirstImported", firstimported);
		}
		catch (Exception e)
		{

			e.printStackTrace();
		}

		if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0))
		{
			args.put("Description", cache.getShortDescription() + "<br /><hr /><br />");
		}

		if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0))
		{
			if (args.containsKey("Description"))
			{
				args.put("Description", args.get("Description") + cache.getLongDescription());
			}
			else
			{
				args.put("Description", cache.getLongDescription());
			}
		}

		args.put("NumTravelbugs", cache.NumTravelbugs);
		args.put("Rating", (int) (cache.Rating * 100));
		// args.put("Vote", cache.);
		// args.put("VotePending", cache.);
		// args.put("Notes", );
		// args.put("Solver", cache.);
		// args.put("ListingCheckSum", cache.);
		args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);

		if (cache.detail != null)
		{
			// write detail information if existing
			args.put("GcId", cache.getGcId());
			args.put("PlacedBy", cache.getPlacedBy());
			args.put("ApiStatus", cache.getApiStatus());
			try
			{
				String stimestamp = iso8601Format.format(cache.getDateHidden());
				args.put("DateHidden", stimestamp);
			}
			catch (Exception e)
			{

				e.printStackTrace();
			}
			args.put("Url", cache.getUrl());
			args.put("TourName", cache.getTourName());
			args.put("GPXFilename_Id", cache.GPXFilename_ID);
			args.put("AttributesPositive", cache.getAttributesPositive().getLow());
			args.put("AttributesPositiveHigh", cache.getAttributesPositive().getHigh());
			args.put("AttributesNegative", cache.getAttributesNegative().getLow());
			args.put("AttributesNegativeHigh", cache.getAttributesNegative().getHigh());
			args.put("Hint", cache.getHint());

		}
		try
		{
			Database.Data.insert("Caches", args);

		}
		catch (Exception exc)
		{
			Logger.Error("Write Cache", "", exc);

		}
	}

	public void WriteToDatabase_Found(Cache cache)
	{
		Parameters args = new Parameters();
		args.put("found", cache.isFound());
		try
		{
			Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(cache.Id) });
			Replication.FoundChanged(cache.Id, cache.isFound());
		}
		catch (Exception exc)
		{
			Logger.Error("Write Cache Found", "", exc);
		}
	}

	public boolean UpdateDatabase(Cache cache)
	{

		Parameters args = new Parameters();

		args.put("Id", cache.Id);
		args.put("GcCode", cache.getGcCode());
		args.put("GcId", cache.getGcId());
		if (cache.Pos.isValid() && !cache.Pos.isZero())
		{
			// Update Cache position only when new position is valid and not zero
			args.put("Latitude", cache.Pos.getLatitude());
			args.put("Longitude", cache.Pos.getLongitude());
		}
		args.put("Name", cache.getName());
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
		args.put("Archived", cache.isArchived() ? 1 : 0);
		args.put("Available", cache.isAvailable() ? 1 : 0);
		args.put("Found", cache.isFound());
		args.put("Type", cache.Type.ordinal());
		args.put("PlacedBy", cache.getPlacedBy());
		args.put("Owner", cache.getOwner());
		args.put("Country", cache.getCountry());
		args.put("State", cache.getState());
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			String stimestamp = iso8601Format.format(cache.getDateHidden());
			args.put("DateHidden", stimestamp);
		}
		catch (Exception e)
		{

			e.printStackTrace();
		}
		args.put("Hint", cache.getHint());

		if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0))
		{
			args.put("Description", cache.getShortDescription() + "<br /><hr /><br />");
		}

		if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0))
		{
			if (args.containsKey("Description"))
			{
				args.put("Description", args.get("Description") + cache.getLongDescription());
			}
			else
			{
				args.put("Description", cache.getLongDescription());
			}
		}

		args.put("Url", cache.getUrl());
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
		args.put("Favorit", cache.isFavorite() ? 1 : 0);
		args.put("ApiStatus", cache.getApiStatus());
		args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);
		args.put("TourName", cache.getTourName());

		try
		{
			long ret = Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(cache.Id) });
			return ret > 0;
		}
		catch (Exception exc)
		{
			Logger.Error("Update Cache", "", exc);
			return false;

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
				Cache ret = ReadFromCursor(reader, false);

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

	public Cache getFromDbByGcCode(String GcCode, boolean witDetail, boolean withDescription)
	{
		String where = withDescription ? sqlgetFromDbByGcCodeWithDiscription : sqlgetFromDbByGcCode;

		CoreCursor reader = Database.Data.rawQuery(where, new String[]
			{ GcCode });

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				Cache ret = ReadFromCursor(reader, withDescription, witDetail);

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
	 * @param writeTmp
	 */
	public boolean UpdateDatabaseCacheState(Cache writeTmp)
	{

		// chk of changes
		boolean changed = false;
		Cache fromDB = getFromDbByCacheId(writeTmp.Id);

		if (fromDB == null) return false; // nichts zum Updaten gefunden

		if (fromDB.isArchived() != writeTmp.isArchived())
		{
			changed = true;
			Replication.ArchivedChanged(writeTmp.Id, writeTmp.isArchived());
		}
		if (fromDB.isAvailable() != writeTmp.isAvailable())
		{
			changed = true;
			Replication.AvailableChanged(writeTmp.Id, writeTmp.isAvailable());
		}

		if (fromDB.NumTravelbugs != writeTmp.NumTravelbugs)
		{
			changed = true;
			Replication.NumTravelbugsChanged(writeTmp.Id, writeTmp.NumTravelbugs);
		}

		if (changed) // Wir brauchen die DB nur Updaten, wenn sich auch etwas
						// geändert hat.
		{

			Parameters args = new Parameters();

			args.put("Archived", writeTmp.isArchived() ? 1 : 0);
			args.put("Available", writeTmp.isAvailable() ? 1 : 0);
			args.put("NumTravelbugs", writeTmp.NumTravelbugs);

			try
			{
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(writeTmp.Id) });
			}
			catch (Exception exc)
			{
				Logger.Error("Ubdate Cache", "", exc);

			}
		}

		return changed;
	}

	/* This seems to be no longer necessary */
	// public void WriteImports(Iterator<Cache> Caches, int CacheCount, ImporterProgress ip)
	// {
	//
	// // Indexing DB
	// CacheList IndexDB = new CacheList();
	// CacheListDAO cacheListDAO = new CacheListDAO();
	// IndexDB = cacheListDAO.ReadCacheList(IndexDB, "", true, true);
	//
	// ip.setJobMax("IndexingDB", IndexDB.size());
	// ArrayList<String> index = new ArrayList<String>();
	// for (int i = 0, n = IndexDB.size(); i < n; i++)
	// {
	// Cache c = IndexDB.get(i);
	// ip.ProgressInkrement("IndexingDB", "index- " + c.getGcCode(), false);
	// index.add(c.getGcCode());
	// }
	//
	// ip.setJobMax("WriteCachesToDB", CacheCount);
	// while (Caches.hasNext())
	// {
	// Cache cache = Caches.next();
	//
	// if (index.contains(cache.getGcCode()))
	// {
	// ip.ProgressInkrement("WriteCachesToDB", "Update DB " + cache.getGcCode(), false);
	// UpdateDatabase(cache);
	// }
	// else
	// {
	// ip.ProgressInkrement("WriteCachesToDB", "Write to DB " + cache.getGcCode(), false);
	// WriteToDatabase(cache);
	// }
	//
	// // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
	// cache.setLongDescription("");
	//
	// }
	// }

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

	public Boolean loadBooleanValue(String gcCode, String key)
	{
		CoreCursor reader = Database.Data.rawQuery("select " + key + " from Caches where GcCode = \"" + gcCode + "\"", null);
		try
		{
			reader.moveToFirst();
			while (!reader.isAfterLast())
			{
				if (reader.getInt(0) != 0)
				{ // gefunden. Suche abbrechen
					return true;
				}
				reader.moveToNext();
			}
		}
		catch (Exception ex)
		{
			return false;
		}
		finally
		{
			reader.close();
		}

		return false;
	}

}

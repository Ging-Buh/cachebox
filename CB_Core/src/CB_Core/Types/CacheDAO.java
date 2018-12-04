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
package CB_Core.Types;

import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.Replication;
import CB_Locator.Coordinate;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CacheDAO {
    static final String SQL_BY_ID = "from Caches c where id = ?";
    static final String SQL_BY_GC_CODE = "from Caches c where GCCode = ?";
    static final String SQL_DETAILS = "PlacedBy, DateHidden, Url, TourName, GpxFilename_ID, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint ";
    static final String SQL_GET_DETAIL_WITH_DESCRIPTION = "Description, Solver, Notes, ShortDescription ";
    static final String SQL_GET_DETAIL_FROM_ID = "select " + SQL_DETAILS + SQL_BY_ID;
    static final String SQL_EXIST_CACHE = "select 1 from Caches where Id = ?";
    static final String SQL_GET_CACHE = "select c.Id, GcCode, Latitude, Longitude, c.Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, Owner, NumTravelbugs, GcId, Rating, Favorit, HasUserData, ListingChanged, CorrectedCoordinates, FavPoints ";
    private static final String log = "CacheDAO";
    public String[] SQL_ENUM = {"c.Id", "GcCode", "Latitude"};

    Cache ReadFromCursor(CoreCursor reader, boolean fullDetails, boolean withDescription) {
        try {
            Cache cache = new Cache(fullDetails);

            cache.Id = reader.getLong(0);
            cache.setGcCode(reader.getString(1).trim());
            cache.Pos = new Coordinate(reader.getDouble(2), reader.getDouble(3));
            cache.setName(reader.getString(4).trim());
            cache.Size = CacheSizes.CacheSizesFromInt(reader.getInt(5));
            cache.setDifficulty(((float) reader.getShort(6)) / 2);
            cache.setTerrain(((float) reader.getShort(7)) / 2);
            cache.setArchived(reader.getInt(8) != 0);
            cache.setAvailable(reader.getInt(9) != 0);
            cache.setFound(reader.getInt(10) != 0);
            cache.Type = CacheTypes.values()[reader.getShort(11)];
            cache.setOwner(reader.getString(12).trim());

            cache.NumTravelbugs = reader.getInt(13);
            cache.setGcId(reader.getString(14));
            cache.Rating = (reader.getShort(15)) / 100.0f;
            if (reader.getInt(16) > 0)
                cache.setFavorite(true);
            else
                cache.setFavorite(false);

            if (reader.getInt(17) > 0)
                cache.setHasUserData(true);
            else
                cache.setHasUserData(false);

            if (reader.getInt(18) > 0)
                cache.setListingChanged(true);
            else
                cache.setListingChanged(false);

            if (reader.getInt(19) > 0)
                cache.setHasCorrectedCoordinates(true);
            else
                cache.setHasCorrectedCoordinates(false);

            cache.favPoints = reader.getInt(20);

            if (fullDetails) {
                readDetailFromCursor(reader, cache.detail, fullDetails, withDescription);
            }

            return cache;
        } catch (Exception exc) {
            Log.err(log, "Read Cache", "", exc);
            return null;
        }
    }

    public boolean readDetail(Cache cache) {
        if (cache.detail != null)
            return true;
        cache.detail = new CacheDetail();

        CoreCursor reader = Database.Data.rawQuery(SQL_GET_DETAIL_FROM_ID, new String[]{String.valueOf(cache.Id)});

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                readDetailFromCursor(reader, cache.detail, false, false);

                reader.close();
                return true;
            } else {
                if (reader != null)
                    reader.close();
                return false;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return false;
        }
    }

    private boolean readDetailFromCursor(CoreCursor reader, CacheDetail detail, boolean withReaderOffset, boolean withDescription) {
        // Reader includes Compleate Cache or Details only
        int readerOffset = withReaderOffset ? 21 : 0;

        detail.PlacedBy = reader.getString(readerOffset + 0).trim();

        if (reader.isNull(readerOffset + 5))
            detail.ApiStatus = Cache.NOT_LIVE;
        else
            detail.ApiStatus = (byte) reader.getInt(readerOffset + 5);

        String sDate = reader.getString(readerOffset + 1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            detail.DateHidden = iso8601Format.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        detail.Url = reader.getString(readerOffset + 2).trim();
        if (reader.getString(readerOffset + 3) != null)
            detail.TourName = reader.getString(readerOffset + 3).trim();
        else
            detail.TourName = "";
        if (reader.getString(readerOffset + 4) != "")
            detail.GPXFilename_ID = reader.getLong(readerOffset + 4);
        else
            detail.GPXFilename_ID = -1;
        detail.setAttributesPositive(new DLong(reader.getLong(readerOffset + 7), reader.getLong(readerOffset + 6)));
        detail.setAttributesNegative(new DLong(reader.getLong(readerOffset + 9), reader.getLong(readerOffset + 8)));

        if (reader.getString(readerOffset + 10) != null)
            detail.setHint(reader.getString(readerOffset + 10).trim());
        else
            detail.setHint("");

        if (withDescription) {
            detail.longDescription = reader.getString(readerOffset + 11);
            detail.tmpSolver = reader.getString(readerOffset + 12);
            detail.tmpNote = reader.getString(readerOffset + 13);
            detail.shortDescription = reader.getString(readerOffset + 14);
        }
        return true;
    }

    public void WriteToDatabase(Cache cache) {
        // int newCheckSum = createCheckSum(WP);
        // Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        Parameters args = new Parameters();
        args.put("Id", cache.Id);
        args.put("GcCode", cache.getGcCode());
        args.put("Latitude", cache.Pos.getLatitude());
        args.put("Longitude", cache.Pos.getLongitude());
        args.put("Name", cache.getName());
        try {
            args.put("Size", cache.Size.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (cache.getDifficulty() * 2));
        args.put("Terrain", (int) (cache.getTerrain() * 2));
        args.put("Archived", cache.isArchived() ? 1 : 0);
        args.put("Available", cache.isAvailable() ? 1 : 0);
        args.put("Found", cache.isFound());
        args.put("Type", cache.Type.ordinal());
        args.put("Owner", cache.getOwner());
        args.put("Country", cache.getCountry());
        args.put("State", cache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String firstimported = iso8601Format.format(new Date());
            args.put("FirstImported", firstimported);
        } catch (Exception e) {

            e.printStackTrace();
        }

        if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0)) {
            args.put("ShortDescription", cache.getShortDescription());
        }

        if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0)) {
            args.put("Description", cache.getLongDescription());
        }

        args.put("NumTravelbugs", cache.NumTravelbugs);
        args.put("Rating", (int) (cache.Rating * 100));
        // args.put("Vote", cache.);
        // args.put("VotePending", cache.);
        // args.put("Notes", );
        // args.put("Solver", cache.);
        // args.put("ListingCheckSum", cache.);
        args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);

        args.put("FavPoints", cache.favPoints);
        if (cache.detail != null) {
            // write detail information if existing
            args.put("GcId", cache.getGcId());
            args.put("PlacedBy", cache.getPlacedBy());
            args.put("ApiStatus", cache.getApiStatus());
            try {
                String stimestamp = iso8601Format.format(cache.getDateHidden());
                args.put("DateHidden", stimestamp);
            } catch (Exception e) {

                e.printStackTrace();
            }
            args.put("Url", cache.getUrl());
            args.put("TourName", cache.getTourName());
            args.put("GPXFilename_Id", cache.getGPXFilename_ID());
            args.put("AttributesPositive", cache.getAttributesPositive().getLow());
            args.put("AttributesPositiveHigh", cache.getAttributesPositive().getHigh());
            args.put("AttributesNegative", cache.getAttributesNegative().getLow());
            args.put("AttributesNegativeHigh", cache.getAttributesNegative().getHigh());
            args.put("Hint", cache.getHint());

        }
        try {
            Database.Data.insert("Caches", args);

        } catch (Exception exc) {
            Log.err(log, "Write Cache", "", exc);

        }
    }

    public void WriteToDatabase_Found(Cache cache) {
        Parameters args = new Parameters();
        args.put("found", cache.isFound());
        try {
            Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(cache.Id)});
            Replication.FoundChanged(cache.Id, cache.isFound());
        } catch (Exception exc) {
            Log.err(log, "Write Cache Found", "", exc);
        }
    }

    public boolean UpdateDatabase(Cache cache) {

        Parameters args = new Parameters();

        args.put("Id", cache.Id);
        args.put("GcCode", cache.getGcCode());
        args.put("GcId", cache.getGcId());
        if (cache.Pos.isValid() && !cache.Pos.isZero()) {
            // Update Cache position only when new position is valid and not zero
            args.put("Latitude", cache.Pos.getLatitude());
            args.put("Longitude", cache.Pos.getLongitude());
        }
        args.put("Name", cache.getName());
        try {
            args.put("Size", cache.Size.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (cache.getDifficulty() * 2));
        args.put("Terrain", (int) (cache.getTerrain() * 2));
        args.put("Archived", cache.isArchived() ? 1 : 0);
        args.put("Available", cache.isAvailable() ? 1 : 0);
        args.put("Found", cache.isFound());
        args.put("Type", cache.Type.ordinal());
        args.put("PlacedBy", cache.getPlacedBy());
        args.put("Owner", cache.getOwner());
        args.put("Country", cache.getCountry());
        args.put("State", cache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String stimestamp = iso8601Format.format(cache.getDateHidden());
            args.put("DateHidden", stimestamp);
        } catch (Exception e) {

            e.printStackTrace();
        }
        args.put("Hint", cache.getHint());

        if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0)) {
            args.put("ShortDescription", cache.getShortDescription());
        }

        if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0)) {
            args.put("Description", cache.getLongDescription());
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
        args.put("GPXFilename_Id", cache.getGPXFilename_ID());
        args.put("Favorit", cache.isFavorite() ? 1 : 0);
        args.put("ApiStatus", cache.getApiStatus());
        args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);
        args.put("TourName", cache.getTourName());
        args.put("FavPoints", cache.favPoints);
        try {
            long ret = Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(cache.Id)});
            return ret > 0;
        } catch (Exception exc) {
            Log.err(log, "Update Cache", "", exc);
            return false;
        }
    }

    public Cache getFromDbByCacheId(long CacheID) {
        CoreCursor reader = Database.Data.rawQuery(SQL_GET_CACHE + SQL_BY_ID, new String[]{String.valueOf(CacheID)});

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Cache ret = ReadFromCursor(reader, false, false);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return null;
        } finally {
            reader = null;
        }

    }

    public Cache getFromDbByGcCode(String GcCode, boolean withDetail) // NO_UCD (test only)
    {
        String where = SQL_GET_CACHE + (withDetail ? ", " + SQL_DETAILS : "") + SQL_BY_GC_CODE;

        CoreCursor reader = Database.Data.rawQuery(where, new String[]{GcCode});

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Cache ret = ReadFromCursor(reader, withDetail, false);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return null;
        }

    }

    public Boolean cacheExists(long CacheID) {

        CoreCursor reader = Database.Data.rawQuery(SQL_EXIST_CACHE, new String[]{String.valueOf(CacheID)});

        boolean exists = (reader.getCount() > 0);

        reader.close();

        return exists;

    }

    /**
     * hier wird nur die Status Abfrage zurück geschrieben und gegebenenfalls die Replication Informationen geschrieben.
     *
     * @param writeTmp
     */
    public boolean UpdateDatabaseCacheState(Cache writeTmp) {

        // chk of changes
        boolean changed = false;
        Cache fromDB = getFromDbByCacheId(writeTmp.Id);

        if (fromDB == null)
            return false; // nichts zum Updaten gefunden

        if (fromDB.isArchived() != writeTmp.isArchived()) {
            changed = true;
            Replication.ArchivedChanged(writeTmp.Id, writeTmp.isArchived());
        }
        if (fromDB.isAvailable() != writeTmp.isAvailable()) {
            changed = true;
            Replication.AvailableChanged(writeTmp.Id, writeTmp.isAvailable());
        }

        if (fromDB.NumTravelbugs != writeTmp.NumTravelbugs) {
            changed = true;
            Replication.NumTravelbugsChanged(writeTmp.Id, writeTmp.NumTravelbugs);
        }

        if (fromDB.favPoints != writeTmp.favPoints) {
            changed = true;
            Replication.NumFavPointsChanged(writeTmp.Id, writeTmp.favPoints);
        }

        if (changed) {

            Parameters args = new Parameters();

            args.put("Archived", writeTmp.isArchived() ? 1 : 0);
            args.put("Available", writeTmp.isAvailable() ? 1 : 0);
            args.put("NumTravelbugs", writeTmp.NumTravelbugs);
            args.put("FavPoints",writeTmp.favPoints);

            try {
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(writeTmp.Id)});
            } catch (Exception exc) {
                Log.err(log, "Update Cache", "", exc);

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

    public ArrayList<String> getGcCodesFromMustLoadImages() {

        ArrayList<String> GcCodes = new ArrayList<String>();

        CoreCursor reader = Database.Data.rawQuery("select GcCode from Caches where Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)", null);

        if (reader.getCount() > 0) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                String GcCode = reader.getString(0);
                GcCodes.add(GcCode);
                reader.moveToNext();
            }
        }
        reader.close();
        return GcCodes;
    }

    public Boolean loadBooleanValue(String gcCode, String key) {
        CoreCursor reader = Database.Data.rawQuery("select " + key + " from Caches where GcCode = \"" + gcCode + "\"", null);
        try {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                if (reader.getInt(0) != 0) { // gefunden. Suche abbrechen
                    return true;
                }
                reader.moveToNext();
            }
        } catch (Exception ex) {
            return false;
        } finally {
            reader.close();
        }

        return false;
    }

}

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
package de.droidcachebox.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.CacheDetail;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GeoCacheSize;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.dataclasses.ImageEntry;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.DLong;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.log.Log;

public class CacheDAO {
    static final String SQL_DETAILS = "PlacedBy, DateHidden, Url, TourName, GpxFilename_ID, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint, Country, State ";
    static final String SQL_GET_DETAIL_WITH_DESCRIPTION = "Description, Solver, Notes, ShortDescription ";
    static final String SQL_GET_CACHE = "select c.Id, GcCode, Latitude, Longitude, c.Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, Owner, NumTravelbugs, GcId, Rating, Favorit, HasUserData, ListingChanged, CorrectedCoordinates, FavPoints ";
    private static final String SQL_BY_ID = "from Caches c where id = ?";
    private static final String SQL_BY_GC_CODE = "from Caches c where GCCode = ?";
    private static final String SQL_GET_DETAIL_FROM_ID = "select " + SQL_DETAILS + SQL_BY_ID;
    private static final String SQL_EXIST_CACHE = "select 1 from Caches where Id = ?";
    private static final String sClass = "CacheDAO";

    public CacheDAO() {
    }

    Cache readFromCursor(CoreCursor reader, boolean fullDetails, boolean withDescription) {
        try {
            Cache cache = new Cache(fullDetails);

            cache.generatedId = reader.getLong(0);
            cache.setGeoCacheCode(reader.getString(1).trim());
            cache.setCoordinate(new Coordinate(reader.getDouble(2), reader.getDouble(3)));
            cache.setGeoCacheName(reader.getString(4).trim());
            cache.geoCacheSize = GeoCacheSize.CacheSizesFromInt(reader.getInt(5));
            cache.setDifficulty(((float) reader.getShort(6)) / 2);
            cache.setTerrain(((float) reader.getShort(7)) / 2);
            cache.setArchived(reader.getInt(8) != 0);
            cache.setAvailable(reader.getInt(9) != 0);
            cache.setFound(reader.getInt(10) != 0);
            cache.setGeoCacheType(GeoCacheType.values()[reader.getShort(11)]);
            cache.setOwner(reader.getString(12).trim());
            cache.numTravelbugs = reader.getInt(13);
            cache.setGeoCacheId(reader.getString(14));
            cache.gcVoteRating = (reader.getShort(15)) / 100.0f;
            cache.setFavorite(reader.getInt(16) > 0);
            cache.setHasUserData(reader.getInt(17) > 0);
            cache.setListingChanged(reader.getInt(18) > 0);
            cache.setHasCorrectedCoordinates(reader.getInt(19) > 0);
            cache.favPoints = reader.getInt(20);
            if (fullDetails) {
                readDetailFromCursor(reader, cache.getGeoCacheDetail(), true, withDescription);
            }
            return cache;
        } catch (Exception exc) {
            Log.err(sClass, "Read Cache", "", exc);
            return null;
        }
    }

    public void readDetail(Cache cache) {
        if (cache.getGeoCacheDetail() != null)
            return;
        cache.setGeoCacheDetail(new CacheDetail());

        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_GET_DETAIL_FROM_ID, new String[]{String.valueOf(cache.generatedId)});

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                readDetailFromCursor(reader, cache.getGeoCacheDetail(), false, false);

                reader.close();
            } else {
                if (reader != null)
                    reader.close();
            }
        } catch (Exception e) {
            reader.close();
        }
    }

    private void readDetailFromCursor(CoreCursor reader, CacheDetail detail, boolean withReaderOffset, boolean withDescription) {
        // Reader includes Compleate Cache or Details only
        int readerOffset = withReaderOffset ? 21 : 0;

        try {
            detail.PlacedBy = reader.getString(readerOffset).trim();
        } catch (Exception e) {
            detail.PlacedBy = "";
        }

        if (reader.isNull(readerOffset + 5))
            detail.ApiStatus = Cache.NOT_LIVE;
        else
            detail.ApiStatus = (byte) reader.getInt(readerOffset + 5);

        String sDate = reader.getString(readerOffset + 1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            detail.DateHidden = iso8601Format.parse(sDate);
        } catch (Exception ex) {
            detail.DateHidden = new Date();
        }

        detail.Url = reader.getString(readerOffset + 2).trim();
        if (reader.getString(readerOffset + 3) != null)
            detail.TourName = reader.getString(readerOffset + 3).trim();
        else
            detail.TourName = "";
        if (reader.getString(readerOffset + 4).length() > 0)
            detail.GPXFilename_ID = reader.getLong(readerOffset + 4);
        else
            detail.GPXFilename_ID = -1;
        detail.setAttributesPositive(new DLong(reader.getLong(readerOffset + 7), reader.getLong(readerOffset + 6)));
        detail.setAttributesNegative(new DLong(reader.getLong(readerOffset + 9), reader.getLong(readerOffset + 8)));

        if (reader.getString(readerOffset + 10) != null)
            detail.setHint(reader.getString(readerOffset + 10).trim());
        else
            detail.setHint("");
        detail.Country = reader.getString(readerOffset + 11);
        detail.State = reader.getString(readerOffset + 12);

        if (withDescription) {
            detail.longDescription = reader.getString(readerOffset + 13);
            detail.tmpSolver = reader.getString(readerOffset + 14);
            detail.tmpNote = reader.getString(readerOffset + 15);
            detail.shortDescription = reader.getString(readerOffset + 16);
        }
    }

    public void writeToDatabase(Cache cache) {
        // int newCheckSum = createCheckSum(WP); // Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        Parameters args = new Parameters();
        args.put("Id", cache.generatedId);
        args.put("GcCode", cache.getGeoCacheCode());
        args.put("Latitude", cache.getCoordinate().getLatitude());
        args.put("Longitude", cache.getCoordinate().getLongitude());
        args.put("Name", cache.getGeoCacheName());
        try {
            args.put("Size", cache.geoCacheSize.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (cache.getDifficulty() * 2));
        args.put("Terrain", (int) (cache.getTerrain() * 2));
        args.put("Archived", cache.isArchived() ? 1 : 0);
        args.put("Available", cache.isAvailable() ? 1 : 0);
        args.put("Found", cache.isFound());
        args.put("Type", cache.getGeoCacheType().ordinal());
        args.put("Owner", cache.getOwner());
        args.put("Country", cache.getCountry());
        args.put("State", cache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            String firstimported = iso8601Format.format(new Date());
            args.put("FirstImported", firstimported);
        } catch (Exception ignored) {
        }

        if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0)) {
            args.put("ShortDescription", cache.getShortDescription());
        }

        if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0)) {
            args.put("Description", cache.getLongDescription());
        }

        args.put("NumTravelbugs", cache.numTravelbugs);
        args.put("Rating", (int) (cache.gcVoteRating * 100));
        // args.put("Vote", cache.);
        // args.put("VotePending", cache.);
        // args.put("Notes", );
        // args.put("Solver", cache.);
        // args.put("ListingCheckSum", cache.);
        args.put("CorrectedCoordinates", cache.hasCorrectedCoordinates() ? 1 : 0);
        args.put("Favorit", cache.isFavorite() ? 1 : 0);
        args.put("FavPoints", cache.favPoints);
        if (cache.getGeoCacheDetail() != null) {
            // write detail information if existing
            args.put("GcId", cache.getGeoCacheId());
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
            CBDB.getInstance().insert("Caches", args);
        } catch (Exception exc) {
            Log.err(sClass, "Write Cache", "", exc);
        }
    }

    public void writeToDatabaseFound(Cache cache) {
        Parameters args = new Parameters();
        args.put("found", cache.isFound());
        try {
            CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(cache.generatedId)});
            Replication.FoundChanged(cache.generatedId, cache.isFound());
        } catch (Exception exc) {
            Log.err(sClass, "Write Cache Found", "", exc);
        }
    }

    public boolean updateDatabase(Cache cache) {

        Parameters args = new Parameters();

        args.put("Id", cache.generatedId);
        args.put("GcCode", cache.getGeoCacheCode());
        args.put("GcId", cache.getGeoCacheId());
        if (cache.getCoordinate().isValid() && !cache.getCoordinate().isZero()) {
            // Update Cache position only when new position is valid and not zero
            args.put("Latitude", cache.getCoordinate().getLatitude());
            args.put("Longitude", cache.getCoordinate().getLongitude());
        }
        args.put("Name", cache.getGeoCacheName());
        try {
            args.put("Size", cache.geoCacheSize.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (cache.getDifficulty() * 2));
        args.put("Terrain", (int) (cache.getTerrain() * 2));
        args.put("Archived", cache.isArchived() ? 1 : 0);
        args.put("Available", cache.isAvailable() ? 1 : 0);
        args.put("Found", cache.isFound());
        args.put("Type", cache.getGeoCacheType().ordinal());
        args.put("PlacedBy", cache.getPlacedBy());
        args.put("Owner", cache.getOwner());
        args.put("Country", cache.getCountry());
        args.put("State", cache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            String stimestamp = iso8601Format.format(cache.getDateHidden());
            args.put("DateHidden", stimestamp);
        } catch (Exception ignored) {
        }
        args.put("Hint", cache.getHint());

        if ((cache.getShortDescription() != null) && (cache.getShortDescription().length() > 0)) {
            args.put("ShortDescription", cache.getShortDescription());
        }

        if ((cache.getLongDescription() != null) && (cache.getLongDescription().length() > 0)) {
            args.put("Description", cache.getLongDescription());
        }

        args.put("Url", cache.getUrl());
        args.put("NumTravelbugs", cache.numTravelbugs);
        args.put("Rating", (int) (cache.gcVoteRating * 100));
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
            long ret = CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(cache.generatedId)});
            return ret > 0;
        } catch (Exception exc) {
            Log.err(sClass, "Update Cache", "", exc);
            return false;
        }
    }

    /**
     * @param CacheID used to query the table Caches
     * @return the geocache without fullDetails and description<br>
     * or null if not in table
     */
    public Cache getFromDbByCacheId(long CacheID) {
        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_GET_CACHE + SQL_BY_ID, new String[]{String.valueOf(CacheID)});
        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Cache ret = readFromCursor(reader, false, false);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            reader.close();
            Log.err(sClass, e);
            return null;
        }

    }

    public Cache getFromDbByGcCode(String GcCode, boolean withDetail) // NO_UCD (test only)
    {
        String where = SQL_GET_CACHE + (withDetail ? ", " + SQL_DETAILS : "") + SQL_BY_GC_CODE;

        CoreCursor reader = CBDB.getInstance().rawQuery(where, new String[]{GcCode});

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Cache ret = readFromCursor(reader, withDetail, false);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            reader.close();
            Log.err(sClass, e);
            return null;
        }

    }

    public boolean cacheExists(long CacheID) {
        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_EXIST_CACHE, new String[]{String.valueOf(CacheID)});
        boolean exists = (reader.getCount() > 0);
        reader.close();
        return exists;
    }

    /**
     * hier wird nur die Status Abfrage zurück geschrieben und gegebenenfalls die Replication Informationen geschrieben.
     *
     * @param writeTmp ?
     */
    public boolean updateDatabaseCacheState(Cache writeTmp) {

        // chk of changes
        boolean changed = false;
        Cache fromDB = getFromDbByCacheId(writeTmp.generatedId);
        Parameters args = new Parameters();

        if (fromDB == null)
            return false; // nichts zum Updaten gefunden

        if (fromDB.isArchived() != writeTmp.isArchived()) {
            changed = true;
            Replication.ArchivedChanged(writeTmp.generatedId, writeTmp.isArchived());
            args.put("Archived", writeTmp.isArchived() ? 1 : 0);
        }
        if (fromDB.isAvailable() != writeTmp.isAvailable()) {
            changed = true;
            Replication.AvailableChanged(writeTmp.generatedId, writeTmp.isAvailable());
            args.put("Available", writeTmp.isAvailable() ? 1 : 0);
        }

        if (fromDB.numTravelbugs != writeTmp.numTravelbugs) {
            changed = true;
            Replication.NumTravelbugsChanged(writeTmp.generatedId, writeTmp.numTravelbugs);
            args.put("NumTravelbugs", writeTmp.numTravelbugs);
        }

        if (fromDB.favPoints != writeTmp.favPoints) {
            changed = true;
            Replication.NumFavPointsChanged(writeTmp.generatedId, writeTmp.favPoints);
            args.put("FavPoints", writeTmp.favPoints);
        }

        if (fromDB.isFound() != writeTmp.isFound()) {
            changed = true;
            Replication.FoundChanged(writeTmp.generatedId, writeTmp.isFound());
            args.put("Found", writeTmp.isFound());
        }

        if (changed) {
            try {
                CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(writeTmp.generatedId)});
            } catch (Exception exc) {
                Log.err(sClass, "Update Cache", "", exc);
            }
        }

        return changed;
    }

    public void updateCacheCountForGPXFilenames() {
        // welche GPXFilenamen sind in der DB erfasst
        CBDB.getInstance().beginTransaction();
        try {
            CoreCursor reader = CBDB.getInstance().rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
            reader.moveToFirst();

            while (!reader.isAfterLast()) {
                long GPXFilename_ID = reader.getLong(0);
                long CacheCount = reader.getLong(1);

                Parameters val = new Parameters();
                val.put("CacheCount", CacheCount);
                CBDB.getInstance().update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

                reader.moveToNext();
            }

            CBDB.getInstance().delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
            CBDB.getInstance().delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
            reader.close();
            CBDB.getInstance().setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            CBDB.getInstance().endTransaction();
        }

        CategoryDAO.getInstance().loadCategoriesFromDatabase();
    }

    public String getNote(Cache cache) {
        String resultString = "";
        CoreCursor c = CBDB.getInstance().rawQuery("select Notes from Caches where Id=?", new String[]{String.valueOf(cache.generatedId)});
        c.moveToFirst();
        if (!c.isAfterLast()) {
            resultString = c.getString(0);
        }
        cache.setNoteChecksum((int) SDBM_Hash.sdbm(resultString));
        return resultString;
    }

    public String getNote(long generatedId) {
        String resultString = "";
        CoreCursor c = CBDB.getInstance().rawQuery("select Notes from Caches where Id=?", new String[]{String.valueOf(generatedId)});
        c.moveToFirst();
        if (!c.isAfterLast()) {
            resultString = c.getString(0);
        }
        return resultString;
    }

    /**
     * geänderte Note nur in die DB schreiben
     *
     * @param cacheId ?
     * @param value   ?
     */
    public void setNote(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Notes", value);
        args.put("HasUserData", true);

        CBDB.getInstance().update("Caches", args, "id=" + cacheId, null);
    }

    public void setNote(Cache cache, String value) {
        int newNoteCheckSum = (int) SDBM_Hash.sdbm(value);

        Replication.NoteChanged(cache.generatedId, cache.getNoteChecksum(), newNoteCheckSum);
        if (newNoteCheckSum != cache.getNoteChecksum()) {
            setNote(cache.generatedId, value);
            cache.setNoteChecksum(newNoteCheckSum);
        }
    }

    public String getSolver(Cache cache) {
        String resultString = getSolver(cache.generatedId);
        cache.setSolverChecksum((int) SDBM_Hash.sdbm(resultString));
        return resultString;
    }

    public String getSolver(long cacheId) {
        try {
            String resultString = "";
            CoreCursor c = CBDB.getInstance().rawQuery("select Solver from Caches where Id=?", new String[]{String.valueOf(cacheId)});
            c.moveToFirst();
            if (!c.isAfterLast()) {
                resultString = c.getString(0);
            }
            return resultString;
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * geänderten Solver nur in die DB schreiben
     */
    private void setSolver(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Solver", value);
        args.put("HasUserData", true);

        CBDB.getInstance().update("Caches", args, "id=" + cacheId, null);
    }

    public void setSolver(Cache cache, String value) {
        int newSolverCheckSum = (int) SDBM_Hash.sdbm(value);

        Replication.SolverChanged(cache.generatedId, cache.getSolverChecksum(), newSolverCheckSum);
        if (newSolverCheckSum != cache.getSolverChecksum()) {
            setSolver(cache.generatedId, value);
            cache.setSolverChecksum(newSolverCheckSum);
        }
    }

    public String getDescription(Cache cache) {
        String description = "";
        CoreCursor reader = CBDB.getInstance().rawQuery("select Description from Caches where Id=?", new String[]{Long.toString(cache.generatedId)});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();

        return description;
    }

    public String getShortDescription(Cache cache) {
        String description = "";
        CoreCursor reader = CBDB.getInstance().rawQuery("select ShortDescription from Caches where Id=?", new String[]{Long.toString(cache.generatedId)});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();

        return description;
    }

    /*
    public ArrayList<String> getGcCodesFromMustLoadImages() {

        ArrayList<String> GcCodes = new ArrayList<String>();

        CoreCursor reader = CBDB.Data.rawQuery("select GcCode from Caches where Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)", null);

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

     */

    /*
    public boolean loadBooleanValue(String gcCode, String key) {
        CoreCursor reader = CBDB.Data.rawQuery("select " + key + " from Caches where GcCode = \"" + gcCode + "\"", null);
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

     */

    public void writeCachesAndLogsAndImagesIntoDB(ArrayList<GroundspeakAPI.GeoCacheRelated> geoCacheRelateds, GpxFilename forCategory) throws InterruptedException {
        AtomicBoolean isCanceled = new AtomicBoolean(false); // todo implement

        CBDB.getInstance().beginTransaction();

        for (GroundspeakAPI.GeoCacheRelated geoCacheRelated : geoCacheRelateds) {
            writeCacheAndLogsAndImagesIntoDB(geoCacheRelated, forCategory, true);
        }

        CBDB.getInstance().setTransactionSuccessful();
        CBDB.getInstance().endTransaction();

        updateCacheCountForGPXFilenames();

    }

    public void writeCacheAndLogsAndImagesIntoDB(GroundspeakAPI.GeoCacheRelated geoCacheRelated, GpxFilename forCategory, boolean keepOldCacheValues) throws InterruptedException {
        ImageDAO imageDAO = new ImageDAO();

        // perhaps react to thread cancel
        Thread.sleep(2);

        Cache cache = geoCacheRelated.cache;
        Cache oldCache = null;
        if (keepOldCacheValues) {
            oldCache = getFromDbByCacheId(cache.generatedId); // !!! without Details and without Description
            if (oldCache != null) {
                loadDetail(oldCache); // Details and Waypoints but without "Description, Solver, Notes, ShortDescription "
                cache.gcVoteRating = oldCache.gcVoteRating;
                if (!cache.isFound()) {
                    if (oldCache.isFound()) cache.setFound(true);
                }
                cache.setFavorite(oldCache.isFavorite());
                cache.setHasUserData(oldCache.isHasUserData());
                cache.setTourName(oldCache.getTourName());
                // solver is independant
                if (oldCache.hasCorrectedCoordinates()) {
                    if (cache.hasCorrectedCoordinates()) {
                        // changed coords from GS stay preserved
                    } else {
                        cache.setCoordinate(oldCache.getCoordinate());
                        cache.setHasCorrectedCoordinates(true);
                    }
                }
            }
        }

        if (forCategory != null) {
            if (oldCache == null) {
                cache.setGPXFilename_ID(forCategory.id);
            } else if (oldCache.getGPXFilename_ID() == 0) {
                cache.setGPXFilename_ID(forCategory.id);
            } else {
                Category c = CoreData.categories.getCategoryByGpxFilenameId(oldCache.getGPXFilename_ID());
                if (c.gpxFileName.equals(forCategory.gpxFileName)) {
                    // update with the new Date
                    cache.setGPXFilename_ID(forCategory.id);
                } else {
                    if (c.pinned) {
                        GpxFilename forPinnedCategory = null;
                        for (GpxFilename g : c) {
                            if (forCategory.importedDate == g.importedDate) {
                                forPinnedCategory = g;
                                break;
                            }
                        }
                        if (forPinnedCategory == null)
                            forPinnedCategory = c.addGpxFilename(c.gpxFileName, forCategory.importedDate);
                        cache.setGPXFilename_ID(forPinnedCategory.id);
                    } else {
                        cache.setGPXFilename_ID(forCategory.id);
                    }
                }
            }
        }

        // if update fails (geoCache not yet in DB) try insert
        if (!updateDatabase(cache)) {
            writeToDatabase(cache);
        }
        // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
        cache.setLongDescription("");

        // check notes from geocaching.com and add to the existing notes
        // todo extract solver?
        if (cache.getTmpNote() != null && cache.getTmpNote().length() > 0 || cache.getUserNote().length() > 0) {
            String begin = "<Import from Geocaching.com>";
            String end = "</Import from Geocaching.com>";
            if (keepOldCacheValues) {
                String oldNote = getNote(cache);

                if (oldNote != null) {
                    oldNote = oldNote.trim();
                } else {
                    oldNote = "";
                }
                if (!oldNote.startsWith(begin)) {
                    begin = "\n" + begin;
                }
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                String newNote;
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    // Copy the old part of Note before the beginning of the groundspeak block
                    newNote = oldNote.substring(0, iBegin);
                    newNote += begin + "\n";
                    newNote += cache.getTmpNote();
                    newNote += "\n" + end;
                    newNote += oldNote.substring(iEnd + end.length());
                } else {
                    newNote = oldNote + "\n";
                    newNote += begin + "\n";
                    newNote += cache.getTmpNote();
                    newNote += "\n" + end;
                }
                cache.setTmpNote(newNote);
                setNote(cache, cache.getUserNote() + cache.getTmpNote());
            } else {
                setNote(cache, cache.getUserNote() + "\n" + begin + "\n" + cache.getTmpNote() + "\n" + end + "\n");
            }
            cache.setUserNote(""); // better is it, if cache reused, will be fetch from db in NotesView
        }

        for (LogEntry log : geoCacheRelated.logs) {
            LogsTableDAO.getInstance().WriteLogEntry(log);
        }

        imageDAO.deleteImagesForCache(cache.getGeoCacheCode());
        for (ImageEntry image : geoCacheRelated.images) {
            imageDAO.writeToDatabase(image, false);
        }

        for (int i = 0, n = cache.getWayPoints().size(); i < n; i++) {
            // must Cast to Full Waypoint. If Waypoint, is wrong created!
            Waypoint waypoint = cache.getWayPoints().get(i);
            boolean update = true;

            // don't refresh wp if aktCache.wp is user changed
            if (oldCache != null) {
                if (oldCache.getWayPoints() != null) {
                    for (int j = 0, m = oldCache.getWayPoints().size(); j < m; j++) {
                        Waypoint oldWaypoint = oldCache.getWayPoints().get(j);
                        if (waypoint.isUserWaypoint && waypoint.waypointType == GeoCacheType.Final)
                            if (oldWaypoint.isUserWaypoint && oldWaypoint.waypointType == GeoCacheType.Final) {
                                waypoint.setWaypointCode(oldWaypoint.getWaypointCode());
                                break;
                            }
                        if (oldWaypoint.getWaypointCode().equalsIgnoreCase(waypoint.getWaypointCode())) {
                            if (oldWaypoint.isUserWaypoint)
                                update = false;
                            break;
                        }
                    }
                }
            }

            if (update) {
                // do not store replication information when importing caches with GC api
                if (!WaypointDAO.getInstance().UpdateDatabase(waypoint, false)) {
                    WaypointDAO.getInstance().WriteToDatabase(waypoint, false); // do not store replication information here
                }
            }

        }

        if (oldCache == null) {
            CBDB.getInstance().cacheList.add(cache);
            // cacheDAO.writeToDatabase(cache);
        } else {
            // 2012-11-17: do not remove old instance from cacheList because of problems with cacheList and MapView
            // Database.getInstance().cacheList.remove(Database.getInstance().cacheList.GetCacheById(cache.Id));
            // Database.getInstance().cacheList.add(cache);
            oldCache.copyFrom(cache); // todo Problem Waypoints of user are no longer seen ? Solution Add to cache.waypoints
            // cacheDAO.updateDatabase(cache);
        }

    }

    /**
     * Load Detail Information from DB
     */
    public void loadDetail(Cache geoCache) {
        readDetail(geoCache);
        // load all Waypoints with full Details
        CB_List<Waypoint> readWaypoints = WaypointDAO.getInstance().getWaypointsFromCacheID(geoCache.generatedId, true);
        for (int i = 0; i < readWaypoints.size(); i++) {
            Waypoint readWaypoint = readWaypoints.get(i);
            boolean found = false;
            for (int j = 0; j < geoCache.getWayPoints().size(); j++) {
                Waypoint existingWaypoint = geoCache.getWayPoints().get(j);
                if (readWaypoint.getWaypointCode().equals(existingWaypoint.getWaypointCode())) {
                    found = true;
                    existingWaypoint.detail = readWaypoint.detail; // copy Detail Info
                    break;
                }
            }
            if (!found) {
                geoCache.getWayPoints().add(readWaypoint);
            }
        }
    }

}

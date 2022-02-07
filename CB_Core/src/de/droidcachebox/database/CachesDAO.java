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

import com.badlogic.gdx.files.FileHandle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.CacheDetail;
import de.droidcachebox.dataclasses.CacheList;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.dataclasses.ImageEntry;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.log.Log;

public class CachesDAO {
    static final String SQL_DETAILS = "PlacedBy, DateHidden, Url, TourName, GpxFilename_ID, ApiStatus, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh, Hint, Country, State ";
    static final String SQL_GET_DETAIL_WITH_DESCRIPTION = "Description, Solver, Notes, ShortDescription ";
    static final String SQL_GET_CACHE = "select c.Id, GcCode, Latitude, Longitude, c.Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, Owner, NumTravelbugs, GcId, Rating, Favorit, HasUserData, ListingChanged, CorrectedCoordinates, FavPoints ";
    private static final String SQL_BY_ID = "from Caches c where id = ?";
    private static final String SQL_BY_GC_CODE = "from Caches c where GCCode = ?";
    private static final String SQL_GET_DETAIL_FROM_ID = "select " + SQL_DETAILS + SQL_BY_ID;
    private static final String SQL_EXIST_CACHE = "select 1 from Caches where Id = ?";
    private static final String sClass = "CacheDAO";

    public CachesDAO() {
    }

    /**
     * selecting by a list of GCCodes
     * !!! only exportBatch
     */
    public CacheList readCacheList(ArrayList<String> GC_Codes, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        ArrayList<String> orParts = new ArrayList<>();

        for (String gcCode : GC_Codes) {
            orParts.add("GcCode like '%" + gcCode + "%'");
        }
        String where = join(orParts);
        CacheList cacheList = new CacheList();
        readCacheList(cacheList, where, withDescription, fullDetails, loadAllWaypoints);
        return cacheList;
    }

    private String join(ArrayList<String> array) {
        StringBuilder retString = new StringBuilder();
        int count = 0;
        for (String tmp : array) {
            retString.append(tmp);
            count++;
            if (count < array.size())
                retString.append(" or ");
        }
        return retString.toString();
    }

    public void readCacheList(String sqlQualification, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        synchronized (CBDB.cacheList) {
            readCacheList(CBDB.cacheList, sqlQualification, withDescription, fullDetails, loadAllWaypoints);
        }
    }

    public void readCacheList(CacheList cacheList, String sqlQualification, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        cacheList.clear();

        // Log.trace(log, "readCacheList 1.Waypoints");
        SortedMap<Long, CB_List<Waypoint>> waypoints;
        waypoints = new TreeMap<>();
        // zuerst alle Waypoints einlesen
        CB_List<Waypoint> wpList = new CB_List<>();
        long aktCacheID = -1;

        String query = fullDetails ? WaypointDAO.SQL_WP_FULL : WaypointDAO.SQL_WP;
        if (!((fullDetails || loadAllWaypoints))) {
            // when CacheList should be loaded without full details and without all Waypoints
            // do not load all waypoints from db!
            query += " where IsStart=\"true\" or Type=" + GeoCacheType.Final.ordinal(); // StartWaypoint or CacheTypes.Final
        }
        query += " order by CacheId";
        CoreCursor reader = CBDB.getInstance().rawQuery(query, null);
        if (reader == null) return;

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Waypoint wp = WaypointDAO.getInstance().getWaypoint(reader, fullDetails);
            if (!(fullDetails || loadAllWaypoints)) {
                // wenn keine FullDetails geladen werden sollen dann sollen nur die Finals und Start-Waypoints geladen werden
                if (!(wp.isStartWaypoint || wp.waypointType == GeoCacheType.Final)) {
                    reader.moveToNext();
                    continue;
                }
            }
            if (wp.geoCacheId != aktCacheID) {
                aktCacheID = wp.geoCacheId;
                wpList = new CB_List<>();
                waypoints.put(aktCacheID, wpList);
            }
            wpList.add(wp);
            reader.moveToNext();
        }
        reader.close();

        // Log.trace(log, "readCacheList 2.Caches");
        try {
            if (fullDetails) {
                query = SQL_GET_CACHE + ", " + SQL_DETAILS;
                if (withDescription) {
                    // load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
                    query += "," + SQL_GET_DETAIL_WITH_DESCRIPTION;
                }
            } else {
                query = SQL_GET_CACHE;

            }

            // an empty sqlQualification and a sqlQualification other than where (p.e for join) starting with 5 blanks (by my definition)
            boolean addWhere = sqlQualification.length() > 0 && !sqlQualification.startsWith("     ");
            query = query + " from Caches c " + (addWhere ? "where " + sqlQualification : sqlQualification);
            reader = CBDB.getInstance().rawQuery(query, null);

        } catch (Exception e) {
            Log.err(sClass, "CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
        }
        if (reader != null) {
            if (reader.getCount() > 0) {
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    Cache cache = new Cache(reader, fullDetails, withDescription);
                    cacheList.add(cache);
                    cache.getWayPoints().clear();
                    if (waypoints.containsKey(cache.generatedId)) {
                        CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.generatedId);

                        for (int i = 0, n = tmpwaypoints.size(); i < n; i++) {
                            cache.getWayPoints().add(tmpwaypoints.get(i));
                        }

                        waypoints.remove(cache.generatedId);
                    }
                    reader.moveToNext();
                }
            }
            reader.close();
        }
    }

    /**
     * @param Where                       sql
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @param isCanceled                  may be
     * @return count deleted
     */
    public long delete(String Where,
                       String SpoilerFolder,
                       String SpoilerFolderLocal,
                       String DescriptionImageFolder,
                       String DescriptionImageFolderLocal,
                       AtomicBoolean isCanceled) {
        try {
            delCacheImages(getGcCodes(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            CBDB.getInstance().beginTransaction();
            long ret = CBDB.getInstance().delete("Caches", Where, null);
            if (isCanceled.get()) {
                ret = 0;
                CBDB.getInstance().endTransaction();
            } else {
                CBDB.getInstance().setTransactionSuccessful();
                CBDB.getInstance().endTransaction();
                updateCacheCountForGPXFilenames(); // CoreData.Categories will be set
            }
            return ret;
        } catch (Exception e) {
            Log.err(sClass, "CacheListDAO.DelFilter()", "Filter ERROR", e);
            return -1;
        }
    }

    private ArrayList<String> getGcCodes(String where) {
        CacheList list = new CacheList();
        readCacheList(list, where, false, false, false);

        ArrayList<String> gcCodes = new ArrayList<>();
        for (int i = 0, n = list.size(); i < n; i++) {
            gcCodes.add(list.get(i).getGeoCacheCode());
        }
        return gcCodes;
    }

    /**
     * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
     *
     * @param listOfGCCodes               listOfGCCodes
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     */
    public void delCacheImages(ArrayList<String> listOfGCCodes, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        String spoilerpath = SpoilerFolder;
        if (SpoilerFolderLocal.length() > 0)
            spoilerpath = SpoilerFolderLocal;

        String imagespath = DescriptionImageFolder;
        if (DescriptionImageFolderLocal.length() > 0)
            imagespath = DescriptionImageFolderLocal;

        Log.debug(sClass, "Del Spoilers from " + spoilerpath);
        delCacheImagesByPath(spoilerpath, listOfGCCodes);
        Log.debug(sClass, "Del Images from " + imagespath);
        delCacheImagesByPath(imagespath, listOfGCCodes);

        ImageDAO imageDAO = new ImageDAO();
        for (final String GcCode : listOfGCCodes) {
            imageDAO.deleteImagesForCache(GcCode);
        }
    }

    private void delCacheImagesByPath(String path, ArrayList<String> list) {
        for (String s : list) {
            final String GcCode = s.toLowerCase();
            String directory = path + "/" + GcCode.substring(0, Math.min(4, GcCode.length()));
            if (!FileIO.directoryExists(directory))
                continue;

            FileHandle dir = new FileHandle(directory);
            FileHandle[] files = dir.list();

            for (FileHandle fileHandle : files) {

                // simplyfied for startswith gccode, thumbs_gccode + ooverwiewthumbs_gccode
                if (!fileHandle.name().toLowerCase().contains(GcCode))
                    continue;

                String filename = directory + "/" + fileHandle.name();
                FileHandle file = new FileHandle(filename);
                if (file.exists()) {
                    if (!file.delete())
                        Log.err(sClass, "Error deleting : " + filename);
                }
            }
        }
    }

    public void readDetail(Cache cache) {
        if (cache.getGeoCacheDetail() != null)
            return;
        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_GET_DETAIL_FROM_ID, new String[]{String.valueOf(cache.generatedId)});
        if (reader != null) {
            if (reader.getCount() > 0) {
                reader.moveToFirst();
                cache.setGeoCacheDetail(new CacheDetail(reader, false, false));
            }
            reader.close();
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

    public void updateFound(Cache cache) {
        Parameters args = new Parameters();
        args.put("found", cache.isFound());
        try {
            CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(cache.generatedId)});
            Replication.updateFound(cache.generatedId, cache.isFound());
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
        Cache cache = null;
        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_GET_CACHE + SQL_BY_ID, new String[]{String.valueOf(CacheID)});
        if (reader != null) {
            if (reader.getCount() > 0) {
                try {
                    reader.moveToFirst();
                    cache = new Cache(reader, false, false);
                } catch (Exception e) {
                    Log.err(sClass, e);
                }
            }
            reader.close();
        }
        return cache;
    }

    public Cache getFromDbByGcCode(String GcCode, boolean withDetail) // (test only)
    {
        Cache cache = null;
        String where = SQL_GET_CACHE + (withDetail ? ", " + SQL_DETAILS : "") + SQL_BY_GC_CODE;
        CoreCursor reader = CBDB.getInstance().rawQuery(where, new String[]{GcCode});
        if (reader != null) {
            if (reader.getCount() > 0) {
                try {
                    reader.moveToFirst();
                    cache = new Cache(reader, withDetail, false);
                } catch (Exception e) {
                    Log.err(sClass, e);
                }
            }
            reader.close();
        }
        return cache;
    }

    public boolean cacheExists(long CacheID) {
        CoreCursor reader = CBDB.getInstance().rawQuery(SQL_EXIST_CACHE, new String[]{String.valueOf(CacheID)});
        boolean exists;
        if (reader != null) {
            exists = reader.getCount() > 0;
            reader.close();
        }
        else exists = false;
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
            Replication.updateFound(writeTmp.generatedId, writeTmp.isFound());
            args.put("Found", writeTmp.isFound());
        }

        if (changed) {
            try {
                CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(writeTmp.generatedId)});
            } catch (Exception exc) {
                Log.err(sClass, "updateDatabaseCacheState", exc);
            }
        }

        return changed;
    }

    public void updateCacheCountForGPXFilenames() {
        // welche GPXFilenamen sind in der DB erfasst
        CBDB.getInstance().beginTransaction();
        CoreCursor reader = CBDB.getInstance().rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
        if (reader != null) {
            if (reader.getCount() > 0) {
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    try {
                        long GPXFilename_ID = reader.getLong(0);
                        long CacheCount = reader.getLong(1);
                        Parameters val = new Parameters();
                        val.put("CacheCount", CacheCount);
                        CBDB.getInstance().update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);
                        reader.moveToNext();
                    } catch (Exception exc) {
                        Log.err(sClass, "updateCacheCountForGPXFilenames", exc);
                    }
                }
                CBDB.getInstance().delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
                CBDB.getInstance().delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
                CBDB.getInstance().setTransactionSuccessful();
            }
            reader.close();
        }
        CBDB.getInstance().endTransaction();
        CategoryDAO.getInstance().loadCategoriesFromDatabase();
    }

    public String getNote(Cache cache) {
        return getStringValue(cache.generatedId, "Notes");
    }

    public String getNote(long generatedId) {
        return getStringValue(generatedId, "Notes");
    }

    private String getStringValue(long generatedId, String column) {
        String resultString = "";
        CoreCursor c = CBDB.getInstance().rawQuery("select " + column + " from Caches where Id=?", new String[]{String.valueOf(generatedId)});
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                resultString = c.getString(0);
                if (resultString == null) resultString = "";
            }
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

    public String getSolver(long generatedId) {
        return getStringValue(generatedId, "Solver");
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
        return getStringValue(cache.generatedId, "Description");
    }

    public String getShortDescription(Cache cache) {
        return getStringValue(cache.generatedId, "ShortDescription");
    }

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
                if (!WaypointDAO.getInstance().updateDatabase(waypoint, false)) {
                    WaypointDAO.getInstance().writeToDatabase(waypoint, false); // do not store replication information here
                }
            }

        }

        if (oldCache == null) {
            CBDB.cacheList.add(cache);
            // writeToDatabase(cache);
        } else {
            // 2012-11-17: do not remove old instance from cacheList because of problems with cacheList and MapView
            // Database.getInstance().cacheList.remove(Database.getInstance().cacheList.GetCacheById(cache.Id));
            // Database.getInstance().cacheList.add(cache);
            oldCache.copyFrom(cache); // todo Problem Waypoints of user are no longer seen ? Solution Add to cache.waypoints
            // updateDatabase(cache);
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

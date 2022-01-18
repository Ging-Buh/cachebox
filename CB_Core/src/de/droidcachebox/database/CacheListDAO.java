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

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.CacheList;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

/**
 * @author ging-buh
 * @author Longri
 */
public class CacheListDAO {
    private static final String log = "CacheListDAO";
    private static CacheListDAO cacheListDAO;

    private CacheListDAO() {
    }

    public static CacheListDAO getInstance() {
        if (cacheListDAO == null) cacheListDAO = new CacheListDAO();
        return cacheListDAO;
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
        readCacheList(CBDB.getInstance().cacheList, sqlQualification, withDescription, fullDetails, loadAllWaypoints);
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
                query = CacheDAO.SQL_GET_CACHE + ", " + CacheDAO.SQL_DETAILS;
                if (withDescription) {
                    // load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
                    query += "," + CacheDAO.SQL_GET_DETAIL_WITH_DESCRIPTION;
                }
            } else {
                query = CacheDAO.SQL_GET_CACHE;

            }

            // an empty sqlQualification and a sqlQualification other than where (p.e for join) starting with 5 blanks (by my definition)
            boolean addWhere = sqlQualification.length() > 0 && !sqlQualification.startsWith("     ");
            query = query + " from Caches c " + (addWhere ? "where " + sqlQualification : sqlQualification);
            reader = CBDB.getInstance().rawQuery(query, null);

        } catch (Exception e) {
            Log.err(log, "CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
        }
        reader.moveToFirst();

        CacheDAO cacheDAO = CacheDAO.getInstance();

        while (!reader.isAfterLast()) {
            Cache cache = cacheDAO.readFromCursor(reader, fullDetails, withDescription);
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
        reader.close();

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
                CacheDAO.getInstance().updateCacheCountForGPXFilenames(); // CoreData.Categories will be set
            }
            return ret;
        } catch (Exception e) {
            Log.err(log, "CacheListDAO.DelFilter()", "Filter ERROR", e);
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

        list.dispose();
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

        Log.debug(log, "Del Spoilers from " + spoilerpath);
        delCacheImagesByPath(spoilerpath, listOfGCCodes);
        Log.debug(log, "Del Images from " + imagespath);
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
                        Log.err(log, "Error deleting : " + filename);
                }
            }
        }
    }
}

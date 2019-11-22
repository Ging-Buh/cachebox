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
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author ging-buh
 * @author Longri
 */
public class CacheListDAO {
    private static final String log = "CacheListDAO";

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
        return readCacheList(where, withDescription, fullDetails, loadAllWaypoints);
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

    /**
     *
     */
    public CacheList readCacheList(String sqlQualification, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        CacheList cacheList = new CacheList();

        // Log.trace(log, "readCacheList 1.Waypoints");
        SortedMap<Long, CB_List<Waypoint>> waypoints;
        waypoints = new TreeMap<>();
        // zuerst alle Waypoints einlesen
        CB_List<Waypoint> wpList = new CB_List<>();
        long aktCacheID = -1;

        String sql = fullDetails ? WaypointDAO.SQL_WP_FULL : WaypointDAO.SQL_WP;
        if (!((fullDetails || loadAllWaypoints))) {
            // when CacheList should be loaded without full details and without all Waypoints
            // do not load all waypoints from db!
            sql += " where IsStart=\"true\" or Type=" + CacheTypes.Final.ordinal(); // StartWaypoint or CacheTypes.Final
        }
        sql += " order by CacheId";
        CoreCursor reader = Database.Data.sql.rawQuery(sql, null);
        if (reader == null)
            return cacheList;

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            WaypointDAO waypointDAO = new WaypointDAO();
            Waypoint wp = waypointDAO.getWaypoint(reader, fullDetails);
            if (!(fullDetails || loadAllWaypoints)) {
                // wenn keine FullDetails geladen werden sollen dann sollen nur die Finals und Start-Waypoints geladen werden
                if (!(wp.IsStart || wp.Type == CacheTypes.Final)) {
                    reader.moveToNext();
                    continue;
                }
            }
            if (wp.CacheId != aktCacheID) {
                aktCacheID = wp.CacheId;
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
                sql = CacheDAO.SQL_GET_CACHE + ", " + CacheDAO.SQL_DETAILS;
                if (withDescription) {
                    // load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
                    sql += "," + CacheDAO.SQL_GET_DETAIL_WITH_DESCRIPTION;
                }
            } else {
                sql = CacheDAO.SQL_GET_CACHE;

            }

            // an empty sqlQualification and a sqlQualification other than where (p.e for join) starting with 5 blanks (by my definition)
            boolean addWhere = sqlQualification.length() > 0 && !sqlQualification.startsWith("     ");
            sql += " from Caches c " + (addWhere ? "where " + sqlQualification : sqlQualification);
            reader = Database.Data.sql.rawQuery(sql, null);

        } catch (Exception e) {
            Log.err(log, "CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
        }
        reader.moveToFirst();

        CacheDAO cacheDAO = new CacheDAO();

        while (!reader.isAfterLast()) {
            Cache cache = cacheDAO.ReadFromCursor(reader, fullDetails, withDescription);
            /*
            // implemented in sql query in november 2019. arbor95
            boolean doAdd = true;
            if (FilterInstances.hasCorrectedCoordinates != 0) {
                if (waypoints.containsKey(cache.Id)) {
                    CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.Id);
                    for (int i = 0, n = tmpwaypoints.size(); i < n; i++) {
                        cache.waypoints.add(tmpwaypoints.get(i));
                    }
                }
                boolean hasCorrectedCoordinates = cache.hasCorrectedCoordiantesOrHasCorrectedFinal();
                if (FilterInstances.hasCorrectedCoordinates < 0) {
                    // show only those without corrected ones
                    if (hasCorrectedCoordinates)
                        doAdd = false;
                } else if (FilterInstances.hasCorrectedCoordinates > 0) {
                    // only those with corrected ones
                    if (!hasCorrectedCoordinates)
                        doAdd = false;
                }
            }
            if (doAdd) {

            }
             */
            cacheList.add(cache);
            cache.waypoints.clear();
            if (waypoints.containsKey(cache.Id)) {
                CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.Id);

                for (int i = 0, n = tmpwaypoints.size(); i < n; i++) {
                    cache.waypoints.add(tmpwaypoints.get(i));
                }

                waypoints.remove(cache.Id);
            }
            // ++Global.CacheCount;
            reader.moveToNext();

        }
        reader.close();

        return cacheList;

    }

    /**
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return count deleted
     */
    public long deleteArchived(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodes("Archived=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            long ret = Database.Data.sql.delete("Caches", "Archived=1", null);
            Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
            return ret;
        } catch (Exception e) {
            Log.err(log, "CacheListDAO.DelArchiv()", "Archiv ERROR", e);
            return -1;
        }
    }

    /**
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return count deleted
     */
    public long deleteFinds(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodes("Found=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            long ret = Database.Data.sql.delete("Caches", "Found=1", null);
            Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
            return ret;
        } catch (Exception e) {
            Log.err(log, "CacheListDAO.DelFound()", "Found ERROR", e);
            return -1;
        }
    }

    /**
     * @param Where                       sql
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return count deleted
     */
    public long deleteFiltered(String Where, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodes(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            Database.Data.sql.beginTransaction();
            long ret = Database.Data.sql.delete("Caches", Where, null);
            Database.Data.sql.setTransactionSuccessful();
            Database.Data.sql.endTransaction();
            Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
            return ret;
        } catch (Exception e) {
            Log.err(log, "CacheListDAO.DelFilter()", "Filter ERROR", e);
            return -1;
        }
    }

    private ArrayList<String> getGcCodes(String where) {
        CacheList list = readCacheList(where, false, false, false);
        ArrayList<String> gcCodes = new ArrayList<>();

        for (int i = 0, n = list.size(); i < n; i++) {
            gcCodes.add(list.get(i).getGcCode());
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

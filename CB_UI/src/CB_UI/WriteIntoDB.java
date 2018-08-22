package CB_UI;

import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;

import java.util.ArrayList;

public class WriteIntoDB {

    public static void CachesAndLogsAndImagesIntoDB(CB_List<Cache> apiCaches, ArrayList<LogEntry> apiLogs, ArrayList<ImageEntry> apiImages) throws InterruptedException {
        // Auf eventuellen Thread Abbruch reagieren
        Thread.sleep(2);

        Database.Data.beginTransaction();

        CacheDAO cacheDAO = new CacheDAO();
        LogDAO logDAO = new LogDAO();
        ImageDAO imageDAO = new ImageDAO();
        WaypointDAO waypointDAO = new WaypointDAO();

        for (int c = 0; c < apiCaches.size(); c++) {
            Cache cache = apiCaches.get(c);
            Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);

            if (aktCache != null && aktCache.isLive())
                aktCache = null;

            if (aktCache == null) {
                aktCache = cacheDAO.getFromDbByCacheId(cache.Id);
            }
            // Read Detail Info of Cache if not available
            if ((aktCache != null) && (aktCache.detail == null)) {
                aktCache.loadDetail();
            }
            // If Cache into DB, extract saved rating
            if (aktCache != null) {
                cache.Rating = aktCache.Rating;
            }

            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
            if (!cacheDAO.UpdateDatabase(cache)) {
                cacheDAO.WriteToDatabase(cache);
            }

            // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
            if (cache.getTmpNote() != null) {
                String oldNote = Database.GetNote(cache);
                String newNote = "";
                if (oldNote == null) {
                    oldNote = "";
                }
                String begin = "<Import from Geocaching.com>";
                String end = "</Import from Geocaching.com>";
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
                    // the beginning of the groundspeak block
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                    newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
                } else {
                    newNote = oldNote + System.getProperty("line.separator");
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                }
                cache.setTmpNote(newNote);
                Database.SetNote(cache, cache.getTmpNote());
            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            cache.setLongDescription("");

            for (LogEntry log : apiLogs) {
                if (log.CacheId != cache.Id)
                    continue;
                // Write Log to database

                logDAO.WriteToDatabase(log);
            }

            for (ImageEntry image : apiImages) {
                if (image.CacheId != cache.Id)
                    continue;
                // Write Image to database

                imageDAO.WriteToDatabase(image, false);
            }

            for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
                // must Cast to Full Waypoint. If Waypoint, is wrong createt!
                Waypoint waypoint = cache.waypoints.get(i);
                boolean update = true;

                // dont refresh wp if aktCache.wp is user changed
                if (aktCache != null) {
                    if (aktCache.waypoints != null) {
                        for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
                            Waypoint wp = aktCache.waypoints.get(j);
                            if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                if (wp.IsUserWaypoint)
                                    update = false;
                                break;
                            }
                        }
                    }
                }

                if (update) {
                    // do not store replication information when importing caches with GC api
                    if (!waypointDAO.UpdateDatabase(waypoint, false)) {
                        waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                    }
                }

            }

            if (aktCache == null) {
                Database.Data.Query.add(cache);
                // cacheDAO.WriteToDatabase(cache);
            } else {
                // 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
                // Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
                // Database.Data.Query.add(cache);
                aktCache.copyFrom(cache);
                // cacheDAO.UpdateDatabase(cache);
            }

        }
        Database.Data.setTransactionSuccessful();
        Database.Data.endTransaction();

        Database.Data.GPXFilenameUpdateCacheCount();

    }

}

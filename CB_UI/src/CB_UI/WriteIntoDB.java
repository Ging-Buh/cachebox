package CB_UI;

import CB_Core.CoreSettingsForward;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.*;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.GeoCacheRelated;

public class WriteIntoDB {
    static CacheDAO cacheDAO = new CacheDAO();
    static LogDAO logDAO = new LogDAO();
    static ImageDAO imageDAO = new ImageDAO();
    static WaypointDAO waypointDAO = new WaypointDAO();

    public static void CachesAndLogsAndImagesIntoDB(ArrayList<GeoCacheRelated> geoCacheRelateds, GpxFilename forCategory) throws InterruptedException {

        if (cacheDAO == null) {
            cacheDAO = new CacheDAO();
            logDAO = new LogDAO();
            imageDAO = new ImageDAO();
            waypointDAO = new WaypointDAO();
        }

        Database.Data.beginTransaction();

        for (GeoCacheRelated geoCacheRelated : geoCacheRelateds) {

            // Auf eventuellen Thread Abbruch reagieren
            Thread.sleep(2);

            Cache cache = geoCacheRelated.cache;
            Cache oldCache = Database.Data.Query.GetCacheById(cache.Id);

            if (oldCache != null && oldCache.isLive()) oldCache = null;

            if (oldCache == null) oldCache = cacheDAO.getFromDbByCacheId(cache.Id);

            // Read Detail Info of Cache if not available
            if ((oldCache != null) && (oldCache.detail == null)) {
                oldCache.loadDetail();
            }
            // If Cache into DB, extract saved rating
            if (oldCache != null) {
                cache.Rating = oldCache.Rating;
            }

            if (forCategory != null) {
                if (oldCache == null) {
                    cache.setGPXFilename_ID(forCategory.Id);
                } else if (oldCache.getGPXFilename_ID() == 0) {
                    cache.setGPXFilename_ID(forCategory.Id);
                } else {
                    Category c = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(oldCache.getGPXFilename_ID());
                    if (c.GpxFilename.equals(forCategory.GpxFileName)) {
                        // update with the new Date
                        cache.setGPXFilename_ID(forCategory.Id);
                    } else {
                        if (c.pinned) {
                            GpxFilename forPinnedCategory = null;
                            for (GpxFilename g : c) {
                                if (forCategory.Imported == g.Imported) {
                                    forPinnedCategory = g;
                                    break;
                                }
                            }
                            if (forPinnedCategory == null)
                                forPinnedCategory = c.addGpxFilename(c.GpxFilename, forCategory.Imported);
                            cache.setGPXFilename_ID(forPinnedCategory.Id);
                        } else {
                            cache.setGPXFilename_ID(forCategory.Id);
                        }
                    }
                }
            }

            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
            if (!cacheDAO.UpdateDatabase(cache)) {
                cacheDAO.WriteToDatabase(cache);
            }

            // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
            // todo solver extrahieren
            if (cache.getTmpNote() != null && cache.getTmpNote().length() > 0) {

                String oldNote = Database.GetNote(cache);
                if (oldNote != null) {
                    oldNote = oldNote.trim();
                } else {
                    oldNote = "";
                }
                String begin = "<Import from Geocaching.com>";
                if (!oldNote.startsWith(begin)) {
                    begin = System.getProperty("line.separator") + begin;
                }
                String end = "</Import from Geocaching.com>";
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                String newNote;
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    // Copy the old part of Note before the beginning of the groundspeak block
                    newNote = oldNote.substring(0, iBegin);
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

            for (LogEntry log : geoCacheRelated.logs) {
                logDAO.WriteToDatabase(log);
            }

            imageDAO.deleteImagesForCache(cache.getGcCode());
            for (ImageEntry image : geoCacheRelated.images) {
                imageDAO.WriteToDatabase(image, false);
            }

            for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
                // must Cast to Full Waypoint. If Waypoint, is wrong createt!
                Waypoint waypoint = cache.waypoints.get(i);
                boolean update = true;

                // dont refresh wp if aktCache.wp is user changed
                if (oldCache != null) {
                    if (oldCache.waypoints != null) {
                        for (int j = 0, m = oldCache.waypoints.size(); j < m; j++) {
                            Waypoint wp = oldCache.waypoints.get(j);
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

            if (oldCache == null) {
                Database.Data.Query.add(cache);
                // cacheDAO.WriteToDatabase(cache);
            } else {
                // 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
                // Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
                // Database.Data.Query.add(cache);
                oldCache.copyFrom(cache);
                // cacheDAO.UpdateDatabase(cache);
            }

        }

        Database.Data.setTransactionSuccessful();
        Database.Data.endTransaction();

        Database.Data.GPXFilenameUpdateCacheCount();

    }

}

package de.droidcachebox.database;

import de.droidcachebox.core.CoreData;

import java.util.ArrayList;

import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;

public class WriteIntoDB {
    static public LogDAO logDAO = new LogDAO();
    static CacheDAO cacheDAO = new CacheDAO();
    static ImageDAO imageDAO = new ImageDAO();
    static WaypointDAO waypointDAO = new WaypointDAO();

    public static void writeCachesAndLogsAndImagesIntoDB(ArrayList<GeoCacheRelated> geoCacheRelateds, GpxFilename forCategory) throws InterruptedException {

        if (cacheDAO == null) {
            cacheDAO = new CacheDAO();
            logDAO = new LogDAO();
            imageDAO = new ImageDAO();
            waypointDAO = new WaypointDAO();
        }

        Database.Data.sql.beginTransaction();

        for (GeoCacheRelated geoCacheRelated : geoCacheRelateds) {
            writeCacheAndLogsAndImagesIntoDB(geoCacheRelated, forCategory);
        }

        Database.Data.sql.setTransactionSuccessful();
        Database.Data.sql.endTransaction();

        Database.Data.updateCacheCountForGPXFilenames();

    }

    public static void writeCacheAndLogsAndImagesIntoDB(GeoCacheRelated geoCacheRelated, GpxFilename forCategory) throws InterruptedException {
        writeCacheAndLogsAndImagesIntoDB(geoCacheRelated, forCategory, true);
    }

    public static void writeCacheAndLogsAndImagesIntoDB(GeoCacheRelated geoCacheRelated, GpxFilename forCategory, boolean keepOldCacheValues) throws InterruptedException {

        // Auf eventuellen Thread Abbruch reagieren
        Thread.sleep(2);

        Cache cache = geoCacheRelated.cache;
        Cache oldCache = null;
        if (keepOldCacheValues) {
            oldCache = cacheDAO.getFromDbByCacheId(cache.generatedId); // !!! without Details and without Description
            if (oldCache != null) {
                oldCache.loadDetail(); // Details and Waypoints but without "Description, Solver, Notes, ShortDescription "
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
                cache.setGPXFilename_ID(forCategory.Id);
            } else if (oldCache.getGPXFilename_ID() == 0) {
                cache.setGPXFilename_ID(forCategory.Id);
            } else {
                Category c = CoreData.categories.getCategoryByGpxFilenameId(oldCache.getGPXFilename_ID());
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
        // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
        cache.setLongDescription("");

        // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
        // todo extract solver?
        if (cache.getTmpNote() != null && cache.getTmpNote().length() > 0 || cache.getUserNote().length() > 0) {
            String begin = "<Import from Geocaching.com>";
            String end = "</Import from Geocaching.com>";
            if (keepOldCacheValues) {
                String oldNote = Database.getNote(cache);

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
                Database.setNote(cache, cache.getUserNote() + cache.getTmpNote());
            } else {
                Database.setNote(cache, cache.getUserNote() + "\n" + begin + "\n" + cache.getTmpNote() + "\n" + end + "\n");
            }
            cache.setUserNote(""); // better is it, if cache reused, will be fetch from db in NotesView
        }

        for (LogEntry log : geoCacheRelated.logs) {
            logDAO.WriteToDatabase(log);
        }

        imageDAO.deleteImagesForCache(cache.getGeoCacheCode());
        for (ImageEntry image : geoCacheRelated.images) {
            imageDAO.writeToDatabase(image, false);
        }

        for (int i = 0, n = cache.getWayPoints().size(); i < n; i++) {
            // must Cast to Full Waypoint. If Waypoint, is wrong created!
            Waypoint waypoint = cache.getWayPoints().get(i);
            boolean update = true;

            // dont refresh wp if aktCache.wp is user changed
            if (oldCache != null) {
                if (oldCache.getWayPoints() != null) {
                    for (int j = 0, m = oldCache.getWayPoints().size(); j < m; j++) {
                        Waypoint oldWaypoint = oldCache.getWayPoints().get(j);
                        if (waypoint.isUserWaypoint && waypoint.waypointType == GeoCacheType.Final)
                            if (oldWaypoint.isUserWaypoint && oldWaypoint.waypointType == GeoCacheType.Final) {
                                waypoint.setGcCode(oldWaypoint.getGcCode());
                                break;
                            }
                        if (oldWaypoint.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                            if (oldWaypoint.isUserWaypoint)
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
            Database.Data.cacheList.add(cache);
            // cacheDAO.writeToDatabase(cache);
        } else {
            // 2012-11-17: do not remove old instance from cacheList because of problems with cacheList and MapView
            // Database.Data.cacheList.remove(Database.Data.cacheList.GetCacheById(cache.Id));
            // Database.Data.cacheList.add(cache);
            oldCache.copyFrom(cache); // todo Problem Waypoints of user are no longer seen ? Solution Add to cache.waypoints
            // cacheDAO.updateDatabase(cache);
        }

    }

}

package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.UnitFormatter;

public class WaypointDAO {

    public static final String SQL_WP = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart from Waypoint";
    public static final String SQL_WP_FULL = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart, Description, Clue from Waypoint";

    private static WaypointDAO waypointDAO;

    private WaypointDAO() {
    }

    public static WaypointDAO getInstance() {
        if (waypointDAO == null) waypointDAO = new WaypointDAO();
        return waypointDAO;
    }

    public void writeToDatabase(Waypoint waypoint) {
        writeToDatabase(waypoint, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public void writeToDatabase(Waypoint waypoint, boolean useReplication) {
        int newCheckSum = createCheckSum(waypoint);
        if (useReplication) {
            Replication.WaypointNew(waypoint.geoCacheId, waypoint.getCheckSum(), newCheckSum, waypoint.getWaypointCode());
        }
        Parameters args = new Parameters();
        args.put("gccode", waypoint.getWaypointCode());
        args.put("cacheid", waypoint.geoCacheId);
        args.put("latitude", waypoint.getLatitude());
        args.put("longitude", waypoint.getLongitude());
        args.put("description", waypoint.getDescription());
        args.put("type", waypoint.waypointType.ordinal());
        args.put("syncexclude", waypoint.isSyncExcluded);
        args.put("userwaypoint", waypoint.isUserWaypoint);
        if (waypoint.getClue() == null)
            waypoint.setClue("");
        args.put("clue", waypoint.getClue());
        args.put("title", waypoint.getTitle());
        args.put("isStart", waypoint.isStartWaypoint);

        try {
            long count = CBDB.getInstance().insert("Waypoint", args);
            if (count <= 0) {
                CBDB.getInstance().update("Waypoint", args, "gccode=\"" + waypoint.getWaypointCode() + "\"", null);
            }
            if (waypoint.isUserWaypoint) {
                args = new Parameters();
                args.put("hasUserData", true);
                CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
            }
        } catch (Exception ignored) {
        }
    }

    public void updateDatabase(Waypoint WP) {
        updateDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public boolean updateDatabase(Waypoint waypoint, boolean useReplication) {
        boolean result = false;
        int newCheckSum = createCheckSum(waypoint);
        if (useReplication) {
            Replication.waypointChanged(waypoint.geoCacheId, waypoint.getCheckSum(), newCheckSum, waypoint.getWaypointCode());
        }
        if (newCheckSum != waypoint.getCheckSum()) {
            Parameters args = new Parameters();
            args.put("gccode", waypoint.getWaypointCode());
            args.put("cacheid", waypoint.geoCacheId);
            args.put("latitude", waypoint.getLatitude());
            args.put("longitude", waypoint.getLongitude());
            args.put("description", waypoint.getDescription());
            args.put("type", waypoint.waypointType.ordinal());
            args.put("syncexclude", waypoint.isSyncExcluded);
            args.put("userwaypoint", waypoint.isUserWaypoint);
            args.put("clue", waypoint.getClue());
            args.put("title", waypoint.getTitle());
            args.put("isStart", waypoint.isStartWaypoint);
            try {
                long count = CBDB.getInstance().update("Waypoint", args, "CacheId=" + waypoint.geoCacheId + " and GcCode=\"" + waypoint.getWaypointCode() + "\"", null);
                if (count > 0)
                    result = true;
            } catch (Exception ignored) {
            }

            if (waypoint.isUserWaypoint) {
                args = new Parameters();
                args.put("hasUserData", true);
                try {
                    CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
                } catch (Exception exc) {
                    return result;
                }
            }
            waypoint.setCheckSum(newCheckSum);
        }
        return result;
    }

    /**
     * Create Waypoint Object from Reader.
     *
     * @param reader  ?
     * @param getFull Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return ?
     */
    public Waypoint getWaypoint(CoreCursor reader, boolean getFull) {
        Waypoint waypoint;

        waypoint = new Waypoint(getFull);

        waypoint.setWaypointCode(reader.getString(0));
        waypoint.geoCacheId = reader.getLong(1);
        double latitude = reader.getDouble(2);
        double longitude = reader.getDouble(3);
        waypoint.setCoordinate(new Coordinate(latitude, longitude));
        waypoint.waypointType = GeoCacheType.values()[reader.getShort(4)];
        waypoint.isSyncExcluded = reader.getInt(5) == 1;
        waypoint.isUserWaypoint = reader.getInt(6) == 1;
        waypoint.setTitle(reader.getString(7).trim());
        waypoint.isStartWaypoint = reader.getInt(8) == 1;

        if (getFull) {
            waypoint.setClue(reader.getString(10));
            waypoint.setDescription(reader.getString(9));
            waypoint.setCheckSum(createCheckSum(waypoint));
        }
        return waypoint;
    }

    private int createCheckSum(Waypoint waypoint) {
        // for Replication
        String sCheckSum = waypoint.getWaypointCode();
        sCheckSum += UnitFormatter.FormatLatitudeDM(waypoint.getLatitude());
        sCheckSum += UnitFormatter.FormatLongitudeDM(waypoint.getLongitude());
        sCheckSum += waypoint.getDescription();
        sCheckSum += waypoint.waypointType.ordinal();
        sCheckSum += waypoint.getClue();
        sCheckSum += waypoint.getTitle();
        if (waypoint.isStartWaypoint)
            sCheckSum += "1";
        return (int) SDBM_Hash.sdbm(sCheckSum);
    }

    public void deleteFromDatabase(Waypoint waypoint) {
        Replication.WaypointDelete(waypoint.geoCacheId, 0, 1, waypoint.getWaypointCode());
        try {
            CBDB.getInstance().delete("Waypoint", "GcCode='" + waypoint.getWaypointCode() + "'", null);
        } catch (Exception ignored) {
        }
    }

    public void writeImportToDatabase(Waypoint waypoint) {
        Parameters args = new Parameters();
        args.put("gccode", waypoint.getWaypointCode());
        args.put("cacheid", waypoint.geoCacheId);
        args.put("latitude", waypoint.getLatitude());
        args.put("longitude", waypoint.getLongitude());
        args.put("description", waypoint.getDescription());
        args.put("type", waypoint.waypointType.ordinal());
        args.put("syncexclude", waypoint.isSyncExcluded);
        args.put("userwaypoint", waypoint.isUserWaypoint);
        args.put("clue", waypoint.getClue());
        args.put("title", waypoint.getTitle());
        args.put("isStart", waypoint.isStartWaypoint);

        try {
            CBDB.getInstance().insertWithConflictReplace("Waypoint", args);

            args = new Parameters();
            args.put("hasUserData", true);
            CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
        } catch (Exception ignored) {
        }
    }

    // Each geoCache should have only one Start-waypoint. Before set a new one, reset here
    public void resetStartWaypoint(Cache cache, Waypoint except) {
        for (int i = 0, n = cache.getWayPoints().size(); i < n; i++) {
            Waypoint wp = cache.getWayPoints().get(i);
            if (except == wp)
                continue;
            if (wp.isStartWaypoint) {
                wp.isStartWaypoint = false;
                Parameters args = new Parameters();
                args.put("isStart", false);
                try {
                    CBDB.getInstance().update("Waypoint", args, "CacheId=" + wp.geoCacheId + " and GcCode=\"" + wp.getWaypointCode() + "\"", null);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Delete all Logs without exist Cache
     */
    public void clearOrphanedWaypoints() {
        String SQL = "DELETE  FROM  Waypoint WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Waypoint.CacheId = c.Id)";
        CBDB.getInstance().execSQL(SQL);
    }

    /**
     * Returns a WaypointList from reading DB!
     *
     * @param geoCacheID ID of Cache
     * @param getFull    Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return ?
     */
    public CB_List<Waypoint> getWaypointsFromCacheID(Long geoCacheID, boolean getFull) {
        CB_List<Waypoint> wpList = new CB_List<>();
        long aktCacheID = -1;

        CoreCursor c = CBDB.getInstance().rawQuery((getFull ? SQL_WP_FULL : SQL_WP) + "  where CacheId = ?", new String[]{String.valueOf(geoCacheID)});
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Waypoint wp = getWaypoint(c, getFull);
                    if (wp.geoCacheId != aktCacheID) {
                        aktCacheID = wp.geoCacheId;
                        wpList = new CB_List<>();
                    }
                    wpList.add(wp);
                    c.moveToNext();
                }
            }
            c.close();
        }
        return wpList;
    }

    public String createFreeGcCode(String cacheGcCode) throws Exception {
        String suffix = cacheGcCode.substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.length(); i++)
            for (int j = 0; j < secondCharCandidates.length(); j++) {
                String gcCode = firstCharCandidates.charAt(i) + secondCharCandidates.substring(j, j + 1) + suffix;
                if (!waypointExists(gcCode))
                    return gcCode;
            }
        throw new Exception("All GcCodes are used! Should never happen!");
    }

    private boolean waypointExists(String gcCode) {
        CoreCursor c = CBDB.getInstance().rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]{gcCode});
        if (c != null) {
            if (c.getCount() > 0) {
                c.close();
                return true;
            }
            c.close();
        }
        return false;
    }

}

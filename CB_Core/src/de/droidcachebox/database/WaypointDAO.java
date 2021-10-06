package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;
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

    public void WriteToDatabase(Waypoint WP) {
        WriteToDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public void WriteToDatabase(Waypoint waypoint, boolean useReplication) {
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
            long count = CBDB.getInstance().sql.insert("Waypoint", args);
            if (count <= 0) {
                CBDB.getInstance().sql.update("Waypoint", args, "gccode=\"" + waypoint.getWaypointCode() + "\"", null);
            }
            if (waypoint.isUserWaypoint) {
                args = new Parameters();
                args.put("hasUserData", true);
                CBDB.getInstance().sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
            }
        } catch (Exception ignored) {
        }
    }

    public void UpdateDatabase(Waypoint WP) {
        UpdateDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public boolean UpdateDatabase(Waypoint WP, boolean useReplication) {
        boolean result = false;
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointChanged(WP.geoCacheId, WP.getCheckSum(), newCheckSum, WP.getWaypointCode());
        }
        if (newCheckSum != WP.getCheckSum()) {
            Parameters args = new Parameters();
            args.put("gccode", WP.getWaypointCode());
            args.put("cacheid", WP.geoCacheId);
            args.put("latitude", WP.getLatitude());
            args.put("longitude", WP.getLongitude());
            args.put("description", WP.getDescription());
            args.put("type", WP.waypointType.ordinal());
            args.put("syncexclude", WP.isSyncExcluded);
            args.put("userwaypoint", WP.isUserWaypoint);
            args.put("clue", WP.getClue());
            args.put("title", WP.getTitle());
            args.put("isStart", WP.isStartWaypoint);
            try {
                long count = CBDB.getInstance().sql.update("Waypoint", args, "CacheId=" + WP.geoCacheId + " and GcCode=\"" + WP.getWaypointCode() + "\"", null);
                if (count > 0)
                    result = true;
            } catch (Exception ignored) {
            }

            if (WP.isUserWaypoint) {
                args = new Parameters();
                args.put("hasUserData", true);
                try {
                    CBDB.getInstance().sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.geoCacheId)});
                } catch (Exception exc) {
                    return result;
                }
            }
            WP.setCheckSum(newCheckSum);
        }
        return result;
    }

    /**
     * Create Waypoint Object from Reader.
     *
     * @param reader ?
     * @param full   Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return ?
     */
    public Waypoint getWaypoint(CoreCursor reader, boolean full) {
        Waypoint WP;

        WP = new Waypoint(full);

        WP.setWaypointCode(reader.getString(0));
        WP.geoCacheId = reader.getLong(1);
        double latitude = reader.getDouble(2);
        double longitude = reader.getDouble(3);
        WP.setCoordinate(new Coordinate(latitude, longitude));
        WP.waypointType = GeoCacheType.values()[reader.getShort(4)];
        WP.isSyncExcluded = reader.getInt(5) == 1;
        WP.isUserWaypoint = reader.getInt(6) == 1;
        WP.setTitle(reader.getString(7).trim());
        WP.isStartWaypoint = reader.getInt(8) == 1;

        if (full) {
            WP.setClue(reader.getString(10));
            WP.setDescription(reader.getString(9));
            WP.setCheckSum(createCheckSum(WP));
        }
        return WP;
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
            CBDB.getInstance().sql.delete("Waypoint", "GcCode='" + waypoint.getWaypointCode() + "'", null);
        } catch (Exception ignored) {
        }
    }

    public void WriteImportToDatabase(Waypoint waypoint) {
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
            CBDB.getInstance().sql.insertWithConflictReplace("Waypoint", args);

            args = new Parameters();
            args.put("hasUserData", true);
            CBDB.getInstance().sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
        } catch (Exception ignored) {
        }
    }

    // Each geoCache should have only one Start-waypoint. Before set a new one, reset here
    public void ResetStartWaypoint(Cache cache, Waypoint except) {
        for (int i = 0, n = cache.getWayPoints().size(); i < n; i++) {
            Waypoint wp = cache.getWayPoints().get(i);
            if (except == wp)
                continue;
            if (wp.isStartWaypoint) {
                wp.isStartWaypoint = false;
                Parameters args = new Parameters();
                args.put("isStart", false);
                try {
                    CBDB.getInstance().sql.update("Waypoint", args, "CacheId=" + wp.geoCacheId + " and GcCode=\"" + wp.getWaypointCode() + "\"", null);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Delete all Logs without exist Cache
     */
    public void ClearOrphanedWaypoints() {
        String SQL = "DELETE  FROM  Waypoint WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Waypoint.CacheId = c.Id)";
        CBDB.getInstance().sql.execSQL(SQL);
    }

    /**
     * Returns a WaypointList from reading DB!
     *
     * @param CacheID ID of Cache
     * @param Full    Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return ?
     */
    public CB_List<Waypoint> getWaypointsFromCacheID(Long CacheID, boolean Full) {
        CB_List<Waypoint> wpList = new CB_List<>();
        long aktCacheID = -1;

        CoreCursor reader = CBDB.getInstance().sql.rawQuery((Full ? SQL_WP_FULL : SQL_WP) + "  where CacheId = ?", new String[]{String.valueOf(CacheID)});
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Waypoint wp = getWaypoint(reader, Full);
            if (wp.geoCacheId != aktCacheID) {
                aktCacheID = wp.geoCacheId;
                wpList = new CB_List<>();

            }
            wpList.add(wp);
            reader.moveToNext();

        }
        reader.close();

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
        CoreCursor c = CBDB.getInstance().sql.rawQuery("select GcCode from Waypoint where GcCode=@gccode", new String[]{gcCode});
        {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                try {
                    c.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            c.close();
            return false;
        }
    }

}

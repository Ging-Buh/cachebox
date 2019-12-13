package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.UnitFormatter;

import java.util.Iterator;

public class WaypointDAO {

    public static final String SQL_WP = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart from Waypoint";
    public static final String SQL_WP_FULL = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart, Description, Clue from Waypoint";

    public void WriteToDatabase(Waypoint WP) {
        WriteToDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public void WriteToDatabase(Waypoint waypoint, boolean useReplication) {
        int newCheckSum = createCheckSum(waypoint);
        if (useReplication) {
            Replication.WaypointNew(waypoint.geoCacheId, waypoint.getCheckSum(), newCheckSum, waypoint.getGcCode());
        }
        Parameters args = new Parameters();
        args.put("gccode", waypoint.getGcCode());
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
            long count = Database.Data.sql.insert("Waypoint", args);
            if (count <= 0) {
                Database.Data.sql.update("Waypoint", args, "gccode=\"" + waypoint.getGcCode() + "\"", null);
            }
            if (waypoint.isUserWaypoint) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist!!!
                args = new Parameters();
                args.put("hasUserData", true);
                Database.Data.sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
            }
        } catch (Exception exc) {
            return;

        }
    }

    public boolean UpdateDatabase(Waypoint WP) {
        return UpdateDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public boolean UpdateDatabase(Waypoint WP, boolean useReplication) {
        boolean result = false;
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointChanged(WP.geoCacheId, WP.getCheckSum(), newCheckSum, WP.getGcCode());
        }
        if (newCheckSum != WP.getCheckSum()) {
            Parameters args = new Parameters();
            args.put("gccode", WP.getGcCode());
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
                long count = Database.Data.sql.update("Waypoint", args, "CacheId=" + WP.geoCacheId + " and GcCode=\"" + WP.getGcCode() + "\"", null);
                if (count > 0)
                    result = true;
            } catch (Exception exc) {
                result = false;

            }

            if (WP.isUserWaypoint) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist (z.B. über API)
                args = new Parameters();
                args.put("hasUserData", true);
                try {
                    Database.Data.sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.geoCacheId)});
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
     * @param reader
     * @param full   Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return
     */
    public Waypoint getWaypoint(CoreCursor reader, boolean full) {
        Waypoint WP = null;

        WP = new Waypoint(full);

        WP.setGcCode(reader.getString(0));
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
        String sCheckSum = waypoint.getGcCode();
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

    public void WriteImports(Iterator<Waypoint> waypointIterator, int waypointCount, ImporterProgress ip) {
        ip.setJobMax("WriteWaypointsToDB", waypointCount);
        while (waypointIterator.hasNext()) {
            Waypoint waypoint = waypointIterator.next();
            ip.ProgressInkrement("WriteWaypointsToDB", String.valueOf(waypoint.geoCacheId), false);
            try {
                WriteImportToDatabase(waypoint);
            } catch (Exception e) {

                e.printStackTrace();
            }

        }

    }

    public void WriteImportToDatabase(Waypoint waypoint) {
        Parameters args = new Parameters();
        args.put("gccode", waypoint.getGcCode());
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
            Database.Data.sql.insertWithConflictReplace("Waypoint", args);

            args = new Parameters();
            args.put("hasUserData", true);
            Database.Data.sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(waypoint.geoCacheId)});
        } catch (Exception exc) {
            return;

        }
    }

    // Hier wird überprüft, ob für diesen Cache ein Start-Waypoint existiert und dieser in diesem Fall zurückgesetzt
    // Damit kann bei der Definition eines neuen Start-Waypoints vorher der alte entfernt werden damit sichergestellt ist dass ein Cache nur
    // 1 Start-Waypoint hat
    public void ResetStartWaypoint(Cache cache, Waypoint except) {
        for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
            Waypoint wp = cache.waypoints.get(i);
            if (except == wp)
                continue;
            if (wp.isStartWaypoint) {
                wp.isStartWaypoint = false;
                Parameters args = new Parameters();
                args.put("isStart", false);
                try {
                    long count = Database.Data.sql.update("Waypoint", args, "CacheId=" + wp.geoCacheId + " and GcCode=\"" + wp.getGcCode() + "\"", null);

                } catch (Exception exc) {

                }
            }
        }
    }

    /**
     * Delete all Logs without exist Cache
     */
    public void ClearOrphanedWaypoints() {
        String SQL = "DELETE  FROM  Waypoint WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Waypoint.CacheId = c.Id)";
        Database.Data.sql.execSQL(SQL);
    }

    /**
     * Returns a WaypointList from reading DB!
     *
     * @param CacheID ID of Cache
     * @param Full    Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return
     */
    public CB_List<Waypoint> getWaypointsFromCacheID(Long CacheID, boolean Full) {
        CB_List<Waypoint> wpList = new CB_List<Waypoint>();
        long aktCacheID = -1;

        StringBuilder sqlState = new StringBuilder(Full ? SQL_WP_FULL : SQL_WP);
        sqlState.append("  where CacheId = ?");

        CoreCursor reader = Database.Data.sql.rawQuery(sqlState.toString(), new String[]{String.valueOf(CacheID)});
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Waypoint wp = getWaypoint(reader, Full);
            if (wp.geoCacheId != aktCacheID) {
                aktCacheID = wp.geoCacheId;
                wpList = new CB_List<Waypoint>();

            }
            wpList.add(wp);
            reader.moveToNext();

        }
        reader.close();

        return wpList;
    }

}

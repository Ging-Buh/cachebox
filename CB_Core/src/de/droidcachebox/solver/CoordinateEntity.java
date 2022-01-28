package de.droidcachebox.solver;

import java.util.ArrayList;

import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;

public class CoordinateEntity extends Entity {

    private final String gcCode;

    public CoordinateEntity(SolverLines solverLines, int id, String gcCode) {
        super(solverLines, id);
        this.gcCode = gcCode;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
    }

    private Coordinate loadFromDB(String table) {
        CoreCursor reader = CBDB.getInstance().rawQuery("select GcCode, Latitude, Longitude from " + table + " where GcCode = \"" + gcCode + "\"", null);
        if (reader != null) {
            try {
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    String sGcCode = reader.getString(0).trim();
                    if (sGcCode.equalsIgnoreCase(gcCode)) {
                        return new CoordinateGPS(reader.getDouble(1), reader.getDouble(2));
                    }
                    reader.moveToNext();
                }
            } finally {
                reader.close();
            }
        }
        return null;
    }

    @Override
    public String calculate() {
        // Cache selCache = CB_UI.GlobalCore.getSelectedCache();
        Cache selCache = null;
        if (SolverLines.solverCacheInterface != null) {
            selCache = SolverLines.solverCacheInterface.globalCoreGetSelectedCache();
        }
        Coordinate coord = null;
        if (selCache != null)
        // In 99,9% der Fälle dürfte der Wegpunkt zum aktuellen Cache gehören
        {
            if (selCache.getGeoCacheCode().equalsIgnoreCase(gcCode)) {
                coord = selCache.getCoordinate();
            } else {
                for (int i = 0, n = selCache.getWayPoints().size(); i < n; i++) {
                    Waypoint wp = selCache.getWayPoints().get(i);
                    if (wp.getWaypointCode().equalsIgnoreCase(gcCode)) {
                        coord = wp.getCoordinate();
                        break;
                    }
                }
            }
        }
        if (coord == null)
            // gesuchten Waypoint nicht im aktuellen Cache gefunden, jetzt alle Caches mit den passenden GC/OC etc. Code suchen
            coord = loadFromDB("Caches");
        if (coord == null)
            // gesuchter Waypoint ist kein Cache-Waypoint, jetzt in Waypoint-Tabelle danach suchen
            coord = loadFromDB("Waypoint");
        if (coord == null)
            return Translation.get("CacheOrWaypointNotFound", gcCode);
        else
            return coord.formatCoordinate();
    }

    public String setCoordinate(String sCoord) {
        if (SolverLines.isError(sCoord))
            return sCoord;
        Coordinate coord;
        try {
            coord = new CoordinateGPS(sCoord);
        } catch (Exception e) {
            return Translation.get("InvalidCoordinate", "SetCoordinate", sCoord);
        }
        if (!coord.isValid())
            return Translation.get("InvalidCoordinate", "SetCoordinate", sCoord);
        WaypointDAO waypointDAO = WaypointDAO.getInstance();
        Waypoint dbWaypoint;
        // Suchen, ob dieser Waypoint bereits vorhanden ist.
        CoreCursor reader = CBDB.getInstance().rawQuery(WaypointDAO.SQL_WP_FULL + " where GcCode = \"" + this.gcCode + "\"", null);
        if (reader == null) {
            return Translation.get("CacheOrWaypointNotFound", gcCode);
        } else {
            try {
                if (reader.getCount() > 0) {
                    reader.moveToFirst();
                    dbWaypoint = waypointDAO.getWaypoint(reader, true);
                } else {
                    return Translation.get("CacheOrWaypointNotFound", gcCode);
                }
            } finally {
                reader.close();
            }
        }
        try {
            // if ((CB_UI.GlobalCore.getSelectedCache() == null) || (CB_UI.GlobalCore.getSelectedCache().Id != dbWaypoint.CacheId))
            if (SolverLines.solverCacheInterface != null) {
                if ((SolverLines.solverCacheInterface.globalCoreGetSelectedCache() == null) || (SolverLines.solverCacheInterface.globalCoreGetSelectedCache().generatedId != dbWaypoint.geoCacheId)) {
                    // Zuweisung soll an einen Waypoint eines anderen als dem aktuellen Cache gemacht werden.
                    // Vermutlich Tippfehler daher Update verhindern. Modale Dialoge gehen in Android nicht
                    CachesDAO cachesDAO = new CachesDAO();
                    Cache cache = cachesDAO.getFromDbByCacheId(dbWaypoint.geoCacheId);
                    // String sFmt = "Change Coordinates of a waypoint which does not belong to the actual Cache?\n";
                    // sFmt += "Cache: [%s]\nWaypoint: [%s]\nCoordinates: [%s]";
                    // String s = String.format(sFmt, cache.Name, waypoint.Title, coord.formatCoordinate());
                    // MessageBox(s, "Solver", MessageBoxButton.YesNo, MessageBoxIcon.Question, DiffCac//heListener);
                    return Translation.get("solverErrDiffCache", coord.formatCoordinate(), dbWaypoint.getTitle(), cache.getGeoCacheName());
                }
            }
            dbWaypoint.setCoordinate(new Coordinate(coord));

            waypointDAO.updateDatabase(dbWaypoint);

            // evtl. bereits geladenen Waypoint aktualisieren
            Cache cacheFromCacheList;
            synchronized (CBDB.getInstance().cacheList) {
                cacheFromCacheList = CBDB.getInstance().cacheList.getCacheByIdFromCacheList(dbWaypoint.geoCacheId);
            }
            if (cacheFromCacheList == null)
                cacheFromCacheList = SolverLines.solverCacheInterface.globalCoreGetSelectedCache();
            if (cacheFromCacheList != null) {
                for (int i = 0, n = cacheFromCacheList.getWayPoints().size(); i < n; i++) {
                    Waypoint wp = cacheFromCacheList.getWayPoints().get(i);
                    if (wp.getWaypointCode().equalsIgnoreCase(this.gcCode)) {
                        wp.setCoordinate(new Coordinate(coord));
                        break;
                    }
                }
                if (SolverLines.solverCacheInterface != null) {
                    if (SolverLines.solverCacheInterface.globalCoreGetSelectedCache().generatedId == cacheFromCacheList.generatedId) {
                        if (SolverLines.solverCacheInterface.globalCoreGetSelectedWaypoint() == null) {
                            SolverLines.solverCacheInterface.globalCoreSetSelectedCache(SolverLines.solverCacheInterface.globalCoreGetSelectedCache());
                        } else {
                            SolverLines.solverCacheInterface.globalCoreSetSelectedWaypoint(SolverLines.solverCacheInterface.globalCoreGetSelectedCache(), SolverLines.solverCacheInterface.globalCoreGetSelectedWaypoint());
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Translation.get("CacheOrWaypointNotFound", this.gcCode);
        }
        return gcCode + "=" + coord.formatCoordinate();
    }

    @Override
    public String toString() {
        return "Gc" + entityId + ":(" + gcCode + ")";
    }

}

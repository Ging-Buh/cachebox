package de.droidcachebox.solver;

import java.util.ArrayList;

import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;

public class CoordinateEntity extends Entity {

    private String gcCode = "";

    public CoordinateEntity(Solver solver, int id, String gcCode) {
        super(solver, id);
        this.gcCode = gcCode;
    }

    @Override
    public void GetAllEntities(ArrayList<Entity> list) {
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest) {
    }

    private Coordinate LoadFromDB(String sql) {
        CoreCursor reader = CBDB.getInstance().rawQuery(sql, null);
        try {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                String sGcCode = reader.getString(0).trim();
                if (sGcCode.equalsIgnoreCase(this.gcCode)) { // gefunden. Suche abbrechen
                    return new CoordinateGPS(reader.getDouble(1), reader.getDouble(2));
                }
                reader.moveToNext();
            }
        } finally {
            reader.close();
        }

        return null;
    }

    @Override
    public String Berechne() {
        // Cache selCache = CB_UI.GlobalCore.getSelectedCache();
        Cache selCache = null;
        if (Solver.solverCacheInterface != null) {
            selCache = Solver.solverCacheInterface.sciGetSelectedCache();
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
            coord = LoadFromDB("select GcCode, Latitude, Longitude from Caches where GcCode = \"" + this.gcCode + "\"");
        if (coord == null)
            // gesuchter Waypoint ist kein Cache-Waypoint, jetzt in Waypoint-Tabelle danach suchen
            coord = LoadFromDB("select GcCode, Latitude, Longitude from Waypoint where GcCode = \"" + this.gcCode + "\"");
        if (coord == null)
            return Translation.get("CacheOrWaypointNotFound", gcCode);
        else
            return coord.formatCoordinate();
    }

    public String SetCoordinate(String sCoord) {
        if (Solver.isError(sCoord))
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
        try {
            reader.moveToFirst();
            if (reader.isAfterLast())
                return Translation.get("CacheOrWaypointNotFound", this.gcCode);
            dbWaypoint = (Waypoint) waypointDAO.getWaypoint(reader, true);
        } finally {
            reader.close();
        }
        try {
            // if ((CB_UI.GlobalCore.getSelectedCache() == null) || (CB_UI.GlobalCore.getSelectedCache().Id != dbWaypoint.CacheId))
            if (Solver.solverCacheInterface != null) {
                if ((Solver.solverCacheInterface.sciGetSelectedCache() == null) || (Solver.solverCacheInterface.sciGetSelectedCache().generatedId != dbWaypoint.geoCacheId)) {
                    // Zuweisung soll an einen Waypoint eines anderen als dem aktuellen Cache gemacht werden.
                    // Vermutlich Tippfehler daher Update verhindern. Modale Dialoge gehen in Android nicht
                    CacheDAO cacheDAO = CacheDAO.getInstance();
                    Cache cache = cacheDAO.getFromDbByCacheId(dbWaypoint.geoCacheId);
                    // String sFmt = "Change Coordinates of a waypoint which does not belong to the actual Cache?\n";
                    // sFmt += "Cache: [%s]\nWaypoint: [%s]\nCoordinates: [%s]";
                    // String s = String.format(sFmt, cache.Name, waypoint.Title, coord.formatCoordinate());
                    // MessageBox(s, "Solver", MessageBoxButton.YesNo, MessageBoxIcon.Question, DiffCac//heListener);
                    return Translation.get("solverErrDiffCache", coord.formatCoordinate(), dbWaypoint.getTitle(), cache.getGeoCacheName());
                }
            }
            dbWaypoint.setCoordinate(new Coordinate(coord));

            waypointDAO.UpdateDatabase(dbWaypoint);

            // evtl. bereits geladenen Waypoint aktualisieren
            Cache cacheFromCacheList;
            synchronized (CBDB.getInstance().cacheList) {
                cacheFromCacheList = CBDB.getInstance().cacheList.getCacheByIdFromCacheList(dbWaypoint.geoCacheId);
            }
            cacheFromCacheList = Solver.solverCacheInterface.sciGetSelectedCache();
            if (cacheFromCacheList != null) {
                for (int i = 0, n = cacheFromCacheList.getWayPoints().size(); i < n; i++) {
                    Waypoint wp = cacheFromCacheList.getWayPoints().get(i);
                    if (wp.getWaypointCode().equalsIgnoreCase(this.gcCode)) {
                        wp.setCoordinate(new Coordinate(coord));
                        break;
                    }
                }
                if (Solver.solverCacheInterface != null) {
                    if (Solver.solverCacheInterface.sciGetSelectedCache().generatedId == cacheFromCacheList.generatedId) {
                        if (Solver.solverCacheInterface.sciGetSelectedWaypoint() == null) {
                            Solver.solverCacheInterface.sciSetSelectedCache(Solver.solverCacheInterface.sciGetSelectedCache());
                        } else {
                            Solver.solverCacheInterface.sciSetSelectedWaypoint(Solver.solverCacheInterface.sciGetSelectedCache(), Solver.solverCacheInterface.sciGetSelectedWaypoint());
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
    public String ToString() {
        return "Gc" + Id + ":(" + gcCode + ")";
    }

}

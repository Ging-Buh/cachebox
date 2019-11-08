package de.droidcachebox.solver;

import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;

public interface SolverCacheInterface {
    Cache sciGetSelectedCache();

    Waypoint sciGetSelectedWaypoint();

    void sciSetSelectedCache(Cache cache);

    void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint);
}

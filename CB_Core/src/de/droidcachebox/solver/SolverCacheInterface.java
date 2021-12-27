package de.droidcachebox.solver;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;

public interface SolverCacheInterface {
    Cache sciGetSelectedCache();

    Waypoint sciGetSelectedWaypoint();

    void sciSetSelectedCache(Cache cache);

    void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint);
}

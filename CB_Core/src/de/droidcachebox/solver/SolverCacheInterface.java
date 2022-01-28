package de.droidcachebox.solver;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;

public interface SolverCacheInterface {
    Cache globalCoreGetSelectedCache();

    Waypoint globalCoreGetSelectedWaypoint();

    void globalCoreSetSelectedCache(Cache cache);

    void globalCoreSetSelectedWaypoint(Cache cache, Waypoint waypoint);
}

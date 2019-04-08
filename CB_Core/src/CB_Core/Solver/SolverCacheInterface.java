package CB_Core.Solver;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SolverCacheInterface {
    Cache sciGetSelectedCache();

    Waypoint sciGetSelectedWaypoint();

    void sciSetSelectedCache(Cache cache);

    void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint);
}

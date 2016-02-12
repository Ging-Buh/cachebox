package CB_Core.Solver;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public interface SolverCacheInterface {
	public Cache sciGetSelectedCache();

	public Waypoint sciGetSelectedWaypoint();

	public void sciSetSelectedCache(Cache cache);

	public void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint);
}

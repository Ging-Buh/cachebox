package cb_server.Events;

import java.util.ArrayList;

import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverCacheInterface;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class SelectedCacheChangedEventList implements SolverCacheInterface {
	public static ArrayList<SelectedCacheChangedEventListner> list = new ArrayList<SelectedCacheChangedEventListner>();
	// hier werden der aktuell ausgewählte cache und wp gespeichert
	private static Cache selectedCache;
	private static Waypoint selectedWaypoint;

	public SelectedCacheChangedEventList() {
		Solver.solverCacheInterface = this;
	}

	public static void Add(SelectedCacheChangedEventListner event) {
		synchronized (list) {
			if (!list.contains(event))
				list.add(event);
		}
	}

	public static void Remove(SelectedCacheChangedEventListner event) {
		synchronized (list) {
			list.remove(event);
		}
	}

	public static void Call(Cache cache, Waypoint waypoint) {
		synchronized (list) {
			for (SelectedCacheChangedEventListner event : list) {
				if (event == null)
					continue;
				// remove Detail Info from old selectedCache
				if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.detail != null)) {
					selectedCache.deleteDetail(false);
				}

				selectedCache = cache;
				selectedWaypoint = waypoint;

				// load Detail Info if not available
				if (selectedCache.detail == null) {
					selectedCache.loadDetail();
				}

				event.SelectedCacheChangedEvent(selectedCache, selectedWaypoint, false, false);
			}
		}

	}

	// SelectedCache has not changed but information of cache
	public static void CacheChanged(Cache cache, Waypoint waypoint) {
		synchronized (list) {
			for (SelectedCacheChangedEventListner event : list) {
				if (event == null)
					continue;
				event.SelectedCacheChangedEvent(selectedCache, selectedWaypoint, true, false);
			}
		}

	}

	// SelectedWaypoint has not changed but information of cache
	public static void WaypointChanged(Cache cache, Waypoint waypoint) {
		synchronized (list) {
			for (SelectedCacheChangedEventListner event : list) {
				if (event == null)
					continue;
				event.SelectedCacheChangedEvent(selectedCache, selectedWaypoint, false, true);
			}
		}

	}

	public static Cache getCache() {
		return selectedCache;
	}

	public static Waypoint getWaypoint() {
		return selectedWaypoint;
	}

	@Override
	public Cache sciGetSelectedCache() {
		return selectedCache;
	}

	@Override
	public Waypoint sciGetSelectedWaypoint() {
		return selectedWaypoint;
	}

	@Override
	public void sciSetSelectedCache(Cache cache) {
		Call(cache, null);
	}

	@Override
	public void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint) {
		Call(cache, waypoint);
	}
}

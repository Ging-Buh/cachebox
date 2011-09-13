package CB_Core.Import;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class ImportHandler implements IImportHandler {

	List<Cache> caches = new LinkedList<Cache>();
	List<LogEntry> logs = new LinkedList<LogEntry>();
	List<Waypoint> waypoints = new LinkedList<Waypoint>();

	public void handleCache(Cache cache) {
		caches.add(cache);
	}

	public Iterator<Cache> getCacheIterator() {
		return caches.iterator();
	}

	public void handleLog(LogEntry log) {
		logs.add(log);
	}

	public Iterator<LogEntry> getLogIterator() {
		return logs.iterator();
	}

	public Iterator<Waypoint> getWaypointIterator() {
		return waypoints.iterator();
	}

	public int CacheCount() {
		return caches.size();
	}

	public int LogCount() {
		return logs.size();
	}

	public void handleWaypoint(Waypoint waypoint) {
		waypoints.add(waypoint);

	}

	public int WaypointCount() {
		return waypoints.size();
	}
}

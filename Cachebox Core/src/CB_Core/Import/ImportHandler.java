package CB_Core.Import;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class ImportHandler implements IImportHandler {

	CacheDAO cacheDAO = new CacheDAO();
	LogDAO logDAO = new LogDAO();
	WaypointDAO waypointDAO = new WaypointDAO();
	
	public Integer cacheCount = 0;
	public Integer logCount = 0;
	public Integer waypointCount = 0;

	public void handleCache(Cache cache) {
		
		if (cacheDAO.cacheExists(cache.Id))
		{
			cacheDAO.UpdateDatabase(cache);
		}
		else
		{
			cacheDAO.WriteToDatabase(cache);
		}
		cacheCount++;
	}

	public void handleLog(LogEntry log) {
		logDAO.WriteToDatabase(log);
		logCount++;
	}

	public void handleWaypoint(Waypoint waypoint) {
		waypointDAO.WriteImportToDatabase(waypoint);
		waypointCount++;
	}
}

package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.Types.Waypoint;

public class DeleteSelectedCache
{
	public static void Execute()
	{
		// Images
		ArrayList<String> GcCodeList = new ArrayList<String>();
		GcCodeList.add(GlobalCore.getSelectedCache().GcCode);
		CacheListDAO dao = new CacheListDAO();
		dao.delCacheImages(GcCodeList);
		GcCodeList = null;
		dao = null;
		// Waypoints
		for (Waypoint wp : GlobalCore.getSelectedCache().waypoints)
		{
			Database.DeleteFromDatabase(wp);
		}
		// Cache
		Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().GcCode + "'", null);
		// Logs
		LogDAO logdao = new LogDAO();
		logdao.ClearOrphanedLogs();
		logdao = null;
		// compact DB
		Database.Data.execSQL("vacuum");
		// Filter Liste neu aufbauen
		EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);
		GlobalCore.setSelectedCache(null);
	}
}

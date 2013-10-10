package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Waypoint;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Utils.Log.Logger;

public class DeleteSelectedCache
{
	public static void Execute()
	{
		// Images
		Logger.DEBUG("Delete Images");
		ArrayList<String> GcCodeList = new ArrayList<String>();
		GcCodeList.add(GlobalCore.getSelectedCache().GcCode);
		CacheListDAO dao = new CacheListDAO();
		dao.delCacheImages(GcCodeList, Config.settings.SpoilerFolder.getValue(), Config.settings.SpoilerFolderLocal.getValue(),
				Config.settings.DescriptionImageFolder.getValue(), Config.settings.DescriptionImageFolderLocal.getValue());
		GcCodeList = null;
		dao = null;
		// Waypoints
		Logger.DEBUG("Delete Waypoints");
		for (Waypoint wp : GlobalCore.getSelectedCache().waypoints)
		{
			Database.DeleteFromDatabase(wp);
		}
		// Cache
		Logger.DEBUG("Delete Cache " + GlobalCore.getSelectedCache().GcCode);
		Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().GcCode + "'", null);
		// Logs
		Logger.DEBUG("Delete Logs");
		LogDAO logdao = new LogDAO();
		logdao.ClearOrphanedLogs();
		logdao = null;
		// compact DB hangs : commented out
		// Logger.DEBUG("Delete compact DB");
		// Database.Data.execSQL("vacuum");
		// Filter Liste neu aufbauen oder gibt es eine schnellere M�glichkeit?
		Logger.DEBUG("Execute LastFilter");
		EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);
		Logger.DEBUG("unselect Cache");
		GlobalCore.setSelectedCache(null);
		Logger.DEBUG("Rebuild View");
		CachListChangedEventList.Call();
	}
}

package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.FilterProperties;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Waypoint;
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
		GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
		CacheListDAO dao = new CacheListDAO();
		dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
		GcCodeList = null;
		dao = null;
		// Waypoints
		Logger.DEBUG("Delete Waypoints");
		for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++)
		{
			Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
			Database.DeleteFromDatabase(wp);
		}
		// Cache
		Logger.DEBUG("Delete Cache " + GlobalCore.getSelectedCache().getGcCode());
		Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);
		// Logs
		Logger.DEBUG("Delete Logs");
		LogDAO logdao = new LogDAO();
		logdao.ClearOrphanedLogs();
		logdao = null;
		// compact DB hangs : commented out
		// Logger.DEBUG("Delete compact DB");
		// Database.Data.execSQL("vacuum");
		// Filter Liste neu aufbauen oder gibt es eine schnellere Möglichkeit?
		Logger.DEBUG("Execute LastFilter");
		EditFilterSettings.ApplyFilter(FilterProperties.LastFilter);
		Logger.DEBUG("unselect Cache");
		GlobalCore.setSelectedCache(null);
		Logger.DEBUG("Rebuild View");
		CachListChangedEventList.Call();
	}
}

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
import CB_UI.Tag;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;

import com.badlogic.gdx.Gdx;

public class DeleteSelectedCache
{
	public static void Execute()
	{
		// Images
		Gdx.app.debug(Tag.TAG, "Delete Images");
		ArrayList<String> GcCodeList = new ArrayList<String>();
		GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
		CacheListDAO dao = new CacheListDAO();
		dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
		GcCodeList = null;
		dao = null;
		// Waypoints
		Gdx.app.debug(Tag.TAG, "Delete Waypoints");
		for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++)
		{
			Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
			Database.DeleteFromDatabase(wp);
		}
		// Cache
		Gdx.app.debug(Tag.TAG, "Delete Cache " + GlobalCore.getSelectedCache().getGcCode());
		Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);
		// Logs
		Gdx.app.debug(Tag.TAG, "Delete Logs");
		LogDAO logdao = new LogDAO();
		logdao.ClearOrphanedLogs();
		logdao = null;
		// compact DB hangs : commented out
		// Gdx.app.debug(Tag.TAG,"Delete compact DB");
		// Database.Data.execSQL("vacuum");
		// Filter Liste neu aufbauen oder gibt es eine schnellere Möglichkeit?
		Gdx.app.debug(Tag.TAG, "Execute LastFilter");
		EditFilterSettings.ApplyFilter(FilterProperties.LastFilter);
		Gdx.app.debug(Tag.TAG, "unselect Cache");
		GlobalCore.setSelectedCache(null);
		Gdx.app.debug(Tag.TAG, "Rebuild View");
		CachListChangedEventList.Call();
	}
}

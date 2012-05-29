package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.FileList;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB.ReturnListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Categories;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_SelectDB_Dialog extends CB_ActionCommand
{

	public CB_Action_Show_SelectDB_Dialog()
	{
		super("manageDB", AID_SHOW_SELECT_DB_DIALOG);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(41);
	}

	@Override
	public void Execute()
	{
		FileList fileList = null;
		try
		{
			fileList = new FileList(Config.WorkPath, "DB3");
		}
		catch (Exception ex)
		{
			Logger.Error("slpash.Initial()", "search number of DB3 files", ex);
		}
		if ((fileList.size() > 1) && Config.settings.MultiDBAsk.getValue())
		{
			SelectDB selectDBDialog = new SelectDB(
					new CB_RectF(0, 0, GL_Listener.glListener.getWidth(), GL_Listener.glListener.getHeight()), "SelectDbDialog");
			selectDBDialog.setReturnListner(new ReturnListner()
			{
				@Override
				public void back()
				{
					returnFromSelectDB();
				}
			});
			GL_Listener.glListener.showDialog(selectDBDialog);
		}
	}

	private void returnFromSelectDB()
	{
		GL_Listener.glListener.closeDialog();
		Config.settings.ReadFromDB();

		GlobalCore.Categories = new Categories();
		GlobalCore.LastFilter = (Config.settings.Filter.getValue().length() == 0) ? new FilterProperties(FilterProperties.presets[0])
				: new FilterProperties(Config.settings.Filter.getValue());
		// filterSettings.LoadFilterProperties(GlobalCore.LastFilter);
		Database.Data.GPXFilenameUpdateCacheCount();

		String sqlWhere = GlobalCore.LastFilter.getSqlWhere();
		Logger.General("Main.ApplyFilter: " + sqlWhere);

		Database.Data.Query.clear();
		Database.Data.Close();
		Database.Data.StartUp(Config.settings.DatabasePath.getValue());

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

		GlobalCore.SelectedCache(null);
		GlobalCore.SelectedWaypoint(null, null);
		CachListChangedEventList.Call();

	}
}

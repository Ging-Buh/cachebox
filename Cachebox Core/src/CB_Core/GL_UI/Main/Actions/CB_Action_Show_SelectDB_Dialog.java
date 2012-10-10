package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.SelectDB;
import CB_Core.GL_UI.Activitys.SelectDB.ReturnListner;
import CB_Core.GL_UI.Controls.Dialogs.WaitDialog;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Cache;
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

	SelectDB selectDBDialog;

	@Override
	public void Execute()
	{
		selectDBDialog = new SelectDB(new CB_RectF(0, 0, GL.that.getWidth(), GL.that.getHeight()), "SelectDbDialog", false);
		selectDBDialog.setReturnListner(new ReturnListner()
		{
			@Override
			public void back()
			{
				returnFromSelectDB();
			}
		});
		selectDBDialog.show();

	}

	WaitDialog wd;

	private void returnFromSelectDB()
	{
		wd = WaitDialog.ShowWait("Load DB ...");

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Config.settings.ReadFromDB();

				GlobalCore.Categories = new Categories();
				GlobalCore.LastFilter = (Config.settings.Filter.getValue().length() == 0) ? new FilterProperties(
						FilterProperties.presets[0]) : new FilterProperties(Config.settings.Filter.getValue());
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

				// set last selected Cache
				String sGc = Config.settings.LastSelectedCache.getValue();
				if (sGc != null && !sGc.equals(""))
				{
					for (Cache c : Database.Data.Query)
					{
						if (c.GcCode.equalsIgnoreCase(sGc))
						{
							GlobalCore.SelectedCache(c);
							break;
						}
					}
				}
				wd.dismis();
			}
		});

		thread.start();

	}
}

package CB_Core.GL_UI.Main.Actions;

import java.io.File;

import CB_Core.Config;
import CB_Core.FileIO;
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
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Cache;
import CB_Core.Types.Categories;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_SelectDB_Dialog extends CB_ActionCommand
{

	public CB_Action_Show_SelectDB_Dialog()
	{
		super("manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
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

				// OwnRepository?
				String fs = GlobalCore.fs;
				String folder = Config.WorkPath + "/Repositories/"
						+ FileIO.GetFileNameWithoutExtension(Config.settings.DatabasePath.getValue()) + "/";
				folder = folder.replace("/", fs);
				File dir = new File(folder);
				if (dir.exists())
				{
					Config.settings.DescriptionImageFolder.setValue(folder + "Images");
					Config.settings.MapPackFolder.setValue(folder + "Maps");
					Config.settings.SpoilerFolder.setValue(folder + "Spoilers");
					Config.settings.TileCacheFolder.setValue(folder + "Cache");
					Config.AcceptChanges();
				}

				GlobalCore.Categories = new Categories();
				GlobalCore.LastFilter = (Config.settings.Filter.getValue().length() == 0) ? new FilterProperties(
						FilterProperties.presets[0]) : new FilterProperties(Config.settings.Filter.getValue());
				// filterSettings.LoadFilterProperties(GlobalCore.LastFilter);
				Database.Data.GPXFilenameUpdateCacheCount();

				String sqlWhere = GlobalCore.LastFilter.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				synchronized (Database.Data.Query)
				{
					Database.Data.Query.clear();
					Database.Data.Close();
					Database.Data.StartUp(Config.settings.DatabasePath.getValue());

					CacheListDAO cacheListDAO = new CacheListDAO();
					cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

					GlobalCore.setSelectedCache(null);
					GlobalCore.setSelectedWaypoint(null, null);
					CachListChangedEventList.Call();

					// set last selected Cache
					String sGc = Config.settings.LastSelectedCache.getValue();
					if (sGc != null && !sGc.equals(""))
					{
						for (Cache c : Database.Data.Query)
						{
							if (c.GcCode.equalsIgnoreCase(sGc))
							{
								GlobalCore.setSelectedCache(c);
								break;
							}
						}
					}
					// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
					if ((GlobalCore.getSelectedCache() == null) && (Database.Data.Query.size() > 0))
					{
						GlobalCore.setSelectedCache(Database.Data.Query.get(0));
					}
				}
				TabMainView.that.filterSetChanged();

				wd.dismis();
			}
		});

		thread.start();

	}
}

package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.CoreSettingsForward;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
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
		return SpriteCache.Icons.get(IconName.manageDB_41.ordinal());
	}

	SelectDB selectDBDialog;

	@Override
	public void Execute()
	{

		if (GlobalCore.getSelectedCache() != null)
		{
			// speichere selektierten Cache, da nicht alles �ber die SelectedCacheEventList l�uft
			Config.settings.LastSelectedCache.setValue(GlobalCore.getSelectedCache().GcCode);
			Config.AcceptChanges();
			Logger.DEBUG("LastSelectedCache = " + GlobalCore.getSelectedCache().GcCode);
		}

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

		Logger.DEBUG("\r\nSwitch DB");
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Database.Data.Query.clear();
				Database.Data.Close();
				Database.Data.StartUp(Config.settings.DatabasePath.getValue());

				Config.settings.ReadFromDB();

				CoreSettingsForward.Categories = new Categories();

				// zuerst den FilterString im neuen JSON Format laden versuchen
				String FilterString = Config.settings.FilterNew.getValue();
				if (FilterString.length() > 0)
				{
					GlobalCore.LastFilter = new FilterProperties(FilterString);
				}
				else
				{
					// Falls kein Neuer gefunden wurde -> das alte Format versuchen
					FilterString = Config.settings.Filter.getValue();
					GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0].toString())
							: new FilterProperties(FilterString);
				}
				// filterSettings.LoadFilterProperties(GlobalCore.LastFilter);

				String sqlWhere = GlobalCore.LastFilter.getSqlWhere(Config.settings.GcLogin.getValue());
				Database.Data.GPXFilenameUpdateCacheCount();

				synchronized (Database.Data.Query)
				{
					CacheListDAO cacheListDAO = new CacheListDAO();
					cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
				}

				// set selectedCache from lastselected Cache
				GlobalCore.setSelectedCache(null);
				String sGc = Config.settings.LastSelectedCache.getValue();
				if (sGc != null && !sGc.equals(""))
				{
					for (Cache c : Database.Data.Query)
					{
						if (c.GcCode.equalsIgnoreCase(sGc))
						{
							Logger.DEBUG("returnFromSelectDB:Set selectedCache to " + c.GcCode + " from lastSaved.");
							GlobalCore.setSelectedCache(c);
							break;
						}
					}
				}
				// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
				if ((GlobalCore.getSelectedCache() == null) && (Database.Data.Query.size() > 0))
				{
					Logger.DEBUG("Set selectedCache to " + Database.Data.Query.get(0).GcCode + " from firstInDB");
					GlobalCore.setSelectedCache(Database.Data.Query.get(0));
				}

				GlobalCore.setAutoResort(Config.settings.StartWithAutoSelect.getValue());

				CachListChangedEventList.Call();

				TabMainView.that.filterSetChanged();

				wd.dismis();
			}
		});

		thread.start();

	}
}
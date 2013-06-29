package CB_Core.GL_UI.Main.Actions;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.GroundspeakAPI.IChkRedyHandler;
import CB_Core.DB.Database;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditCache;
import CB_Core.GL_UI.Activitys.Import;
import CB_Core.GL_UI.Activitys.SyncActivity;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.PopUps.SearchDialog;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.CacheListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCacheList extends CB_Action_ShowView
{

	public CB_Action_ShowCacheList()
	{
		super("cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.cacheListView == null) && (tabMainView != null) && (tab != null)) TabMainView.cacheListView = new CacheListView(
				tab.getContentRec(), "CacheListView");

		if ((TabMainView.cacheListView != null) && (tab != null)) tab.ShowView(TabMainView.cacheListView);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.cacheListView;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.cacheList_7.ordinal());
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditCache editCache = null;
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_RESORT:
					synchronized (Database.Data.Query)
					{
						Database.Data.Query.Resort();
					}
					return true;
				case MenuID.MI_FilterSet:
					TabMainView.actionShowFilter.Execute();
					return true;
				case MenuID.MI_RESET_FILTER:
					GlobalCore.LastFilter = new FilterProperties(FilterProperties.presets[0].ToString());
					EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);
					return true;
				case MenuID.MI_SEARCH_LIST:

					if (SearchDialog.that == null)
					{
						new SearchDialog();
					}

					SearchDialog.that.showNotCloseAutomaticly();

					return true;
				case MenuID.MI_IMPORT:
					Import imp = new Import();
					imp.show();
					return true;
				case MenuID.MI_SYNC:
					SyncActivity sync = new SyncActivity();
					sync.show();
					return true;
				case MenuID.MI_MANAGE_DB:
					TabMainView.actionShowSelectDbDialog.Execute();
					return true;
				case MenuID.MI_AUTO_RESORT:
					GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
					if (GlobalCore.getAutoResort())
					{
						synchronized (Database.Data.Query)
						{
							Database.Data.Query.Resort();
						}
					}
					return true;
				case MenuID.MI_CHK_STATE_API:
					// First check API-Key with visual Feedback
					GroundspeakAPI.chkAPiLogInWithWaitDialog(new IChkRedyHandler()
					{

						@Override
						public void chekReady()
						{
							TimerTask tt = new TimerTask()
							{

								@Override
								public void run()
								{
									new CB_Action_Command_chkState().Execute();
								}
							};
							new CB_Action_Command_chkState().Execute();
							Timer t = new Timer();
							t.schedule(tt, 400);
						}
					});

					return true;

				case MenuID.MI_NEW_CACHE:
					if (editCache == null) editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
					editCache.Create();
					return true;

				case MenuID.AID_SHOW_DELETE_DIALOG:
					TabMainView.actionDelCaches.Execute();
					return true;
				}
				return false;
			}
		});

		String DBName = Config.settings.DatabasePath.getValue();
		try
		{
			int Pos = DBName.lastIndexOf("/");
			DBName = DBName.substring(Pos + 1);
			Pos = DBName.lastIndexOf(".");
			DBName = DBName.substring(0, Pos);
		}
		catch (Exception e)
		{
			DBName = "???";
		}

		MenuItem mi;
		cm.addItem(MenuID.MI_RESORT, "ResortList", SpriteCache.Icons.get(IconName.sort_39.ordinal()));
		cm.addItem(MenuID.MI_FilterSet, "filter", SpriteCache.Icons.get(IconName.filter_13.ordinal()));
		cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", SpriteCache.Icons.get(IconName.filter_13.ordinal()));
		cm.addItem(MenuID.MI_SEARCH_LIST, "search", SpriteCache.Icons.get(IconName.lupe_12.ordinal()));
		cm.addItem(MenuID.MI_IMPORT, "import", SpriteCache.Icons.get(IconName.import_40.ordinal()));
		if (SyncActivity.RELEASED) cm.addItem(MenuID.MI_SYNC, "sync", SpriteCache.Icons.get(IconName.import_40.ordinal()));
		mi = cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", SpriteCache.Icons.get(IconName.manageDB_41.ordinal()));
		mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
		mi.setCheckable(true);
		mi.setChecked(GlobalCore.getAutoResort());
		cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", SpriteCache.Icons.get(IconName.GCLive_35.ordinal()));
		cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", SpriteCache.Icons.get(IconName.addCache_57.ordinal()));
		cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", SpriteCache.Icons.get(IconName.delete_28.ordinal()));
		return cm;
	}

	public void setName(String newName)
	{
		this.name = newName;
	}

	public void setNameExtention(String newExtention)
	{
		this.nameExtention = newExtention;
	}
}

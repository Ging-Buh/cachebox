package CB_UI.GL_UI.Main.Actions;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.FilterProperties;
import CB_Core.DB.Database;
import CB_Core.Types.CacheWithWP;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.IChkRedyHandler;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Activitys.Import;
import CB_UI.GL_UI.Activitys.SearchOverPosition;
import CB_UI.GL_UI.Activitys.SyncActivity;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_Utils.StringH;

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
		return SpriteCacheBase.Icons.get(IconName.cacheList_7.ordinal());
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
						CacheWithWP nearstCacheWp = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(),
								new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

						GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
						if (CacheListView.that != null) CacheListView.that.setSelectedCacheVisible();
					}
					return true;
				case MenuID.MI_FilterSet:
					TabMainView.actionShowFilter.Execute();
					return true;
				case MenuID.MI_RESET_FILTER:
					GlobalCore.LastFilter = new FilterProperties(FilterProperties.presets[0].toString());
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
					// Import imp = new Import();
					// imp.show();
					showImportMenu();
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
							Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(),
									GlobalCore.getSelectedWaypoint()));
						}
					}
					return true;
				case MenuID.MI_CHK_STATE_API:
					// First check API-Key with visual Feedback
					GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler()
					{

						@Override
						public void chekReady(int MemberType)
						{
							TimerTask tt = new TimerTask()
							{

								@Override
								public void run()
								{
									new CB_Action_Command_chkState().Execute();
								}
							};
							Timer t = new Timer();
							t.schedule(tt, 100);
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
				case MenuID.MI_RpcGetExportList:

					// RpcClientCB rpc = new RpcClientCB();
					// RpcAnswer answer = rpc.getExportList();
					// if (answer != null)
					// {
					// GL_MsgBox.Show("RpcAntwort: " + answer.toString());
					// }
					//
					// ShowAPIImportList impApi = new ShowAPIImportList();
					// impApi.show();
					return true;
				}
				return false;
			}

		});

		String DBName = Config.DatabasePath.getValue();
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
		cm.addItem(MenuID.MI_RESORT, "ResortList", SpriteCacheBase.Icons.get(IconName.sort_39.ordinal()));
		cm.addItem(MenuID.MI_FilterSet, "filter", SpriteCacheBase.Icons.get(IconName.filter_13.ordinal()));
		cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", SpriteCacheBase.Icons.get(IconName.filter_13.ordinal()));
		cm.addItem(MenuID.MI_SEARCH_LIST, "search", SpriteCacheBase.Icons.get(IconName.lupe_12.ordinal()));
		cm.addItem(MenuID.MI_IMPORT, "import", SpriteCacheBase.Icons.get(IconName.import_40.ordinal()));
		if (SyncActivity.RELEASED) cm.addItem(MenuID.MI_SYNC, "sync", SpriteCacheBase.Icons.get(IconName.import_40.ordinal()));
		mi = cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", SpriteCacheBase.Icons.get(IconName.manageDB_41.ordinal()));
		mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
		mi.setCheckable(true);
		mi.setChecked(GlobalCore.getAutoResort());
		cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
		cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", SpriteCacheBase.Icons.get(IconName.addCache_57.ordinal()));
		cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", SpriteCacheBase.Icons.get(IconName.delete_28.ordinal()));
		if (!StringH.isEmpty(Config.CBS_IP.getValue())) cm.addItem(MenuID.MI_RpcGetExportList, "Import from CB-Server",
				SpriteCacheBase.Icons.get(IconName.list_21.ordinal()));

		return cm;
	}

	private void showImportMenu()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS)
				{
					showImportMenu_GS();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_CBS)
				{
					// Menü noch nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_CBS();
					import_CBS();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GPX)
				{
					// Menü nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_GPX();
					import_GPX();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GCV)
				{
					// Menü nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_GCV();
					import_GCV();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT)
				{
					Import imp = new Import();
					imp.show();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GS, "Groundspeak API");
		if (!StringH.isEmpty(Config.CBS_IP.getValue())) mi = icm.addItem(MenuID.MI_IMPORT_CBS, "CB-Server");
		mi = icm.addItem(MenuID.MI_IMPORT_GPX, "GPX");
		mi = icm.addItem(MenuID.MI_IMPORT_GCV, "GC Vote");
		mi = icm.addItem(MenuID.MI_IMPORT, "Import");
		icm.Show();
	}

	protected void showImportMenu_GCV()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GCV)
				{
					import_GCV();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GCV, "GC-Vote Import");

		icm.Show();
	}

	private void import_GCV()
	{
		Import imp = new Import(MenuID.MI_IMPORT_GCV);
		imp.show();
	}

	protected void showImportMenu_GPX()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GPX)
				{
					import_GPX();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GPX, "GPX Import");

		icm.Show();
	}

	private void import_GPX()
	{
		Import imp = new Import(MenuID.MI_IMPORT_GPX);
		imp.show();
	}

	protected void showImportMenu_CBS()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_CBS)
				{
					import_CBS();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_CBS, "CB-Server");

		icm.Show();
	}

	private void import_CBS()
	{
		Import imp = new Import(MenuID.MI_IMPORT_CBS);
		imp.show();
	}

	private void showImportMenu_GS()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS_PQ)
				{
					Import imp = new Import(MenuID.MI_IMPORT_GS_PQ);
					imp.show();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS_API)
				{
					new SearchOverPosition().show();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GS_PQ, "Pocket Query");
		mi = icm.addItem(MenuID.MI_IMPORT_GS_API, "Umkreissuche");

		icm.Show();
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

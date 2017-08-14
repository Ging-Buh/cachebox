package CB_UI.GL_UI.Main.Actions;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.CacheWithWP;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.IChkRedyHandler;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Activitys.SyncActivity;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

public class CB_Action_ShowCacheList extends CB_Action_ShowView {

	public CB_Action_ShowCacheList() {
		super("cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
	}

	@Override
	public void Execute() {
		if ((TabMainView.cacheListView == null) && (tabMainView != null) && (tab != null))
			TabMainView.cacheListView = new CacheListView(tab.getContentRec(), "CacheListView");

		if ((TabMainView.cacheListView != null) && (tab != null))
			tab.ShowView(TabMainView.cacheListView);
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.cacheListView;
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.cacheListIcon.name());
	}

	@Override
	public boolean hasContextMenu() {
		return true;
	}

	@Override
	public Menu getContextMenu() {
		Menu cm = new Menu("CacheListContextMenu");

		cm.addOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				EditCache editCache = null;
				switch (((MenuItem) v).getMenuItemId()) {
				case MenuID.MI_RESORT:
					synchronized (Database.Data.Query) {
						CacheWithWP nearstCacheWp = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

						if (nearstCacheWp != null)
							GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
						if (TabMainView.cacheListView != null)
							TabMainView.cacheListView.setSelectedCacheVisible();
					}
					return true;
				case MenuID.MI_FilterSet:
					TabMainView.actionShowFilter.Execute();
					return true;
				case MenuID.MI_RESET_FILTER:
					FilterInstances.setLastFilter(new FilterProperties());
					EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
					return true;
				case MenuID.MI_SEARCH_LIST:

					if (SearchDialog.that == null) {
						new SearchDialog();
					}

					SearchDialog.that.showNotCloseAutomaticly();

					return true;
				case MenuID.MI_IMPORT:
					TabMainView.actionShowImportMenu.CallExecute();
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
					if (GlobalCore.getAutoResort()) {
						synchronized (Database.Data.Query) {
							Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
						}
					}
					return true;
				case MenuID.MI_CHK_STATE_API:

					if (GroundspeakAPI.ApiLimit()) {
						GlobalCore.MsgDownloadLimit();
						return true;
					}

					// First check API-Key with visual Feedback
					GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler() {

						@Override
						public void checkReady(int MemberType) {
							TimerTask tt = new TimerTask() {

								@Override
								public void run() {
									new CB_Action_chkState().Execute();
								}
							};
							Timer t = new Timer();
							t.schedule(tt, 100);
						}
					});

					return true;

				case MenuID.MI_NEW_CACHE:
					if (editCache == null)
						editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
					editCache.create();
					return true;

				case MenuID.AID_SHOW_DELETE_DIALOG:
					TabMainView.actionDelCaches.Execute();
					return true;
				}
				return false;
			}

		});

		String DBName = Config.DatabaseName.getValue();
		try {
			int Pos = DBName.lastIndexOf(".");
			DBName = DBName.substring(0, Pos);
		} catch (Exception e) {
			DBName = "???";
		}

		MenuItem mi;
		cm.addItem(MenuID.MI_RESORT, "ResortList", Sprites.getSprite(IconName.sortIcon.name()));
		cm.addItem(MenuID.MI_FilterSet, "filter", Sprites.getSprite(IconName.filter.name()));
		cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", Sprites.getSprite(IconName.filter.name()));
		cm.addItem(MenuID.MI_SEARCH_LIST, "search", Sprites.getSprite(IconName.lupe.name()));
		cm.addItem(MenuID.MI_IMPORT, "importExport", Sprites.getSprite(IconName.importIcon.name()));
		if (SyncActivity.RELEASED)
			cm.addItem(MenuID.MI_SYNC, "sync", Sprites.getSprite(IconName.importIcon.name()));
		mi = cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", Sprites.getSprite(IconName.manageDb.name()));
		mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
		mi.setCheckable(true);
		mi.setChecked(GlobalCore.getAutoResort());
		cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", Sprites.getSprite(IconName.dayGcLiveIcon.name()));
		cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", Sprites.getSprite(IconName.addCacheIcon.name()));
		cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", Sprites.getSprite(IconName.DELETE.name()));

		return cm;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void setNameExtension(String newExtension) {
		this.nameExtension = newExtension;
	}
}

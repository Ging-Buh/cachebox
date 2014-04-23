package CB_UI.GL_UI.Menu;

import java.util.ArrayList;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchGC;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.DeleteSelectedCache;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Controls.Dialogs.HintDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CB_AllContextMenuHandler
{

	public static void showBtnCacheContextMenu()
	{

		boolean selectedCacheIsNull = (GlobalCore.getSelectedCache() == null);

		boolean selectedCacheIsNoGC = false;

		if (!selectedCacheIsNull)
		{
			selectedCacheIsNoGC = !GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
		}

		Menu icm = new Menu("BtnCacheContextMenu");
		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_RELOAD_CACHE_INFO, "ReloadCacheAPI", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);
		if (selectedCacheIsNoGC) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_WAYPOINTS, "Waypoints", SpriteCacheBase.BigIcons.get(16));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_SHOW_LOGS, "ShowLogs", SpriteCacheBase.Icons.get(IconName.list_21.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_HINT, "hint");
		if (mi != null)
		{
			boolean enabled = false;
			if (!selectedCacheIsNull && (GlobalCore.getSelectedCache().hasHint())) enabled = true;
			mi.setEnabled(enabled);
			mi.setIcon(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.hint_19.ordinal())));
		}

		mi = icm.addItem(MenuID.MI_SPOILER, "spoiler", SpriteCacheBase.Icons.get(IconName.images_22.ordinal()));
		if (selectedCacheIsNull)
		{
			mi.setEnabled(GlobalCore.getSelectedCache().SpoilerExists());
		}
		else
		{
			mi.setEnabled(false);
		}

		mi = icm.addItem(MenuID.MI_SOLVER, "Solver", SpriteCacheBase.Icons.get(IconName.solver_24.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		if (GlobalCore.JokerisOnline())
		{
			mi = icm.addItem(MenuID.MI_JOKER, "joker", SpriteCacheBase.Icons.get(IconName.jokerPhone_25.ordinal()));
			// Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5

			if (mi != null)
			{
				boolean enabled = false;
				if (GlobalCore.JokerisOnline()) enabled = true;

				mi.setEnabled(enabled);
			}

		}

		mi = icm.addItem(MenuID.MI_EDIT_CACHE, "MI_EDIT_CACHE");
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCacheBase.Icons.get(IconName.favorit_42.ordinal()));
		mi.setCheckable(true);
		if (selectedCacheIsNull) mi.setEnabled(false);
		else
			mi.setChecked(GlobalCore.getSelectedCache().isFavorite());

		mi = icm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");
		if (selectedCacheIsNull) mi.setEnabled(false);

		icm.Show();

	}

	static CancelWaitDialog wd;

	private static OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			EditCache editCache = null;
			CacheDAO dao = null;
			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_HINT:
				HintDialog.show();
				return true;

			case MenuID.MI_RELOAD_CACHE_INFO:
				wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
				{

					@Override
					public void isCanceld()
					{

					}
				}, new Runnable()
				{

					@Override
					public void run()
					{
						SearchGC searchC = new SearchGC(GlobalCore.getSelectedCache().getGcCode());

						searchC.number = 1;

						ArrayList<Cache> apiCaches = new ArrayList<Cache>();
						ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
						ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

						CB_UI.Api.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages,
								GlobalCore.getSelectedCache().GPXFilename_ID);

						try
						{
							GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}

						// Reload result from DB
						synchronized (Database.Data.Query)
						{
							String sqlWhere = GlobalCore.LastFilter.getSqlWhere(Config.GcLogin.getValue());
							CacheListDAO cacheListDAO = new CacheListDAO();
							cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
						}

						CachListChangedEventList.Call();

						CachListChangedEventList.Call();
						wd.close();
					}
				});

				return true;

			case MenuID.MI_WAYPOINTS:
				if (TabMainView.actionShowWaypointView != null) TabMainView.actionShowWaypointView.Execute();
				return true;

			case MenuID.MI_SHOW_LOGS:
				if (TabMainView.actionShowLogView != null) TabMainView.actionShowLogView.Execute();
				return true;

			case MenuID.MI_SPOILER:
				if (TabMainView.actionShowSpoilerView != null) TabMainView.actionShowSpoilerView.Execute();
				return true;

			case MenuID.MI_SOLVER:
				if (TabMainView.actionShowSolverView != null) TabMainView.actionShowSolverView.Execute();
				return true;

			case MenuID.MI_JOKER:
				if (TabMainView.actionShowJokerView != null) TabMainView.actionShowJokerView.Execute();
				return true;

			case MenuID.MI_EDIT_CACHE:
				if (editCache == null) editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
				editCache.Update(GlobalCore.getSelectedCache());
				return true;

			case MenuID.MI_FAVORIT:
				if (GlobalCore.getSelectedCache() != null)
				{
					GlobalCore.getSelectedCache().setFavorit(!GlobalCore.getSelectedCache().isFavorite());
					if (dao == null) dao = new CacheDAO();
					dao.UpdateDatabase(GlobalCore.getSelectedCache());
					CachListChangedEventList.Call();
				}
				return true;

			case MenuID.MI_DELETE_CACHE:
				DeleteSelectedCache.Execute();
				return true;

			default:
				return false;

			}

		}
	};

}

package CB_Core.GL_UI.Menu;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.DeleteSelectedCache;
import CB_Core.GL_UI.Activitys.EditCache;
import CB_Core.GL_UI.Controls.Dialogs.HintDialog;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowActivity;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CB_AllContextMenuHandler
{

	public static void showBtnCacheContextMenu()
	{

		boolean selectedCacheIsNull = (GlobalCore.getSelectedCache() == null);

		Menu icm = new Menu("BtnCacheContextMenu");
		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_RELOAD_CACHE_INFO, "ReloadCacheAPI", SpriteCache.Icons.get(IconName.GCLive_35.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_WAYPOINTS, "Waypoints", SpriteCache.BigIcons.get(IconName.autoSelectOff_16.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_SHOW_LOGS, "ShowLogs", SpriteCache.Icons.get(IconName.list_21.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_HINT, "hint");
		if (mi != null)
		{
			boolean enabled = false;
			if (!selectedCacheIsNull && (!Database.Hint(GlobalCore.getSelectedCache()).equals(""))) enabled = true;
			mi.setEnabled(enabled);
			mi.setIcon(new SpriteDrawable(SpriteCache.Icons.get(IconName.hint_19.ordinal())));
		}

		mi = icm.addItem(MenuID.MI_SPOILER, "spoiler", SpriteCache.Icons.get(IconName.images_22.ordinal()));
		if (selectedCacheIsNull)
		{
			mi.setEnabled(GlobalCore.getSelectedCache().SpoilerExists());
		}
		else
		{
			mi.setEnabled(false);
		}

		mi = icm.addItem(MenuID.MI_SOLVER, "Solver", SpriteCache.Icons.get(IconName.solver_24.ordinal()));
		if (selectedCacheIsNull) mi.setEnabled(false);

		if (GlobalCore.JokerisOnline())
		{
			mi = icm.addItem(MenuID.MI_JOKER, "joker", SpriteCache.Icons.get(IconName.jokerPhone_25.ordinal()));
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

		mi = icm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCache.Icons.get(IconName.favorit_42.ordinal()));
		mi.setCheckable(true);
		if (selectedCacheIsNull) mi.setEnabled(false);
		else
			mi.setChecked(GlobalCore.getSelectedCache().Favorit());

		mi = icm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");
		if (selectedCacheIsNull) mi.setEnabled(false);

		icm.Show();

	}

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
				new CB_Action_ShowActivity("reload_CacheInfo", MenuID.MI_RELOAD_CACHE_INFO, ViewConst.RELOAD_CACHE,
						SpriteCache.Icons.get(IconName.GCLive_35.ordinal())).Execute();
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
					GlobalCore.getSelectedCache().setFavorit(!GlobalCore.getSelectedCache().Favorit());
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

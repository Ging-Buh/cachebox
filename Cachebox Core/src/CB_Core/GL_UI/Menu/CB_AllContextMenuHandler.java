package CB_Core.GL_UI.Menu;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
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

		mi = icm.addItem(MenuID.MI_DESCRIPTION, "Description", SpriteCache.Icons.get(20));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_RELOAD_CACHE_INFO, "ReloadCacheAPI", SpriteCache.Icons.get(35));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_WAYPOINTS, "Waypoints", SpriteCache.BigIcons.get(16));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_SHOW_LOGS, "ShowLogs", SpriteCache.Icons.get(21));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MenuID.MI_HINT, "hint");
		if (mi != null)
		{
			boolean enabled = false;
			if (!selectedCacheIsNull && (!Database.Hint(GlobalCore.getSelectedCache()).equals(""))) enabled = true;
			mi.setEnabled(enabled);
			mi.setIcon(new SpriteDrawable(SpriteCache.Icons.get(19)));
		}

		mi = icm.addItem(MenuID.MI_SPOILER, "spoiler", SpriteCache.Icons.get(22));
		if (selectedCacheIsNull)
		{
			mi.setEnabled(GlobalCore.getSelectedCache().SpoilerExists());
		}
		else
		{
			mi.setEnabled(false);
		}

		mi = icm.addItem(MenuID.MI_SOLVER, "Solver", SpriteCache.Icons.get(24));
		if (selectedCacheIsNull) mi.setEnabled(false);

		if (GlobalCore.JokerisOnline())
		{
			mi = icm.addItem(MenuID.MI_JOKER, "joker", SpriteCache.Icons.get(25));
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

		mi = icm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");
		if (selectedCacheIsNull) mi.setEnabled(false);

		icm.show();

	}

	private static OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			EditCache editCache = null;
			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_HINT:
				HintDialog.show();
				return true;

			case MenuID.MI_RELOAD_CACHE_INFO:
				new CB_Action_ShowActivity("reload_CacheInfo", MenuID.MI_RELOAD_CACHE_INFO, ViewConst.RELOAD_CACHE,
						SpriteCache.Icons.get(35)).Execute();
				return true;

			case MenuID.MI_DESCRIPTION:
				if (TabMainView.actionShowDescriptionView != null) TabMainView.actionShowDescriptionView.Execute();
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

			case MenuID.MI_DELETE_CACHE:
				DeleteSelectedCache.Execute();
				return true;

			default:
				return false;

			}

		}
	};

}

package CB_Core.GL_UI.Menu;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.HintDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Math.CB_RectF;

public class CB_AllContextMenuHandler
{
	// Constant Item ID´s

	// public static final int MI_QUIT = x;
	public static final int MI_ABOUT = 1;
	public static final int MI_DAY_NIGHT = 2;
	public static final int MI_SETTINGS = 3;
	public static final int MI_SCREENLOCK = 4;
	public static final int MI_QUIT = 5;
	public static final int MI_DESCRIPTION = 6;
	public static final int MI_WAYPOINTS = 7;
	public static final int MI_SHOW_LOGS = 8;
	public static final int MI_HINT = 9;
	public static final int MI_SPOILER = 10;
	public static final int MI_FIELDNOTES = 11;
	public static final int MI_NOTES = 12;
	public static final int MI_SOLVER = 13;
	public static final int MI_JOKER = 14;
	public static final int MI_Layer = 15;
	public static final int MI_ALIGN_TO_COMPSS = 16;
	public static final int MI_SMOOTH_SCROLLING = 17;
	public static final int MI_SEARCH = 18;
	public static final int MI_TREC_REC = 19;
	public static final int MI_HIDE_FINDS = 20;
	public static final int MI_SHOW_RATINGS = 21;
	public static final int MI_SHOW_DT = 22;
	public static final int MI_SHOW_TITLE = 23;
	public static final int MI_SHOW_DIRECT_LINE = 24;
	public static final int MI_MAPVIEW_VIEW = 25;

	public static final CB_RectF MENU_REC = new CB_RectF(0, 0, 400, 60); // wird mit jedem Item größer

	// public static final CB_RectF MENU_Item_REC = new CB_RectF(0, 0, 50, 400);

	public static void showBtnMiscContextMenu()
	{

		Menu cm = new Menu(MENU_REC, "MiscContextMenu");

		cm.setItemClickListner(onItemClickListner);

		cm.addItem(MI_ABOUT, "Menu Item About");
		cm.addItem(MI_DAY_NIGHT, "Menu Item Day Night");
		cm.addItem(MI_SETTINGS, "Menu Item Settings");
		cm.addItem(MI_SCREENLOCK, "Menu Item Screenlock");
		cm.addItem(MI_QUIT, "Menu Item Quit");

		cm.show();

		// icm = new IconContextMenu(Main, R.menu.menu_misc);
		// icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		// Menu IconMenu = icm.getMenu();
		//
		// Global.TranslateMenuItem(IconMenu, R.id.miAbout, "about");
		// Global.TranslateMenuItem(IconMenu, R.id.miDayNight, "DayNight");
		// Global.TranslateMenuItem(IconMenu, R.id.miSettings, "settings");
		// Global.TranslateMenuItem(IconMenu, R.id.miScreenLock, "screenlock");
		// Global.TranslateMenuItem(IconMenu, R.id.miClose, "quit");
		//
		// icm.show();
	}

	public static void showBtnCacheContextMenu()
	{

		boolean selectedCacheIsNull = (GlobalCore.SelectedCache() == null);

		Menu icm = new Menu(MENU_REC, "BtnCacheContextMenu");
		icm.setItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MI_DESCRIPTION, "Description", SpriteCache.BtnIcons.get(20));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MI_WAYPOINTS, "Waypoints", SpriteCache.BigIcons.get(16));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MI_SHOW_LOGS, "ShowLogs", SpriteCache.BtnIcons.get(21));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MI_HINT, "hint");
		if (mi != null)
		{
			boolean enabled = false;
			if (!selectedCacheIsNull && (!Database.Hint(GlobalCore.SelectedCache()).equals(""))) enabled = true;
			mi.setEnabled(enabled);
			mi.setIcon(SpriteCache.BtnIcons.get(19));
		}

		mi = icm.addItem(MI_SPOILER, "spoiler", SpriteCache.BtnIcons.get(22));
		if (selectedCacheIsNull)
		{
			mi.setEnabled(GlobalCore.SelectedCache().SpoilerExists());
		}
		else
		{
			mi.setEnabled(false);
		}

		icm.addItem(MI_FIELDNOTES, "Fieldnotes", SpriteCache.BtnIcons.get(23));
		mi = icm.addItem(MI_NOTES, "Notes", SpriteCache.BtnIcons.get(23));
		if (selectedCacheIsNull) mi.setEnabled(false);

		mi = icm.addItem(MI_SOLVER, "Solver", SpriteCache.BtnIcons.get(24));
		if (selectedCacheIsNull) mi.setEnabled(false);

		if (Config.settings.hasCallPermission.getValue())
		{
			mi = icm.addItem(MI_JOKER, "joker", SpriteCache.BtnIcons.get(25));
			// Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5

			if (mi != null)
			{
				boolean enabled = false;
				if (GlobalCore.JokerisOnline()) enabled = true;

				mi.setEnabled(enabled);
			}

		}

		icm.show();

	}

	public static void showMapViewGLContextMenu()
	{
		Menu icm = new Menu(MENU_REC, "menu_mapviewgl");
		icm.setItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MI_Layer, "Layer");
		mi = icm.addItem(MI_ALIGN_TO_COMPSS, "AlignToCompass");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.GetAlignToCompass());

		mi = icm.addItem(MI_SMOOTH_SCROLLING, "SmoothScrolling");
		mi = icm.addItem(MI_SETTINGS, "settings", SpriteCache.BtnIcons.get(26));
		mi = icm.addItem(MI_SEARCH, "search", SpriteCache.BtnIcons.get(27));
		mi = icm.addItem(MI_MAPVIEW_VIEW, "view");
		mi = icm.addItem(MI_TREC_REC, "TrackRec");

		icm.show();
	}

	public static void showMapViewLayerMenu()
	{
		Menu icm = new Menu(MENU_REC, "MapViewShowLayerContextMenu");

		icm.setItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MI_HIDE_FINDS, "HideFinds");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.hideMyFinds);

		mi = icm.addItem(MI_SHOW_RATINGS, "ShowRatings");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showRating);

		mi = icm.addItem(MI_SHOW_DT, "ShowDT");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showDT);

		mi = icm.addItem(MI_SHOW_TITLE, "ShowTitle");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showTitles);

		mi = icm.addItem(MI_SHOW_DIRECT_LINE, "ShowDirectLine");
		mi.setCheckable(true);
		mi.setChecked(MapView.that.showDirektLine);

		icm.show();
	}

	private static OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MI_HINT:
				HintDialog.show();
				return true;

			case MI_MAPVIEW_VIEW:
				showMapViewLayerMenu();
				return true;

			case MI_ALIGN_TO_COMPSS:
				MapView.that.SetAlignToCompass(!MapView.that.GetAlignToCompass());
				return true;

			case MI_HIDE_FINDS:
				MapView.that.hideMyFinds = !MapView.that.hideMyFinds;
				return true;

			case MI_SHOW_RATINGS:
				MapView.that.showRating = !MapView.that.showRating;
				return true;

			case MI_SHOW_DT:
				MapView.that.showDT = !MapView.that.showDT;
				return true;

			case MI_SHOW_TITLE:
				MapView.that.showTitles = !MapView.that.showTitles;
				return true;

			case MI_SHOW_DIRECT_LINE:
				MapView.that.showDirektLine = !MapView.that.showDirektLine;
				return true;

			default:
				String br = System.getProperty("line.separator");

				String msgText = "Ein OnClick vom Menu Item kommt an" + br + "Item " + ((MenuItem) v).getMenuItemId() + br + br
						+ "geklickt";
				// String msgTitle = "CB_ALLContextMenuHandler";
				String msgTitle = "CB_AL";

				GL_MsgBox.Show(msgText, msgTitle, MessageBoxButtons.OK, MessageBoxIcon.Information, null);
				return false;

			}

		}
	};

}

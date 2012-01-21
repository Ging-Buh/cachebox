/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use Main file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cachebox_test.Ui;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.SmoothScrollingTyp;
import CB_Core.Log.Logger;
import android.view.Menu;
import android.view.MenuItem;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.cachebox_test.Map.Layer;
import de.cachebox_test.Views.MapView;

/**
 * Diese Klasse enthält alle Statischen Methoden, um ein bestimmtes ContextMenu aufzurufen. Als Grundlage, für ein Menü, dient das
 * IconContextMenu. Das IconContextMenu kann keine Unter Menüs aus einer Menu.xml verwalten. Daher wird bei der Verwendung von Untermenüs
 * ein jeweils neues Menü angezeigt, was für den User keinen Unterschied macht.
 * 
 * @author Longri
 */
public class AllContextMenuCallHandler
{

	/**
	 * Statische Instanz des IconContextMenus. Da immer nur ein Menu dargestellt wird, reicht hier eine Statische Instanz.
	 */
	public static IconContextMenu icm;

	/**
	 * Staticher Pointer zur main class
	 */
	public static main Main = (main) main.mainActivity;

	public static void showBtnMiscContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_misc);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miAbout, "about");
		Global.TranslateMenuItem(IconMenu, R.id.miDayNight, "DayNight");
		Global.TranslateMenuItem(IconMenu, R.id.miSettings, "settings");
		Global.TranslateMenuItem(IconMenu, R.id.miScreenLock, "screenlock");
		Global.TranslateMenuItem(IconMenu, R.id.miClose, "quit");

		icm.show();
	}

	public static void showWayPointViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_waypointview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		try
		{
			MenuItem mi = IconMenu.findItem(R.id.menu_waypointview_edit);
			if (mi != null)
			{
				mi.setTitle(GlobalCore.Translations.Get("edit"));
				// mi.setVisible(Main.waypointView.aktWaypoint != null);
				if (Main.waypointView.aktWaypoint == null)
				{
					IconMenu.removeItem(mi.getItemId());
				}
			}
			Global.TranslateMenuItem(IconMenu, R.id.menu_waypointview_new, "addWaypoint");
			mi = IconMenu.findItem(R.id.menu_waypointview_delete);
			if (mi != null)
			{
				mi.setTitle(GlobalCore.Translations.Get("delete"));
				// mi.setVisible((Main.waypointView.aktWaypoint != null) &&
				// (Main.waypointView.aktWaypoint.IsUserWaypoint));
				if (!((Main.waypointView.aktWaypoint != null) && (Main.waypointView.aktWaypoint.IsUserWaypoint)))
				{
					IconMenu.removeItem(mi.getItemId());
				}
			}
			mi = IconMenu.findItem(R.id.menu_waypointview_project);
			if (mi != null)
			{
				mi.setTitle(GlobalCore.Translations.Get("Projection"));
				// mi.setVisible((Main.waypointView.aktWaypoint != null ||
				// Main.waypointView.aktCache!=null));
				if (!((Main.waypointView.aktWaypoint != null || Main.waypointView.aktCache != null)))
				{
					IconMenu.removeItem(mi.getItemId());
				}
			}
			mi = IconMenu.findItem(R.id.menu_waypointview_gps);
			if (mi != null)
			{
				mi.setTitle(GlobalCore.Translations.Get("FromGps"));

			}
		}
		catch (Exception e)
		{
			Logger.Error("WaypointView.BeforeShowMenu()", IconMenu.toString(), e);
		}

		icm.show();
	}

	public static void showBtnToolsContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_tools);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miTrackRec, "TrackRec");
		Global.TranslateMenuItem(IconMenu, R.id.miVoiceRecorder, "VoiceRec");
		Global.TranslateMenuItem(IconMenu, R.id.miTakePhoto, "TakePhoto");
		Global.TranslateMenuItem(IconMenu, R.id.miRecordVideo, "RecVideo");
		Global.TranslateMenuItem(IconMenu, R.id.miDeleteCaches, "DeleteCaches");

		try
		{
			MenuItem mi = IconMenu.findItem(R.id.miVoiceRecorder);
			if (mi != null) if (!Main.getVoiceRecIsStart()) mi.setTitle("Voice Recorder");
			else
				mi.setTitle("Stop Voice Rec.");
		}
		catch (Exception exc)
		{
		}

		icm.show();
	}

	public static void showTrackContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_track);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miTrackStart, "start");
		Global.TranslateMenuItem(IconMenu, R.id.miTrackPause, "pause");
		Global.TranslateMenuItem(IconMenu, R.id.miTrackStop, "stop");

		icm.show();
	}

	public static void showBtnNavContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_nav);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.miMapView, "Map");
		Global.TranslateMenuItem(IconMenu, R.id.miCompassView, "Compass");
		Global.TranslateMenuItem(IconMenu, R.id.miNavigateTo, "NavigateTo");
		icm.show();
	}

	public static void showBtnCacheContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_cache);

		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.miDescription, "Description");
		Global.TranslateMenuItem(IconMenu, R.id.miWaypoints, "Waypoints");
		Global.TranslateMenuItem(IconMenu, R.id.miLogView, "ShowLogs");
		Global.TranslateMenuItem(IconMenu, R.id.miHint, "hint");
		Global.TranslateMenuItem(IconMenu, R.id.miSpoilerView, "spoiler");
		Global.TranslateMenuItem(IconMenu, R.id.miFieldNotes, "Fieldnotes");
		Global.TranslateMenuItem(IconMenu, R.id.miNotes, "Notes");
		Global.TranslateMenuItem(IconMenu, R.id.miSolver, "Solver");
		Global.TranslateMenuItem(IconMenu, R.id.miTelJoker, "joker");

		boolean selectedCacheIsNull = (GlobalCore.SelectedCache() == null);

		// Menu Item Hint enabled / disabled
		boolean enabled = false;
		if (!selectedCacheIsNull && (!Database.Hint(GlobalCore.SelectedCache()).equals(""))) enabled = true;
		MenuItem mi = IconMenu.findItem(R.id.miHint);
		if (mi != null)
		{
			mi.setEnabled(enabled);
			mi.setIcon(Global.BtnIcons[19]);
		}
		mi = IconMenu.findItem(R.id.miSpoilerView);
		// Saarfuchs: hier musste noch abgetestet werden, dass auch ein Cache
		// selektiert ist, sonst Absturz
		if (mi != null && !selectedCacheIsNull)
		{
			mi.setEnabled(GlobalCore.SelectedCache().SpoilerExists());
		}
		else
		{
			mi.setEnabled(false);
		}
		// Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5
		enabled = false;
		if (Global.JokerisOnline()) enabled = true;
		mi = IconMenu.findItem(R.id.miTelJoker);
		if (mi != null)
		{
			mi.setEnabled(enabled);
			if (!Config.settings.hasCallPermission.getValue())
			{
				IconMenu.removeItem(R.id.miTelJoker);
			}
		}

		if (selectedCacheIsNull)
		{
			mi = IconMenu.findItem(R.id.miDescription);
			mi.setEnabled(false);

			mi = IconMenu.findItem(R.id.miWaypoints);
			mi.setEnabled(false);

			mi = IconMenu.findItem(R.id.miLogView);
			mi.setEnabled(false);

			mi = IconMenu.findItem(R.id.miNotes);
			mi.setEnabled(false);

			mi = IconMenu.findItem(R.id.miSolver);
			mi.setEnabled(false);
		}

		icm.show();
	}

	public static void showBtnListsContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_lists);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miCacheList, "cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")");
		Global.TranslateMenuItem(IconMenu, R.id.miTrackList, "Tracks");
		Global.TranslateMenuItem(IconMenu, R.id.miTbList, "TBList");

		icm.show();
	}

	public static void showCachelistViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_cache_list);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		String DBName = Config.settings.DatabasePath.getValue();
		int Pos = DBName.lastIndexOf("/");
		DBName = DBName.substring(Pos + 1);
		Pos = DBName.lastIndexOf(".");
		DBName = DBName.substring(0, Pos);

		Global.TranslateMenuItem(IconMenu, R.id.miManageDB, "manage", "  (" + DBName + ")");
		MenuItem miAutoResort = Global.TranslateMenuItem(IconMenu, R.id.miAutoResort, "AutoResort");
		miAutoResort.setCheckable(true);
		miAutoResort.setChecked(Global.autoResort);
		Global.TranslateMenuItem(IconMenu, R.id.miResort, "ResortList");
		Global.TranslateMenuItem(IconMenu, R.id.miFilterset, "filter");
		Global.TranslateMenuItem(IconMenu, R.id.miSearch, "search");
		Global.TranslateMenuItem(IconMenu, R.id.miAddCache, "ManuallyAddCache");
		Global.TranslateMenuItem(IconMenu, R.id.miImport, "import");

		// Search Caches
		// MenuItem mi = IconMenu.findItem(R.id.searchcaches_online);
		// if (mi != null)
		// mi.setEnabled(Global.APIisOnline());
		// mi.setIcon(Global.Icons[36]);
		// Global.TranslateMenuItem(IconMenu, R.id.searchcaches_online,
		// "FindCachesOnline");
		//
		icm.show();
	}

	public static void showCacheDescViewContextMenu()
	{
		if (Main.descriptionView.aktCache == null) return;

		icm = new IconContextMenu(Main, R.menu.menu_descview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		MenuItem miFavorite = Global.TranslateMenuItem(IconMenu, R.id.mi_descview_favorite, "Favorite");
		miFavorite.setCheckable(true);
		miFavorite.setChecked(Main.descriptionView.aktCache.Favorit());

		Global.TranslateMenuItem(IconMenu, R.id.mi_descview_update, "ReloadCacheAPI");

		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener()
		{

			@Override
			public void onIconContextItemSelected(MenuItem item, Object info)
			{
				switch (item.getItemId())
				{

				case R.id.mi_descview_favorite:
					Main.descriptionView.aktCache.setFavorit(!Main.descriptionView.aktCache.Favorit());
					CacheDAO dao = new CacheDAO();
					dao.UpdateDatabase(Main.descriptionView.aktCache);
					Main.descriptionView.cacheInfo.invalidate();
					break;
				case R.id.mi_descview_update:
					Main.descriptionView.reloadCacheInfo();
					break;

				default:

				}
			}
		});

		icm.show();

	}

	public static void showTrackableListViewContextMenu()
	{

		icm = new IconContextMenu(Main, R.menu.menu_trackablelistview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		icm.show();
	}

	public static void showTrackListViewContextMenu()
	{

		icm = new IconContextMenu(Main, R.menu.menu_tracklistview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.menu_tracklistview_generate, "generate");
		Global.TranslateMenuItem(IconMenu, R.id.menu_tracklistview_load, "load");
		Global.TranslateMenuItem(IconMenu, R.id.menu_tracklistview_delete, "delete");

		icm.show();
	}

	public static void showTrackListView_generateContextMenu()
	{

		icm = new IconContextMenu(Main, R.menu.menu_tracklistview_generate);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miTrackStart, "start");
		Global.TranslateMenuItem(IconMenu, R.id.miTrackPause, "pause");
		Global.TranslateMenuItem(IconMenu, R.id.miTrackStop, "stop");

		icm.show();
	}

	public static void showMapViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_mapview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.layer, "Layer");
		Global.TranslateMenuItem(IconMenu, R.id.miAlignCompass, "AlignToCompass");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth, "SmoothScrolling");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_go_settings, "settings");
		Global.TranslateMenuItem(IconMenu, R.id.miSearch, "search");
		Global.TranslateMenuItem(IconMenu, R.id.mimapview_view, "view");
		Global.TranslateMenuItem(IconMenu, R.id.mi_Track, "TrackRec");

		try
		{

			MenuItem mi = IconMenu.findItem(R.id.miAlignCompass);
			mi.setCheckable(true);
			mi.setChecked(Main.mapView.alignToCompass);

		}
		catch (Exception exc)
		{
			Logger.Error("MapView.BeforeShowMenu()", "", exc);
			return;
		}

		icm.show();
	}

	public static void showMapViewGLContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_mapviewgl);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.layer, "Layer");
		Global.TranslateMenuItem(IconMenu, R.id.miAlignCompass, "AlignToCompass");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth, "SmoothScrolling");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_go_settings, "settings");
		Global.TranslateMenuItem(IconMenu, R.id.miSearch, "search");
		Global.TranslateMenuItem(IconMenu, R.id.mimapview_view, "view");
		Global.TranslateMenuItem(IconMenu, R.id.mi_Track, "TrackRec");

		try
		{

			MenuItem mi = IconMenu.findItem(R.id.miAlignCompass);
			mi.setCheckable(true);
			mi.setChecked(Main.mapViewGl.mapViewGlListener.alignToCompass);

		}
		catch (Exception exc)
		{
			Logger.Error("MapViewGL.BeforeShowMenu()", "", exc);
			return;
		}

		icm.show();
	}

	public static void showMapViewGLLayerMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_map_view_layer);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miMap_HideFinds, "HideFinds");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowRatings, "ShowRatings");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowDT, "ShowDT");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowTitles, "ShowTitle");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowDirektLine, "ShowDirectLine");

		MenuItem miFinds = IconMenu.findItem(R.id.miMap_HideFinds);
		MenuItem miRaiting = IconMenu.findItem(R.id.miMap_ShowRatings);
		MenuItem miDT = IconMenu.findItem(R.id.miMap_ShowDT);
		MenuItem miTitles = IconMenu.findItem(R.id.miMap_ShowTitles);
		MenuItem miLine = IconMenu.findItem(R.id.miMap_ShowDirektLine);

		miFinds.setChecked(Main.mapViewGl.mapViewGlListener.hideMyFinds);
		miRaiting.setChecked(Main.mapViewGl.mapViewGlListener.showRating);
		miDT.setChecked(Main.mapViewGl.mapViewGlListener.showDT);
		miTitles.setChecked(Main.mapViewGl.mapViewGlListener.showTitles);
		miLine.setChecked(Main.mapViewGl.mapViewGlListener.showDirektLine);

		icm.show();
	}

	public static void showMapLayerMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_layer);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		IconMenu.clear();
		for (Layer layer : MapView.Manager.Layers)
		{
			MenuItem mi22 = IconMenu.add(layer.Name);
			mi22.setCheckable(true);
			if (layer == Main.mapView.CurrentLayer)
			{
				mi22.setChecked(true);
			}
		}
		icm.show();
	}

	public static void showMapGLLayerMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_layer);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		IconMenu.clear();
		for (Layer layer : MapView.Manager.Layers)
		{
			MenuItem mi22 = IconMenu.add(layer.Name);
			mi22.setCheckable(true);
			if (layer == Main.mapViewGl.GetCurrentLayer())
			;
			{
				mi22.setChecked(true);
			}
		}
		icm.show();
	}

	public static void showMapSmoothMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_smooth);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);

		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth_none, "none");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth_normal, "normal");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth_fine, "fine");
		Global.TranslateMenuItem(IconMenu, R.id.mapview_smooth_superfine, "superfine");

		MenuItem mi2 = IconMenu.findItem(R.id.mapview_smooth_none);
		if (mi2 != null) mi2.setChecked(GlobalCore.SmoothScrolling == SmoothScrollingTyp.none);
		mi2 = IconMenu.findItem(R.id.mapview_smooth_normal);
		if (mi2 != null) mi2.setChecked(GlobalCore.SmoothScrolling == SmoothScrollingTyp.normal);
		mi2 = IconMenu.findItem(R.id.mapview_smooth_fine);
		if (mi2 != null) mi2.setChecked(GlobalCore.SmoothScrolling == SmoothScrollingTyp.fine);
		mi2 = IconMenu.findItem(R.id.mapview_smooth_superfine);
		if (mi2 != null) mi2.setChecked(GlobalCore.SmoothScrolling == SmoothScrollingTyp.superfine);

		icm.show();
	}

	public static void showMapViewLayerMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_map_view_layer);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.miMap_HideFinds, "HideFinds");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowRatings, "ShowRatings");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowDT, "ShowDT");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowTitles, "ShowTitle");
		Global.TranslateMenuItem(IconMenu, R.id.miMap_ShowDirektLine, "ShowDirectLine");

		MenuItem miFinds = IconMenu.findItem(R.id.miMap_HideFinds);
		MenuItem miRaiting = IconMenu.findItem(R.id.miMap_ShowRatings);
		MenuItem miDT = IconMenu.findItem(R.id.miMap_ShowDT);
		MenuItem miTitles = IconMenu.findItem(R.id.miMap_ShowTitles);
		MenuItem miLine = IconMenu.findItem(R.id.miMap_ShowDirektLine);

		miFinds.setChecked(Main.mapView.hideMyFinds);
		miRaiting.setChecked(Main.mapView.showRating);
		miDT.setChecked(Main.mapView.showDT);
		miTitles.setChecked(Main.mapView.showTitles);
		miLine.setChecked(Main.mapView.showDirektLine);

		icm.show();
	}

	public static void showFiledNotesViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_fieldnotesview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();

		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_found, "found");
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_notfound, "DNF");
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_maintenance, "maintenance");
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_addnote, "writenote");
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_manage, "ManageNotes");

		MenuItem mi = IconMenu.findItem(R.id.c_fnv_edit);
		if (mi != null)
		{
			mi.setEnabled(Main.fieldNotesView.aktFieldNote != null);

		}
		mi = IconMenu.findItem(R.id.c_fnv_delete);
		if (mi != null)
		{
			mi.setEnabled(Main.fieldNotesView.aktFieldNote != null);
		}

		mi = IconMenu.findItem(R.id.c_fnv_selectcache);
		if (mi != null)
		{
			mi.setEnabled((Main.fieldNotesView.aktFieldNote != null)
					&& (Database.Data.Query.GetCacheByGcCode(Main.fieldNotesView.aktFieldNote.gcCode) != null));
		}

		mi = IconMenu.findItem(R.id.fieldnotesview_found);
		if (mi != null)
		{
			if (Config.settings.isChris.getValue())
			{
				mi.setIcon(R.drawable.chris_log0);
			}
			else
			{
				mi.setIcon(R.drawable.log0);
			}
			;
		}

		icm.show();

	}

	public static void showFiledNotesView_manageContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_fieldnotesview_manage);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_upload, "uploadFieldNotes");
		Global.TranslateMenuItem(IconMenu, R.id.fieldnotesview_deleteall, "DeleteAllNotes");

		icm.show();
	}

	public static void showFiledNotesView_ItemContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.cmenu_fieldnotesview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu = icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.c_fnv_selectcache, "SelectCache");
		Global.TranslateMenuItem(IconMenu, R.id.c_fnv_edit, "edit");
		Global.TranslateMenuItem(IconMenu, R.id.c_fnv_delete, "delete");
		icm.show();
	}

}

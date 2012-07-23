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
import android.view.Menu;
import android.view.MenuItem;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;

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
		// Global.TranslateMenuItem(IconMenu, R.id.miMapViewGl, "Map");
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
		if (GlobalCore.JokerisOnline()) enabled = true;
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
		miAutoResort.setChecked(GlobalCore.autoResort);
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

}

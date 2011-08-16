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

package de.droidcachebox.Ui;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.droidcachebox.DAO.CacheDAO;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.MapView.SmoothScrollingTyp;
import de.droidcachebox.Database;
import android.view.Menu;
import android.view.MenuItem;



/**
 * Diese Klasse enthält alle Statischen Methoden, 
 * um ein bestimmtes ContextMenu aufzurufen.
 * 
 * Als Grundlage, für ein Menü, dient das IconContextMenu.
 * Das IconContextMenu kann keine Unter Menüs aus einer Menu.xml verwalten.
 * Daher wird bei der Verwendung von Untermenüs ein jeweils neues Menü angezeigt, was für den User 
 * keinen Unterschied macht.
 * 
 * @author Longri
 *
 */
public class AllContextMenuCallHandler 
{
	
	/**
 	 * Statische Instanz des IconContextMenus.
	 * Da immer nur ein Menu dargestellt wird,
	 * reicht hier eine Statische Instanz.
	 */
	private static IconContextMenu icm;
	
	/**
	 * Staticher Pointer zur main class
	 */
	public static main Main = (main) main.mainActivity;

	public static void showBtnMiscContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_misc);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		  
	  	  icm.show();
	}
	
	
	public static void showWayPointViewContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_waypointview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		
		Menu menu = icm.getMenu();
		
		try
		{
			MenuItem mi = menu.findItem(R.id.menu_waypointview_edit);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("edit"));
//				mi.setVisible(Main.waypointView.aktWaypoint != null);
				if(Main.waypointView.aktWaypoint == null)
				{
					menu.removeItem(mi.getItemId());
				}
			}
			Global.TranslateMenuItem(menu, R.id.menu_waypointview_new, "addWaypoint");
			mi = menu.findItem(R.id.menu_waypointview_delete);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("delete"));
//				mi.setVisible((Main.waypointView.aktWaypoint != null) && (Main.waypointView.aktWaypoint.IsUserWaypoint));
				if(!((Main.waypointView.aktWaypoint != null) && (Main.waypointView.aktWaypoint.IsUserWaypoint)))
				{
					menu.removeItem(mi.getItemId());
				}
			}
			mi = menu.findItem(R.id.menu_waypointview_project);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("Projection"));
//				mi.setVisible((Main.waypointView.aktWaypoint != null || Main.waypointView.aktCache!=null));
				if(!((Main.waypointView.aktWaypoint != null || Main.waypointView.aktCache!=null)))
				{
					menu.removeItem(mi.getItemId());
				}
			}
		} catch (Exception e)
		{
			Logger.Error("WaypointView.BeforeShowMenu()", menu.toString(), e);
		}
		
		
		
	  	icm.show();
	}

	public static void showBtnToolsContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_tools);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
    	
  		Menu IconMenu=icm.getMenu();
  		
  		Global.TranslateMenuItem(IconMenu, R.id.miSettings, "settings");
		Global.TranslateMenuItem(IconMenu, R.id.miAbout, "about");
      	try
    	{
    		MenuItem mi = IconMenu.findItem(R.id.miVoiceRecorder);
    		if (mi != null)
    			if (!Main.getVoiceRecIsStart())
    				mi.setTitle("Voice Recorder");
    			else
    				mi.setTitle("Stop Voice Rec.");
    	} catch (Exception exc)
    	{ }

    	  icm.show();
	}
	
	public static void showTrackContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_track);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
    	icm.show();
	}

	public static void showBtnNavContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_nav);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		
		Menu IconMenu=icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.miSolver, "Map");
		Global.TranslateMenuItem(IconMenu, R.id.miSolver, "Compass");
		icm.show();
	}

	public static void showBtnCacheContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_cache);
		
  		  icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
    	  
    	  Menu IconMenu=icm.getMenu();
    	  Global.TranslateMenuItem(IconMenu, R.id.miSolver, "Solver");
    	  Global.TranslateMenuItem(IconMenu, R.id.miNotes, "Notes");
    	  Global.TranslateMenuItem(IconMenu, R.id.miDescription, "Description");
    	  Global.TranslateMenuItem(IconMenu, R.id.miWaypoints, "Waypoints");
    	  Global.TranslateMenuItem(IconMenu, R.id.miHint, "hint");
    	  Global.TranslateMenuItem(IconMenu, R.id.miTelJoker, "joker");
    	  Global.TranslateMenuItem(IconMenu, R.id.miLogView, "ShowLogs");
    	  
    	  boolean selectedCacheIsNull = (GlobalCore.SelectedCache() == null);
    	  
    	  // Menu Item Hint enabled / disabled
    	  boolean enabled = false;
    	  if (!selectedCacheIsNull && (!Database.Hint(GlobalCore.SelectedCache()).equals("")))
    		  enabled = true;
    	  MenuItem mi = IconMenu.findItem(R.id.miHint);
    	  if (mi != null)
    		  mi.setEnabled(enabled);
    	  mi = IconMenu.findItem(R.id.miSpoilerView);
    	  // Saarfuchs: hier musste noch abgetestet werden, dass auch ein Cache selektiert ist, sonst Absturz
    	  if (mi != null && !selectedCacheIsNull ) 
    	  {
    		  mi.setEnabled( GlobalCore.SelectedCache().SpoilerExists() );
    	  }
    	  else {
    		  mi.setEnabled( false );
    	  }
    	  // Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5
    	  enabled = false;
    	  if (Global.JokerisOnline())
    		  enabled = true;
    	  mi = IconMenu.findItem(R.id.miTelJoker);
    	  if (mi != null)
    		  mi.setEnabled(enabled);

    	  if(selectedCacheIsNull)
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

	public static void showBtnListsContextMenu() {
		icm = new IconContextMenu(Main, R.menu.menu_lists);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu=icm.getMenu();
		
		Global.TranslateMenuItem(IconMenu, R.id.miCacheList, "cacheList","  (" + String.valueOf(Database.Data.Query.size()) + ")" );
		Global.TranslateMenuItem(IconMenu, R.id.miTrackList, "Tracks");
		
		
  	  icm.show();
	}
	
	public static void showCachelistViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_cache_list);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu=icm.getMenu();
		
		String DBName = Config.GetString("DatabasePath");
		int Pos = DBName.lastIndexOf("/");
		DBName= DBName.substring(Pos+1);
    	Pos=DBName.lastIndexOf(".");
    	DBName=DBName.substring(0, Pos);
    	
    	Global.TranslateMenuItem(IconMenu, R.id.miManageDB, "manage" ,"  (" + DBName + ")");
    	MenuItem miAutoResort = Global.TranslateMenuItem(IconMenu, R.id.miAutoResort, "AutoResort");
		miAutoResort.setCheckable(true);
		miAutoResort.setChecked(Global.autoResort);
		Global.TranslateMenuItem(IconMenu, R.id.miResort, "ResortList");
		Global.TranslateMenuItem(IconMenu, R.id.miFilterset, "filter");
		
		// Search Caches
		MenuItem mi = IconMenu.findItem(R.id.searchcaches_online);
		if (mi != null)
			mi.setEnabled(Global.APIisOnline());
			mi.setIcon(Global.Icons[36]);
		
		icm.show();
	}
	
	public static void showCacheDescViewContextMenu()
	{
		if(Main.descriptionView.aktCache==null) return;
		
		icm = new IconContextMenu(Main, R.menu.menu_descview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu IconMenu=icm.getMenu();
				
		MenuItem miFavorite = Global.TranslateMenuItem(IconMenu, R.id.mi_descview_favorite, "Favorite");
		miFavorite.setCheckable(true);
		miFavorite.setChecked(Main.descriptionView.aktCache.Favorit());
		
		
				
		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
			
			@Override
			public void onIconContextItemSelected(MenuItem item, Object info) {
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
	
	public static void showTrackListViewContextMenu()
	{
		
		icm = new IconContextMenu(Main, R.menu.menu_tracklistview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		  
	  	icm.show();
	}
	
	public static void showTrackListView_generateContextMenu()
	{
		
		icm = new IconContextMenu(Main, R.menu.menu_tracklistview_generate);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		  
	  	icm.show();
	}
	
	public static void showMapViewContextMenu()
	{
		icm = new IconContextMenu(Main, R.menu.menu_mapview);
		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
		Menu menu=icm.getMenu();
		

		try
		{
			
			
			MenuItem mi = menu.findItem(R.id.miAlignCompass);
			mi.setCheckable(Main.mapView.alignToCompass);
						
			
			
			// Search Caches	 
            mi = menu.findItem(R.id.searchcaches_online);	 
            if (mi != null)	 
                    mi.setEnabled(Global.APIisOnline());			

			
			
		} catch (Exception exc)
		{
			Logger.Error("MapView.BeforeShowMenu()","",exc);
			return;
		}
		
		icm.show();
	}
	
	public static void showMapLayerMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_layer);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
    	
  		Menu IconMenu=icm.getMenu();
  		
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
	
	public static void showMapSmoothMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_smooth);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
    	
  		Menu IconMenu=icm.getMenu();
  		
				MenuItem mi2 = IconMenu.findItem(R.id.mapview_smooth_none);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.none);
				mi2 = IconMenu.findItem(R.id.mapview_smooth_normal);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.normal);
				mi2 = IconMenu.findItem(R.id.mapview_smooth_fine);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.fine);
				mi2 = IconMenu.findItem(R.id.mapview_smooth_superfine);
				if (mi2 != null)
					mi2.setChecked(Global.SmoothScrolling == SmoothScrollingTyp.superfine);
			
			
			
		icm.show();
	}
    
	public static void showMapViewLayerMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_map_view_layer);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
  		Menu IconMenu=icm.getMenu();

			MenuItem miFinds = IconMenu.findItem(R.id.miMap_HideFinds);
			MenuItem miRaiting = IconMenu.findItem(R.id.miMap_ShowRatings);
			MenuItem miDT = IconMenu.findItem(R.id.miMap_ShowDT);
			MenuItem miTitles = IconMenu.findItem(R.id.miMap_ShowTitles);
			
			miFinds.setChecked(Main.mapView.hideMyFinds);
			miRaiting.setChecked(Main.mapView.showRating);
			miDT.setChecked(Main.mapView.showDT);
			miTitles.setChecked(Main.mapView.showTitles);
					
		icm.show();
	}


	public static void showFiledNotesViewContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_fieldnotesview);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
  		Menu IconMenu=icm.getMenu();

		MenuItem mi = IconMenu.findItem(R.id.c_fnv_edit);
		if (mi !=null)
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
			mi.setEnabled((Main.fieldNotesView.aktFieldNote != null) && (Database.Data.Query.GetCacheByGcCode(Main.fieldNotesView.aktFieldNote.gcCode) != null));
		}	
					
		icm.show();
		
	}
	
	public static void showFiledNotesView_manageContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.menu_fieldnotesview_manage);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
  		icm.show();
	}
	
	public static void showFiledNotesView_ItemContextMenu() 
	{
		icm = new IconContextMenu(Main, R.menu.cmenu_fieldnotesview);
  		icm.setOnIconContextItemSelectedListener(Main.OnIconContextItemSelectedListener);
  		icm.show();
	}
	
	
	
}

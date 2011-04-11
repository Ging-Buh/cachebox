package de.droidcachebox;

import java.io.File;
import java.util.Map;

import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Components.ClockView;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Database;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class main extends Activity implements SelectedCacheEvent, LocationListener {
	private Button buttonDB;
	private Button buttonCache;
	private Button buttonMap;
	private Button buttonInfo;
	private Button buttonMisc;
	private FrameLayout frame;
	private LinearLayout layoutButtons;
	private FrameLayout frameCacheName;
	private FrameLayout frameClock;
	
// Views
	private ViewOptionsMenu aktView;
	private CacheNameView cacheNameView;
	private ClockView clockView;
	private MapView mapView;
	private CacheListView cacheListView;
	private WaypointView waypointView;
	private LogView logView;
	private DescriptionView descriptionView;
	
	// Powermanager
    protected PowerManager.WakeLock mWakeLock;
    // GPS
	private LocationManager locationManager;
	private String provider;
	
	// to store, which menu should be viewd
	private enum nextMenuType { nmDB, nmCache, nmMap, nmInfo, nmMisc }
	private nextMenuType nextMenu = nextMenuType.nmDB;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SelectedCacheEventList.Add(this);

        Global.InitPaints();
        Global.InitIcons(this);
        
        frameCacheName = (FrameLayout)this.findViewById(R.id.frameCacheName);
        cacheNameView = new CacheNameView(this);
        frameCacheName.addView(cacheNameView);
        
        frameClock = (FrameLayout)this.findViewById(R.id.frameClock);
        clockView = new ClockView(this);
        frameClock.addView(clockView);

        layoutButtons = (LinearLayout)this.findViewById(R.id.layoutButtons);
        layoutButtons.setBackgroundColor(Global.TitleBarColor);
        
        frame = (FrameLayout)this.findViewById(R.id.layoutContent);
        frame.setBackgroundColor(Global.EmptyBackground);
        
        // Ausschalten verhindern
        /* This code together with the one in onDestroy() 
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        
        // GPS
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		provider = LocationManager.GPS_PROVIDER;
		Location location = locationManager.getLastKnownLocation(provider);
		locationManager.requestLocationUpdates(provider, 1000, 1, this);                
        
        
        File dir = new File(Config.GetString("MapPackFolder"));
        String[] files = dir.list();
        for (String file : files)
        {
        	MapView.Manager.LoadMapPack(Config.GetString("MapPackFolder") + "/" + file);
        }
        if (Database.Data == null)
        {
	        // initialize Database
	        Database.Data = new Database(DatabaseType.CacheBox);
	    	Database.FieldNotes = new Database(DatabaseType.FieldNotes); 
//	        Database.Data.StartUp("/sdcard/db3 [1].db3");
	    	File path = Environment.getExternalStorageDirectory();
	        Database.Data.StartUp(path.getPath() + "/daheim.db3");
	        Database.Data.Query.LoadCaches("");
        }
        Descriptor.Init();

        mapView = new MapView(this, "Map-View");
        mapView.Initialize();
        mapView.CurrentLayer = MapView.Manager.GetLayerByName(Config.GetString("CurrentMapLayer"), Config.GetString("CurrentMapLayer"), "");
        
        cacheListView = new CacheListView(this);
        waypointView = new WaypointView(this);
        logView = new LogView(this, "Log-View");
        descriptionView = new DescriptionView(this, "Cache-Beschreibung");
        
        this.buttonDB = (Button)this.findViewById(R.id.buttonDB);
        registerForContextMenu(buttonDB);
        this.buttonDB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	frame.removeAllViews();
                frame.addView(cacheListView);
            }
          });
//      this.buttonDB.setLongClickable(true);
  /*      
        this.buttonDB.setOnLongClickListener(new View.OnLongClickListener() {	
			@Override
			public boolean onLongClick(View v) {
            	nextMenu = main.nextMenuType.nmDB;
            	openOptionsMenu();
				return true;
			}
		});
*/
        this.buttonCache = (Button)this.findViewById(R.id.buttonCache);
        registerForContextMenu(buttonCache);
        this.buttonCache.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	frame.removeAllViews();
        		descriptionView.setCache(Global.SelectedCache());
                frame.addView(descriptionView);
            }
          });
        
        this.buttonMap = (Button)this.findViewById(R.id.buttonMap);
        registerForContextMenu(buttonMap);
        this.buttonMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	frame.removeAllViews();
                frame.addView(mapView);
                aktView = mapView;
            }
          });
        
        
        this.buttonInfo = (Button)this.findViewById(R.id.buttonInfo);
        registerForContextMenu(buttonInfo);
        this.buttonInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	frame.removeAllViews();
                frame.addView(logView);
            }
          });
        
        this.buttonMisc = (Button)this.findViewById(R.id.buttonMisc);
        registerForContextMenu(buttonMisc);
        this.buttonMisc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	nextMenu = main.nextMenuType.nmMisc;
            	openOptionsMenu();            	
            }
          });
        

        mapView.OnShow();
    }

    @Override
    public void onDestroy() {
            this.mWakeLock.release();
            super.onDestroy();
    }

    /** hook into menu button for activity */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
//      populateMenu(menu);
    	getMenuInflater().inflate(R.menu.menu_mapview, menu);
      return super.onCreateOptionsMenu(menu);
    }
    
    /** when menu button option selected */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
    	if (aktView != null)
    		return aktView.ItemSelected(item);
    	return super.onOptionsItemSelected(item);
    }    
    public boolean onPrepareContextMenu(Menu menu)
    {
    	menu.clear();
    	int menuId = 0;
    	switch(nextMenu)
    	{
    	// DB
    	case nmDB:
    		menuId = R.menu.menu_db;
    		getMenuInflater().inflate(menuId, menu);
    		break;
    	// Cache
    	case nmCache:
    		menuId = R.menu.menu_cache;
    		getMenuInflater().inflate(menuId, menu);
    		break;
    	// Map
    	case nmMap:
    		menuId = R.menu.menu_map;
    		getMenuInflater().inflate(menuId, menu);
    		break;
    	// Info
    	case nmInfo:
    		menuId = R.menu.menu_info;
    		getMenuInflater().inflate(menuId, menu);
    		break;
    	// Misc
    	case nmMisc:
    		menuId = R.menu.menu_misc;
    		getMenuInflater().inflate(menuId, menu);
    		break;
    	}
    	return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onContextItemSelected(MenuItem item)
    {
		frame.removeAllViews();
    	switch (item.getItemId())
    	{
    	// DB
    	case R.id.miAutoResort:
    		if (item.isChecked())
    			item.setChecked(false);
    		else
    			item.setChecked(true);
    		return true;
    	case R.id.miCacheList:
    		frame.addView(cacheListView);
    		return true;
    	// Cache
    	case R.id.miDescription:
    		descriptionView.setCache(Global.SelectedCache());
    		frame.addView(descriptionView);
    		return true;
    	// Map
    	case R.id.miMapView:
    		frame.addView(mapView);
    		aktView = mapView;
    		return true;
    	// Info
    	case R.id.miLogView:
    		frame.addView(logView);
    		return true;
    	// Misc
    	case R.id.miClose:
    		finish();
    		return true;
		default:
			return super.onOptionsItemSelected(item);
    	}
    }    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      if (v == buttonDB)
    	  inflater.inflate(R.menu.menu_db, menu);
      else if (v == buttonCache)
    	  inflater.inflate(R.menu.menu_cache, menu);
      else if (v == buttonMap)
    	  inflater.inflate(R.menu.menu_map, menu);
      else if (v == buttonInfo)
    	  inflater.inflate(R.menu.menu_info, menu);
      else if (v == buttonMisc)
    	  inflater.inflate(R.menu.menu_misc, menu);
    }
    
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
    {
/*    	TextView textview = (TextView)this.findViewById(R.id.textCacheName);
    	textview.setText(cache.Name);*/
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Global.Location = location;
		PositionEventList.Call(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
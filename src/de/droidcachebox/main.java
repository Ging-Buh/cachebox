package de.droidcachebox;

import java.io.File;
import java.util.Map;

import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Components.ClockView;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Locator.Locator;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.NotesView;
import de.droidcachebox.Views.Settings;
import de.droidcachebox.Views.SolverView;
import de.droidcachebox.Views.SpoilerView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Database;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class main extends Activity implements SelectedCacheEvent, LocationListener {
    LayoutInflater inflater;
	private ImageButton buttonDB;
	private ImageButton buttonCache;
	private ImageButton buttonMap;
	private ImageButton buttonInfo;
	private ImageButton buttonMisc;
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
	private SpoilerView spoilerView;
	private NotesView notesView;
	private SolverView solverView;
	private Settings settingsView;
	
	int width;
    int height;

	
	// Powermanager
    protected PowerManager.WakeLock mWakeLock;
    // GPS
	private LocationManager locationManager;
	private String provider;
	// Compass
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mCompassValues;
    
	// to store, which menu should be viewd
	private enum nextMenuType { nmDB, nmCache, nmMap, nmInfo, nmMisc }
	private nextMenuType nextMenu = nextMenuType.nmDB;

	
	
	
   
	
	
    final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        	mCompassValues = event.values;
        	Global.Locator.setCompassHeading(mCompassValues[0]); 
        	PositionEventList.Call(mCompassValues[0]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        SelectedCacheEventList.Add(this);
        
        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
         this.width = d.getWidth();
         this.height = d.getHeight();


        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);       
        
        frameCacheName = (FrameLayout)this.findViewById(R.id.frameCacheName);
        cacheNameView = new CacheNameView(this);
        frameCacheName.addView(cacheNameView);
        
        frameClock = (FrameLayout)this.findViewById(R.id.frameClock);
        clockView = new ClockView(this);
        frameClock.addView(clockView);

        settingsView = new Settings(this,inflater);
        
        
        frame = (FrameLayout)this.findViewById(R.id.layoutContent);
        frame.setBackgroundColor(Config.GetBool("nightMode")? Global.Colors.Night.EmptyBackground : Global.Colors.Day.EmptyBackground);
        
        
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
		Global.Locator = new Locator();
		locationManager.requestLocationUpdates(provider, 1000, 1, this);                
        
        mapView = new MapView(this, "Map-View");
        mapView.Initialize();
        mapView.CurrentLayer = MapView.Manager.GetLayerByName(Config.GetString("CurrentMapLayer"), Config.GetString("CurrentMapLayer"), "");
        
        cacheListView = new CacheListView(this);
        waypointView = new WaypointView(this);
        logView = new LogView(this);
        descriptionView = new DescriptionView(this, "Cache-Beschreibung");
        spoilerView = new SpoilerView(this, inflater);
        notesView = new NotesView(this, inflater);
        solverView = new SolverView(this, inflater);
        
        this.buttonDB = (ImageButton)this.findViewById(R.id.buttonDB);
        registerForContextMenu(buttonDB);
        this.buttonDB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showView(cacheListView);
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
        this.buttonCache = (ImageButton)this.findViewById(R.id.buttonCache);
        registerForContextMenu(buttonCache);
        this.buttonCache.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showView(descriptionView);
            }
          });
        
        this.buttonMap = (ImageButton)this.findViewById(R.id.buttonMap);
        registerForContextMenu(buttonMap);
        this.buttonMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showView(mapView);
            }
          });
        
        
        this.buttonInfo = (ImageButton)this.findViewById(R.id.buttonInfo);
        registerForContextMenu(buttonInfo);
        this.buttonInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showView(logView);
            }
          });
        
        this.buttonMisc = (ImageButton)this.findViewById(R.id.buttonMisc);
        registerForContextMenu(buttonMisc);
        this.buttonMisc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	nextMenu = main.nextMenuType.nmMisc;
            	openOptionsMenu();            	
            }
          });
        
        mapView.InitializeMap();
        Global.SelectedCache(Database.Data.Query.get(0));
    }
    
  

    @Override
    public void onDestroy() {
            this.mWakeLock.release();
            super.onDestroy();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /** hook into menu button for activity */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
  		return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
    	int menuId = aktView.GetMenuId();
    	if (menuId > 0)
    	{
    		getMenuInflater().inflate(menuId, menu);
    	}
    	aktView.BeforeShowMenu(menu);
      return super.onPrepareOptionsMenu(menu);
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
    		showView(cacheListView);
    		return true;
    	// Cache
    	case R.id.miDescription:
    		showView(descriptionView);
    		return true;
    	case R.id.miWaypoints:
    		showView(waypointView);
    		return true;
    	case R.id.miNotes:
    		showView(notesView);
    		return true;
    	case R.id.miSolver:
    		showView(solverView);
    		return true;
    	// Map
    	case R.id.miMapView:
    		showView(mapView);
    		return true;
    	// Info
    	case R.id.miLogView:
    		showView(logView);
    		return true;
    	case R.id.miSpoilerView:
    		showView(spoilerView);
    		return true;
    	// Misc
    	case R.id.miClose:
    		finish();
    		return true;
    	case R.id.miDayNight:
    		changeDayNight();
    		return true;
    	case R.id.miSettings:
    		showView(settingsView);
    		return true;
		default:
			return super.onOptionsItemSelected(item);
    	}
    }    
    
    private void showView(ViewOptionsMenu view)
    {
    	if (aktView != null)
    		aktView.OnHide();
    	aktView = view;
    	frame.removeAllViews();
    	frame.addView((View) aktView);
    	aktView.OnShow();    
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
    
    public void changeDayNight()
    {
    	Boolean value = Config.GetBool("nightMode");
    	value = !value;
    	Config.Set("nightMode",value);
    }
    

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Global.Locator.setLocation(location);
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
		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			Global.Locator.setLocation(null);
		if (status == LocationProvider.OUT_OF_SERVICE)
			Global.Locator.setLocation(null);
	}
}
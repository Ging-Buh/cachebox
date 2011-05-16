package de.droidcachebox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Locator.Locator;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.CompassView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.FieldNotesView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.NotesView;
import de.droidcachebox.Views.SolverView;
import de.droidcachebox.Views.SpoilerView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Views.Forms.EditWaypoint;
import de.droidcachebox.Views.Forms.HintDialog;
import de.droidcachebox.Views.Forms.ScreenLock;
import de.droidcachebox.Views.Forms.Settings;
import de.droidcachebox.Database;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class main extends Activity implements SelectedCacheEvent,LocationListener {
    LayoutInflater inflater;
	private ImageButton buttonDB;
	private ImageButton buttonCache;
	private ImageButton buttonMap;
	private ImageButton buttonInfo;
	private ImageButton buttonMisc;
	private FrameLayout frame;
	private RelativeLayout TopLayout;
	private FrameLayout frameCacheName;
	
	
// Views
	private static Integer aktViewId = -1;
	private ViewOptionsMenu aktView = null;
	private CacheNameView cacheNameView;
	
	private MapView mapView;					// ID 0
	private CacheListView cacheListView;		// ID 1
	private WaypointView waypointView;			// ID 2
	private LogView logView;					// ID 3
	private DescriptionView descriptionView;	// ID 4
	private SpoilerView spoilerView;			// ID 5
	private NotesView notesView;				// ID 6
	private SolverView solverView;				// ID 7
	private CompassView compassView;			// ID 8
	private FieldNotesView fieldNotesView;		// ID 9
	private ArrayList<View> ViewList = new ArrayList<View>();
	
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
    
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
    	if (requestCode == 12345)
    	{
    		counterStopped = false;
    		counter.start();
    		return;
    	}
    	aktView.ActivityResult(requestCode, resultCode, data);
    }


    private class MyCount extends CountDownTimer {
    	public MyCount(long millisInFuture, long countDownInterval) {
    		 super(millisInFuture, countDownInterval);
    		 }        	
    	@Override
    	public void onFinish() {
    		startScreenLock();
//    		Toast.makeText(getApplicationContext(), "timer", Toast.LENGTH_LONG).show();
    	}
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			
		}        
    }
    MyCount counter = null;
    private boolean counterStopped = false;
    public void startScreenLock()
    {
		counter.cancel();
		counterStopped = true;
		final Intent mainIntent = new Intent().setClass( this, ScreenLock.class);
		this.startActivityForResult(mainIntent, 12345);
    }
    
    @Override
    public void onUserInteraction(){
    	if (counterStopped)
    		return;
    	if (counter != null)
    		counter.start();

    }
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	ActivityUtils.onActivityCreateSetTheme(this);    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        /*
        final Handler handler = new Handler();        
        aTimer = new Timer("ScreenLockTimer");
        TimerTask blinkTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                       
                       public void run() {   
                    	   Toast.makeText(getApplicationContext(), "timer", Toast.LENGTH_SHORT).show();
                       }
                });
            }
        };        
        aTimer.schedule(blinkTimerTask, 30000);
*/
        counter = new MyCount(60000, 60000);
        counter.start();

        
        // add Event Handler
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
        
        TopLayout=(RelativeLayout)this.findViewById(R.id.layoutTop);     
        
        
        frame = (FrameLayout)this.findViewById(R.id.layoutContent);
     
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
        
        mapView = new MapView(this, inflater);
        mapView.Initialize();
        mapView.CurrentLayer = MapView.Manager.GetLayerByName(Config.GetString("CurrentMapLayer"), Config.GetString("CurrentMapLayer"), "");
        Global.TrackDistance = Config.GetInt("TrackDistance");

        compassView = new CompassView(this, inflater);
        cacheListView = new CacheListView(this);
        waypointView = new WaypointView(this, this);
        logView = new LogView(this);
        fieldNotesView = new FieldNotesView(this);
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
        
        
        fillViewList();
        if (aktViewId != -1)
        {
        	// Zeige letzte gespeicherte View beim neustart der Activity
        	showView(aktViewId);
        	
        	// Initialisiere Icons neu.
        	 Global.InitIcons(this, Config.GetBool("nightMode"));
        	
        }
    }
    
    void fillViewList()
    {
    	ViewList.add(mapView);				// ID 0
    	ViewList.add(cacheListView);		// ID 1
    	ViewList.add(waypointView);			// ID 2
    	ViewList.add(logView);				// ID 3
    	ViewList.add(descriptionView);		// ID 4
    	ViewList.add(spoilerView);			// ID 5
    	ViewList.add(notesView);			// ID 6
    	ViewList.add(solverView);			// ID 7	
    	ViewList.add(compassView);			// ID 8	
    	ViewList.add(fieldNotesView);		// ID 9
    }

    @Override
    public void onDestroy() {
    		if (isFinishing())
    		{
                this.mWakeLock.release();
        		counter.cancel();
        		TrackRecorder.StopRecording();
        		locationManager.removeUpdates(this);
    		}
			super.onDestroy();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        counter.start();
        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop()
    {
        mSensorManager.unregisterListener(mListener);
		counter.cancel();
        super.onStop();
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
    	case R.id.miHint:
    		if (Global.selectedCache == null)
    			return true;
    		String hint = Global.selectedCache.Hint();
    		if (hint.equals(""))
    			return true;
    		
    		final Intent hintIntent = new Intent().setClass(this, HintDialog.class);
	        Bundle b = new Bundle();
	        b.putSerializable("Hint", hint);
	        hintIntent.putExtras(b);
    		this.startActivity(hintIntent);
    		return true;
    	case R.id.miFieldNotes:
    		showView(fieldNotesView);
    		return true;
    	// Misc
    	case R.id.miClose:
    		finish();
    		return true;
    	case R.id.miScreenLock:
    		startScreenLock();
    		return true;
    	case R.id.miDayNight:
    		Config.changeDayNight();
    		ActivityUtils.changeToTheme(this,Config.GetBool("nightMode")? ActivityUtils.THEME_NIGHT : ActivityUtils.THEME_DAY );
    		return true;
    	case R.id.miSettings:
    		final Intent mainIntent = new Intent().setClass( this, Settings.class);

    		this.startActivity(mainIntent);
    		
		
    		return true;
    	case R.id.miCompassView:
    		showView(compassView);
    		HideLayoutTop();
    		return true;	
    		
		default:
			return super.onOptionsItemSelected(item);
    	}
    }    
    
    
    private void showView(Integer viewId)
    {
    	showView((ViewOptionsMenu)ViewList.get(viewId));
    }
    
    private void showView(ViewOptionsMenu view)
    {
    	if (aktView != null)
    		aktView.OnHide();
    	aktView = view;
    	frame.removeAllViews();
    	frame.addView((View) aktView);
    	aktView.OnShow();  
    	aktViewId=ViewList.indexOf(aktView);
    	ShowLayoutTop();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      if (v == buttonDB)
    	  inflater.inflate(R.menu.menu_db, menu);
      else if (v == buttonCache)
      {
    	  inflater.inflate(R.menu.menu_cache, menu);
    	  Global.Translations.TranslateMenuItem(menu, R.id.miSolver, "Solver");
    	  Global.Translations.TranslateMenuItem(menu, R.id.miNotes, "Notes");
    	  Global.Translations.TranslateMenuItem(menu, R.id.miDescription, "Description");
    	  Global.Translations.TranslateMenuItem(menu, R.id.miWaypoints, "Waypoints");
    	  
      } else if (v == buttonMap)
    	  inflater.inflate(R.menu.menu_map, menu);
      else if (v == buttonInfo)
      {
    	  inflater.inflate(R.menu.menu_info, menu);
    	  
    	  // Menu Item Hint enabled / disabled
    	  boolean enabled = false;
    	  if ((Global.selectedCache != null) && (!Global.selectedCache.Hint().equals("")))
    		  enabled = true;
    	  MenuItem mi = menu.findItem(R.id.miHint);
    	  if (mi != null)
    		  mi.setEnabled(enabled);
    	  Global.Translations.TranslateMenuItem(menu, R.id.miHint, "hint");
      } else if (v == buttonMisc)
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
		Global.Locator.setLocation(location);
		PositionEventList.Call(location);
/*
            if (!initialResortAfterFirstFixCompleted && sender.LastValidPosition.Valid)
            {
                if (Global.SelectedCache == null)
                    Resort(null);
                initialResortAfterFirstFixCompleted = true;
            }
            if (!initialFixSoundCompleted && Global.Locator.NumSatellites > 0)
            {
                Sound oSound = new Sound(Global.AppPath + "\\data\\sounds\\GPS_Fix.wav");
                oSound.Play();
                initialFixSoundCompleted = true;
            }
*/
            TrackRecorder.recordPosition();
/*
            if (curView != null)
                (curView as ViewPanel).OnPositionChanged(Global.Locator);

            // schau die 50 nächsten Caches durch, wenn einer davon näher ist als der aktuell nächste -> umsortieren und raus
            // only when showing Map or cacheList
            if (Global.autoResort && (curView == views[4] || curView == views[2]))
            {
                int z = 0;
                if (!(Global.NearestCache == null))
                    foreach (Geocaching.Cache cache in Geocaching.Cache.Query)
                    {
                        z++;
                        if (z >= 50)
                            return;
                        if (cache.Distance < Global.NearestCache.Distance)
                        {
                            Resort(null);
                            Sound oSound = new Sound(Global.AppPath + "\\data\\sounds\\AutoResort.wav");
                            oSound.Play();
                            return;
                        }
                    }
            }
		
 */
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
	
	
	public void EditCoordinate(Coordinate coord)
	{
		
	}

	private void HideLayoutTop()
	{
		TopLayout.setVisibility(View.GONE);
	}

	private void ShowLayoutTop()
	{
		TopLayout.setVisibility(View.VISIBLE);
	}
	
}
package de.droidcachebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Log.ILog;
import CB_Core.Log.Logger;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import de.droidcachebox.ExtAudioRecorder;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Custom_Controls.DebugInfoPanel;
import de.droidcachebox.Custom_Controls.Mic_On_Flash;
import de.droidcachebox.Custom_Controls.downSlider;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.droidcachebox.Events.CachListChangedEventList;
import de.droidcachebox.Events.CacheListChangedEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Locator.Locator;
import de.droidcachebox.Views.AboutView;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.CompassView;
import de.droidcachebox.Views.EmptyViewTemplate;
import de.droidcachebox.Views.FieldNotesView;
import de.droidcachebox.Views.JokerView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.NotesView;
import de.droidcachebox.Views.SolverView;
import de.droidcachebox.Views.SpoilerView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.FilterSettings.EditFilterSettings;
import de.droidcachebox.Views.FilterSettings.PresetListView;
import de.droidcachebox.Views.Forms.HintDialog;
import de.droidcachebox.Views.Forms.ImportDialog;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.ScreenLock;
import de.droidcachebox.Views.Forms.SelectDB;
import de.droidcachebox.Views.Forms.Settings;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Database;
import de.droidcachebox.Geocaching.CacheList;
import android.app.Activity;
import android.database.Cursor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.location.GpsStatus;

public class main extends Activity implements SelectedCacheEvent,LocationListener,CacheListChangedEvent, GpsStatus.NmeaListener, ILog 
{
	/*
	 * private static member
	 */
	
		private static Integer aktViewId = -1;
	    private static long GPSTimeStamp = 0;
		private static MapView mapView = null;					// ID 0
		private static CacheListView cacheListView = null;		// ID 1
		private static WaypointView waypointView = null;		// ID 2
		private static LogView logView = null;					// ID 3
		private static DescriptionView descriptionView = null;	// ID 4
		private static SpoilerView spoilerView = null;			// ID 5
		private static NotesView notesView = null;				// ID 6
		private static SolverView solverView = null;			// ID 7
		private static CompassView compassView = null;			// ID 8
		private static FieldNotesView fieldNotesView = null;	// ID 9
		private static EmptyViewTemplate TestEmpty = null;		// ID 10
		private static AboutView aboutView = null;				// ID 11
		private static JokerView jokerView = null;				// ID 12
	    
	
		// Media
	    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 61216516;
	    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 61216517;
	    private static Uri cameraVideoURI;
	    private static File mediafile = null;
	    private static String mediafilename = null;
	    private static String mediaTimeString = null; 
	    private static String basename = null;
	    private static String mediaCacheName = null;
	    private static Coordinate mediaCoordinate = null; 
	    
	    private static Boolean mVoiceRecIsStart = false;
	/*
	 * public static member
	 */
		public static Activity mainActivity;
		public static Boolean isRestart=false;
	
    
    /*
	 * private member
	 */
		private LayoutInflater inflater;
		private int width;
	    private int height;
	    private ExtAudioRecorder extAudioRecorder = null;
	    private boolean initialResortAfterFirstFixCompleted = false;
	    private boolean initialFixSoundCompleted = false;
	    private boolean approachSoundCompleted = false;
	    
		private ImageButton buttonDB;
		private ImageButton buttonCache;
		private ImageButton buttonNav;
		private ImageButton buttonInfo;
		private ImageButton buttonMisc;
		private FrameLayout frame;
		private RelativeLayout TopLayout;
		private FrameLayout frameCacheName;
		private downSlider InfoDownSlider;
		
		private Mic_On_Flash Mic_Icon;
		private DebugInfoPanel debugInfoPanel;
	
		// Views
		private ViewOptionsMenu aktView = null;
		private CacheNameView cacheNameView;
		
		private ArrayList<View> ViewList = new ArrayList<View>();
		private int lastBtnDBView=1;
	    private int lastBtnCacheView=4;
	    private int lastBtnNavView=0;
	    private int lastBtnInfoView=3;
	    private int lastBtnMiscView=11;
	    ArrayList <Integer> btnDBActionIds ;
	    ArrayList <Integer> btnCacheActionIds ;
	    ArrayList <Integer> btnNavActionIds ;
	    ArrayList <Integer> btnInfoActionIds ;
	    ArrayList <Integer> btnMiscActionIds ;
	    
		// Powermanager
	    protected PowerManager.WakeLock mWakeLock;
	    // GPS
		public LocationManager locationManager;
		// Compass
	    private SensorManager mSensorManager;
	    private Sensor mSensor;
	    private float[] mCompassValues;
	    
		// to store, which menu should be viewd
		private enum nextMenuType { nmDB, nmCache, nmMap, nmInfo, nmMisc }
		private nextMenuType nextMenu = nextMenuType.nmDB;
		
		private Boolean getVoiceRecIsStart(){return mVoiceRecIsStart;}
		
		// Screenlock Counter
		private ScreenLockTimer counter = null;
		private boolean counterStopped = false;
		
		private IconContextMenu icm;
		
	/*
	 * Classes
	 */
	 private class ScreenLockTimer extends CountDownTimer 
	 {
    	public ScreenLockTimer(long millisInFuture, long countDownInterval) 
    	{
    		 super(millisInFuture, countDownInterval);
    	}        	
    	@Override
    	public void onFinish() 
    	{
    		startScreenLock();
//    		Toast.makH_LONG).show();
    	}
		@Override
		public void onTick(long millisUntilFinished) 
		{
		}        
    }
   
	
	 /*
	  * Overrides
	  */
	   	
	 /** Called when the activity is first created. */
	    @Override public void onCreate(Bundle savedInstanceState) {
	    	ActivityUtils.onActivityCreateSetTheme(this);    	
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        if(!Config.GetBool("AllowLandscape"))
	        {
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }
	        else
	        {
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	        }
	        
	        Logger.Add(this);
	        
	        	        
	        try
	        {
	        setContentView(R.layout.main);
	        } catch (Exception exc)
	        {
	        	Logger.Error("main.onCreate()","setContentView", exc);
	        }
	        
	        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        mainActivity= this;
			mainActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

	        
	        int Time = ((Config.GetInt("LockM")*60)+Config.GetInt("LockSec"))*1000;
	        counter = new ScreenLockTimer(Time, Time);
	        counter.start();

	        findViewsById();
	        
	        if (aktViewId == -1)Logger.General("------ Start Rev: " + Global.CurrentRevision + "-------");
	        
	        // add Event Handler
	        SelectedCacheEventList.Add(this);
	        CachListChangedEventList.Add(this);
	        
	        WindowManager w = getWindowManager();
	        Display d = w.getDefaultDisplay();
	        width = d.getWidth();
	        height = d.getHeight();

	         
	        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);       
	        
	        
	        // Ausschalten verhindern
	        /* This code together with the one in onDestroy() 
	         * will make the screen be always on until this Activity gets destroyed. */
	        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	        this.mWakeLock.acquire();
	        
	        initialLocationManager();
	        initialMapView();
	        initialViews();
	        initalMicIcon(); 
	        initialButtons();
	        initialCaheInfoSlider();
	        
	        if(Global.SelectedCache()==null)
	        {
		        CacheList cacheList = Database.Data.Query;
		        if( cacheList.size() > 0 ) {
					Cache cache = cacheList.get(0);
					Global.SelectedCache(cache);
		        }
	        }
	        else // Activity wurde neu Gestartet
	        {
	        	Global.SelectedCache(Global.SelectedCache());
	        }
	        
	        if (aktViewId != -1)
	        {
	        	// Zeige letzte gespeicherte View beim neustart der Activity
	        	showView(aktViewId);
	        	
	        	// Initialisiere Icons neu.
	        	 Global.InitIcons(this, Config.GetBool("nightMode"));
	        }
	        else
	        {
	        	// Zeige About View als erstes!
	        	showView(11);
	        }
	        
	        
	        CacheListChangedEvent();
	        
	        
	        setDebugVisible();
	        
	        if (Config.GetBool("TrackRecorderStartup"))TrackRecorder.StartRecording();
	        
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
	   
	    @Override public void onUserInteraction()
	    {
	    	if (counterStopped)
	    		return;
	    	if (counter != null)
	    		counter.start();

	    }
    
	    @Override public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
		{
	    	approachSoundCompleted = false;
		}

	    public void newLocationReceived (Location location)
	    {
			Global.Locator.setLocation(location);
			PositionEventList.Call(location);
			
			InfoDownSlider.setNewLocation(location);

	        if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)
	        {
	            if (Global.SelectedCache() == null)
	                Database.Data.Query.Resort();
	            initialResortAfterFirstFixCompleted = true;
	        }
	        if (!initialFixSoundCompleted && GlobalCore.LastValidPosition.Valid)
	        {
	        	Global.PlaySound("GPS_Fix.wav");
	        	initialFixSoundCompleted = true;
	        }
	        
	        if (Global.SelectedCache() != null)
	        {
		        float distance = Global.SelectedCache().Distance(false);
	            if (Global.SelectedWaypoint() != null)
	            {
	            	distance = Global.SelectedWaypoint().Distance();
	            }
		        
		        if (!approachSoundCompleted && (distance< Config.GetInt("SoundApproachDistance")))
		        {
		        	Global.PlaySound("Approach.wav");
		        	approachSoundCompleted = true;
		        }
	        }
	        

			TrackRecorder.recordPosition();
	        // schau die 50 nächsten Caches durch, wenn einer davon näher ist als der aktuell nächste -> umsortieren und raus
	        // only when showing Map or cacheList
			if (!Global.ResortAtWork)
			{
				if (Global.autoResort && ((aktView == mapView) || (aktView == cacheListView)))
				{
	                int z = 0;
	                if (!(Global.NearestCache() == null))
	                {
	                    for (Cache cache : Database.Data.Query)
	                    {
	                        z++;
	                        if (z >= 50)
	                            return;
	                        if (cache.Distance(true) < Global.NearestCache().Distance(true))
	                        {
	                            Database.Data.Query.Resort();
	                            Global.PlaySound("AutoResort.wav");
	                            return;
	                        }
	                    }
	                }
				}
			}
	    	
	    }
	    
		@Override public void onLocationChanged(Location location) {
			
			if ( location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)) // Neue Position von GPS-Empfänger
			{
				newLocationReceived (location);
		        GPSTimeStamp = java.lang.System.currentTimeMillis();
		        return;
			}
			
			if ( location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) // Neue Position von Netzwerk
			{
				if ((java.lang.System.currentTimeMillis() - GPSTimeStamp) > 10000) //Wenn 10 Sekunden kein gültiges GPS Signal
				{
					newLocationReceived (location);
		    		Toast.makeText(mainActivity, "Network-Position", Toast.LENGTH_SHORT).show();
				}
			}
			
			
			
		}

		@Override public void onProviderDisabled(String provider) 
		{
		}

		@Override public void onProviderEnabled(String provider) 
		{
		}

		@Override public void onStatusChanged(String provider, int status, Bundle extras) 
		{
		}
		
	    @Override public boolean onKeyDown(int keyCode, KeyEvent event) 
	    {
		    Log.d("SolHunter", "Key event code "+keyCode);
		    if (keyCode == KeyEvent.KEYCODE_BACK) 
		    {
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
					{
					    @Override
					    public void onClick(DialogInterface dialog, int which) 
					    {
					        switch (which)
					        {
					        case -1:
					            //Yes button clicked
					            try {
									finish();
								} catch (Throwable e) {
									
									e.printStackTrace();
								}
					            break;
					        case -2:
					            //No button clicked
					        	dialog.dismiss();
					            break;
					        }
					    }
					};
					MessageBox.Show("Close DroidCB?","Question",MessageBoxButtons.YesNo,MessageBoxIcon.Question,dialogClickListener);
					
				return true;
	    	}
	    	return false;
	    }
		
	    @Override public void CacheListChangedEvent() 
		{
			//Database.Data.Query.size();
			if ((Global.LastFilter == null) || (Global.LastFilter.ToString().equals("")) || (Global.LastFilter.ToString().equals(PresetListView.presets[0])))
	        {
				this.buttonDB.getBackground().clearColorFilter();
	        }
	        else
	        {
	        	this.buttonDB.getBackground().setColorFilter(Color.argb(255, 250, 128, 114), Mode.MULTIPLY); //Color.Salmon;
	        };        
		}
		
		@Override protected void onActivityResult(int requestCode, int resultCode,Intent data) 
	    {
			// SelectDB
	    	if (requestCode == 546132)
	    	{
	    		if (resultCode == RESULT_OK)
	    		{
//	    			Toast.makeText(getApplicationContext(), "DB wechsel momentan nur mit Neustart...", Toast.LENGTH_LONG).show();
	                Database db = new Database(Database.DatabaseType.CacheBox, mainActivity);
	                if (!db.StartUp(Config.GetString("DatabasePath")))
	                    return;
	                Database.Data = null;
	                Database.Data = db;
/*	    		
	                SqlCeCommand command = new SqlCeCommand(" select GcCode from FieldNotes WHERE Type = 1 ", Database.FieldNotes.Connection);
	                SqlCeDataReader reader = command.ExecuteReader();
	                if (reader == null)
	                    throw new Exception("Startup: Cannot execute SQL statement Copy Founds to TB");
	                string GcCode = "";
	                while (reader.Read())
	                    GcCode += "'" + reader.GetString(0) + "', ";
	                if (GcCode.Length > 0)
	                {
	                    GcCode = GcCode.Substring(0, GcCode.Length - 2);
	                    SqlCeCommand commandUpdate = new SqlCeCommand(" UPDATE Caches SET Found = 1 WHERE GcCode IN (" + GcCode + ") ", Database.Data.Connection);
	                    int founds = commandUpdate.ExecuteNonQuery();
	                }
*/
	                Global.LastFilter = (Config.GetString("Filter").length() == 0) ? new FilterProperties(PresetListView.presets[0]) : new FilterProperties(Config.GetString("Filter"));
//	                filterSettings.LoadFilterProperties(Global.LastFilter);

					String sqlWhere = Global.LastFilter.getSqlWhere();
					Logger.General("Main.ApplyFilter: " + sqlWhere);
					Database.Data.Query.clear();
					Database.Data.Query.LoadCaches(sqlWhere);

//	                Database.Data.GPXFilenameUpdateCacheCount();

	                Global.SelectedCache(null);
	                Global.SelectedWaypoint(null, null);

	                // after the database is changed the custom MapPacks has to be loaded
//	                loadMapPacks(true);
	                
	                CachListChangedEventList.Call();
	    		}
	    		return;
	    	}
	    	// Intent Result Take Photo
	    	if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) 
	    	{
	            if (resultCode == RESULT_OK)
	            {
	                Log.d("DroidCachebox","Picture taken!!!");
	                CacheDraw.ReloadSpoilerRessources(Global.selectedCache);
	                String MediaFolder = Config.GetString("UserImageFolder");
	            	String TrackFolder = Config.GetString("TrackFolder");
	            	String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/"); 
	            	// Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und die Koordinaten nach der Aufnahme verwendet werden.
	            	mediaTimeString = Global.GetTrackDateTimeString();
	            	TrackRecorder.AnnotateMedia(basename + ".jpg", relativPath + "/" + basename + ".jpg", GlobalCore.LastValidPosition, mediaTimeString);
	            	
	            	return;
	            } else
	            {
	                Log.d("DroidCachebox","Picture NOT taken!!!");
	                return;
	            }
	        }

	    	// Intent Result Record Video
	    	if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
	            if (resultCode == RESULT_OK){
	                Log.d("DroidCachebox","Video taken!!!");
	                //Global.selectedCache.ReloadSpoilerRessources();

	                String[] projection = { MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE  };
	                Cursor cursor = managedQuery(cameraVideoURI, projection, null, null, null);
	                int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
	                cursor.moveToFirst();
	                String recordedVideoFilePath = cursor.getString(column_index_data);
	                
	                String ext = FileIO.GetFileExtension(recordedVideoFilePath);
	                String MediaFolder = Config.GetString("UserImageFolder");

	                // Video in Media-Ordner verschieben
	                File  source = new File(recordedVideoFilePath);
	                File destination = new File(MediaFolder + "/" + basename + "." + ext);
	                // Datei wird umbenannt/verschoben
	                if(!source.renameTo(destination))
	                {
	                	Log.d("DroidCachebox","Fehler beim Umbenennen der Datei: " + source.getName());
	                }
	                
	            	String TrackFolder = Config.GetString("TrackFolder");
	            	String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/"); 
	            	TrackRecorder.AnnotateMedia(basename + "." + ext, relativPath + "/" + basename + "." + ext, mediaCoordinate , mediaTimeString);
	            	
	            	return;
	            } else
	            {
	                Log.d("DroidCachebox","Video NOT taken!!!");
	                return;
	            }
	        }

	    	if (requestCode == 12345)
	    	{
	    		counterStopped = false;
	    		counter.start();
	    		return;
	    	}
	    	if (requestCode == 123456)
	    	{
	    		
	    		return;
	    	}
	    	
	    	aktView.ActivityResult(requestCode, resultCode, data);
	    }

		@Override public void onCreateContextMenu(final ContextMenu menu, View v,ContextMenuInfo menuInfo) 
		    {
		      super.onCreateContextMenu(menu, v, menuInfo);
		      MenuInflater inflater = getMenuInflater();
		      
		      if (v instanceof ViewOptionsMenu)
		      {
		    	  int id = ((ViewOptionsMenu)v).GetContextMenuId();
		    	  if (id > 0)
		    	  {
		        	  inflater.inflate(id, menu);
		        	  ((ViewOptionsMenu)v).BeforeShowContextMenu(menu);
		    	  }
		    	  return;
		      }
		      
		      
		      
		      if (v == buttonDB)
		      {
		    	initialBtnDbContextMenu();
		      }
		  	  else if (v == buttonCache)
		      {
		   		  initialBtnCacheContextMenu();
		      } 
		  	  else if (v == buttonNav)
		      {
		    	initialBtnNavContextMenu();
		  	  }
		      else if (v == buttonInfo)
		      {
		    	initialBtnInfoContextMenu();
		      } 
		      else if (v == buttonMisc)
		      {   		
		    	initialBtnMiscContextMenu();
		      }
		    }

		@Override protected void onResume()
		{
			super.onResume();
			counter.start();
			mSensorManager.registerListener(mListener, mSensor,SensorManager.SENSOR_DELAY_GAME);
			if(!Config.GetBool("AllowLandscape"))
	        {
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }
	        else
	        {
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	        }
		}

		@Override protected void onStop()
		{
			mSensorManager.unregisterListener(mListener);
			counter.cancel();
			super.onStop();
		}

		@Override public void onDestroy() 
		{
			frame.removeAllViews();
			if(isRestart)
			{
				super.onDestroy();
				isRestart=false;
			}
			else
			{
	    		if (isFinishing())
	    		{
	                Config.Set("MapInitLatitude", mapView.center.Latitude);
	                Config.Set("MapInitLongitude", mapView.center.Longitude);
	                Config.AcceptChanges();

	                this.mWakeLock.release();
	        		counter.cancel();
	        		TrackRecorder.StopRecording();
	        		// GPS Verbindung beenden
	        		locationManager.removeUpdates(this);
	        		// Voice Recorder stoppen
	                if (extAudioRecorder != null) 
	                {            
			            extAudioRecorder.stop();
			            extAudioRecorder.release();
			            extAudioRecorder = null;
	                }
	                Global.SelectedCache(null);
	                SelectedCacheEventList.list.clear();
	                PositionEventList.list.clear();
	                SelectedCacheEventList.list.clear();
	                SelectedLangChangedEventList.list.clear();
	                CachListChangedEventList.list.clear();
	                if (aktView != null)
	                {
	                	aktView.OnHide();
	                	aktView.OnFree();
	                }
	                aktView = null;
	                for (View vom : ViewList)
	                {
	                	if (vom instanceof ViewOptionsMenu)
	                	{
	                		((ViewOptionsMenu)vom).OnFree();
	                	}	                		
	                }
	                ViewList.clear();
	                TestEmpty = null;
	                cacheListView = null;
	                mapView = null;
	                notesView = null;
	                jokerView = null;
	                descriptionView = null;
	                mainActivity = null;
	                debugInfoPanel.OnFree();
	                debugInfoPanel = null;
	    			super.onDestroy();
					System.exit(0);
	    		} else
	    		{
	    			if (aktView != null)
	    				aktView.OnHide();
	    			super.onDestroy();
	    		}
			}
	    } 
	
		


    
   
    public void startScreenLock()
    {
		counter.cancel();
		counterStopped = true;
		// ScreenLock nur Starten, wenn der Config Wert größer 10 sec ist.
		// Das verhindert das selber aussperren!
		if(!(Config.GetInt("LockM")==0 && Config.GetInt("LockSec")<10))
		{
			final Intent mainIntent = new Intent().setClass( this, ScreenLock.class);
			this.startActivityForResult(mainIntent, 12345);
		}
    }
    
    
	
	/*
	 *  Handler
	 */
    
    
    private final View.OnClickListener ButtonOnClick = new OnClickListener() 
    {
		@Override
		public void onClick(View v) 
		{
			if(v==buttonDB){showView(lastBtnDBView);}
			
			else if(v==buttonCache){showView(lastBtnCacheView);}
			
			else if(v==buttonNav){showView(lastBtnNavView);}
			
			else if(v==buttonInfo){showView(lastBtnInfoView);}
			
			else if(v==buttonMisc){showView(lastBtnMiscView);}
		}
	};
	
	    
	private final SensorEventListener mListener = new SensorEventListener() 
	    {
	        public void onSensorChanged(SensorEvent event) 
	        {
	        	mCompassValues = event.values;
	        	Global.Locator.setCompassHeading(mCompassValues[0]); 
	        	PositionEventList.Call(mCompassValues[0]);
	        }

	        public void onAccuracyChanged(Sensor sensor, int accuracy) 
	        {
	        }
	    };
	
	   	    
	    
    
	public void showView(Integer ID)
    {
    	if(!(ID>ViewList.size()))
    	{
    		showView((ViewOptionsMenu)ViewList.get(ID));
    	}
    	else
    	{
    		switch(ID)
    		{
    		case 102: //Settings
    			final Intent mainIntent = new Intent().setClass( mainActivity, Settings.class);
        		Bundle b = new Bundle();
    		    b.putSerializable("Show", -1);
    		    mainIntent.putExtras(b);
        		mainActivity.startActivity(mainIntent);
        		break;
        		
    		case 101: // Filtersettings
    			final Intent mainIntent1 = new Intent().setClass( mainActivity, EditFilterSettings.class);
	    		mainActivity.startActivity(mainIntent1);
	    		break;
	    	
    		case 103: // Filtersettings
    			final Intent mainIntent2 = new Intent().setClass( mainActivity, ImportDialog.class);
	    		mainActivity.startActivity(mainIntent2);
	    		break;
	    	
    		}
    		    		
    	}
    	
    	// zuordnung zur letzten aktion
    	if(btnDBActionIds.contains(ID)){lastBtnDBView=ID; return;}
    	if(btnCacheActionIds.contains(ID)){lastBtnCacheView=ID; return;}
    	if(btnNavActionIds.contains(ID)){lastBtnNavView=ID; return;}
    	if(btnInfoActionIds.contains(ID)){lastBtnInfoView=ID; return;}
    	if(btnMiscActionIds.contains(ID)){lastBtnMiscView=ID; return;}
    	
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
    }
    
    

    /*
	 * Initial ContextMenu Methods
	 */

	private void initialBtnMiscContextMenu() 
	{
		icm = new IconContextMenu(this, R.menu.menu_misc);
		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
			
			@Override
			public void onIconContextItemSelected(MenuItem item, Object info) {
				switch (item.getItemId())
		    	{
				// Misc
		    	case R.id.miScreenLock:
			        startScreenLock();
		    		break;
		    		
		    	case R.id.miDayNight:
		    		frame.removeAllViews();
		    		Config.changeDayNight();
		    		ActivityUtils.changeToTheme(mainActivity,Config.GetBool("nightMode")? ActivityUtils.THEME_NIGHT : ActivityUtils.THEME_DAY );
		    		Toast.makeText(mainActivity, "changeDayNight", Toast.LENGTH_SHORT).show();
		    		break;
		    		
		    	case R.id.miSettings:
		    		showView(102);
		    		break;
		    		
		    	case R.id.miVoiceRecorder:
		    		if (!getVoiceRecIsStart()) // Voice Recorder starten
		    		{
			            Log.d("DroidCachebox", "Starting voice recorder on the phone...");
			            
			    		//define the file-name to save voice taken by activity
			            String directory = Config.GetString("UserImageFolder");
			            if (!FileIO.DirectoryExists(directory))
			            {
			                Log.d("DroidCachebox", "Media-Folder does not exist...");
			                break;
			            }
			           
			            basename = Global.GetDateTimeString();
			            
			            if (Global.selectedCache != null)
			            {
			            	String validName=FileIO.RemoveInvalidFatChars(Global.selectedCache.GcCode + "-" + Global.selectedCache.Name);
			            	mediaCacheName = validName.substring(0,(validName.length()>32)? 32 : validName.length());
			                //Title = Global.selectedCache.Name;
			            }
			            else
			            {
			            	mediaCacheName = "Voice";
			            }
	
			            basename += " " + mediaCacheName;
			            mediafilename = (directory + "/" + basename + ".wav");
			            
			            // Start recording
			            //extAudioRecorder = ExtAudioRecorder.getInstanse(true);	  // Compressed recording (AMR)
			            extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed recording (WAV)

			            extAudioRecorder.setOutputFile(mediafilename);
			            extAudioRecorder.prepare();
			            extAudioRecorder.start();

			            String MediaFolder = Config.GetString("UserImageFolder");
		            	String TrackFolder = Config.GetString("TrackFolder");
		            	String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/"); 
		            	// Da eine Voice keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
		            	TrackRecorder.AnnotateMedia(basename + ".wav", relativPath + "/" + basename + ".wav", GlobalCore.LastValidPosition, Global.GetTrackDateTimeString());
			    		Toast.makeText(mainActivity, "Start Voice Recorder", Toast.LENGTH_SHORT).show();

			            setVoiceRecIsStart(true);
			    		counter.cancel();			// Während der Aufnahme Screen-Lock-Counter stoppen
			    		counterStopped = true;

			    		break;	
		    		}
		    		else
		    		{	// Voice Recorder stoppen
			            Log.d("DroidCachebox", "Stoping voice recorder on the phone...");
			            // Stop recording
			    		setVoiceRecIsStart(false);
			            break;
		    		}
		    		
		    	case R.id.miTakePhoto:
		            Log.d("DroidCachebox", "Starting camera on the phone...");
		            
		    		//define the file-name to save photo taken by Camera activity
		            String directory = Config.GetString("UserImageFolder");
		            if (!FileIO.DirectoryExists(directory))
		            {
		                Log.d("DroidCachebox", "Media-Folder does not exist...");
		                break;
		            }
		           
		            basename = Global.GetDateTimeString();
		            
		            if (Global.selectedCache != null)
		            {
		            	String validName=FileIO.RemoveInvalidFatChars(Global.selectedCache.GcCode + "-" + Global.selectedCache.Name);
		            	mediaCacheName = validName.substring(0,(validName.length()>32)? 32 : validName.length());
		                //Title = Global.selectedCache.Name;
		            }
		            else
		            {
		            	mediaCacheName = "Image";
		            }

		            basename += " " + mediaCacheName;
		            mediafile = new File(directory + "/" + basename + ".jpg");

		    		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediafile));
		    		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		    		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

		    		break;
		    		
		    	case R.id.miRecordVideo:
		            Log.d("DroidCachebox", "Starting video on the phone...");
		            
		    		//define the file-name to save video taken by Camera activity
		            directory = Config.GetString("UserImageFolder");
		            if (!FileIO.DirectoryExists(directory))
		            {
		                Log.d("DroidCachebox", "Media-Folder does not exist...");
		                break;
		            }
		           
		            basename = Global.GetDateTimeString();
		            
		            if (Global.selectedCache != null)
		            {
		            	String validName=FileIO.RemoveInvalidFatChars(Global.selectedCache.GcCode + "-" + Global.selectedCache.Name);
		            	mediaCacheName = validName.substring(0,(validName.length()>32)? 32 : validName.length());
		                //Title = Global.selectedCache.Name;
		            }
		            else
		            {
		            	mediaCacheName = "Video";
		            }

		            basename += " " + mediaCacheName;
		            mediafile = new File(directory + "/" + basename + ".3gp");
		            
	            	// Da ein Video keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
	            	mediaTimeString = Global.GetTrackDateTimeString();
	            	mediaCoordinate = GlobalCore.LastValidPosition;
	            	
		    		ContentValues values = new ContentValues();  
		    		values.put(MediaStore.Video.Media.TITLE, "captureTemp.mp4");  
		    		cameraVideoURI = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);  

		    		final Intent videointent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		    		videointent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediafile));
		    		videointent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		    		//videointent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAXIMUM_VIDEO_SIZE);               

		    		startActivityForResult(videointent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
		    		break;	
		    		
		    		
		    	case R.id.miAbout:
		    		showView(11);
		    		break;
		    	
		    	case R.id.miTestEmpty:
		    		showView(10);
		    		break;
		    		
		    	case R.id.miImport:
		    		showView(103);
		    		break;
		    		
		    	default:
					
		    	}
		    }
		});
		
		Menu IconMenu=icm.getMenu();
		Global.TranslateMenuItem(IconMenu, R.id.miSettings, "settings");
		Global.TranslateMenuItem(IconMenu, R.id.miAbout, "about");
      	try
    	{
    		MenuItem mi = IconMenu.findItem(R.id.miVoiceRecorder);
    		if (mi != null)
    			if (!getVoiceRecIsStart())
    				mi.setTitle("Voice Recorder");
    			else
    				mi.setTitle("Stop Voice Rec.");
    	} catch (Exception exc)
    	{ }
	  	  
	  	  icm.show();
	}

	private void initialBtnInfoContextMenu() 
	{
		icm = new IconContextMenu(this, R.menu.menu_info);
  		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() 
  		{
  			
  			@Override
  			public void onIconContextItemSelected(MenuItem item, Object info) 
  			{
  				switch (item.getItemId())
  		    	{
  			// Info
  		    	case R.id.miLogView:
  		    		showView(3);
  		    		break;
  		    	case R.id.miSpoilerView:
  		    		showView(5);
  		    		break;
  		    	case R.id.miHint:
  		    		if (Global.selectedCache == null)
  		    			break;
  		    		String hint = Database.Hint(Global.selectedCache);
  		    		if (hint.equals(""))
  		    			break;
  		    		
  		    		final Intent hintIntent = new Intent().setClass(mainActivity, HintDialog.class);
  			        Bundle b = new Bundle();
  			        b.putSerializable("Hint", hint);
  			        hintIntent.putExtras(b);
  			        mainActivity.startActivity(hintIntent);
  		    		break;
  		    	case R.id.miFieldNotes:
  		    		showView(9);
  		    		// beim Anzeigen der FieldNotesView gleich das Optionsmenü zeigen
  		    		openOptionsMenu();
  		    		break;				
  		    	case R.id.miTelJoker:
  		    		//showView(9);
  		    		// beim Anzeigen der FieldNotesView gleich das Optionsmenü zeigen
  		    		//openOptionsMenu();
  		    		if (Global.Jokers.isEmpty())
  		    		{ // Wenn Telefonjoker-Liste leer neu laden
	  		    		try
	  		    		{
	  		    			URL url = new URL("http://www.gcjoker.de/cachebox.php?md5=" + Config.GetString("GcJoker") + "&wpt=" + Global.selectedCache.GcCode);
	  		    			URLConnection urlConnection = url.openConnection();
	  		    			HttpURLConnection httpConnection=(HttpURLConnection)urlConnection;
	  		    			
	  		    			//Get the HTTP response code
	  		    			if(httpConnection.getResponseCode()==HttpURLConnection.HTTP_OK)
	  		    			{
	  		    				InputStream inputStream = httpConnection.getInputStream();
	  		    				if(inputStream != null)
	  		    				{
	  		    					String line;
	  		    					try
	  		    					{
	  		    						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	  		    						while((line=reader.readLine()) != null)
	  		    						{
	  		    							String[] s = line.split(";",7);
	  		    							try
	  		    							{
	  		    								if (s[0].equals("2")) 	// 2 entspricht Fehler, Fehlerursache ist in S[1]
	  		    								{
	  		    									MessageBox.Show(s[1],null);
	  		    									break;
	  		    								}
	  		    								if (s[0].equals("1")) 	// 1 entspricht Warnung, Ursache ist in S[1]
	  		    								{						// es können aber noch gültige Einträge folgen
	  		    									MessageBox.Show(s[1],null);
	  		    								}
	  		    								if (s[0].equals("0")) 	// Normaler Eintrag
	  		    								{
	  		    									Global.Jokers.AddJoker(s[1], s[2], s[3], s[4], s[5], s[6]);
	  		    								}
	  		    							} catch (Exception exc)
	  		    							{
	  		    					        	Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", exc);
	 		    								break;
	  		    							}
	  		    						}
	  		    						if (Global.Jokers.isEmpty()){
	  		    							MessageBox.Show("Keine Joker bekannt",null);
	  		    						}
	  		    						else {
		  		    			        	Logger.General("Open JokerView...");
		 		    	  		    		showView(12);
	  		    						}
	  		    					}
	  		    					finally
	  		    					{
	  		    						inputStream.close();
	  		    					}
	  		    				}
	  		    			 }
	  		    		}
	  		    		catch(MalformedURLException urlEx){
	  		    			Logger.Error("main.initialBtnInfoContextMenu()", "MalformedURLException HTTP response Jokers", urlEx);
	  		                Log.d("DroidCachebox",urlEx.getMessage());		
	  		    			 }
	  		    		catch (IOException ioEx){
	  		    			Logger.Error("main.initialBtnInfoContextMenu()", "IOException HTTP response Jokers", ioEx);
	  		                Log.d("DroidCachebox",ioEx.getMessage());	
	  		                MessageBox.Show("Fehler bei Internetzugriff",null);
	  		    			 }
	  		    		catch(Exception ex){
	  		    			Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", ex);
	  		                Log.d("DroidCachebox",ex.getMessage());		
	  		    		}
  		    	}
   		    		break;				
  		    	}
  		    }
  		});
    	  
    	  // Menu Item Hint enabled / disabled
    	  boolean enabled = false;
    	  if ((Global.selectedCache != null) && (!Database.Hint(Global.selectedCache).equals("")))
    		  enabled = true;
    	  MenuItem mi = icm.menu.findItem(R.id.miHint);
    	  if (mi != null)
    		  mi.setEnabled(enabled);
    	  mi = icm.menu.findItem(R.id.miSpoilerView);
    	  // Saarfuchs: hier musste noch abgetestet werden, dass auch ein Cache selektiert ist, sonst Absturz
    	  if (mi != null && Global.selectedCache!=null ) 
    	  {
    		  mi.setEnabled( Global.selectedCache.SpoilerExists() );
    	  }
    	  else {
    		  mi.setEnabled( false );
    	  }
    	  // Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5
    	  enabled = false;
    	  if (Global.JokerisOnline())
    		  enabled = true;
    	  mi = icm.menu.findItem(R.id.miTelJoker);
    	  if (mi != null)
    		  mi.setEnabled(enabled);

    	  Menu IconMenu=icm.getMenu();
    	  Global.TranslateMenuItem(IconMenu, R.id.miHint, "hint");
    	  Global.TranslateMenuItem(IconMenu, R.id.miTelJoker, "joker");
    	  icm.show();
	}

	private void initialBtnNavContextMenu() 
	{
		icm = new IconContextMenu(this, R.menu.menu_map);
		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() 
		{
			
			@Override
			public void onIconContextItemSelected(MenuItem item, Object info) 
			{
				switch (item.getItemId())
		    	{
				// Nav
		    	case R.id.miCompassView:
		    		showView(8);
		    		
		    		break;
		    	
		    	case R.id.miMapView:
		    		showView(0);
		    		
		    		break;
		    		
		    	default:
					
		    	}
		    }
		});
  	  icm.show();
	}

	private void initialBtnCacheContextMenu() 
	{
		icm = new IconContextMenu(this, R.menu.menu_cache);
  		  icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() 
  		  {

  			@Override
  			public void onIconContextItemSelected(MenuItem item, Object info) 
  			{
  				switch (item.getItemId())
  		    	{
  			// Cache
  		    	case R.id.miDescription:
  		    		showView(4);
  		    		break;
  		    	case R.id.miWaypoints:
  		    		showView(2);
  		    		break;
  		    	case R.id.miNotes:
  		    		showView(6);
  		    		break;
  		    	case R.id.miSolver:
  		    		showView(7);
  		    		break;
  					
  		    	}
  		    }
  		});
    	  
    	  Menu IconMenu=icm.getMenu();
    	  Global.TranslateMenuItem(IconMenu, R.id.miSolver, "Solver");
    	  Global.TranslateMenuItem(IconMenu, R.id.miNotes, "Notes");
    	  Global.TranslateMenuItem(IconMenu, R.id.miDescription, "Description");
    	  Global.TranslateMenuItem(IconMenu, R.id.miWaypoints, "Waypoints");
    	  icm.show();
	}

	private void initialBtnDbContextMenu() {
		icm = new IconContextMenu(this, R.menu.menu_db);
		icm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() 
		{
			
			@Override
			public void onIconContextItemSelected(MenuItem item, Object info) 
			{
				switch (item.getItemId())
		    	{
				// DB
		    	case R.id.miCacheList:
		    		showView(1);
		    		break;
		    	
		    	case R.id.miFilterset:
		    		showView(101);//Filtersettings
		    		break;
		    	case R.id.miManageDB:
		    		SelectDB.autoStart = false;
		    		Intent selectDBIntent = new Intent().setClass(mainActivity, SelectDB.class);
/*			        Bundle b = new Bundle();
			        b.putSerializable("Waypoint", aktWaypoint);
			        mainIntent.putExtras(b);*/
		    		mainActivity.startActivityForResult(selectDBIntent, 546132);
		    		break;
		    	case R.id.miResort:
		    		Database.Data.Query.Resort();
		    		break;
		    	case R.id.miAutoResort:
		    		Global.autoResort = !(Global.autoResort);
		            
		            Config.Set("AutoResort", Global.autoResort);

		            if (Global.autoResort)
		            	Database.Data.Query.Resort();
		    		break;
		    	
		    	}
		    }
		});
		Menu IconMenu=icm.getMenu();
		
		String DBName = Config.GetString("DatabasePath");
		int Pos = DBName.lastIndexOf("/");
		DBName= DBName.substring(Pos+1);
    	Pos=DBName.lastIndexOf(".");
    	DBName=DBName.substring(0, Pos);
		
		Global.TranslateMenuItem(IconMenu, R.id.miCacheList, "cacheList","  (" + String.valueOf(Database.Data.Query.size()) + ")" );
		Global.TranslateMenuItem(IconMenu, R.id.miFilterset, "filter");
		Global.TranslateMenuItem(IconMenu, R.id.miManageDB, "manage" ,"  (" + DBName + ")");
		Global.TranslateMenuItem(IconMenu, R.id.miResort, "ResortList");
		MenuItem miAutoResort = Global.TranslateMenuItem(IconMenu, R.id.miAutoResort, "AutoResort");
		miAutoResort.setCheckable(true);
		miAutoResort.setChecked(Global.autoResort);
		//AutoResortButton.ButtonImage = (Global.autoResort) ? Global.Icons[6] : Global.Icons[7];
  	  icm.show();
	}
    
  

	/*
	 * Initial Methods
	 */
    
    private void findViewsById() 
    {
    	frameCacheName = (FrameLayout)this.findViewById(R.id.frameCacheName);
    	TopLayout=(RelativeLayout)this.findViewById(R.id.layoutTop);     
        frame = (FrameLayout)this.findViewById(R.id.layoutContent);
        InfoDownSlider = (downSlider)this.findViewById(R.id.downSlider); 
        
        debugInfoPanel=(DebugInfoPanel)this.findViewById(R.id.debugInfo);
        Mic_Icon = (Mic_On_Flash)this.findViewById(R.id.mic_flash);
        
    	buttonDB = (ImageButton)this.findViewById(R.id.buttonDB);
    	buttonCache = (ImageButton)this.findViewById(R.id.buttonCache);
    	buttonNav = (ImageButton)this.findViewById(R.id.buttonMap);
    	buttonInfo = (ImageButton)this.findViewById(R.id.buttonInfo);
    	buttonMisc = (ImageButton)this.findViewById(R.id.buttonMisc);
    }
	    
	private void initialViews() 
	{
		if (compassView == null)
			compassView = new CompassView(this, inflater);
		if (cacheListView == null)
			cacheListView = new CacheListView(this);
		if (waypointView == null)
			waypointView = new WaypointView(this, this);
		if (logView == null)
			logView = new LogView(this);
		if (fieldNotesView == null)
			fieldNotesView = new FieldNotesView(this, this);
		registerForContextMenu(fieldNotesView);
		if (descriptionView == null)
			descriptionView = new DescriptionView(this, inflater);
		if (spoilerView == null)
			spoilerView = new SpoilerView(this, inflater);
		if (notesView == null)
			notesView = new NotesView(this, inflater);
		if (solverView == null)
			solverView = new SolverView(this, inflater);
		if (TestEmpty == null)
			TestEmpty = new EmptyViewTemplate(this, inflater);
		if (aboutView == null)
			aboutView = new AboutView(this, inflater);
		if (jokerView == null)
			jokerView = new JokerView(this, this);
		
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
    	ViewList.add(TestEmpty);			// ID 10
    	ViewList.add(aboutView);			// ID 11
    	ViewList.add(jokerView);			// ID 12

	}

	private void initialLocationManager() 
	{
		// GPS
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria(); // noch nötig ???
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		Global.Locator = new Locator();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
		locationManager.addNmeaListener(this);
	}

	private void initialMapView() 
	{
		if (mapView == null)
		{
	        mapView = new MapView(this, inflater);
			mapView.Initialize();
			mapView.CurrentLayer = MapView.Manager.GetLayerByName(Config.GetString("CurrentMapLayer"), Config.GetString("CurrentMapLayer"), "");
			Global.TrackDistance = Config.GetInt("TrackDistance");
			mapView.InitializeMap();
		}		
	}
	
	private void initalMicIcon() {
		Mic_Icon.SetOff(); 
		Mic_Icon.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				// Stoppe Aufnahme durch klick auf Mikrofon-Icon
				setVoiceRecIsStart(false); 
			}
		});
	}

	private void initialButtons() 
	{
		registerForContextMenu(buttonDB);
		buttonDB.setOnClickListener(ButtonOnClick);
		
		registerForContextMenu(buttonCache);
		buttonCache.setOnClickListener(ButtonOnClick);
		
		registerForContextMenu(buttonNav);
		this.buttonNav.setOnClickListener(ButtonOnClick);
		
		registerForContextMenu(buttonInfo);
		this.buttonInfo.setOnClickListener(ButtonOnClick);
		
		registerForContextMenu(buttonMisc);
		this.buttonMisc.setOnClickListener(ButtonOnClick);
		
		
		/*
		 * action ID übersicht
			
			Views:
			mapView				// ID 0
			cacheListView		// ID 1
			waypointView		// ID 2
			logView				// ID 3
			descriptionView		// ID 4
			spoilerView			// ID 5
			notesView			// ID 6
			solverView			// ID 7	
			compassView			// ID 8	
			fieldNotesView		// ID 9
			TestEmpty			// ID 10
			jokerView			// ID 12
			
			Activitys:
			filterSettings		// ID 101
			Settings			// ID 102
			
			
		 */
		
		btnDBActionIds = new ArrayList<Integer>();
		btnDBActionIds.add(1);  	//cacheListView 
//		btnDBActionIds.add(101);  	//filterSettings
		
		btnCacheActionIds = new ArrayList<Integer>();
		btnCacheActionIds.add(4);	//descriptionView
		btnCacheActionIds.add(2);	//waypointView
		btnCacheActionIds.add(6);	//notesView
		btnCacheActionIds.add(7);	//solverView
		
		btnNavActionIds = new ArrayList<Integer>();
		btnNavActionIds.add(0);		//mapView
		btnNavActionIds.add(8);		//compassView
		
		btnInfoActionIds = new ArrayList<Integer>();
		btnInfoActionIds.add(3);	//logView
		btnInfoActionIds.add(5);	//SpoilerView
		btnInfoActionIds.add(9);	//fieldNotesView
		btnInfoActionIds.add(12);	//jokerView
		
		btnMiscActionIds = new ArrayList<Integer>();
//		btnMiscActionIds.add(102);	//Settings
		btnMiscActionIds.add(11);	//About
		
		
	}
	
	
	/*
	 * InfoSlider
	 */
	
    private void initialCaheInfoSlider() 
	{
    	
    	// Set Layout Hight
    	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, (int) (Global.scaledFontSize_normal*2.2));
    	TopLayout.setLayoutParams(lp);
    	
    	
    	
		cacheNameView = new CacheNameView(this);
		cacheNameView.setHeight((int) (Global.scaledFontSize_normal*2.2));
		frameCacheName.addView(cacheNameView);
		InfoDownSlider.setOnTouchListener(new OnTouchListener() 
		{
			
			boolean drag;
			@Override public boolean onTouch(View v, MotionEvent event) 
			{
				 // events when touching the screen

				 int eventaction = event.getAction();
				 int X = (int)event.getX();
				 int Y = (int)event.getY();
				  if(InfoDownSlider.contains(X, Y)) drag=true;
				 
				 
				 switch (eventaction ) 
				 {
				 	case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on a ball
				 		setDebugMsg("Down");
				 		break;


				 	case MotionEvent.ACTION_MOVE: // touch drag with the ball
					 // move the balls the same as the finger
				
				 		setDebugMsg("Move:" + String.format("%n")+ "x= " + X + String.format("%n") + "y= " + Y);
				 		if (drag)InfoDownSlider.setPos(Y-25); //y - 25 minus halbe Button Höhe
				 		break;
				 		
				 	case MotionEvent.ACTION_UP:
				 		if (drag)InfoDownSlider.ActionUp();
				 		drag=false;
				 		break;

				 }
				
				if(drag)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		});
	}
 	
	
	/*
	 * Setter
	 */
	  
	 public void setDebugVisible()
		{
			if(Config.GetBool("DebugShowPanel"))
			{
				debugInfoPanel.setVisibility(View.VISIBLE);
			}
			else
			{
				debugInfoPanel.setVisibility(View.GONE);
			}
		}
	
	 public void setDebugMsg(String msg)
	 {
		 debugInfoPanel.setMsg(msg);
	 }
	 
	 public void setVoiceRecIsStart(Boolean value)
	    {
	    	mVoiceRecIsStart=value;
	    	if(mVoiceRecIsStart)
	    	{ Mic_Icon.SetOn();} 
	    	else 
	    	{   // Aufnahme stoppen
	    		Mic_Icon.SetOff();
	            if (extAudioRecorder != null) 
	            {            
		            extAudioRecorder.stop();
		            extAudioRecorder.release();
		            extAudioRecorder = null;
		    		Toast.makeText(mainActivity, "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
	            }
	    		counterStopped = false; // ScreenLock-Counter wieder starten
	    		counter.start();
	    	}
	    	
	    }

	@Override
	public void onNmeaReceived(long timestamp, String nmea) 
	{
		if (nmea.substring(0, 6).equalsIgnoreCase("$GPGGA"))
		{
			String[] s = nmea.split(",");
			try
			{
				if (s[11].equals(""))
					return;
				double altCorrection = Double.valueOf(s[11]);
				Logger.General("AltCorrection: " + String.valueOf(altCorrection));
				Global.Locator.altCorrection = altCorrection;
				// Höhenkorrektur ändert sich normalerweise nicht, einmal auslesen reicht...
				locationManager.removeNmeaListener(this);
			} catch (Exception exc)
			{
				// keine Höhenkorrektur vorhanden
			}
		}
	}

	 public void setScreenLockTimerNew(int value)
	 {
		 counter.cancel();
		 counter = new ScreenLockTimer(value,value);
		 counter.start();
	 }

	
	 
	 
	 
	 static class LockClass { };
	 static LockClass lockObject = new LockClass();

	 
	 /**
	     * Empfängt die gelogten Meldungen und schreibt sie in die Debug.txt
	     */
		@Override public void receiveLog(String Msg) 
		{
			synchronized (lockObject)
	        {
	        	File file = new File(Config.WorkPath + "/debug.txt");
	        	FileWriter writer;
	        	try {
					writer = new FileWriter(file, true);
					writer.write(Msg);
		            writer.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        }
		}

	
		/**
		 * Empfängt die gelogten Meldungen in kurz Form und schreibt sie
		 * ins Debung Panel, wenn dieses sichtbar ist!
		 */
	@Override public void receiveShortLog(String Msg) 
	{
		debugInfoPanel.addLogMsg(Msg);
		
	}
	 
	 
}
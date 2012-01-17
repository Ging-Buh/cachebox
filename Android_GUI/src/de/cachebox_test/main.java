package de.cachebox_test;

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
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Log.ILog;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;

import de.cachebox_test.Components.CacheNameView;
import de.cachebox_test.Components.search;
import de.cachebox_test.Components.search.searchMode;
import de.cachebox_test.Custom_Controls.DebugInfoPanel;
import de.cachebox_test.Custom_Controls.DescriptionViewControl;
import de.cachebox_test.Custom_Controls.Mic_On_Flash;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.cachebox_test.Custom_Controls.QuickButtonList.HorizontalListView;
import de.cachebox_test.Custom_Controls.QuickButtonList.QuickButtonItem;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Enums.Actions;
import de.cachebox_test.Events.GpsStateChangeEvent;
import de.cachebox_test.Events.GpsStateChangeEventList;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Locator.GPS;
import de.cachebox_test.Locator.Locator;
import de.cachebox_test.Map.MapViewGlListener;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Ui.Sizes;
import de.cachebox_test.Views.AboutView;
import de.cachebox_test.Views.CacheListView;
import de.cachebox_test.Views.CompassView;
import de.cachebox_test.Views.DescriptionView;
import de.cachebox_test.Views.FieldNotesView;
import de.cachebox_test.Views.JokerView;
import de.cachebox_test.Views.LogView;
import de.cachebox_test.Views.MapView;
import de.cachebox_test.Views.MapViewGL;
import de.cachebox_test.Views.NotesView;
import de.cachebox_test.Views.SolverView;
import de.cachebox_test.Views.SpoilerView;
import de.cachebox_test.Views.TrackListView;
import de.cachebox_test.Views.TrackableListView;
import de.cachebox_test.Views.WaypointView;
import de.cachebox_test.Views.AdvancedSettingsForms.SettingsScrollView;
import de.cachebox_test.Views.FilterSettings.EditFilterSettings;
import de.cachebox_test.Views.FilterSettings.PresetListViewItem;
import de.cachebox_test.Views.Forms.ApiSearchPosDialog;
import de.cachebox_test.Views.Forms.DeleteDialog;
import de.cachebox_test.Views.Forms.GcApiLogin;
import de.cachebox_test.Views.Forms.HintDialog;
import de.cachebox_test.Views.Forms.ImportDialog;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.MessageBoxButtons;
import de.cachebox_test.Views.Forms.MessageBoxIcon;
import de.cachebox_test.Views.Forms.ParkingDialog;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;
import de.cachebox_test.Views.Forms.ScreenLock;
import de.cachebox_test.Views.Forms.SelectDB;

public class main extends AndroidApplication implements SelectedCacheEvent, LocationListener, CB_Core.Events.CacheListChangedEvent,
		GpsStatus.NmeaListener, ILog, GpsStateChangeEvent
{
	/*
	 * private static member
	 */

	public static Integer aktViewId = -1;
	private static long GPSTimeStamp = 0;
	public static MapView mapView = null; // ID 0
	public static CacheListView cacheListView = null; // ID 1
	public static MapViewGL mapViewGl = null; // ID 2
	private static LogView logView = null; // ID 3
	public static DescriptionView descriptionView = null; // ID 4
	private static SpoilerView spoilerView = null; // ID 5
	private static NotesView notesView = null; // ID 6
	private static SolverView solverView = null; // ID 7
	private static CompassView compassView = null; //
	public static FieldNotesView fieldNotesView = null; // ID 9
	private static AboutView aboutView = null; // ID 11
	private static JokerView jokerView = null; // ID 12
	private static TrackListView tracklistView = null; // ID 13
	private static TrackableListView trackablelistView = null; // ID 14
	public static WaypointView waypointView = null; // ID 15

	private View viewGl = null;
	private MapViewGlListener mapViewGlListener = null;
	public static LinearLayout strengthLayout;

	public LinearLayout searchLayout;
	private search Search;

	/**
	 * Night Mode aktive
	 */
	public static Boolean N = false;

	// Media

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
	public static AndroidApplication mainActivity;
	public static Boolean isRestart = false;
	public static Boolean isFirstStart = true;

	/*
	 * private member
	 */
	private LayoutInflater inflater;

	private ExtAudioRecorder extAudioRecorder = null;
	private boolean initialResortAfterFirstFixCompleted = false;
	private boolean initialFixSoundCompleted = false;
	private boolean approachSoundCompleted = false;
	private boolean runsWithAkku = true;

	private ImageButton buttonDB;
	private ImageButton buttonCache;
	private ImageButton buttonNav;
	private ImageButton buttonTools;
	private ImageButton buttonMisc;
	private FrameLayout frame;
	private LinearLayout TopLayout;
	// private LinearLayout frameCacheName;
	public downSlider InfoDownSlider;
	public HorizontalListView QuickButtonList;

	private String GcCode = null;
	private String name = null;
	private String guid = null;

	private Mic_On_Flash Mic_Icon;
	private static DebugInfoPanel debugInfoPanel;

	// Views
	private ViewOptionsMenu aktView = null;
	private CacheNameView cacheNameView;

	private ArrayList<View> ViewList = new ArrayList<View>();
	// private int lastBtnDBView = 1;
	// private int lastBtnCacheView = 4;
	// private int lastBtnNavView = 0;
	// private int lastBtnToolsView = -1;
	// private int lastBtnMiscView = 11;
	// ArrayList<Integer> btnDBActionIds;
	// ArrayList<Integer> btnCacheActionIds;
	// ArrayList<Integer> btnNavActionIds;
	// ArrayList<Integer> btnToolsActionIds;
	// ArrayList<Integer> btnMiscActionIds;

	// Powermanager
	protected PowerManager.WakeLock mWakeLock;
	// GPS
	public static LocationManager locationManager;
	// Compass
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float[] mCompassValues;

	public Boolean getVoiceRecIsStart()
	{
		return mVoiceRecIsStart;
	}

	// Screenlock Counter
	private ScreenLockTimer counter = null;
	private boolean counterStopped = false;

	public static Vibrator vibrator;

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
			// Toast.makH_LONG).show();
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
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (Config.settings == null)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else if (!Config.settings.AllowLandscape.getValue())
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}

		// initialize receiver for screen switched on/off
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		BroadcastReceiver mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);

		Logger.Add(this);

		N = Config.settings.nightMode.getValue();

		try
		{
			setContentView(R.layout.main);
		}
		catch (Exception exc)
		{
			Logger.Error("main.onCreate()", "setContentView", exc);
		}

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainActivity = this;
		AllContextMenuCallHandler.Main = this;
		mainActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mapViewGlListener = new MapViewGlListener();

		// initial UiSizes
		Sizes.initial(false, this);

		int Time = Config.settings.ScreenLock.getValue();
		counter = new ScreenLockTimer(Time, Time);
		counter.start();

		findViewsById();

		// add Event Handler
		SelectedCacheEventList.Add(this);
		CachListChangedEventList.Add(this);
		GpsStateChangeEventList.Add(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Date now = new Date();
		boolean c = (now.getMonth() == 11 && !Config.settings.dontShowChris.getValue());
		Config.settings.isChris.setValue(c);

		Config.AcceptChanges();

		// Ausschalten verhindern
		/*
		 * This code together with the one in onDestroy() will make the screen be always on until this Activity gets destroyed.
		 */
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		this.mWakeLock.acquire();

		initialLocationManager();
		initialMapView();
		initialMapViewGl();
		initialViews();
		initalMicIcon();
		initialButtons();
		initialCaheInfoSlider();

		if (GlobalCore.SelectedCache() == null)
		{
			if (Database.Data == null)
			{
				String FilterString = Config.settings.Filter.getValue();
				Global.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0])
						: new FilterProperties(FilterString);
				String sqlWhere = Global.LastFilter.getSqlWhere();

				// initialize Database
				Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
				String database = Config.settings.DatabasePath.getValue();
				Database.Data.StartUp(database);

				GlobalCore.Categories = new Categories();
				Database.Data.GPXFilenameUpdateCacheCount();

				CacheListDAO cacheListDAO = new CacheListDAO();
				cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

			}

			CacheList cacheList = Database.Data.Query;
			if (cacheList.size() > 0)
			{
				Cache cache = cacheList.get(0);
				GlobalCore.SelectedCache(cache);
			}
		}
		else
		// Activity wurde neu Gestartet
		{
			GlobalCore.SelectedCache(GlobalCore.SelectedCache());
		}

		Search = new search(this);

		if (aktViewId != -1)
		{
			// Zeige letzte gespeicherte View beim neustart der Activity
			showView(aktViewId);

		}
		else
		{

			// Start CB!

			Logger.General("------ Start Rev: " + Global.CurrentRevision + "-------");

			// Zeige About View als erstes!
			showView(11);

			// chk if NightMode saved
			if (N)
			{
				ActivityUtils.changeToTheme(mainActivity, ActivityUtils.THEME_NIGHT, true);
			}

		}

		// Initialisiere Icons neu.
		Global.InitIcons(this);

		if (Config.settings.nightMode.getValue())
		{
			if (Config.settings.isChris.getValue())
			{
				buttonCache.setBackgroundResource(R.drawable.chris_night_cache_button_image_selector);
				buttonDB.setBackgroundResource(R.drawable.night_db_button_image_selector);
				buttonMisc.setBackgroundResource(R.drawable.night_misc_button_image_selector);
				buttonNav.setBackgroundResource(R.drawable.night_nav_button_image_selector);
				buttonTools.setBackgroundResource(R.drawable.night_find_button_image_selector);
			}
			else
			{
				buttonCache.setBackgroundResource(R.drawable.night_cache_button_image_selector);
				buttonDB.setBackgroundResource(R.drawable.night_db_button_image_selector);
				buttonMisc.setBackgroundResource(R.drawable.night_misc_button_image_selector);
				buttonNav.setBackgroundResource(R.drawable.night_nav_button_image_selector);
				buttonTools.setBackgroundResource(R.drawable.night_find_button_image_selector);
			}
		}
		else
		{
			if (Config.settings.isChris.getValue())
			{
				buttonCache.setBackgroundResource(R.drawable.chris_cache_button_image_selector);
				buttonDB.setBackgroundResource(R.drawable.chris_db_button_image_selector);
				buttonMisc.setBackgroundResource(R.drawable.chris_misc_button_image_selector);
				buttonNav.setBackgroundResource(R.drawable.chris_nav_button_image_selector);
				buttonTools.setBackgroundResource(R.drawable.chris_find_button_image_selector);
			}
			else
			{
				buttonCache.setBackgroundResource(R.drawable.cache_button_image_selector);
				buttonDB.setBackgroundResource(R.drawable.db_button_image_selector);
				buttonMisc.setBackgroundResource(R.drawable.misc_button_image_selector);
				buttonNav.setBackgroundResource(R.drawable.nav_button_image_selector);
				buttonTools.setBackgroundResource(R.drawable.find_button_image_selector);
			}
		}
		buttonCache.invalidate();

		CacheListChangedEvent();

		setDebugVisible();

		if (Config.settings.TrackRecorderStartup.getValue()) TrackRecorder.StartRecording();

		try
		{
			this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
		catch (Exception e)
		{
			// sometimes:
			// ERROR/ActivityThread(15416): Activity de.cachebox_test.main has
			// leaked IntentReceiver
			// de.cachebox_test.main$7@4745a0f0 that was originally registered
			// here.
			// Are you missing a call to unregisterReceiver()?
		}

		downSlider.isInitial = false;
		int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? Sizes
				.getQuickButtonListHeight() : 0;
		setQuickButtonHeight(sollHeight);

		if (isFirstStart)
		{
			// ask for API key only if Rev-Number changed, like at new
			// installation and API Key is Empty
			if (Config.settings.newInstall.getValue() && Config.GetAccessToken().equals(""))
			{
				askToGetApiKey();
			}
			else
			{
				chkGpsIsOn();
			}

			if (Config.settings.newInstall.getValue())
			{
				String Welcome = "";
				String LangId = getString(R.string.langId);
				try
				{
					Welcome = GlobalCore.Translations.getTextFile("welcome", LangId);

					if (Config.settings.isChris.getValue())
					{
						Welcome += GlobalCore.Translations.getTextFile("chris", LangId);
					}

					Welcome += GlobalCore.Translations.getTextFile("changelog", LangId);
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				MessageBox.Show(Welcome, GlobalCore.Translations.Get("welcome"), MessageBoxIcon.None);

			}

			if (input == null)
			{
				AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
				config.useGL20 = true;
				graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
						: config.resolutionStrategy);
				input = new AndroidInput(this, graphics.getView(), config);
			}

		}

		final Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			GcCode = extras.getString("GcCode");
			name = extras.getString("name");
			guid = extras.getString("guid");

			// MessageBox.Show("GcCode=" + GcCode + String.format("%n") +
			// "name =" + name + String.format("%n") + "guid =" + guid);

			startTimer();
		}

	}

	public void iniInput()
	{
		// TODO set Stage as InputProzessor?
	}

	boolean flag = false;

	private void startTimer()
	{
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				startSearch();
			}
		};
		timer.schedule(task, 500);
	}

	private void startSearch()
	{
		if (GcCode != null)
		{
			Thread t = new Thread()
			{
				public void run()
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (flag)
							{
								flag = false;
								Search.Show();
								Search.addSearch(GcCode, searchMode.GcCode);
							}
							else
							{
								flag = true;
								showView(1);
								startTimer();
							}

						}
					});
				}
			};

			t.start();

		}
	}

	/** hook into menu button for activity */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (aktView != null) return aktView.ItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUserInteraction()
	{
		if (counterStopped) return;
		if (counter != null) counter.start();

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		approachSoundCompleted = false;
		cacheListView.setSelectedCacheVisible(0);
		initialCaheInfoSlider();

		// QuickButtonsAdapter.notifyDataSetInvalidated();
		// QuickButtonList.invalidate();

	}

	public void newLocationReceived(Location location)
	{
		try
		{
			Global.Locator.setLocation(location);
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "Global.Locator.setLocation(location)", e);
			e.printStackTrace();
		}

		try
		{
			PositionEventList.Call(location);
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "PositionEventList.Call(location)", e);
			e.printStackTrace();
		}

		try
		{
			InfoDownSlider.setNewLocation(location);
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "InfoDownSlider.setNewLocation(location)", e);
			e.printStackTrace();
		}

		try
		{
			if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)
			{
				if (GlobalCore.SelectedCache() == null)
				{
					Database.Data.Query.Resort();
				}
				initialResortAfterFirstFixCompleted = true;
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)", e);
			e.printStackTrace();
		}

		try
		{
			if (!initialFixSoundCompleted && GlobalCore.LastValidPosition.Valid
					&& location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))
			{
				Global.PlaySound("GPS_Fix.ogg");
				initialFixSoundCompleted = true;
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "Global.PlaySound(GPS_Fix.ogg)", e);
			e.printStackTrace();
		}

		try
		{
			if (GlobalCore.SelectedCache() != null)
			{
				float distance = GlobalCore.SelectedCache().Distance(false);
				if (GlobalCore.SelectedWaypoint() != null)
				{
					distance = GlobalCore.SelectedWaypoint().Distance();
				}

				if (!approachSoundCompleted && (distance < Config.settings.SoundApproachDistance.getValue()))
				{
					Global.PlaySound("Approach.ogg");
					approachSoundCompleted = true;

					// switch to Compass if the option seted
					if (Config.settings.switchViewApproach.getValue())
					{
						showView(8);
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "Global.PlaySound(Approach.ogg)", e);
			e.printStackTrace();
		}

		try
		{
			TrackRecorder.recordPosition();
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "TrackRecorder.recordPosition()", e);
			e.printStackTrace();
		}

		try
		{
			// schau die 50 nächsten Caches durch, wenn einer davon näher ist
			// als der aktuell nächste -> umsortieren und raus
			// only when showing Map or cacheList
			if (!GlobalCore.ResortAtWork)
			{
				if (Global.autoResort && ((aktView == mapView) || (aktView == cacheListView)))
				{
					int z = 0;
					if (!(GlobalCore.NearestCache() == null))
					{
						for (Cache cache : Database.Data.Query)
						{
							z++;
							if (z >= 50) return;
							if (cache.Distance(true) < GlobalCore.NearestCache().Distance(true))
							{
								Database.Data.Query.Resort();
								Global.PlaySound("AutoResort.ogg");
								return;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "Resort", e);
			e.printStackTrace();
		}

	}

	@Override
	public void onLocationChanged(Location location)
	{

		try
		{
			if (location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)) // Neue
																						// Position
																						// von
																						// GPS-Empfänger
			{
				newLocationReceived(location);
				GPSTimeStamp = java.lang.System.currentTimeMillis();
				return;
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.onLocationChanged()", "GPS_PROVIDER", e);
			e.printStackTrace();
		}

		try
		{
			// Neue Position vom Netzwerk
			if (location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))
			{
				// Wenn 10 Sekunden kein GPS Signal
				if ((java.lang.System.currentTimeMillis() - GPSTimeStamp) > NetworkPositionTime)
				{
					NetworkPositionTime = 90000;
					newLocationReceived(location);
					if (initialFixSoundCompleted)
					{
						Global.PlaySound("GPS_lose.ogg");
						initialFixSoundCompleted = false;
					}

					Toast.makeText(mainActivity, "Network-Position", Toast.LENGTH_SHORT).show();
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.onLocationChanged()", "NETWORK_PROVIDER", e);
			e.printStackTrace();
		}

	}

	/*
	 * Wenn 10 Sekunden kein gültiges GPS Signal gefunden wird. Aber nur beim Ersten mal. Danach warten wir lieber 90 sec
	 */
	private int NetworkPositionTime = 10000;

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// Log.d("SolHunter", "Key event code " + keyCode);
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
						// Yes button clicked
						try
						{
							finish();
						}
						catch (Throwable e)
						{

							e.printStackTrace();
						}
						break;
					case -2:
						// No button clicked
						dialog.dismiss();
						break;
					}
				}
			};
			MessageBox.Show(GlobalCore.Translations.Get("QuitReally"), GlobalCore.Translations.Get("Quit?"), MessageBoxButtons.YesNo,
					MessageBoxIcon.Question, dialogClickListener);

			return true;
		}
		return false;
	}

	@Override
	public void CacheListChangedEvent()
	{
		int ButtonBackGroundResource = 0;

		if ((Global.LastFilter == null) || (Global.LastFilter.ToString().equals(""))
				|| (PresetListViewItem.chkPresetFilter(FilterProperties.presets[0], Global.LastFilter.ToString()))
				&& !Global.LastFilter.isExtendsFilter())
		{
			ButtonBackGroundResource = N ? R.drawable.night_db_button_image_selector
					: Config.settings.isChris.getValue() ? R.drawable.chris_db_button_image_selector : R.drawable.db_button_image_selector;
		}
		else
		{
			ButtonBackGroundResource = N ? R.drawable.night_db_button_image_selector_filter : R.drawable.db_button_image_selector_filter;
		}
		;

		this.buttonDB.setBackgroundResource(ButtonBackGroundResource);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// SelectDB
		if (requestCode == 546132)
		{
			if (resultCode == RESULT_OK)
			{
				// Toast.makeText(getApplicationContext(),
				// "DB wechsel momentan nur mit Neustart...",
				// Toast.LENGTH_LONG).show();
				Database db = new AndroidDB(Database.DatabaseType.CacheBox, mainActivity);
				if (!db.StartUp(Config.settings.DatabasePath.getValue())) return;
				Database.Data = null;
				Database.Data = db;
				/*
				 * SqlCeCommand command = new SqlCeCommand(" select GcCode from FieldNotes WHERE Type = 1 " ,
				 * Database.FieldNotes.Connection); SqlCeDataReader reader = command.ExecuteReader(); if (reader == null) throw new
				 * Exception ("Startup: Cannot execute SQL statement Copy Founds to TB"); string GcCode = ""; while (reader.Read()) GcCode
				 * += "'" + reader.GetString(0) + "', "; if (GcCode.Length > 0) { GcCode = GcCode.Substring(0, GcCode.Length - 2);
				 * SqlCeCommand commandUpdate = new SqlCeCommand(" UPDATE Caches SET Found = 1 WHERE GcCode IN (" + GcCode + ") ",
				 * Database.Data.Connection); int founds = commandUpdate.ExecuteNonQuery(); }
				 */

				Config.settings.ReadFromDB();

				GlobalCore.Categories = new Categories();
				Global.LastFilter = (Config.settings.Filter.getValue().length() == 0) ? new FilterProperties(FilterProperties.presets[0])
						: new FilterProperties(Config.settings.Filter.getValue());
				// filterSettings.LoadFilterProperties(Global.LastFilter);
				Database.Data.GPXFilenameUpdateCacheCount();

				String sqlWhere = Global.LastFilter.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				Database.Data.Query.clear();
				CacheListDAO cacheListDAO = new CacheListDAO();
				cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

				// Database.Data.GPXFilenameUpdateCacheCount();

				GlobalCore.SelectedCache(null);
				GlobalCore.SelectedWaypoint(null, null);
				CachListChangedEventList.Call();

				// beim zurückkehren aus der DB-Auswahl muss der Slider neu
				// initialisiert werden
				downSlider.isInitial = false;
			}
			return;
		}
		// Intent Result Take Photo
		if (requestCode == Global.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				// Log.d("DroidCachebox", "Picture taken!!!");
				GlobalCore.SelectedCache().ReloadSpoilerRessources();
				String MediaFolder = Config.settings.UserImageFolder.getValue();
				String TrackFolder = Config.settings.TrackFolder.getValue();
				String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
				// Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und
				// die Koordinaten nach der Aufnahme verwendet werden.
				mediaTimeString = Global.GetTrackDateTimeString();
				TrackRecorder.AnnotateMedia(basename + ".jpg", relativPath + "/" + basename + ".jpg", GlobalCore.LastValidPosition,
						mediaTimeString);

				return;
			}
			else
			{
				// Log.d("DroidCachebox", "Picture NOT taken!!!");
				return;
			}
		}

		// Intent Result Record Video
		if (requestCode == Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				// Log.d("DroidCachebox", "Video taken!!!");
				// Global.selectedCache.ReloadSpoilerRessources();

				String[] projection =
					{ MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE };
				Cursor cursor = managedQuery(cameraVideoURI, projection, null, null, null);
				int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				cursor.moveToFirst();
				String recordedVideoFilePath = cursor.getString(column_index_data);

				String ext = FileIO.GetFileExtension(recordedVideoFilePath);
				String MediaFolder = Config.settings.UserImageFolder.getValue();

				// Video in Media-Ordner verschieben
				File source = new File(recordedVideoFilePath);
				File destination = new File(MediaFolder + "/" + basename + "." + ext);
				// Datei wird umbenannt/verschoben
				if (!source.renameTo(destination))
				{
					// Log.d("DroidCachebox", "Fehler beim Umbenennen der Datei: " + source.getName());
				}

				String TrackFolder = Config.settings.TrackFolder.getValue();
				String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
				TrackRecorder.AnnotateMedia(basename + "." + ext, relativPath + "/" + basename + "." + ext, mediaCoordinate,
						mediaTimeString);

				return;
			}
			else
			{
				// Log.d("DroidCachebox", "Video NOT taken!!!");
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

		// Intent Result get API key
		if (requestCode == 987654321)
		{
			// no, we check GPS
			chkGpsIsOn();
			if (SettingsScrollView.Me != null)
			{
				SettingsScrollView.Me.ListInvalidate();
			}
		}

		if (requestCode == Global.REQUEST_CODE_API_TARGET_DIALOG)
		{
			if (data == null) return;
			Bundle bundle = data.getExtras();
			if (bundle != null)
			{
				searchCoord = (Coordinate) bundle.getSerializable("CoordResult");
				if (searchCoord != null)
				{
					searchOnlineNow();
				}
			}

			return;
		}

		// Intent Result Delete Caches
		if (requestCode == Global.REQUEST_CODE_DELETE_DIALOG)
		{
			if (data == null) return;
			Bundle bundle = data.getExtras();
			int selection = bundle.getInt("DelResult");// enthält Rückgabe Wert
			long nun = 0;
			switch (selection)
			{
			case 0: // Archived gewählt
			{
				CacheListDAO dao = new CacheListDAO();
				nun = dao.DelArchiv();
				FilterProperties props = Global.LastFilter;
				String sqlWhere = props.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				Database.Data.Query.clear();
				dao.ReadCacheList(Database.Data.Query, sqlWhere);
				String msg = GlobalCore.Translations.Get("DeletedCaches", String.valueOf(nun));
				Toast(msg);
				return;
			}
			case 1:// Found gewählt
			{
				CacheListDAO dao = new CacheListDAO();
				nun = dao.DelFound();
				FilterProperties props = Global.LastFilter;
				String sqlWhere = props.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				Database.Data.Query.clear();
				dao.ReadCacheList(Database.Data.Query, sqlWhere);
				String msg = GlobalCore.Translations.Get("DeletedCaches", String.valueOf(nun));
				Toast(msg);
				return;
			}
			case 2:// Filter gewählt
			{
				CacheListDAO dao = new CacheListDAO();
				nun = dao.DelFilter(Global.LastFilter.getSqlWhere());
				Global.LastFilter = new FilterProperties(FilterProperties.presets[0]);
				EditFilterSettings.ApplyFilter(mainActivity, Global.LastFilter);
				String msg = GlobalCore.Translations.Get("DeletedCaches", String.valueOf(nun));
				Toast(msg);
				return;
			}
			}
			return;
		}

		aktView.ActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();

		if (v instanceof ViewOptionsMenu)
		{
			int id = ((ViewOptionsMenu) v).GetContextMenuId();
			if (id > 0)
			{
				inflater.inflate(id, menu);
				((ViewOptionsMenu) v).BeforeShowContextMenu(menu);
			}
			return;
		}

		if (v == buttonDB)
		{
			AllContextMenuCallHandler.showBtnListsContextMenu();
		}
		else if (v == buttonCache)
		{
			AllContextMenuCallHandler.showBtnCacheContextMenu();
		}
		else if (v == buttonNav)
		{
			AllContextMenuCallHandler.showBtnNavContextMenu();
		}
		else if (v == buttonTools)
		{
			AllContextMenuCallHandler.showBtnToolsContextMenu();
		}
		else if (v == buttonMisc)
		{
			AllContextMenuCallHandler.showBtnMiscContextMenu();
		}
	}

	@Override
	protected void onPause()
	{

		if (input == null)
		{
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, graphics.getView(), config);
		}

		if (graphics != null)
		{
			if (aktViewId != 2)
			{
				graphics.isShown = false;
			}
		}
		super.onPause();
		graphics.isShown = true;
	}

	@Override
	protected void onResume()
	{

		if (input == null)
		{
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, graphics.getView(), config);
		}
		if (graphics != null)
		{
			if (aktViewId != 2)
			{
				graphics.isShown = false;
			}
		}
		super.onResume();
		graphics.isShown = true;

		if (runsWithAkku) counter.start();
		mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		if (!Config.settings.AllowLandscape.getValue())
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}

		int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? Sizes
				.getQuickButtonListHeight() : 0;
		((main) main.mainActivity).setQuickButtonHeight(sollHeight);
		downSlider.isInitial = false;
		InfoDownSlider.invalidate();
	}

	@Override
	protected void onStop()
	{
		mSensorManager.unregisterListener(mListener);
		this.unregisterReceiver(this.mBatInfoReceiver);
		counter.cancel();
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		frame.removeAllViews();
		if (isRestart)
		{
			super.onDestroy();
			isRestart = false;
		}
		else
		{
			if (isFinishing())
			{

				GlobalCore.Translations.writeMisingStringsFile();

				Config.settings.MapInitLatitude.setValue(mapViewGlListener.center.Latitude);
				Config.settings.MapInitLongitude.setValue(mapViewGlListener.center.Longitude);
				Config.settings.WriteToDB();

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
				GlobalCore.SelectedCache(null);
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
						((ViewOptionsMenu) vom).OnFree();
					}
				}
				ViewList.clear();
				cacheListView = null;
				mapView = null;
				mapViewGl = null;
				notesView = null;
				jokerView = null;
				descriptionView = null;
				mainActivity = null;
				debugInfoPanel.OnFree();
				debugInfoPanel = null;
				InfoDownSlider = null;

				Config.AcceptChanges();

				Database.Data.Close();
				Database.FieldNotes.Close();

				Database.Settings.Close();
				super.onDestroy();
				System.exit(0);
			}
			else
			{
				if (aktView != null) aktView.OnHide();
				super.onDestroy();
			}
		}
	}

	/**
	 * Handling Screen OFF and Screen ON Intents
	 * 
	 * @author -jwei http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
	 */
	public static class ScreenReceiver extends BroadcastReceiver
	{

		// thanks Jason
		public static boolean wasScreenOn = true;

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
			{
				// do whatever you need to do here
				Energy.setDontRender();
				wasScreenOn = false;
			}
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				// and do whatever you need to do here
				Energy.resetDontRender();
				wasScreenOn = true;
			}
		}

	}

	/**
	 * Startet die Bildschirm Sperre
	 */
	public void startScreenLock()
	{
		startScreenLock(false);
	}

	/**
	 * Startet die Bildschirm Sperre. Mit der der Übergabe von force = true, werden abfragen ob im Akkubetrieb oder die Zeit Einstellungen
	 * ignoriert.
	 * 
	 * @param force
	 */
	public void startScreenLock(boolean force)
	{

		if (!force)
		{
			if (!runsWithAkku) return;
			counter.cancel();
			counterStopped = true;
			// ScreenLock nur Starten, wenn der Config Wert größer 10 sec ist.
			// Das verhindert das selber aussperren!
			if ((Config.settings.ScreenLock.getValue() / 1000 < 10)) return;
		}

		dontStop = true;
		ScreenLock.isShown = true;

		final Intent mainIntent = new Intent().setClass(this, ScreenLock.class);
		this.startActivityForResult(mainIntent, 12345);
	}

	/*
	 * Handler
	 */

	private final View.OnClickListener ButtonOnClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (v == buttonDB)
			{
				showView(1);
			}

			else if (v == buttonCache)
			{
				showView(4);
			}

			else if (v == buttonNav)
			{
				showView(0);
			}

			else if (v == buttonTools)
			{
				// showView();
			}

			else if (v == buttonMisc)
			{
				showView(11);
			}
		}
	};

	private final SensorEventListener mListener = new SensorEventListener()
	{
		public void onSensorChanged(SensorEvent event)
		{
			try
			{
				mCompassValues = event.values;
				Global.Locator.setCompassHeading(mCompassValues[0]);
				PositionEventList.Call(mCompassValues[0]);
			}
			catch (Exception e)
			{
				Logger.Error("main.mListener.onSensorChanged()", "", e);
				e.printStackTrace();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}
	};

	public void showView(Integer ID)
	{
		if (ID == -1) return;// keine Action

		if (!(aktView == null) && ID == aktViewId)
		{
			aktView.OnShow();
			return;
		}

		if (ID >= ViewList.size())
		{
			switch (ID)
			{
			case 102: // Settings
				final Intent mainIntent = new Intent().setClass(mainActivity, SettingsScrollView.class);
				Bundle b = new Bundle();
				b.putSerializable("Show", -1);
				mainIntent.putExtras(b);
				mainActivity.startActivity(mainIntent);
				break;

			case 101: // Filtersettings
				final Intent mainIntent1 = new Intent().setClass(mainActivity, EditFilterSettings.class);
				mainActivity.startActivity(mainIntent1);
				break;

			case 103: // Import
				final Intent mainIntent2 = new Intent().setClass(mainActivity, ImportDialog.class);
				mainActivity.startActivity(mainIntent2);
				break;

			case 14: // TrackableListView
				trackablelistView = new TrackableListView(this, this);
				showView(trackablelistView, 14);
				break;

			case 13: // TrackListView
				tracklistView = new TrackListView(this, this);
				showView(tracklistView, 13);
				break;

			case 12: // jokerView
				if (Config.settings.hasCallPermission.getValue())
				{
					jokerView = new JokerView(this, this);
					showView(jokerView, 12);
				}
				break;

			case 11: // aboutView
				aboutView = new AboutView(this, inflater);
				showView(aboutView, 11);
				break;

			case 9: // fieldNotesView
				fieldNotesView = new FieldNotesView(this, this);
				showView(fieldNotesView, 9);
				break;

			case 8: // compassView
				compassView = new CompassView(this, inflater);
				compassView.reInit();
				showView(compassView, 8);
				break;
			case 7: // solverView
				solverView = new SolverView(this, inflater);
				showView(solverView, 7);
				break;
			case 6: // notesView
				notesView = new NotesView(this, inflater);
				showView(notesView, 6);
				break;
			case 5: // spoilerView
				spoilerView = new SpoilerView(this, inflater);
				showView(spoilerView, 5);
				break;
			case 4: // descriptionView
				descriptionView = new DescriptionView(this, inflater);
				showView(descriptionView, 4);
				break;
			case 3: // descriptionView
				logView = new LogView(this);
				showView(logView, 3);
				break;
			case 15: // waypointView
				waypointView = new WaypointView(this, this);
				showView(waypointView, 15);
				break;
			case 2:
				mapViewGl = null;
				initialMapViewGl();
				showView(mapViewGl, 2);
				break;
			}
		}
		else
		{
			showView((ViewOptionsMenu) ViewList.get(ID), ID);

		}

	}

	private void showView(ViewOptionsMenu view, int Id)
	{
		if (aktView != null)
		{
			aktView.OnHide();

			if (aktView.equals(trackablelistView))
			{
				// Instanz löschenn
				aktView = null;
				trackablelistView.OnFree();
				trackablelistView = null;
			}
			else if (aktView.equals(tracklistView))
			{
				// Instanz löschenn
				aktView = null;
				tracklistView.OnFree();
				tracklistView = null;
			}
			else if (aktView.equals(jokerView))
			{
				// Instanz löschenn
				aktView = null;
				jokerView.OnFree();
				jokerView = null;
			}
			else if (aktView.equals(aboutView))
			{
				// Instanz löschenn
				aktView = null;
				aboutView.OnFree();
				aboutView = null;
			}
			else if (aktView.equals(fieldNotesView))
			{
				// Instanz löschenn
				aktView = null;
				fieldNotesView.OnFree();
				fieldNotesView = null;
			}
			else if (aktView.equals(compassView))
			{
				// Instanz löschenn
				aktView = null;
				compassView.OnFree();
				compassView = null;
			}
			else if (aktView.equals(solverView))
			{
				// Instanz löschenn
				aktView = null;
				solverView.OnFree();
				solverView = null;
			}
			else if (aktView.equals(notesView))
			{
				// Instanz löschenn
				aktView = null;
				notesView.OnFree();
				notesView = null;
			}
			else if (aktView.equals(spoilerView))
			{
				// Instanz löschenn
				aktView = null;
				spoilerView.OnFree();
				spoilerView = null;
			}
			else if (aktView.equals(descriptionView))
			{
				// Instanz löschenn
				aktView = null;
				descriptionView.OnFree();
				descriptionView = null;
			}
			else if (aktView.equals(logView))
			{
				// Instanz löschenn
				aktView = null;
				logView.OnFree();
				logView = null;
			}
			else if (aktView.equals(waypointView))
			{
				// Instanz löschenn
				aktView = null;
				waypointView.OnFree();
				waypointView = null;
			}
			else if (aktView.equals(mapViewGl))
			{
				this.onPause();
			}
		}

		System.gc();

		aktView = view;
		frame.removeAllViews();
		ViewParent parent = ((View) aktView).getParent();
		if (parent != null)
		{
			// aktView ist noch gebunden, also lösen
			((FrameLayout) parent).removeAllViews();
		}
		frame.addView((View) aktView);
		aktView.OnShow();
		aktViewId = Id;
		InfoDownSlider.invalidate();
		((View) aktView).forceLayout();

	}

	/*
	 * show ContextMenus
	 */

	public IconContextItemSelectedListener OnIconContextItemSelectedListener = new IconContextItemSelectedListener()
	{

		@Override
		public void onIconContextItemSelected(MenuItem item, Object info)
		{

			switch (item.getItemId())
			{
			case R.id.miScreenLock:
				startScreenLock(true);
				break;
			case R.id.miDeleteCaches:
				DeleteFilterSelection();
				break;
			case R.id.miClose:
				onKeyDown(KeyEvent.KEYCODE_BACK, null);
				break;
			case R.id.miDayNight:
				switchDayNight();
				break;
			case R.id.miSettings:
				showView(102);
				break;
			case R.id.miVoiceRecorder:
				recVoice();
				break;
			case R.id.miParking:
				showParkingDialog();
				break;
			case R.id.miRecordVideo:
				recVideo();
				break;
			case R.id.miAbout:
				showView(11);
				break;
			// case R.id.miTestEmpty:showView(10);break;
			case R.id.miImport:
				showView(103);
				break;
			case R.id.miLogView:
				showView(3);
				break;
			case R.id.miSpoilerView:
				showView(5);
				break;
			case R.id.miHint:
				showHint();
				break;
			case R.id.miFieldNotes:
				showView(9);
				openOptionsMenu();
				break;
			case R.id.miTelJoker:
				showJoker();
				break;
			case R.id.miCompassView:
				showView(8);
				break;
			case R.id.miMapView:
				showView(0);
				break;
			case R.id.miMapViewGl:
				showView(2);
				break;
			case R.id.miDescription:
				showView(4);
				break;
			case R.id.miWaypoints:
				showView(15);
				break;
			case R.id.miNotes:
				showView(6);
				break;
			case R.id.miSolver:
				showView(7);
				break;
			case R.id.miCacheList:
				showView(1);
				break;
			case R.id.miTrackList:
				showView(13);
				break;
			case R.id.miFilterset:
				showView(101);
				break;
			case R.id.miManageDB:
				showManageDB();
				break;
			case R.id.miResort:
				Database.Data.Query.Resort();
				break;
			case R.id.miAutoResort:
				switchAutoResort();
				break;
			case R.id.miSearch:
				ListSearch();
				break;
			case R.id.miAddCache:
				addCache();
				break;
			// case R.id.searchcaches_online:
			// searchOnline();
			// break;
			case R.id.miChkState:
				chkCachesStateFilterSelection();
				break;
			case R.id.miTbList:
				showTbList();
				break;
			case R.id.miTakePhoto:
				takePhoto();
				break;
			case R.id.miTrackRec:
				AllContextMenuCallHandler.showTrackContextMenu();
				break;
			case R.id.miTrackStart:
				TrackRecorder.StartRecording();
				break;
			case R.id.miTrackStop:
				TrackRecorder.StopRecording();
				break;
			case R.id.miTrackPause:
				TrackRecorder.PauseRecording();
				break;
			case R.id.menu_tracklistview_generate:
				AllContextMenuCallHandler.showTrackListView_generateContextMenu();
				break;
			case R.id.miNavigateTo:
				NavigateTo();
				break;

			default: // wenn kein Eintrag gefunden wurde, versuch es im Akt View
				if (aktView != null) aktView.ItemSelected(item);

			}
		}

	};

	public static void vibrate()
	{
		if (Config.settings.vibrateFeedback.getValue()) vibrator.vibrate(20);
	}

	OnItemClickListener QuickButtonOnItemClickListner = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{

			// give feedback
			vibrate();

			QuickButtonItem clicedItem = Global.QuickButtonList.get(arg2);

			switch (clicedItem.getActionId())
			{
			case 0:
				showView(4);
				break;
			case 1:
				showView(15);
				break;
			case 2:
				showView(3);
				break;
			case 3:
				showView(0);
				break;
			case 4:
				showView(8);
				break;
			case 5:
				showView(1);
				break;
			case 6:
				showView(13);
				break;
			case 7:
				takePhoto();
				break;
			case 8:
				recVideo();
				break;
			case 9:
				recVoice();
				break;
			case 10:
				Search.Show();
				break;
			case 11:
				showView(101);
				break;
			case 12:
				startScreenLock(true);
				break;
			case 13:
				switchAutoResort();
				QuickButtonList.invalidate();
				break;
			case 14:
				showView(7);
				break;
			case 15:
				if (GlobalCore.SelectedCache() != null && GlobalCore.SelectedCache().SpoilerExists()) showView(5);
				break;
			case 16:
				if (GlobalCore.SelectedCache() != null && !(GlobalCore.SelectedCache().hint.equals(""))) showHint();
				break;
			case 17:
				showParkingDialog();
				break;
			case 18:
				switchDayNight();
				break;
			}
		}
	};

	/*
	 * Initial Methods
	 */

	private void findViewsById()
	{

		TopLayout = (LinearLayout) this.findViewById(R.id.layoutTop);
		frame = (FrameLayout) this.findViewById(R.id.layoutContent);
		InfoDownSlider = (downSlider) this.findViewById(R.id.downSlider);

		debugInfoPanel = (DebugInfoPanel) this.findViewById(R.id.debugInfo);
		Mic_Icon = (Mic_On_Flash) this.findViewById(R.id.mic_flash);

		buttonDB = (ImageButton) this.findViewById(R.id.buttonDB);
		buttonCache = (ImageButton) this.findViewById(R.id.buttonCache);
		buttonNav = (ImageButton) this.findViewById(R.id.buttonMap);
		buttonTools = (ImageButton) this.findViewById(R.id.buttonInfo);
		buttonMisc = (ImageButton) this.findViewById(R.id.buttonMisc);

		cacheNameView = (CacheNameView) this.findViewById(R.id.main_cache_name_view);
		QuickButtonList = (HorizontalListView) this.findViewById(R.id.main_quick_button_list);
		strengthLayout = (LinearLayout) this.findViewById(R.id.main_strength_control);

		searchLayout = (LinearLayout) this.findViewById(R.id.searchDialog);

		if (Config.settings.nightMode.getValue())
		{
			if (Config.settings.isChris.getValue())
			{
				buttonCache.setBackgroundResource(R.drawable.chris_night_cache_button_image_selector);
			}
			else
			{
				buttonCache.setBackgroundResource(R.drawable.night_cache_button_image_selector);
			}
		}
		else
		{
			if (Config.settings.isChris.getValue())
			{
				buttonCache.setBackgroundResource(R.drawable.chris_cache_button_image_selector);
				buttonDB.setBackgroundResource(R.drawable.chris_db_button_image_selector);
				buttonMisc.setBackgroundResource(R.drawable.chris_misc_button_image_selector);
				buttonNav.setBackgroundResource(R.drawable.chris_nav_button_image_selector);
				buttonTools.setBackgroundResource(R.drawable.chris_find_button_image_selector);
			}
			else
			{
				buttonCache.setBackgroundResource(R.drawable.cache_button_image_selector);
			}
		}
		buttonCache.invalidate();
	}

	private void initialViews()
	{
		if (cacheListView == null) cacheListView = new CacheListView(this);
		ViewList.add(mapView); // ID 0
		ViewList.add(cacheListView); // ID 1
	}

	private void initialLocationManager()
	{

		try
		{
			if (locationManager != null)
			{
				// ist schon initialisiert
				return;
			}

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
			// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
			// 1000, 1, this);
			// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
			// 1000, 10, this);

			/*
			 * Longri: Ich habe die Zeiten und Distanzen der Location Updates angepasst. Der Network Provider hat eine schlechte
			 * genauigkeit, darher reich es wenn er alle 10sec einen wert liefert, wen der alte um 500m abweicht. Beim GPS Provider habe ich
			 * die aktualiesierungs Zeit verkürzt, damit bei deaktiviertem Hardware Kompass aber die Werte trotzdem noch in einem gesunden
			 * Verhältnis zwichen Performance und Stromverbrauch, geliefert werden. Andere apps haben hier 0.
			 */

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 500, this);

			locationManager.addNmeaListener(this);
			locationManager.addGpsStatusListener(new GPS.GpsStatusListener(locationManager));
		}
		catch (Exception e)
		{
			Logger.Error("main.initialLocationManager()", "", e);
			e.printStackTrace();
		}

	}

	private void initialMapView()
	{
		try
		{
			if (mapView == null)
			{
				mapView = new MapView(this, inflater);
				mapView.Initialize();
				mapView.CurrentLayer = MapView.Manager.GetLayerByName(Config.settings.CurrentMapLayer.getValue(),
						Config.settings.CurrentMapLayer.getValue(), "");
				Global.TrackDistance = Config.settings.TrackDistance.getValue();
				mapView.InitializeMap();
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.initialMapView()", "", e);
			e.printStackTrace();
		}
	}

	private void initialMapViewGl()
	{
		try
		{
			if (mapViewGl == null)
			{
				viewGl = initializeForView(mapViewGlListener, true);

				mapViewGl = new MapViewGL(this, inflater, viewGl, mapViewGlListener);

				mapViewGl.Initialize();
				// mapViewGl.CurrentLayer =
				// MapView.Manager.GetLayerByName(Config.settings.CurrentMapLayer.getValue(),
				// Config.settings.CurrentMapLayer.getValue(), "");
				// Global.TrackDistance =
				// Config.settings.TrackDistance.getValue();
				mapViewGl.InitializeMap();

			}
		}
		catch (Exception e)
		{
			Logger.Error("main.initialMapViewGl()", "", e);
			e.printStackTrace();
		}
	}

	private void initalMicIcon()
	{
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

		registerForContextMenu(buttonTools);
		this.buttonTools.setOnClickListener(ButtonOnClick);

		registerForContextMenu(buttonMisc);
		this.buttonMisc.setOnClickListener(ButtonOnClick);

	}

	/*
	 * InfoSlider
	 */

	private void initialCaheInfoSlider()
	{

		QuickButtonList.setHeight(Sizes.getQuickButtonListHeight());
		QuickButtonList.setAdapter(QuickButtonsAdapter);
		QuickButtonList.setOnItemClickListener(QuickButtonOnItemClickListner);
		String ConfigActionList = Config.settings.quickButtonList.getValue();
		String[] ConfigList = ConfigActionList.split(",");
		Global.QuickButtonList = Actions.getListFromConfig(ConfigList);

		// cacheNameView.setHeight((int) (Sizes.getScaledRefSize_normal() *
		// 3.3));

	}

	private void showParkingDialog()
	{
		final Intent parkingIntent = new Intent().setClass(mainActivity, ParkingDialog.class);
		mainActivity.startActivityForResult(parkingIntent, Global.REQUEST_CODE_PARKING_DIALOG);
	}

	private void takePhoto()
	{
		// Log.d("DroidCachebox", "Starting camera on the phone...");

		// define the file-name to save photo taken by Camera activity
		String directory = Config.settings.UserImageFolder.getValue();
		if (!FileIO.DirectoryExists(directory))
		{
			// Log.d("DroidCachebox", "Media-Folder does not exist...");
			return;
		}

		basename = Global.GetDateTimeString();

		if (GlobalCore.SelectedCache() != null)
		{
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.SelectedCache().GcCode + "-" + GlobalCore.SelectedCache().Name);
			mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
			// Title = Global.SelectedCache().Name;
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
		startActivityForResult(intent, Global.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private void recVideo()
	{
		// Log.d("DroidCachebox", "Starting video on the phone...");

		// define the file-name to save video taken by Camera activity
		String directory = Config.settings.UserImageFolder.getValue();
		if (!FileIO.DirectoryExists(directory))
		{
			// Log.d("DroidCachebox", "Media-Folder does not exist...");
			return;
		}

		basename = Global.GetDateTimeString();

		if (GlobalCore.SelectedCache() != null)
		{
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.SelectedCache().GcCode + "-" + GlobalCore.SelectedCache().Name);
			mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
			// Title = Global.SelectedCache().Name;
		}
		else
		{
			mediaCacheName = "Video";
		}

		basename += " " + mediaCacheName;
		mediafile = new File(directory + "/" + basename + ".3gp");

		// Da ein Video keine Momentaufnahme ist, muss die Zeit und die
		// Koordinaten beim Start der Aufnahme verwendet werden.
		mediaTimeString = Global.GetTrackDateTimeString();
		mediaCoordinate = GlobalCore.LastValidPosition;

		ContentValues values = new ContentValues();
		values.put(MediaStore.Video.Media.TITLE, "captureTemp.mp4");
		cameraVideoURI = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

		final Intent videointent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		videointent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediafile));
		videointent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		// videointent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,
		// MAXIMUM_VIDEO_SIZE);

		startActivityForResult(videointent, Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	private void recVoice()
	{
		if (!getVoiceRecIsStart()) // Voice Recorder starten
		{
			// Log.d("DroidCachebox", "Starting voice recorder on the phone...");

			// define the file-name to save voice taken by activity
			String directory = Config.settings.UserImageFolder.getValue();
			if (!FileIO.DirectoryExists(directory))
			{
				// Log.d("DroidCachebox", "Media-Folder does not exist...");
				return;
			}

			basename = Global.GetDateTimeString();

			if (GlobalCore.SelectedCache() != null)
			{
				String validName = FileIO.RemoveInvalidFatChars(GlobalCore.SelectedCache().GcCode + "-" + GlobalCore.SelectedCache().Name);
				mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
				// Title = Global.SelectedCache().Name;
			}
			else
			{
				mediaCacheName = "Voice";
			}

			basename += " " + mediaCacheName;
			mediafilename = (directory + "/" + basename + ".wav");

			// Start recording
			// extAudioRecorder = ExtAudioRecorder.getInstanse(true); //
			// Compressed recording (AMR)
			extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed
																	// recording
																	// (WAV)

			extAudioRecorder.setOutputFile(mediafilename);
			extAudioRecorder.prepare();
			extAudioRecorder.start();

			String MediaFolder = Config.settings.UserImageFolder.getValue();
			String TrackFolder = Config.settings.TrackFolder.getValue();
			String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
			// Da eine Voice keine Momentaufnahme ist, muss die Zeit und die
			// Koordinaten beim Start der Aufnahme verwendet werden.
			TrackRecorder.AnnotateMedia(basename + ".wav", relativPath + "/" + basename + ".wav", GlobalCore.LastValidPosition,
					Global.GetTrackDateTimeString());
			Toast.makeText(mainActivity, "Start Voice Recorder", Toast.LENGTH_SHORT).show();

			setVoiceRecIsStart(true);
			counter.cancel(); // Während der Aufnahme Screen-Lock-Counter
								// stoppen
			counterStopped = true;

			return;
		}
		else
		{ // Voice Recorder stoppen
			// Log.d("DroidCachebox", "Stoping voice recorder on the phone...");
			// Stop recording
			setVoiceRecIsStart(false);
			return;
		}
	}

	private void DeleteFilterSelection()
	{
		final Intent delIntent = new Intent().setClass(mainActivity, DeleteDialog.class);
		mainActivity.startActivityForResult(delIntent, Global.REQUEST_CODE_DELETE_DIALOG);
	}

	private void showHint()
	{
		if (GlobalCore.SelectedCache() == null) return;
		String hint = Database.Hint(GlobalCore.SelectedCache());
		if (hint.equals("")) return;

		final Intent hintIntent = new Intent().setClass(mainActivity, HintDialog.class);
		Bundle b = new Bundle();
		b.putSerializable("Hint", hint);
		hintIntent.putExtras(b);
		mainActivity.startActivity(hintIntent);
	}

	private void showJoker()
	{
		if (!Config.settings.hasCallPermission.getValue()) return;

		// Debug add Joker
		// Global.Jokers.AddJoker("Andre", "Höpfner", "Katipa", "12", "030 ++++++", "24/7");
		// Global.Jokers.AddJoker("Andre", "Höpfner", "Katipa", "12", "030 ++++++", "24/7");

		if (Global.Jokers.isEmpty())
		{ // Wenn Telefonjoker-Liste leer neu laden

			try
			{
				URL url = new URL("http://www.gcjoker.de/cachebox.php?md5=" + Config.settings.GcJoker.getValue() + "&wpt="
						+ GlobalCore.SelectedCache().GcCode);
				URLConnection urlConnection = url.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;

				// Get the HTTP response code
				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					InputStream inputStream = httpConnection.getInputStream();
					if (inputStream != null)
					{
						String line;
						try
						{
							BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
							while ((line = reader.readLine()) != null)
							{
								String[] s = line.split(";", 7);
								try
								{
									if (s[0].equals("2")) // 2 entspricht
									// Fehler,
									// Fehlerursache ist
									// in S[1]
									{
										MessageBox.Show(s[1]);
										break;
									}
									if (s[0].equals("1")) // 1 entspricht
									// Warnung, Ursache
									// ist in S[1]
									{ // es können aber noch gültige Einträge
										// folgen
										MessageBox.Show(s[1]);
									}
									if (s[0].equals("0")) // Normaler Eintrag
									{
										Global.Jokers.AddJoker(s[1], s[2], s[3], s[4], s[5], s[6]);
									}
								}
								catch (Exception exc)
								{
									Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", exc);
									return;
								}
							}
							if (Global.Jokers.isEmpty())
							{
								MessageBox.Show(GlobalCore.Translations.Get("noJokers"));
							}
							else
							{
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
			catch (MalformedURLException urlEx)
			{
				Logger.Error("main.initialBtnInfoContextMenu()", "MalformedURLException HTTP response Jokers", urlEx);
				// Log.d("DroidCachebox", urlEx.getMessage());
			}
			catch (IOException ioEx)
			{
				Logger.Error("main.initialBtnInfoContextMenu()", "IOException HTTP response Jokers", ioEx);
				// Log.d("DroidCachebox", ioEx.getMessage());
				MessageBox.Show(GlobalCore.Translations.Get("internetError"));
			}
			catch (Exception ex)
			{
				Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", ex);
				// Log.d("DroidCachebox", ex.getMessage());
			}
		}
	}

	private void showTbList()
	{
		// MessageBox.Show("comming soon", "sorry", MessageBoxIcon.Asterisk);
		showView(14);
	}

	private void switchDayNight()
	{
		// frame.removeAllViews();
		Config.changeDayNight();
		DescriptionViewControl.mustLoadDescription = true;
		downSlider.isInitial = false;
		ActivityUtils.changeToTheme(mainActivity, Config.settings.nightMode.getValue() ? ActivityUtils.THEME_NIGHT
				: ActivityUtils.THEME_DAY);
		Toast.makeText(mainActivity, "changeDayNight", Toast.LENGTH_SHORT).show();

	}

	private void switchAutoResort()
	{
		Global.autoResort = !(Global.autoResort);

		Config.settings.AutoResort.setValue(Global.autoResort);

		if (Global.autoResort)
		{
			Database.Data.Query.Resort();
		}
	}

	private void showManageDB()
	{
		SelectDB.autoStart = false;
		Config.settings.WriteToDB();
		Intent selectDBIntent = new Intent().setClass(mainActivity, SelectDB.class);
		mainActivity.startActivityForResult(selectDBIntent, 546132);
	}

	public void ListSearch()
	{
		Search.Show();
	}

	private void NavigateTo()
	{
		if (GlobalCore.SelectedCache() != null)
		{
			double lat = GlobalCore.SelectedCache().Latitude();
			double lon = GlobalCore.SelectedCache().Longitude();

			if (GlobalCore.SelectedWaypoint() != null)
			{
				lat = GlobalCore.SelectedWaypoint().Latitude();
				lon = GlobalCore.SelectedWaypoint().Longitude();
			}

			/*
			 * Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon)); if (intent != null) {
			 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); this.startActivity(intent); }
			 */

			String INTENT_EXTRA_KEY_LATITUDE = "latitude";
			String INTENT_EXTRA_KEY_LONGITUDE = "longitude";
			// Long/Latarefloatvaluesin decimaldegreeformat(+-DDD.DDDDD).

			PackageManager currentPM = getPackageManager();

			Intent intent = null;
			try
			{
				intent = currentPM.getLaunchIntentForPackage("com.navigon.navigator");
			}
			catch (Exception e)
			{
				// Kein Navigon ohne public intent Instaliert
				e.printStackTrace();
			}

			if (intent == null)
			{
				try
				{
					intent = new Intent("android.intent.action.navigon.START_PUBLIC");
				}
				catch (Exception e)
				{
					// Kein Navigon mit public intent Instaliert
					e.printStackTrace();
				}
			}

			Intent implicitIntent = null;
			try
			{
				implicitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + lat + "," + lon));
			}
			catch (Exception e)
			{
				// Kein Google Navigator mit public intent Instaliert
				e.printStackTrace();
			}

			try
			{
				if (intent != null)
				{
					intent.putExtra(INTENT_EXTRA_KEY_LATITUDE, (float) lat);
					intent.putExtra(INTENT_EXTRA_KEY_LONGITUDE, (float) lon);
					startActivity(intent);
				}
				else if (implicitIntent != null)
				{
					startActivity(implicitIntent);
				}
			}
			catch (Exception e)
			{
				// Start Extern Navigation Fehler
				Logger.Error("main.NavigateTo()", "Start Extern Navigation Fehler", e);
			}

		}
	}

	private void addCache()
	{
		/*
		 * String accessToken = Config.settings.GcAPI"); ArrayList<String> caches = new ArrayList<String>(); caches.add("GC2XVHW");
		 * caches.add("GC1T2XP"); caches.add("GC1090W"); caches.clear(); for (int i = 0; i < 100; i++) { caches.add("GC2XV" + i); }
		 * CB_Core.Api.GroundspeakAPI.GetGeocacheStatus(accessToken, caches);
		 */

		int status = CB_Core.Api.GroundspeakAPI.GetCacheLimits(Config.GetAccessToken());
		if (status != 0) MessageBox.Show(CB_Core.Api.GroundspeakAPI.LastAPIError);

		MessageBox.Show("Cache hinzufügen ist noch nicht implementiert!", "Sorry", MessageBoxIcon.Asterisk);
	}

	public void GetApiAuth()
	{
		Intent gcApiLogin = new Intent().setClass(mainActivity, GcApiLogin.class);
		mainActivity.startActivityForResult(gcApiLogin, 987654321);
	}

	public void searchOnline()
	{
		if ("".equals(Config.GetAccessToken()))
		{
			MessageBox.Show(GlobalCore.Translations.Get("apiKeyNeeded"), GlobalCore.Translations.Get("Clue"), MessageBoxButtons.OK,
					MessageBoxIcon.Exclamation, null);
		}
		else
		{
			IsPremiumThread = new isPremiumThread();
			IsPremiumThread.execute("");
			pd = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("chkApiState"), "Groundspeak API", MessageBoxButtons.Cancel,
					MessageBoxIcon.GC_Live, Cancel1ClickListner);
		}
	}

	private isPremiumThread IsPremiumThread;

	private final DialogInterface.OnClickListener Cancel1ClickListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			IsPremiumThread.cancel(false);
			dialog.dismiss();
		}
	};

	private class isPremiumThread extends AsyncTask<String, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(String... params)
		{
			int ret = GroundspeakAPI.GetMembershipType(Config.GetAccessToken());
			isPremiumReadyHandler.sendMessage(isPremiumReadyHandler.obtainMessage(ret));
			return null;
		}

	}

	private Handler isPremiumReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			pd.dismiss();

			if (msg.what == 3)
			{
				// searchOnlineNow();
				showTargetApiDialog();
			}
			else
			{
				MessageBox.Show(GlobalCore.Translations.Get("GC_basic"), GlobalCore.Translations.Get("GC_title"),
						MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, PremiumMemberResult);
			}
		}
	};

	private DialogInterface.OnClickListener PremiumMemberResult = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			switch (button)
			{
			case -1:
				showTargetApiDialog();
				break;

			}

			dialog.dismiss();

		}
	};

	private DialogInterface pd;
	private loaderThread LoaderThread;
	private chkStateThread ChkStateThread;

	private void showTargetApiDialog()
	{
		final Intent mainIntent = new Intent().setClass(this, ApiSearchPosDialog.class);
		this.startActivityForResult(mainIntent, Global.REQUEST_CODE_API_TARGET_DIALOG);
	}

	private void searchOnlineNow()
	{
		LoaderThread = new loaderThread();
		LoaderThread.execute("");
		pd = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("searchingOnline"), "Groundspeak API", MessageBoxButtons.Cancel,
				MessageBoxIcon.GC_Live, CancelClickListner);
	}

	private final DialogInterface.OnClickListener CancelClickListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			LoaderThread.cancel(false);
			dialog.dismiss();
		}
	};

	private Coordinate searchCoord = null;

	private class loaderThread extends AsyncTask<String, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(String... params)
		{
			if (searchCoord == null)
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));
				return null;
			}

			String accessToken = Config.GetAccessToken();

			// alle per API importierten Caches landen in der Category und
			// GpxFilename
			// API-Import
			// Category suchen, die dazu gehört
			CategoryDAO categoryDAO = new CategoryDAO();
			Category category = categoryDAO.GetCategory(GlobalCore.Categories, "API-Import");
			if (category == null) return null; // should not happen!!!

			GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
			if (gpxFilename == null) return null;

			ArrayList<Cache> apiCaches = new ArrayList<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
			CB_Core.Api.SearchForGeocaches.SearchCoordinate searchC = new CB_Core.Api.SearchForGeocaches.SearchCoordinate();

			searchC.withoutFinds = Config.settings.SearchWithoutFounds.getValue();
			searchC.withoutOwn = Config.settings.SearchWithoutOwns.getValue();

			searchC.pos = searchCoord;
			searchC.distanceInMeters = Config.settings.lastSearchRadius.getValue() * 1000;
			searchC.number = 50;
			CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id);
			if (apiCaches.size() > 0)
			{
				Database.Data.beginTransaction();

				CacheDAO cacheDAO = new CacheDAO();
				LogDAO logDAO = new LogDAO();
				ImageDAO imageDAO = new ImageDAO();
				WaypointDAO waypointDAO = new WaypointDAO();

				for (Cache cache : apiCaches)
				{
					cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
					cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());
					if (Database.Data.Query.GetCacheById(cache.Id) == null)
					{
						Database.Data.Query.add(cache);
						cacheDAO.WriteToDatabase(cache);

						for (LogEntry log : apiLogs)
						{
							if (log.CacheId != cache.Id) continue;
							// Write Log to database

							logDAO.WriteToDatabase(log);
						}

						for (ImageEntry image : apiImages)
						{
							if (image.CacheId != cache.Id) continue;
							// Write Image to database

							imageDAO.WriteToDatabase(image, false);
						}

						for (Waypoint waypoint : cache.waypoints)
						{

							waypointDAO.WriteToDatabase(waypoint);
						}
					}
				}
				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();

				Database.Data.GPXFilenameUpdateCacheCount();

				if (mapView.isShown())
				{
					mapView.updateCacheList();
					mapView.Render(true);
				}

			}

			onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
			return null;
		}

	}

	private void chkCachesStateFilterSelection()
	{

		ChkStateThread = new chkStateThread();
		ChkStateThread.execute("");
		pd = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("chkState"), "Groundspeak API", MessageBoxButtons.Cancel,
				MessageBoxIcon.GC_Live, ChkStateThreadCancelClickListner);

	}

	private class chkStateThread extends AsyncTask<String, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(String... params)
		{
			ArrayList<Cache> chkList = new ArrayList<Cache>();
			Iterator<Cache> cIterator = Database.Data.Query.iterator();

			do
			{
				chkList.add(cIterator.next());
			}
			while (cIterator.hasNext());

			// in 100èrter Blöcke Teilen

			int start = 0;
			int stop = 100;
			ArrayList<Cache> addedReturnList = new ArrayList<Cache>();

			int result;
			ArrayList<Cache> chkList100;
			do
			{
				Iterator<Cache> Iterator2 = chkList.iterator();
				chkList100 = new ArrayList<Cache>();

				int index = 0;
				do
				{
					if (index >= start && index <= stop)
					{
						chkList100.add(Iterator2.next());
					}
					else
					{
						Iterator2.next();
					}
					index++;
				}
				while (Iterator2.hasNext());

				result = GroundspeakAPI.GetGeocacheStatus(Config.GetAccessToken(), chkList100);
				addedReturnList.addAll(chkList100);
				start += 101;
				stop += 101;
			}
			while (chkList100.size() == 101);

			if (result == 0)
			{
				Database.Data.beginTransaction();

				Iterator<Cache> iterator = addedReturnList.iterator();
				CacheDAO dao = new CacheDAO();
				do
				{
					Cache writeTmp = iterator.next();
					dao.UpdateDatabaseCacheState(writeTmp);
				}
				while (iterator.hasNext());

				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));

			}
			else
			{
				onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));

			}
			return null;
		}

	}

	private final DialogInterface.OnClickListener ChkStateThreadCancelClickListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			ChkStateThread.cancel(false);
			dialog.dismiss();
		}
	};

	private Handler onlineSearchReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				pd.dismiss();

				cacheListView.notifyCacheListChange();
				break;
			}

			case 2:
			{
				pd.dismiss();
				MessageBox.Show(GlobalCore.Translations.Get("errorAPI"), GlobalCore.Translations.Get("Error"), MessageBoxIcon.Error);
				break;
			}

			}
		}
	};

	/*
	 * Setter
	 */

	public void setDebugVisible()
	{
		if (Config.settings.DebugShowPanel.getValue())
		{
			debugInfoPanel.setVisibility(View.VISIBLE);
			debugInfoPanel.onShow();
		}
		else
		{
			debugInfoPanel.setVisibility(View.GONE);
			debugInfoPanel.onShow();
		}
	}

	String debugMsg = "";

	public void setDebugMsg(String msg)
	{
		debugMsg = msg;
		Thread t = new Thread()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						debugInfoPanel.setMsg(debugMsg);
					}
				});
			}
		};

		t.start();

	}

	public void setVoiceRecIsStart(Boolean value)
	{
		mVoiceRecIsStart = value;
		if (mVoiceRecIsStart)
		{
			Mic_Icon.SetOn();
		}
		else
		{ // Aufnahme stoppen
			Mic_Icon.SetOff();
			if (extAudioRecorder != null)
			{
				extAudioRecorder.stop();
				extAudioRecorder.release();
				extAudioRecorder = null;
				Toast.makeText(mainActivity, "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
			}
			if (runsWithAkku)
			{
				counterStopped = false; // ScreenLock-Counter wieder starten
				counter.start();
			}
		}

	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea)
	{
		try
		{
			if (nmea.length() >= 6 && nmea.substring(0, 6).equalsIgnoreCase("$GPGGA"))
			{
				String[] s = nmea.split(",");
				try
				{
					if (s[11].equals("")) return;
					double altCorrection = Double.valueOf(s[11]);
					Logger.General("AltCorrection: " + String.valueOf(altCorrection));
					Global.Locator.altCorrection = altCorrection;
					// Höhenkorrektur ändert sich normalerweise nicht, einmal
					// auslesen reicht...
					locationManager.removeNmeaListener(this);
				}
				catch (Exception exc)
				{
					// keine Höhenkorrektur vorhanden
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.onNmeaReceived()", "", e);
			e.printStackTrace();
		}
	}

	public void setScreenLockTimerNew(int value)
	{
		counter.cancel();
		counter = new ScreenLockTimer(value, value);
		if (runsWithAkku) counter.start();
	}

	static class LockClass
	{
	};

	static LockClass lockObject = new LockClass();

	/**
	 * Empfängt die gelogten Meldungen und schreibt sie in die Debug.txt
	 */
	@Override
	public void receiveLog(String Msg)
	{
		synchronized (lockObject)
		{
			File file = new File(Config.WorkPath + "/debug.txt");
			FileWriter writer;
			try
			{
				writer = new FileWriter(file, true);
				writer.write(Msg);
				writer.close();
			}
			catch (IOException e)
			{

				e.printStackTrace();
			}

			Log.d("CACHEBOX", Msg);
		}
	}

	/**
	 * Empfängt die gelogten Meldungen in kurz Form und schreibt sie ins Debung Panel, wenn dieses sichtbar ist!
	 */
	@Override
	public void receiveShortLog(String Msg)
	{
		debugMsg = Msg;
		Thread t = new Thread()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						debugInfoPanel.addLogMsg(debugMsg);
					}
				});
			}
		};

		t.start();

	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context arg0, Intent intent)
		{
			try
			{
				int plugged = intent.getIntExtra("plugged", -1);

				if (runsWithAkku != (plugged == 0))
				{
					// if loading status has changed
					runsWithAkku = plugged == 0;
					if (!runsWithAkku)
					{
						// activate counter when device runs with accu
						counter.cancel();
						counterStopped = true;
					}
					else
					{
						// deactivate counter when device is plugged in
						counter.start();
						counterStopped = false;
					}
				}
			}
			catch (Exception e)
			{
				Logger.Error("main.mBatInfoReceiver.onReceive()", "", e);
				e.printStackTrace();
			}
		}

	};

	int horizontalListViewHeigt;

	public void setQuickButtonHeight(int value)
	{
		horizontalListViewHeigt = value;
		Thread t = new Thread()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						QuickButtonList.setHeight(horizontalListViewHeigt);
						QuickButtonList.invalidate();
						TopLayout.requestLayout();
						frame.requestLayout();
					}
				});
			}
		};

		t.start();

	}

	/**
	 * Adapter für die QuickButton Lists.
	 * 
	 * @author Longri
	 */
	public BaseAdapter QuickButtonsAdapter = new BaseAdapter()
	{

		@Override
		public int getCount()
		{
			return Global.QuickButtonList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return Global.QuickButtonList.get(position);
		}

	};

	public static int getQuickButtonHeight()
	{
		return ((main) mainActivity).QuickButtonList.getHeight();
	}

	/**
	 * Überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung ausgegeben.
	 */
	private void chkGpsIsOn()
	{
		try
		{
			LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				Thread t = new Thread()
				{
					public void run()
					{
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								MessageBox.Show(GlobalCore.Translations.Get("GPSon?"), GlobalCore.Translations.Get("GPSoff"),
										MessageBoxButtons.YesNo, MessageBoxIcon.Question, new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int button)
											{
												// Behandle das ergebniss
												switch (button)
												{
												case -1:
													// yes open gps settings
													Intent gpsOptionsIntent = new Intent(
															android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

													startActivity(gpsOptionsIntent);
													break;
												case -2:
													// no,
													break;
												case -3:

													break;
												}

												dialog.dismiss();
											}

										});
							}
						});
					}
				};

				t.start();

			}
		}
		catch (Exception e)
		{
			Logger.Error("main.chkGpsIsOn()", "", e);
			e.printStackTrace();
		}
	}

	@Override
	public void GpsStateChanged()
	{
		try
		{
			setSatStrength();
		}
		catch (Exception e)
		{
			Logger.Error("main.GpsStateChanged()", "setSatStrength()", e);
			e.printStackTrace();
		}
	}

	private static View[] balken = null;

	public static void setSatStrength()
	{
		try
		{
			de.cachebox_test.Locator.GPS.setSatStrength(strengthLayout, balken);
		}
		catch (Exception e)
		{
			Logger.Error("main.setSatStrength()", "de.cachebox_test.Locator.GPS.setSatStrength()", e);
			e.printStackTrace();
		}
	}

	private void askToGetApiKey()
	{
		MessageBox.Show(GlobalCore.Translations.Get("wantApi"), GlobalCore.Translations.Get("welcome"), MessageBoxButtons.YesNo,
				MessageBoxIcon.GC_Live, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int button)
					{
						// Behandle das ergebniss
						switch (button)
						{
						case -1:
							// yes get Api key
							GetApiAuth();
							break;
						case -2:
							// no, we check GPS
							chkGpsIsOn();
							break;
						case -3:

							break;
						}

						dialog.dismiss();
					}

				});
	}

	public static void Toast(String Msg)
	{
		Toast.makeText(mainActivity, Msg, Toast.LENGTH_SHORT).show();
	}

}

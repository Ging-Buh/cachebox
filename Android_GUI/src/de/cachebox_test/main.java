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
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.TrackRecorder;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector.IShowViewListner;
import CB_Core.Events.platformConector.isOnlineListner;
import CB_Core.Events.platformConector.trackListListner;
import CB_Core.GL_UI.MenuID;
import CB_Core.GL_UI.MenuItemConst;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.ViewID.UI_Pos;
import CB_Core.GL_UI.ViewID.UI_Type;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.GL_UI.Main.MainView;
import CB_Core.Log.ILog;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Trackable;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.Locator;
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
import android.graphics.Point;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.DefaultGLSurfaceView;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;

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
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Ui.AndroidClipboard;
import de.cachebox_test.Views.AboutView;
import de.cachebox_test.Views.CacheListView;
import de.cachebox_test.Views.CompassView;
import de.cachebox_test.Views.DescriptionView;
import de.cachebox_test.Views.FieldNotesView;
import de.cachebox_test.Views.JokerView;
import de.cachebox_test.Views.LogView;
import de.cachebox_test.Views.MapView;
import de.cachebox_test.Views.NotesView;
import de.cachebox_test.Views.SolverView;
import de.cachebox_test.Views.SpoilerView;
import de.cachebox_test.Views.TrackListView;
import de.cachebox_test.Views.TrackableListView;
import de.cachebox_test.Views.ViewGL;
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
import de.cachebox_test.Views.Forms.ParkingDialog;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;
import de.cachebox_test.Views.Forms.ScreenLock;
import de.cachebox_test.Views.Forms.SelectDB;

public class main extends AndroidApplication implements SelectedCacheEvent, LocationListener, CB_Core.Events.CacheListChangedEvent,
		GpsStatus.NmeaListener, ILog, GpsStateChangeEvent
{

	private static final boolean useGL_Tab = true;
	static private final char BACKSPACE = 8;

	/*
	 * private static member
	 */

	public static ViewID aktViewId = null;
	public static ViewID aktTabViewId = null;

	private static long GPSTimeStamp = 0;
	public static MapView mapView = null; // ID 0
	public static CacheListView cacheListView = null; // ID 1

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

	private static devicesSizes ui;

	/**
	 * viewGl kann mehrere ID beinhalten, vieGL ist nur die Basis für alle Views auf Basis von GL_View_Base </br> TestView = 16 </br>
	 * CreditsView = 17 </br> MapView = 18 </br>
	 */
	public static ViewGL viewGL = null;

	/**
	 * gdxView ist die Android.View für gdx
	 */
	private View gdxView = null;

	private GL_Listener glListener = null;

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
	private FrameLayout tabFrame;
	private FrameLayout GlFrame;

	private LinearLayout TopLayout;
	public downSlider InfoDownSlider;
	public HorizontalListView QuickButtonList;

	private String GcCode = null;
	private String name = null;
	private String guid = null;

	private Mic_On_Flash Mic_Icon;
	private static DebugInfoPanel debugInfoPanel;

	// Views
	private ViewOptionsMenu aktView = null;
	private ViewOptionsMenu aktTabView = null;
	private CacheNameView cacheNameView;

	private ArrayList<ViewOptionsMenu> ViewList = new ArrayList<ViewOptionsMenu>();

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

	/**
	 * For Use the Vibrator, Call vibrate(); So the User can switch of this in Settings
	 */
	private static Vibrator vibrator;

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

		if (GlobalCore.isTab)
		{
			// Tab Modus nur Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}

		initialPlatformConector();

		// initialize receiver for screen switched on/off
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		BroadcastReceiver mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);

		Logger.Add(this);

		N = Config.settings.nightMode.getValue();

		setContentView(GlobalCore.isTab ? R.layout.tab_main : R.layout.main);

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainActivity = this;
		AllContextMenuCallHandler.Main = this;
		mainActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// initial UiSizes
		// UiSize Structur für die Berechnung der Größen zusammen stellen!
		// Resources res = this.getResources();

		final Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			ui = (devicesSizes) extras.get("UI");
		}

		UiSizes.initial(ui);

		// Initial GL_Listner in Full Screen with font color black

		if (useGL_Tab)
		{
			glListener = new Tab_GL_Listner(UiSizes.getWindowWidth(), UiSizes.getWindowHeight());
		}
		else
		{
			glListener = new GL_Listener(UiSizes.getWindowWidth(), UiSizes.getWindowHeight());
		}

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

		initialLocationManager();
		initialMapView();
		initialViewGL();
		initialViews();
		initalMicIcon();
		initialButtons();
		initialCaheInfoSlider();

		if (GlobalCore.SelectedCache() == null)
		{
			if (Database.Data == null)
			{
				String FilterString = Config.settings.Filter.getValue();
				GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0])
						: new FilterProperties(FilterString);
				String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

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

		if (useGL_Tab)
		{
			initialViewGL();

			glListener.onStart();
			if (tabFrame != null) tabFrame.setVisibility(View.INVISIBLE);
			if (frame != null) frame.setVisibility(View.INVISIBLE);

			InfoDownSlider.invalidate();

		}
		else
		{
			if (aktViewId != null)
			{
				// Zeige letzte gespeicherte View beim neustart der Activity
				showView(aktViewId);

			}
			else
			{

				// Start CB!

				Logger.General("------ Start Rev: " + Global.CurrentRevision + "-------");

				// Zeige About View als erstes!
				// showView(ViewConst.ABOUT_VIEW);

				// und Map wenn Tablet
				if (GlobalCore.isTab)
				{
					// showView(MAP_VIEW);
					// showView(ViewConst.DESCRIPTION_VIEW);
				}

				// chk if NightMode saved
				if (N)
				{
					ActivityUtils.changeToTheme(mainActivity, ActivityUtils.THEME_NIGHT, true);
				}

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

		int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? UiSizes
				.getQuickButtonListHeight() : 0;

		setQuickButtonHeight(sollHeight);

		LinearLayout BtnLayout = (LinearLayout) this.findViewById(R.id.layoutButtons);
		BtnLayout.setVisibility(View.INVISIBLE);

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
								showView(ViewConst.CACHE_LIST_VIEW);
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
		if (aktView == null) return false;
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

		setSelectedCache_onUI(cache, waypoint);
	}

	public void setSelectedCache_onUI(Cache cache, Waypoint waypoint)
	{
		((main) main.mainActivity).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				approachSoundCompleted = false;
				cacheListView.setSelectedCacheVisible(0);
				initialCaheInfoSlider();
			}
		});
	}

	public void newLocationReceived(Location location)
	{

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
			InfoDownSlider.setNewLocation(GlobalCore.LastPosition);
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
						showView(ViewConst.COMPASS_VIEW);
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
			// schau die 50 nächsten Caches durch, wenn einer davon näher ist
			// als der aktuell nächste -> umsortieren und raus
			// only when showing Map or cacheList
			if (!GlobalCore.ResortAtWork)
			{
				if (GlobalCore.autoResort && ((aktView == mapView) || (aktView == cacheListView || aktView == compassView)))
				{
					int z = 0;
					if (!(GlobalCore.NearestCache() == null))
					{
						boolean resort = false;
						if (GlobalCore.NearestCache().Found)
						{
							resort = true;
						}
						else
						{
							for (Cache cache : Database.Data.Query)
							{
								z++;
								if (z >= 50) return;
								if (cache.Archived) continue;
								if (!cache.Available) continue;
								if (cache.Found) continue;
								if (cache.ImTheOwner()) continue;
								if (cache.Type == CacheTypes.Mystery) if (!cache.MysterySolved()) continue;
								if (cache.Distance(true) < GlobalCore.NearestCache().Distance(true))
								{
									resort = true;
									break;
								}
							}
						}
						if (resort)
						{
							Database.Data.Query.Resort();
							Global.PlaySound("AutoResort.ogg");
							return;
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
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		return CB_Core.Events.platformConector.sendKeyUp(keyCode);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Quitt();

			return true;
		}

		// send KeyCode to OpenGL-UI
		Character chr = (char) event.getUnicodeChar();
		if (keyCode == Keys.BACKSPACE) chr = BACKSPACE;

		if (CB_Core.Events.platformConector.sendKeyDown(keyCode) && CB_Core.Events.platformConector.sendKey(chr)) return true;

		return false;
	}

	private void Quitt()
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
	}

	@Override
	public void CacheListChangedEvent()
	{
		int ButtonBackGroundResource = 0;

		if ((GlobalCore.LastFilter == null) || (GlobalCore.LastFilter.ToString().equals(""))
				|| (PresetListViewItem.chkPresetFilter(FilterProperties.presets[0], GlobalCore.LastFilter.ToString()))
				&& !GlobalCore.LastFilter.isExtendsFilter())
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
		// Back from Activitiy
		glListener.onStart();

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
				GlobalCore.LastFilter = (Config.settings.Filter.getValue().length() == 0) ? new FilterProperties(
						FilterProperties.presets[0]) : new FilterProperties(Config.settings.Filter.getValue());
				// filterSettings.LoadFilterProperties(GlobalCore.LastFilter);
				Database.Data.GPXFilenameUpdateCacheCount();

				String sqlWhere = GlobalCore.LastFilter.getSqlWhere();
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
				FilterProperties props = GlobalCore.LastFilter;
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
				FilterProperties props = GlobalCore.LastFilter;
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
				nun = dao.DelFilter(GlobalCore.LastFilter.getSqlWhere());
				GlobalCore.LastFilter = new FilterProperties(FilterProperties.presets[0]);
				EditFilterSettings.ApplyFilter(mainActivity, GlobalCore.LastFilter);
				String msg = GlobalCore.Translations.Get("DeletedCaches", String.valueOf(nun));
				Toast(msg);
				return;
			}
			}
			return;
		}

		if (aktView != null) aktView.ActivityResult(requestCode, resultCode, data);
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
		Logger.LogCat("Main=> onPause");
		if (input == null)
		{
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, graphics.getView(), config);
		}

		super.onPause();
		// graphics.isShown = true;
	}

	@Override
	protected void onResume()
	{
		Logger.LogCat("Main=> onResume");
		if (input == null)
		{
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, graphics.getView(), config);
		}

		super.onResume();

		if (runsWithAkku) counter.start();
		if (mSensorManager != null) mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? UiSizes
				.getQuickButtonListHeight() : 0;
		((main) main.mainActivity).setQuickButtonHeight(sollHeight);
		downSlider.isInitial = false;
		InfoDownSlider.invalidate();

		// Ausschalten verhindern
		/*
		 * This code together with the one in onDestroy() will make the screen be always on until this Activity gets destroyed.
		 */
		if (Config.settings.SuppressPowerSaving.getValue())
		{
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			this.mWakeLock.acquire();
		}

	}

	@Override
	protected void onStop()
	{
		Logger.LogCat("Main=> onStop");

		if (mSensorManager != null) mSensorManager.unregisterListener(mListener);
		this.unregisterReceiver(this.mBatInfoReceiver);
		counter.cancel();
		super.onStop();

		// Ausschalten wieder zulassen!
		/*
		 * This code together with the one in onDestroy() will make the screen be always on until this Activity gets destroyed.
		 */
		if (Config.settings.SuppressPowerSaving.getValue())
		{
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
			this.mWakeLock.acquire();
		}
	}

	@Override
	public void onDestroy()
	{

		Logger.LogCat("Main=> onDestroy");
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

				// Config.settings.MapInitLatitude.setValue(mapViewGlListener.center.Latitude);
				// Config.settings.MapInitLongitude.setValue(mapViewGlListener.center.Longitude);
				// Config.settings.MapInitLatitude.setValue(MapViewForGl.center.Latitude);
				// Config.settings.MapInitLongitude.setValue(MapViewForGl.center.Longitude);

				Config.settings.WriteToDB();

				if (this.mWakeLock != null) this.mWakeLock.release();
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

				if (aktTabView != null)
				{
					aktTabView.OnHide();
					aktTabView.OnFree();
				}
				aktTabView = null;

				for (ViewOptionsMenu vom : ViewList)
				{
					vom.OnFree();
				}
				ViewList.clear();
				cacheListView = null;
				mapView = null;
				viewGL = null;
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
				if (aktTabView != null) aktTabView.OnHide();
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
				Energy.setDisplayOff();
				wasScreenOn = false;
			}
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				// and do whatever you need to do here
				Energy.setDisplayOn();
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

		// dontStop = true;
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
				showView(ViewConst.CACHE_LIST_VIEW);
			}

			else if (v == buttonCache)
			{
				showView(ViewConst.DESCRIPTION_VIEW);
			}

			else if (v == buttonNav)
			{
				showView(ViewConst.MAP_VIEW);
			}

			else if (v == buttonTools)
			{

			}

			else if (v == buttonMisc)
			{
				showView(ViewConst.ABOUT_VIEW);
			}
		}
	};

	private final SensorEventListener mListener = new SensorEventListener()
	{
		public void onSensorChanged(SensorEvent event)
		{

			if (GlobalCore.Locator == null)
			{
				GlobalCore.Locator = new Locator();
			}

			try
			{
				mCompassValues = event.values;
				GlobalCore.Locator.setCompassHeading(mCompassValues[0]);
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

	public static boolean ScreenLockOff = false;

	/**
	 * Gibt die zur ViewID gehörige View zurück und erstellst eine Instanz, wenn sie nicht exestiert.
	 * 
	 * @param ID
	 *            ViewID
	 * @return View
	 */
	private ViewOptionsMenu getView(ViewID ID)
	{
		// first chek if view on List
		if (ID.getID() < ViewList.size())
		{
			return ViewList.get(ID.getID());
		}

		if (ID == ViewConst.TB_LIST_VIEW) return trackablelistView = new TrackableListView(this, this);
		else if (ID == ViewConst.TRACK_LIST_VIEW) return tracklistView = new TrackListView(this, this);
		else if (ID == ViewConst.JOKER_VIEW) return jokerView = new JokerView(this, this);
		else if (ID == ViewConst.ABOUT_VIEW) return aboutView = new AboutView(this, inflater);
		else if (ID == ViewConst.FIELD_NOTES_VIEW) return fieldNotesView = new FieldNotesView(this, this);
		else if (ID == ViewConst.SOLVER_VIEW) return solverView = new SolverView(this, inflater);
		else if (ID == ViewConst.NOTES_VIEW) return notesView = new NotesView(this, inflater);
		else if (ID == ViewConst.SPOILER_VIEW) return spoilerView = new SpoilerView(this, inflater);
		else if (ID == ViewConst.DESCRIPTION_VIEW) return descriptionView = new DescriptionView(this, inflater);
		else if (ID == ViewConst.LOG_VIEW) return logView = new LogView(this);
		else if (ID == ViewConst.WAYPOINT_VIEW) return waypointView = new WaypointView(this, this);
		else if (ID == ViewConst.COMPASS_VIEW)
		{
			compassView = new CompassView(this, inflater);
			compassView.reInit();
			return compassView;
		}
		return null;
	}

	private void showActivity(ViewID ID)
	{
		if (ID == ViewConst.SETTINGS)
		{
			final Intent mainIntent = new Intent().setClass(mainActivity, SettingsScrollView.class);
			Bundle b = new Bundle();
			b.putSerializable("Show", -1);
			mainIntent.putExtras(b);
			mainActivity.startActivity(mainIntent);
		}
		else if (ID == ViewConst.FILTER_SETTINGS)
		{
			final Intent mainIntent1 = new Intent().setClass(mainActivity, EditFilterSettings.class);
			mainActivity.startActivity(mainIntent1);
		}
		else if (ID == ViewConst.IMPORT)
		{
			final Intent mainIntent2 = new Intent().setClass(mainActivity, ImportDialog.class);
			mainActivity.startActivity(mainIntent2);
		}
		else if (ID == ViewConst.SEARCH)
		{
			Search.Show();
		}
		else if (ID == ViewConst.MANAGE_DB)
		{
			showManageDB();
		}
		else if (ID == ViewConst.CHK_STATE_API)
		{
			chkCachesStateFilterSelection();
		}
		else if (ID == ViewConst.RELOAD_CACHE)
		{
			if (descriptionView != null) descriptionView.reloadCacheInfo();
		}
		else if (ID == ViewConst.NAVIGATE_TO)
		{
			NavigateTo();
		}
		else if (ID == ViewConst.TRACK_REC)
		{
			AllContextMenuCallHandler.showTrackContextMenu();
		}
		else if (ID == ViewConst.VOICE_REC)
		{
			recVoice();
		}
		else if (ID == ViewConst.TAKE_PHOTO)
		{
			takePhoto();
		}
		else if (ID == ViewConst.VIDEO_REC)
		{
			recVideo();
		}
		else if (ID == ViewConst.DELETE_CACHES)
		{
			DeleteFilterSelection();
		}
		else if (ID == ViewConst.PARKING)
		{
			showParkingDialog();
		}
		else if (ID == ViewConst.DAY_NIGHT)
		{
			switchDayNight();
		}
		else if (ID == ViewConst.LOCK)
		{
			startScreenLock(true);
		}
		else if (ID == ViewConst.QUIT)
		{
			Quitt();
		}

	}

	private void showView(ViewOptionsMenu view, ViewID ID)
	{
		if (ID == ViewConst.GL_MAP_VIEW) sendContentFrameSizeChanged();
		if (GlobalCore.isTab)
		{
			if (ID.getPos() == ViewID.UI_Pos.Right)
			{
				showTabletView(view, ID);
				return;
			}
		}

		if (aktView != null)
		{
			aktView.OnHide();

			if (ID.getType() == UI_Type.OpenGl)
			{
				this.onPause();
			}

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

		}

		if (ID.getType() == UI_Type.OpenGl)
		{

			ShowViewGL(ID);
			return;

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

		InfoDownSlider.invalidate();
		((View) aktView).forceLayout();

	}

	private void ShowViewGL(ViewID Id)
	{
		Log.d("CACHEBOX", "GL Frame" + GlFrame.getMeasuredWidth() + "/" + GlFrame.getMeasuredHeight());

		initialViewGL();

		glListener.onStart();
		glListener.setGLViewID(Id);

		if (aktTabViewId != null && aktTabViewId.getType() == ViewID.UI_Type.OpenGl)
		{
			tabFrame.setVisibility(View.INVISIBLE);
		}

		if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.OpenGl)
		{
			frame.setVisibility(View.INVISIBLE);
		}

		InfoDownSlider.invalidate();

	}

	private void showTabletView(ViewOptionsMenu view, ViewID Id)
	{

		if (aktTabView != null)
		{
			aktTabView.OnHide();

		}

		System.gc();

		aktTabView = view;
		tabFrame.removeAllViews();
		ViewParent parent = ((View) aktTabView).getParent();
		if (parent != null)
		{
			// aktView ist noch gebunden, also lösen
			((FrameLayout) parent).removeAllViews();
		}
		tabFrame.addView((View) aktTabView);
		aktTabView.OnShow();
		aktTabViewId = Id;
		InfoDownSlider.invalidate();
		((View) aktTabView).forceLayout();

	}

	/*
	 * 
	 * 
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
				Quitt();
				break;
			case R.id.miDayNight:
				switchDayNight();
				break;
			case R.id.miSettings:
				showView(ViewConst.SETTINGS);
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
				showView(ViewConst.CREDITS_VIEW); // Show CreditsView
				break;
			case R.id.miImport:
				showView(ViewConst.IMPORT);
				break;
			case R.id.miLogView:
				showView(ViewConst.LOG_VIEW);
				break;
			case R.id.miSpoilerView:
				showView(ViewConst.SPOILER_VIEW);
				break;
			case R.id.miHint:
				showHint();
				break;
			case R.id.miFieldNotes:
				showView(ViewConst.FIELD_NOTES_VIEW);
				openOptionsMenu();
				break;
			case R.id.miTelJoker:
				showJoker();
				break;
			case R.id.miCompassView:
				showView(ViewConst.COMPASS_VIEW);
				break;
			case R.id.miMapView:
				showView(ViewConst.MAP_VIEW);
				break;
			case R.id.miViewGL:
				showView(ViewConst.MAP_CONTROL_TEST_VIEW);
				// showView(ViewConst.TEST_VIEW);
				break;
			case R.id.miViewMap3:
				showView(ViewConst.GL_MAP_VIEW);
				break;
			case R.id.miDescription:
				showView(ViewConst.DESCRIPTION_VIEW);
				break;
			case R.id.miWaypoints:
				showView(ViewConst.WAYPOINT_VIEW);
				break;
			case R.id.miNotes:
				showView(ViewConst.NOTES_VIEW);
				break;
			case R.id.miSolver:
				showView(ViewConst.SOLVER_VIEW);
				break;
			case R.id.miCacheList:
				showView(ViewConst.CACHE_LIST_VIEW);
				break;
			case R.id.miTrackList:
				showView(ViewConst.TRACK_LIST_VIEW);
				break;
			case R.id.miFilterset:
				showView(ViewConst.FILTER_SETTINGS);
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
				showView(ViewConst.DESCRIPTION_VIEW);
				break;
			case 1:
				showView(ViewConst.WAYPOINT_VIEW);
				break;
			case 2:
				showView(ViewConst.LOG_VIEW);
				break;
			case 3:
				showView(ViewConst.MAP_VIEW);
				break;
			case 4:
				showView(ViewConst.COMPASS_VIEW);
				break;
			case 5:
				showView(ViewConst.CACHE_LIST_VIEW);
				break;
			case 6:
				showView(ViewConst.TRACK_LIST_VIEW);
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
			// case 11:
			// showView(101);
			// break;
			case 12:
				startScreenLock(true);
				break;
			case 13:
				switchAutoResort();
				QuickButtonList.invalidate();
				break;
			case 14:
				showView(ViewConst.SOLVER_VIEW);
				break;
			case 15:
				if (GlobalCore.SelectedCache() != null && GlobalCore.SelectedCache().SpoilerExists()) showView(ViewConst.SPOILER_VIEW);
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
		tabFrame = (FrameLayout) this.findViewById(R.id.tabletLayoutContent);
		GlFrame = (FrameLayout) this.findViewById(R.id.layoutGlContent);

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
		QuickButtonList.setBackgroundDrawable(Global.BtnIcons[20]);

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

			// Global.Locator = new Locator();
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
				GlobalCore.TrackDistance = Config.settings.TrackDistance.getValue();
				mapView.InitializeMap();
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.initialMapView()", "", e);
			e.printStackTrace();
		}
	}

	// Zwischenspeicher für die touchDown Positionen der einzelnen Finger
	private SortedMap<Integer, Point> touchDownPos = new TreeMap<Integer, Point>();

	// Abstand zweier Punkte
	private int distance(Point p1, Point p2)
	{
		return (int) Math.round(Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)));
	}

	private void initialViewGL()
	{
		try
		{
			if (viewGL == null)
			{
				gdxView = initializeForView(glListener, false);

				int GlSurfaceType = -1;
				if (gdxView instanceof GLSurfaceView20) GlSurfaceType = ViewGL.GLSURFACE_VIEW20;
				if (gdxView instanceof GLSurfaceViewCupcake) GlSurfaceType = ViewGL.GLSURFACE_CUPCAKE;
				if (gdxView instanceof DefaultGLSurfaceView) GlSurfaceType = ViewGL.GLSURFACE_DEFAULT;
				if (gdxView instanceof GLSurfaceView) GlSurfaceType = ViewGL.GLSURFACE_GLSURFACE;

				ViewGL.setSurfaceType(GlSurfaceType);

				Logger.DEBUG("InitializeForView...");

				switch (GlSurfaceType)
				{
				case ViewGL.GLSURFACE_VIEW20:
					((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
					break;
				case ViewGL.GLSURFACE_CUPCAKE:
					((GLSurfaceViewCupcake) gdxView).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
					break;
				case ViewGL.GLSURFACE_DEFAULT:
					((DefaultGLSurfaceView) gdxView).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
					break;
				case ViewGL.GLSURFACE_GLSURFACE:
					((GLSurfaceView) gdxView).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
					break;
				}

				gdxView.setOnTouchListener(new OnTouchListener()
				{
					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						// Weitergabe der Toucheingabe an den Gl_Listener
						// ToDo: noch nicht fertig!!!!!!!!!!!!!
						int p = event.getActionIndex();
						try
						{
							switch (event.getActionMasked())
							{
							case MotionEvent.ACTION_POINTER_DOWN:
							case MotionEvent.ACTION_DOWN:
								glListener.onTouchDownBase((int) event.getX(p), (int) event.getY(p), event.getPointerId(p), 0);
								break;
							case MotionEvent.ACTION_MOVE:
								glListener.onTouchDraggedBase((int) event.getX(p), (int) event.getY(p), event.getPointerId(p));
								break;
							case MotionEvent.ACTION_POINTER_UP:
							case MotionEvent.ACTION_UP:
								glListener.onTouchUpBase((int) event.getX(p), (int) event.getY(p), event.getPointerId(p), 0);
								break;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
							return false;
						}
						return true;
					}
				});

				// mapViewGl = new MapViewGL(this, inflater, viewGl, mapViewGlListener);
				viewGL = new ViewGL(this, inflater, gdxView, glListener);

				viewGL.Initialize();
				viewGL.InitializeMap();

				GlFrame.removeAllViews();
				ViewParent parent = ((View) gdxView).getParent();
				if (parent != null)
				{
					// aktView ist noch gebunden, also lösen
					((RelativeLayout) parent).removeAllViews();
				}
				GlFrame.addView((View) gdxView);

			}
		}
		catch (Exception e)
		{
			Logger.Error("main.initialViewGL()", "", e);
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

		QuickButtonList.setHeight(UiSizes.getQuickButtonListHeight());
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
								showView(ViewConst.JOKER_VIEW);
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
		showView(ViewConst.TB_LIST_VIEW);
	}

	private void switchDayNight()
	{
		// frame.removeAllViews();
		// Config.changeDayNight();
		DescriptionViewControl.mustLoadDescription = true;
		downSlider.isInitial = false;
		ActivityUtils.changeToTheme(mainActivity, Config.settings.nightMode.getValue() ? ActivityUtils.THEME_NIGHT
				: ActivityUtils.THEME_DAY);
		Toast.makeText(mainActivity, "changeDayNight", Toast.LENGTH_SHORT).show();

	}

	private void switchAutoResort()
	{
		GlobalCore.autoResort = !(GlobalCore.autoResort);

		Config.settings.AutoResort.setValue(GlobalCore.autoResort);

		if (GlobalCore.autoResort)
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
				implicitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon));
			}
			catch (Exception e)
			{
				// Kein Google Navigator mit public intent Instaliert
				e.printStackTrace();
			}

			try
			{
				boolean NAVIGON_is_Start = true;
				if (intent != null)
				{
					try
					{
						intent.putExtra(INTENT_EXTRA_KEY_LATITUDE, (float) lat);
						intent.putExtra(INTENT_EXTRA_KEY_LONGITUDE, (float) lon);
						startActivity(intent);
					}
					catch (Exception e)
					{
						NAVIGON_is_Start = false;
					}
				}

				if (implicitIntent != null && !NAVIGON_is_Start)
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
					GlobalCore.Locator.altCorrection = altCorrection;
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

	/**
	 * Empfängt die gelogten Meldungen in kurz Form und schreibt sie ins Debung Panel, wenn dieses sichtbar ist!
	 */
	@Override
	public void receiveLogCat(String Msg)
	{
		Log.d("CACHEBOX", Msg);
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
						sendContentFrameSizeChanged();
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

	public void sendContentFrameSizeChanged()
	{
		if (frame != null)
		{
			if (GL_UISizes.set_Top_Buttom_Height(TopLayout.getHeight(), buttonDB.getHeight()))
			{
				if (MainView.mainView != null)
				{
					MainView.mainView.requestLayout();
				}
			}
		}
	}

	public void showView(ViewID ID)
	{
		if (ID == null)
		{
			Logger.LogCat("main.showView(is NULL)");
			return;// keine Action
		}

		String Pos = "null";
		String Type = "null";
		if (ID.getPos() != null)
		{
			Pos = ID.getPos().toString();
			Type = ID.getType().toString();
		}

		Logger.LogCat("main.showView(" + ID.getID() + "/" + Pos + "/" + Type + ")");

		if (ID.getType() == ViewID.UI_Type.Activity)
		{
			showActivity(ID);
			return;
		}

		if (GlobalCore.isTab)
		{
			if (ID.getPos() == UI_Pos.Left)
			{
				if (!(aktView == null) && ID == aktViewId)
				{
					aktView.OnShow();
					return;
				}
			}
			else
			{
				if (!(aktTabView == null) && ID == aktViewId)
				{
					aktTabView.OnShow();
					return;
				}
			}
		}
		else
		{
			if (!(aktView == null) && ID == aktViewId)
			{
				aktView.OnShow();
				return;
			}
		}

		if (ID.getPos() == UI_Pos.Left)
		{
			aktViewId = ID;
		}
		else
		{
			aktTabViewId = ID;
		}

		if (aktTabViewId != null && aktTabViewId.getType() == ViewID.UI_Type.Android)
		{
			if (tabFrame != null) tabFrame.setVisibility(View.VISIBLE);
		}

		if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.Android)
		{
			frame.setVisibility(View.VISIBLE);
		}

		if (ID.getType() == UI_Type.Android)
		{
			showView(getView(ID), ID);
		}

		if (ID.getType() == UI_Type.OpenGl)
		{
			ShowViewGL(ID);
		}

		if (ID == ViewConst.MAP_VIEW)
		{
			ScreenLockOff = true;
			startScreenLock(true);
		}

	}

	// ########### Platform Conector ##########################

	private void initialPlatformConector()
	{

		GlobalCore.platform = Plattform.Android;

		CB_Core.Events.platformConector.setisOnlineListner(new isOnlineListner()
		{
			/*
			 * isOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen
			 */

			@Override
			public boolean isOnline()
			{
				ConnectivityManager cm = (ConnectivityManager) main.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				if (netInfo != null && netInfo.isConnectedOrConnecting())
				{
					return true;
				}
				return false;
			}
		});

		CB_Core.Events.platformConector.setShowViewListner(new IShowViewListner()
		{

			@Override
			public void show(final ViewID viewID, int x, int y, final int width, final int height)
			{

				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Logger.LogCat("Show View from GL =>" + viewID.getID());

						// set Content size

						if (viewID.getType() != ViewID.UI_Type.Activity)
						{
							if (viewID.getPos() == UI_Pos.Left)
							{
								android.view.ViewGroup.LayoutParams params = frame.getLayoutParams();

								params.height = height;
								params.width = width;
							}
							else
							{
								if (tabFrame != null)
								{
									android.view.ViewGroup.LayoutParams params = tabFrame.getLayoutParams();

									params.height = height;
									params.width = width;
								}
							}
						}

						showView(viewID);
					}
				});

			}

			@Override
			public void hide(final ViewID viewID)
			{

				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (aktTabViewId != null && aktTabViewId == viewID && aktTabViewId.getPos() == UI_Pos.Right)
						{
							tabFrame.setVisibility(View.INVISIBLE);
							aktTabViewId = null;
							aktTabView = null;
						}

						if (aktViewId != null && aktViewId == viewID && aktViewId.getPos() == UI_Pos.Left)
						{
							frame.setVisibility(View.INVISIBLE);
							aktViewId = null;
							aktView = null;
						}
					}
				});

			}

			@Override
			public void showForDialog()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (aktView != null) ((View) aktView).setVisibility(View.INVISIBLE);
						if (aktTabView != null) ((View) aktTabView).setVisibility(View.INVISIBLE);
						if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.INVISIBLE);
						if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.INVISIBLE);
					}
				});

			}

			@Override
			public void hideForDialog()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (aktView != null) ((View) aktView).setVisibility(View.VISIBLE);
						if (aktTabView != null) ((View) aktTabView).setVisibility(View.VISIBLE);
						if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.VISIBLE);
						if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.VISIBLE);
					}
				});

			}

			@Override
			public void menuItemClicked(MenuID ID)
			{
				if (ID == MenuItemConst.TRACK_LIST_LOAD)
				{
					if (tracklistView != null) tracklistView.HandleLoad();
				}
				else if (ID == MenuItemConst.TRACK_LIST_DELETE)
				{
					if (tracklistView != null) tracklistView.HandleTrackDelete();
				}
				else if (ID == MenuItemConst.TRACK_LIST_CIRCLE)
				{
					if (tracklistView != null) tracklistView.HandleGenerate_Circle();
				}
				else if (ID == MenuItemConst.TRACK_LIST_P2P)
				{
					if (tracklistView != null) tracklistView.HandleGenerate_Point2Point();
				}
				else if (ID == MenuItemConst.TRACK_LIST_PROJECT)
				{
					if (tracklistView != null) tracklistView.HandleGenerate_Projection();
				}
				else if (ID == MenuItemConst.SHOW_WP_CONTEXT_MENU)
				{
					if (waypointView != null) waypointView.BeforeShowMenu(null);
				}
				else if (ID == MenuItemConst.SHOW_FIELD_NOTE_CONTEXT_MENU)
				{
					if (fieldNotesView != null) fieldNotesView.BeforeShowMenu(null);
				}
				else if (ID == MenuItemConst.SHOW_TB_CONTEXT_MENU)
				{
					if (trackablelistView != null) trackablelistView.BeforeShowMenu(null);
				}

			}
		});

		CB_Core.Events.platformConector.setGetTrackListner(new trackListListner()
		{

			@Override
			public String[] getTracks()
			{
				String[] ret = null;

				if (RouteOverlay.Routes != null)
				{
					ret = new String[RouteOverlay.Routes.size()];

					int i = 0;
					for (Trackable r : RouteOverlay.Routes)
					{
						ret[i] = r.FileName;
					}
				}
				return ret;
			}
		});

		// set AndroidClipboard

		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		if (cm != null) GlobalCore.setDefaultClipboard(new AndroidClipboard(cm));

	}

	// #########################################################

}

package de.cachebox_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openintents.intents.FileManagerIntents;

import CB_Core.Config;
import CB_Core.Energy;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.TrackRecorder;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.GpsStateChangeEvent;
import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.Events.KeyboardFocusChangedEvent;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.invalidateTextureEventList;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.IGetApiKey;
import CB_Core.Events.platformConector.IHardwarStateListner;
import CB_Core.Events.platformConector.IQuit;
import CB_Core.Events.platformConector.IShowViewListner;
import CB_Core.Events.platformConector.IgetFileListner;
import CB_Core.Events.platformConector.IgetFileReturnListner;
import CB_Core.Events.platformConector.IgetFolderListner;
import CB_Core.Events.platformConector.IgetFolderReturnListner;
import CB_Core.Events.platformConector.IsetKeybordFocus;
import CB_Core.Events.platformConector.IsetScreenLockTime;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.ViewID.UI_Pos;
import CB_Core.GL_UI.ViewID.UI_Type;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Activitys.settings.SettingsActivity;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.SearchDialog;
import CB_Core.GL_UI.Controls.PopUps.SearchDialog.searchMode;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.GL_Listener.GL.renderStartet;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Locator.Locator;
import CB_Core.Log.ILog;
import CB_Core.Log.Logger;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.DefaultGLSurfaceView;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;

import de.CB_PlugIn.IPlugIn;
import de.cachebox_test.NotifyService.LocalBinder;
import de.cachebox_test.Components.CacheNameView;
import de.cachebox_test.Custom_Controls.DebugInfoPanel;
import de.cachebox_test.Custom_Controls.Mic_On_Flash;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.QuickButtonList.HorizontalListView;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Locator.GPS;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AndroidClipboard;
import de.cachebox_test.Views.AboutView;
import de.cachebox_test.Views.DescriptionView;
import de.cachebox_test.Views.JokerView;
import de.cachebox_test.Views.NotesView;
import de.cachebox_test.Views.SolverView;
import de.cachebox_test.Views.SpoilerView;
import de.cachebox_test.Views.TrackableListView;
import de.cachebox_test.Views.ViewGL;
import de.cachebox_test.Views.Forms.GcApiLogin;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;

public class main extends AndroidApplication implements SelectedCacheEvent, LocationListener, CB_Core.Events.CacheListChangedEventListner,
		GpsStatus.NmeaListener, ILog, GpsStateChangeEvent, KeyboardFocusChangedEvent
{

	private static ServiceConnection mConnection;
	private static Intent serviceIntent;
	private static Service myNotifyService;
	private static BroadcastReceiver mReceiver;
	public boolean KeybordShown = false;

	public HorizontalListView QuickButtonList;

	private static hiddenTextField mTextField;

	/*
	 * private static member
	 */

	public static ViewID aktViewId = null;
	public static ViewID aktTabViewId = null;

	public static DescriptionView descriptionView = null; // ID 4
	private static SpoilerView spoilerView = null; // ID 5
	private static NotesView notesView = null; // ID 6
	private static SolverView solverView = null; // ID 7
	private static AboutView aboutView = null; // ID 11
	private static JokerView jokerView = null; // ID 12
	private static TrackableListView trackablelistView = null; // ID 14

	/**
	 * viewGl kann mehrere ID beinhalten, vieGL ist nur die Basis für alle Views auf Basis von GL_View_Base </br> TestView = 16 </br>
	 * CreditsView = 17 </br> MapView = 18 </br>
	 */
	public static ViewGL viewGL = null;

	/**
	 * gdxView ist die Android.View für gdx
	 */
	private View gdxView = null;

	private GL glListener = null;

	public static LinearLayout strengthLayout;

	// public LinearLayout searchLayout;

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

	private boolean runsWithAkku = true;

	private FrameLayout frame;
	private FrameLayout tabFrame;
	private FrameLayout GlFrame;

	private LinearLayout TopLayout;

	public downSlider InfoDownSlider;

	private String GcCode = null;
	private String GpxPath = null;

	private boolean mustRunSearch = false;

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
			Logger.DEBUG("create ScreenLockTimer innstanz: " + millisInFuture + "/" + countDownInterval);
		}

		@Override
		public void onFinish()
		{
			Logger.DEBUG("ScreenLockTimer => onFinish");

			startScreenLock();
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		Logger.DEBUG(" => onSaveInstanceState");

		savedInstanceState.putBoolean("isTab", GlobalCore.isTab);
		savedInstanceState.putBoolean("useSmallSkin", GlobalCore.useSmallSkin);
		savedInstanceState.putString("WorkPath", Config.WorkPath);

		savedInstanceState.putInt("WindowWidth", UiSizes.ui.Window.width);
		savedInstanceState.putInt("WindowHeight", UiSizes.ui.Window.height);

		if (GlobalCore.SelectedCache() != null) savedInstanceState.putString("selectedCacheID", GlobalCore.SelectedCache().GcCode);
		if (GlobalCore.SelectedWaypoint() != null) savedInstanceState.putString("selectedWayPoint", GlobalCore.SelectedWaypoint().GcCode);

		// TODO onSaveInstanceState => save more

		super.onSaveInstanceState(savedInstanceState);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mainActivity = this;

		if (savedInstanceState != null)
		{
			// restore ACB after Kill
			Logger.DEBUG("restore ACB after Kill");

			GlobalCore.restartAfterKill = true;
			GlobalCore.isTab = savedInstanceState.getBoolean("isTab");
			GlobalCore.useSmallSkin = savedInstanceState.getBoolean("useSmallSkin");
			String workPath = savedInstanceState.getString("WorkPath");

			// Read Config
			Config.Initialize(workPath, workPath + "/cachebox.config");

			// hier muss die Config Db initialisiert werden
			Database.Settings = new AndroidDB(DatabaseType.Settings, this);
			if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
			Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");
			// initialize Database
			Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
			Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

			Config.AcceptChanges();

			Resources res = this.getResources();
			devicesSizes ui = new devicesSizes();

			ui.Window = new Size(savedInstanceState.getInt("WindowWidth"), savedInstanceState.getInt("WindowHeight"));
			ui.Density = res.getDisplayMetrics().density;
			ui.ButtonSize = new Size(res.getDimensionPixelSize(R.dimen.BtnSize),
					(int) ((res.getDimensionPixelSize(R.dimen.BtnSize) - 5.3333f * ui.Density)));
			ui.RefSize = res.getDimensionPixelSize(R.dimen.RefSize);
			ui.TextSize_Normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
			ui.ButtonTextSize = res.getDimensionPixelSize(R.dimen.BtnTextSize);
			ui.IconSize = res.getDimensionPixelSize(R.dimen.IconSize);
			ui.Margin = res.getDimensionPixelSize(R.dimen.Margin);
			ui.ArrowSizeList = res.getDimensionPixelSize(R.dimen.ArrowSize_List);
			ui.ArrowSizeMap = res.getDimensionPixelSize(R.dimen.ArrowSize_Map);
			ui.TB_IconSize = res.getDimensionPixelSize(R.dimen.TB_icon_Size);
			ui.isLandscape = false;

			UiSizes.initial(ui);

			Global.Paints.init(this);
			Global.InitIcons(this);

			new de.cachebox_test.Map.AndroidManager();

			GlobalCore.restartCache = savedInstanceState.getString("selectedCacheID");
			GlobalCore.restartWaypoint = savedInstanceState.getString("selectedWayPoint");

			// TODO onCreate => restore more from onSaveInstanceState

		}
		else
		{
			GlobalCore.restartAfterKill = false;
		}

		ActivityUtils.onActivityCreateSetTheme(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Initial CB running notification Icon
		if (mConnection == null && serviceIntent == null || GlobalCore.restartAfterKill)
		{
			if (serviceIntent == null) serviceIntent = new Intent(this, NotifyService.class);

			if (mConnection == null) mConnection = new ServiceConnection()
			{

				@Override
				public void onServiceConnected(ComponentName name, IBinder service)
				{
					myNotifyService = ((LocalBinder) service).getService();
				}

				@Override
				public void onServiceDisconnected(ComponentName name)
				{
					myNotifyService.unbindService(this);
				}
			};

			NotifyService.finish = false;
			try
			{
				bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
			}
			catch (Exception e)
			{
				Logger.Error("main on create", "Service register error", e);
			}
		}

		if (GlobalCore.isTab)
		{
			// Tab Modus nur Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// initialize receiver for screen switched on/off
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		if (mReceiver == null)
		{
			mReceiver = new ScreenReceiver();
			registerReceiver(mReceiver, filter);
		}

		Logger.Add(this);

		// N = Config.settings.nightMode.getValue();

		setContentView(GlobalCore.isTab ? R.layout.tab_main : R.layout.main);

		findViewsById();
		initialPlatformConector();

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mainActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		glListener = new Tab_GL_Listner(UiSizes.getWindowWidth(), UiSizes.getWindowHeight());

		int Time = Config.settings.ScreenLock.getValue();
		counter = new ScreenLockTimer(Time, Time);
		counter.start();

		// add Event Handler
		SelectedCacheEventList.Add(this);
		CachListChangedEventList.Add(this);
		GpsStateChangeEventList.Add(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Config.settings.DebugMode.setValue(false);
		Config.AcceptChanges();

		initialLocationManager();

		initialViewGL();
		initalMicIcon();

		glListener.onStart();
		if (tabFrame != null) tabFrame.setVisibility(View.INVISIBLE);
		if (frame != null) frame.setVisibility(View.INVISIBLE);

		InfoDownSlider.invalidate();

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
				if (!GlobalCore.restartAfterKill) chkGpsIsOn();
			}

			if (Config.settings.newInstall.getValue())
			{
				String Welcome = "";
				String LangId = getString(R.string.langId);
				try
				{
					Welcome = GlobalCore.Translations.getTextFile("welcome", LangId);

					Welcome += GlobalCore.Translations.getTextFile("changelog", LangId);
				}
				catch (IOException e1)
				{
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

				input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), config);

			}

		}

		if (aktView != null) ((View) aktView).setVisibility(View.INVISIBLE);
		if (aktTabView != null) ((View) aktTabView).setVisibility(View.INVISIBLE);
		if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.INVISIBLE);
		if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.INVISIBLE);

		// initial hidden EditText
		initialHiddenEditText();
	}

	boolean flag = false;

	private void startSearchTimer()
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

								if (SearchDialog.that == null)
								{
									new SearchDialog();
								}

								SearchDialog.that.showNotCloseAutomaticly();
								SearchDialog.that.addSearch(GcCode, searchMode.GcCode);
							}
							else
							{
								flag = true;
								TabMainView.that.showCacheList();
								startSearchTimer();
							}

						}
					});
				}
			};

			t.start();

		}
	}

	CancelWaitDialog wd;

	private void startGPXImport()
	{
		Logger.LogCat("startGPXImport");
		if (GpxPath != null)
		{

			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					Logger.LogCat("startGPXImport:Timer startet");
					Thread t = new Thread()
					{
						public void run()
						{
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									wd = CancelWaitDialog.ShowWait(GlobalCore.Translations.Get("ImportGPX"), new IcancelListner()
									{

										@Override
										public void isCanceld()
										{
											wd.close();
										}
									}, new Runnable()
									{

										@Override
										public void run()
										{
											Date ImportStart = new Date();
											Logger.LogCat("startGPXImport:Timer startet");
											Importer importer = new Importer();
											ImporterProgress ip = new ImporterProgress();
											Database.Data.beginTransaction();

											importer.importGpx(GpxPath, ip);

											Database.Data.setTransactionSuccessful();
											Database.Data.endTransaction();

											// Import ready
											wd.close();

											// finish close activity and notify changes

											CachListChangedEventList.Call();

											Date Importfin = new Date();
											long ImportZeit = Importfin.getTime() - ImportStart.getTime();

											String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C "
													+ String.valueOf(GPXFileImporter.LogCount) + "L in " + String.valueOf(ImportZeit);

											Logger.DEBUG(Msg);

											FilterProperties props = GlobalCore.LastFilter;

											EditFilterSettings.ApplyFilter(props);

											GL.that.Toast(Msg, 3000);
										}
									});

								}
							});
						}
					};

					t.start();
				}
			};
			timer.schedule(task, 500);

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
				GlobalCore.switchToCompassCompleted = false;
			}
		});
	}

	public void newLocationReceived(Location location)
	{

		if (!location.hasBearing())
		{
			location.setBearing(compassHeading);
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
			InfoDownSlider.setNewLocation(GlobalCore.LastPosition);
		}
		catch (Exception e)
		{
			Logger.Error("main.newLocationReceived()", "InfoDownSlider.setNewLocation(location)", e);
			e.printStackTrace();
		}

	}

	@Override
	public void onLocationChanged(Location location)
	{

		newLocationReceived(location);

	}

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
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			if (KeyboardWasClosed)
			{
				KeyboardWasClosed = false;
				return true;
			}
			else
			{
				// if Dialog or Activity shown, close that first
				if (GL.that.closeShownDialog()) return true;

				if (!GL.that.keyBackCliced()) TabMainView.actionClose.Execute();
				return true;
			}

		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void CacheListChangedEvent()
	{

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Back from Activitiy
		if (requestCode != Global.REQUEST_CODE_KEYBOARDACTIVITY && requestCode != Global.REQUEST_CODE_SCREENLOCK)
		{
			glListener.onStart();
		}
		else
		{
			return;
		}

		// Intent Result Take Photo
		if (requestCode == Global.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				GL.that.RunIfInitial(new runOnGL()
				{

					@Override
					public void run()
					{
						if (GlobalCore.SelectedCache() != null) GlobalCore.SelectedCache().ReloadSpoilerRessources();
						String MediaFolder = Config.settings.UserImageFolder.getValue();
						String TrackFolder = Config.settings.TrackFolder.getValue();
						String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
						// Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und
						// die Koordinaten nach der Aufnahme verwendet werden.
						mediaTimeString = Global.GetTrackDateTimeString();
						TrackRecorder.AnnotateMedia(basename + ".jpg", relativPath + "/" + basename + ".jpg", GlobalCore.LastValidPosition,
								mediaTimeString);
					}
				});

				return;
			}
			else
			{
				// Log.d("DroidCachebox", "Picture NOT taken!!!");
				return;
			}
		}

		if (requestCode == Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR)
		{
			if (resultCode == android.app.Activity.RESULT_OK && data != null)
			{
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null)
				{
					String filePath = fileUri.getPath();
					if (filePath != null)
					{
						if (getFileReturnListner != null) getFileReturnListner.getFieleReturn(filePath);
						if (getFolderReturnListner != null) getFolderReturnListner.getFolderReturn(filePath);
					}
				}
			}
			return;

		}

		// Intent Result Record Video
		if (requestCode == Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				GL.that.RunIfInitial(new runOnGL()
				{

					@Override
					public void run()
					{
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
					}
				});

				return;
			}
			else
			{
				// Log.d("DroidCachebox", "Video NOT taken!!!");
				return;
			}
		}

		// Intent Result get API key
		if (requestCode == Global.REQUEST_CODE_GET_API_KEY)
		{
			GL.that.RunIfInitial(new runOnGL()
			{

				@Override
				public void run()
				{
					if (SettingsActivity.that != null)
					{
						SettingsActivity.that.resortList();
					}
				}
			});

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

	}

	@Override
	protected void onPause()
	{
		Logger.LogCat("Main=> onPause");

		stopped = true;

		unbindPluginServices();

		if (input == null)
		{
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), config);
		}

		if (isFinishing())
		{
			if (mConnection != null)
			{
				NotifyService.finish = true;
				unbindService(mConnection);
				mConnection = null;
			}

		}

		super.onPause();

		Logger.DEBUG("Main=> onPause release SuppressPowerSaving");

		if (this.mWakeLock != null) this.mWakeLock.release();
	}

	Dialog pWaitD;
	private boolean stopped = false;

	private void showWaitToRenderStartet()
	{
		if (pWaitD == null)
		{

			pWaitD = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("waitForGL"), "", MessageBoxButtons.NOTHING,
					MessageBoxIcon.None, null);
			stopped = false;

			GL.that.RunIfInitial(new runOnGL()
			{

				@Override
				public void run()
				{
					GL.that.registerRenderStartetListner(new renderStartet()
					{

						@Override
						public void renderIsStartet()
						{
							pWaitD.dismiss();
							pWaitD = null;
						}
					});
				}
			});

		}
	}

	@Override
	protected void onResume()
	{

		if (stopped)
		{
			showWaitToRenderStartet();
			invalidateTextureEventList.Call();
		}

		Logger.DEBUG("Main=> onResume");
		if (input == null)
		{
			Logger.DEBUG("Main=> onResume input== null");
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useGL20 = true;
			graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
					: config.resolutionStrategy);
			input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), config);
		}

		super.onResume();

		if (counter != null)
		{
			if (runsWithAkku)
			{
				counter.start();
			}
			else
			{
				counter.cancel();
			}
		}

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
			Logger.DEBUG("Main=> onResume SuppressPowerSaving");

			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

			int flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK;

			this.mWakeLock = pm.newWakeLock(flags, "My Tag");
			this.mWakeLock.acquire();
		}

		try
		{
			initialOnTouchListner();
		}
		catch (Exception e)
		{
			Logger.Error("onResume", "initialOnTouchListner", e);
		}

		// register KeyboardFocusChangedEvent
		KeyboardFocusChangedEventList.Add(this);

		// Initial PlugIn
		fillPluginList();
		bindPluginServices();

		final Bundle extras = getIntent().getExtras();
		if (!GlobalCore.restartAfterKill && extras != null)
		{
			GcCode = extras.getString("GcCode");
			GpxPath = extras.getString("GpxPath");
			if (GpxPath != null) Logger.LogCat("GPX found: " + GpxPath);
			mustRunSearch = true;

		}

	}

	@Override
	protected void onStop()
	{
		Logger.DEBUG("Main=> onStop");

		if (mSensorManager != null) mSensorManager.unregisterListener(mListener);

		try
		{
			this.unregisterReceiver(this.mBatInfoReceiver);
		}
		catch (Exception e)
		{
			Logger.Error("Main=> onStop", "unregisterReceiver", e);
		}
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

		// unregister KeyboardFocusChangedEvent
		KeyboardFocusChangedEventList.Remove(this);
	}

	@Override
	public void onDestroy()
	{

		if (mReceiver != null) this.unregisterReceiver(mReceiver);
		mReceiver = null;

		Log.d("CACHEBOX", "Main=> onDestroy");
		// frame.removeAllViews();
		if (isRestart)
		{
			Log.d("CACHEBOX", "Main=> onDestroy isFinishing");
			Logger.DEBUG("Main=> onDestroy isRestart");
			super.onDestroy();
			isRestart = false;
		}
		else
		{
			if (isFinishing())
			{
				Log.d("CACHEBOX", "Main=> onDestroy isFinishing");

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

				SpriteCache.destroyCache();

				Database.Settings.Close();
				super.onDestroy();
				System.exit(0);
			}
			else
			{
				Log.d("CACHEBOX", "Main=> onDestroy isFinishing==false");

				if (aktView != null) aktView.OnHide();
				if (aktTabView != null) aktTabView.OnHide();

				Database.Settings.Close();
				Database.Data.Close();
				Database.FieldNotes.Close();

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
		Logger.DEBUG("Start Screenlock (force:" + force + ")");

		if (!force)
		{
			if (!runsWithAkku) return;
			counter.cancel();
			counterStopped = true;
			// ScreenLock nur Starten, wenn der Config Wert größer 10 sec ist.
			// Das verhindert das selber aussperren!
			if ((Config.settings.ScreenLock.getValue() / 1000 < 10)) return;
		}

		// TODO move/create ScreenLock on GDX
	}

	/*
	 * Handler
	 */

	private float compassHeading = 0;

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
				compassHeading = mCompassValues[0];

				GlobalCore.Locator.setCompassHeading(mCompassValues[0]);
				PositionEventList.Call(mCompassValues[0], "CompassValue");
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
		else if (ID == ViewConst.JOKER_VIEW) return jokerView = new JokerView(this, this);
		else if (ID == ViewConst.ABOUT_VIEW) return aboutView = new AboutView(this, inflater);
		else if (ID == ViewConst.SOLVER_VIEW) return solverView = new SolverView(this, inflater);
		else if (ID == ViewConst.NOTES_VIEW) return notesView = new NotesView(this, inflater);
		else if (ID == ViewConst.SPOILER_VIEW) return spoilerView = new SpoilerView(this, inflater);
		else if (ID == ViewConst.DESCRIPTION_VIEW)
		{
			if (descriptionView != null)
			{
				return descriptionView;
			}
			else
			{
				return descriptionView = new DescriptionView(this, inflater);
			}

		}
		return null;
	}

	private void showActivity(ViewID ID)
	{
		if (ID == ViewConst.RELOAD_CACHE)
		{
			if (descriptionView != null) descriptionView.reloadCacheInfo();
		}
		else if (ID == ViewConst.NAVIGATE_TO)
		{
			NavigateTo();
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
		else if (ID == ViewConst.LOCK)
		{
			startScreenLock(true);
		}

	}

	private void showView(ViewOptionsMenu view, ViewID ID)
	{
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
				descriptionView.OnHide();

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

	public static void vibrate()
	{
		if (Config.settings.vibrateFeedback.getValue()) vibrator.vibrate(Config.settings.VibrateTime.getValue());
	}

	/*
	 * Initial Methods
	 */

	private void findViewsById()
	{
		QuickButtonList = (HorizontalListView) this.findViewById(R.id.main_quick_button_list);
		TopLayout = (LinearLayout) this.findViewById(R.id.layoutTop);
		frame = (FrameLayout) this.findViewById(R.id.layoutContent);
		tabFrame = (FrameLayout) this.findViewById(R.id.tabletLayoutContent);
		GlFrame = (FrameLayout) this.findViewById(R.id.layoutGlContent);

		InfoDownSlider = (downSlider) this.findViewById(R.id.downSlider);

		debugInfoPanel = (DebugInfoPanel) this.findViewById(R.id.debugInfo);
		Mic_Icon = (Mic_On_Flash) this.findViewById(R.id.mic_flash);

		cacheNameView = (CacheNameView) this.findViewById(R.id.main_cache_name_view);

		strengthLayout = (LinearLayout) this.findViewById(R.id.main_strength_control);

		// searchLayout = (LinearLayout) this.findViewById(R.id.searchDialog);

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
			 * genauigkeit, darher reicht es wenn er alle 10sec einen wert liefert, wen der alte um 500m abweicht. Beim GPS Provider habe
			 * ich die aktualiesierungs Zeit verkürzt, damit bei deaktiviertem Hardware Kompass aber die Werte trotzdem noch in einem
			 * gesunden Verhältnis zwichen Performance und Stromverbrauch, geliefert werden. Andere apps haben hier 0.
			 */

			long updateTime = Config.settings.gpsUpdateTime.getValue();

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, this);
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

	private void initialViewGL()
	{
		try
		{
			// if (viewGL == null)
			// {

			if (gdxView != null) Logger.DEBUG("gdxView war initialisiert=" + gdxView.toString());
			gdxView = initializeForView(glListener, false);

			Logger.DEBUG("Initial new gdxView=" + gdxView.toString());

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

			initialOnTouchListner();

			if (viewGL == null) viewGL = new ViewGL(this, inflater, gdxView, glListener);

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

			// }
		}
		catch (Exception e)
		{
			Logger.Error("main.initialViewGL()", "", e);
			e.printStackTrace();
		}

	}

	private void initialOnTouchListner() throws Exception
	{

		if (gdxView == null) throw new Exception("gdx view nicht initialisiert");

		gdxView.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, final MotionEvent event)
			{
				return sendMotionEvent(event);
			}

		});
	}

	public boolean sendMotionEvent(final MotionEvent event)
	{
		// Weitergabe der Toucheingabe an den Gl_Listener
		// ToDo: noch nicht fertig!!!!!!!!!!!!!

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

		try
		{
			switch (action & MotionEvent.ACTION_MASK)
			{
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				Thread threadDown = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						glListener.onTouchDownBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex),
								event.getPointerId(pointerIndex), 0);
					}
				});
				threadDown.run();

				break;
			case MotionEvent.ACTION_MOVE:
				Thread threadMove = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						glListener.onTouchDraggedBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex),
								event.getPointerId(pointerIndex));
					}
				});
				threadMove.run();

				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:

				Thread threadUp = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						glListener.onTouchUpBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex),
								event.getPointerId(pointerIndex), 0);
					}
				});
				threadUp.run();

				break;
			}
		}
		catch (Exception e)
		{
			Logger.Error("gdxView.OnTouchListener", "", e);
			return false;
		}
		return true;
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

	private void showJoker()
	{

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
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
												GL_MsgBox.Show(s[1]);
												break;
											}
											if (s[0].equals("1")) // 1 entspricht
											// Warnung, Ursache
											// ist in S[1]
											{ // es können aber noch gültige Einträge
												// folgen
												GL_MsgBox.Show(s[1]);
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
						GL_MsgBox.Show(GlobalCore.Translations.Get("internetError"));
					}
					catch (Exception ex)
					{
						Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", ex);
						// Log.d("DroidCachebox", ex.getMessage());
					}
				}

				if (Global.Jokers.isEmpty())
				{
					GL_MsgBox.Show(GlobalCore.Translations.Get("noJokers"));
				}
				else
				{
					Logger.General("Open JokerView...");

					main.this.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							showView(ViewConst.JOKER_VIEW);
						}
					});
				}
			}
		});
		thread.start();

	}

	private void showTbList()
	{
		// MessageBox.Show("comming soon", "sorry", MessageBoxIcon.Asterisk);
		showView(ViewConst.TB_LIST_VIEW);
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

	private void NavigateTo()
	{
		if (GlobalCore.SelectedCache() != null)
		{
			double lat = GlobalCore.SelectedCache().Latitude();
			double lon = GlobalCore.SelectedCache().Pos.getLongitude();

			if (GlobalCore.SelectedWaypoint() != null)
			{
				lat = GlobalCore.SelectedWaypoint().Pos.getLatitude();
				lon = GlobalCore.SelectedWaypoint().Pos.getLongitude();
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
				// implicitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon));
				// Changed the call for navigation app to maps.google.com...
				// Copilot Live 9.3 listens for this intent call and google maps is working with this too...
				implicitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon));

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
		if (status != 0) GL_MsgBox.Show(CB_Core.Api.GroundspeakAPI.LastAPIError);

		GL_MsgBox.Show("Cache hinzufügen ist noch nicht implementiert!", "Sorry", MessageBoxIcon.Asterisk);
	}

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
					if (!s[6].equals("1") & !s[6].equals("2")) return; // Fix ungültig
					double altCorrection = Double.valueOf(s[11]);
					if (altCorrection == 0) return;
					Logger.General("AltCorrection: " + String.valueOf(altCorrection));
					GlobalCore.Locator.altCorrection = altCorrection;
					Log.d("NMEA.AltCorrection", Double.toString(altCorrection));
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
		Logger.DEBUG("setScreenLockTimerNew");
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

		Log.d("CACHEBOX", Msg);

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
					}
				});
			}
		};

		t.start();

	}

	/**
	 * Überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung ausgegeben.
	 */
	private void chkGpsIsOn()
	{
		try
		{
			if (!GpsOn())
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

	private boolean GpsOn()
	{
		LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean GpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return GpsOn;
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
							// now, we check GPS
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

	// ########### PlugIn Method ##########################

	// PlugIn Methodes
	private static final String LOG_TAG = "CB_PlugIn";
	private static final String ACTION_PICK_PLUGIN = "de.cachebox.action.PICK_PLUGIN";
	private static final String KEY_PKG = "pkg";
	private static final String KEY_SERVICENAME = "servicename";
	private ArrayList<HashMap<String, String>> services;
	private PluginServiceConnection pluginServiceConnection[] = new PluginServiceConnection[4];

	private void fillPluginList()
	{
		services = new ArrayList<HashMap<String, String>>();
		PackageManager packageManager = getPackageManager();
		Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
		// Log.d(LOG_TAG, "fillPluginList: " + list);

		Config.settings.hasFTF_PlugIn.setValue(false);
		Config.settings.hasPQ_PlugIn.setValue(false);
		int i;
		try
		{
			for (i = 0; i < list.size(); ++i)
			{
				ResolveInfo info = list.get(i);
				ServiceInfo sinfo = info.serviceInfo;
				// Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo);
				if (sinfo != null)
				{

					if (sinfo.packageName.contains("de.CB_FTF_PlugIn")) // Don't bind, is an Widget
					{
						Config.settings.hasFTF_PlugIn.setValue(true);
					}
					else if (sinfo.packageName.contains("de.CB_PQ_PlugIn"))// Don't bind, is an Widget
					{
						Config.settings.hasPQ_PlugIn.setValue(true);
					}
					else
					// PlugIn for Bind
					{
						HashMap<String, String> item = new HashMap<String, String>();
						item.put(KEY_PKG, sinfo.packageName);
						item.put(KEY_SERVICENAME, sinfo.name);
						services.add(item);
						if (i <= 4)
						{
							inflateToView(i, packageManager, sinfo.packageName);

						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Config.AcceptChanges();
	}

	@SuppressWarnings("unused")
	private void inflateToView(int rowCtr, PackageManager packageManager, String packageName)
	{
		try
		{
			ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
			Resources res = packageManager.getResourcesForApplication(info);

			XmlResourceParser xres = res.getLayout(0x7f030000);

		}
		catch (NameNotFoundException ex)
		{
			Log.e(LOG_TAG, "NameNotFoundException", ex);
		}
	}

	private void bindPluginServices()
	{

		for (int i = 0; i < services.size(); ++i)
		{
			final PluginServiceConnection con = new PluginServiceConnection();

			pluginServiceConnection[i] = con;
			final Intent intent = new Intent();
			final HashMap<String, String> data = services.get(i);
			intent.setClassName(data.get(KEY_PKG), data.get(KEY_SERVICENAME));
			// Log.d(LOG_TAG, "bindPluginServices: " + intent);
			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
						bindService(intent, con, Context.BIND_AUTO_CREATE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			};
			t.start();

		}

	}

	private void unbindPluginServices()
	{
		if (services != null)
		{
			for (int i = 0; i < services.size(); ++i)
			{

				for (PluginServiceConnection con : pluginServiceConnection)
				{
					if (con != null)
					{
						unbindService(con);
					}
				}
			}
		}
	}

	class PluginServiceConnection implements ServiceConnection
	{
		public void onServiceConnected(ComponentName className, IBinder boundService)
		{
			int idx = getServiceConnectionIndex();
			Log.d(LOG_TAG, "onServiceConnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0)
			{
				Global.iPlugin[idx] = IPlugIn.Stub.asInterface((IBinder) boundService);

				// set Joker PlugIn

				if (Global.iPlugin != null && Global.iPlugin[0] != null)
				{
					Config.settings.hasCallPermission.setValue(true);
				}
				else
				{
					Config.settings.hasCallPermission.setValue(false);
				}

				Config.AcceptChanges();
			}
		}

		public void onServiceDisconnected(ComponentName className)
		{
			int idx = getServiceConnectionIndex();
			Log.d(LOG_TAG, "onServiceDisconnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0) Global.iPlugin[idx] = null;
		}

		private int getServiceConnectionIndex()
		{
			for (int i = 0; i < pluginServiceConnection.length; ++i)
				if (this == pluginServiceConnection[i]) return i;
			return -1;
		}
	};

	// #########################################################

	// ########### Platform Conector ##########################

	private void initialPlatformConector()
	{

		GlobalCore.platform = Plattform.Android;

		platformConector.setisOnlineListner(new IHardwarStateListner()
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

			@Override
			public boolean isGPSon()
			{
				return GpsOn();
			}

			@Override
			public void vibrate()
			{
				main.vibrate();
			}

			@SuppressWarnings("unused")
			@Override
			public CB_Core.Locator.GpsStatus getGpsStatus()
			{
				GpsStatus status = null;
				locationManager.getGpsStatus(status);

				CB_Core.Locator.GpsStatus coreStatus = new CB_Core.Locator.GpsStatus();

				int index = 0;
				if (status == null) return null;
				for (GpsSatellite sat : status.getSatellites())
				{
					CB_Core.Locator.GpsSatellite coreSat = new CB_Core.Locator.GpsSatellite(sat.getPrn());
					coreSat.setSnr(sat.getSnr());
					coreSat.setElevation(sat.getElevation());
					coreSat.setAzimuth(sat.getAzimuth());
					coreStatus.setSatelite(index, coreSat);
					index++;
				}

				return coreStatus;
			}
		});

		platformConector.setShowViewListner(new IShowViewListner()
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

						if (viewID.getType() != ViewID.UI_Type.Activity && width > 1)
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

						if (InfoDownSlider != null)
						{
							InfoDownSlider.ActionUp();
							((View) InfoDownSlider).setVisibility(View.VISIBLE);
						}

						if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.VISIBLE);

						if (viewID == ViewConst.RELOAD_CACHE)
						{
							reloadSelectedCacheInfo();
						}
						else if (viewID == ViewConst.JOKER_VIEW)
						{
							showJoker();
						}
						else
						{
							showView(viewID);
						}

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
						if (GlobalCore.isTab)
						{
							if (viewID.getPos() == UI_Pos.Left)
							{
								if (!(aktView == null) && viewID == aktViewId)
								{
									aktView.OnHide();
								}
							}
							else
							{
								if (!(aktTabView == null) && viewID == aktViewId)
								{
									aktTabView.OnHide();
								}
							}
						}
						else
						{
							if (!(aktView == null) && viewID == aktViewId)
							{
								aktView.OnHide();
							}
						}
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

						// set position of slider
						downSlider.ButtonShowStateChanged();
					}
				});

			}

			@Override
			public void firstShow()
			{
				Timer timer = new Timer();
				TimerTask task = new TimerTask()
				{
					@Override
					public void run()
					{
						downSlider.ButtonShowStateChanged();
					}
				};
				timer.schedule(task, 200);

				if (mustRunSearch)
				{
					Logger.LogCat("mustRunSearch");
					if (GcCode != null) startSearchTimer();
					if (GpxPath != null) startGPXImport();
				}

			}

			@Override
			public void dayNightSwitched()
			{

				Global.InitIcons(mainActivity);
				Global.initTheme(mainActivity);

				if (aktViewId == ViewConst.DESCRIPTION_VIEW || aktTabViewId == ViewConst.DESCRIPTION_VIEW)
				{
					if (descriptionView.getVisibility() == View.VISIBLE)
					{
						if (aktView == descriptionView)
						{
							hide(ViewConst.DESCRIPTION_VIEW);
							descriptionView = null;

						}
					}

				}
			}

		});

		// set AndroidClipboard
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		AndroidClipboard acb = new AndroidClipboard(cm);

		if (cm != null) GlobalCore.setDefaultClipboard(acb);

		platformConector.setGetFileListner(new IgetFileListner()
		{

			@Override
			public void getFile(String initialPath, String extension, String TitleText, String ButtonText,
					IgetFileReturnListner returnListner)
			{
				getFileReturnListner = returnListner;
				getFolderReturnListner = null;

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);

				// Construct URI from file name.
				File file = new File(initialPath);
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, TitleText);
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, ButtonText);

				try
				{
					main.mainActivity.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}

			}
		});

		platformConector.setGetFolderListner(new IgetFolderListner()
		{

			@Override
			public void getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner)
			{
				getFileReturnListner = null;
				getFolderReturnListner = returnListner;

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

				// Construct URI from file name.
				File file = new File(initialPath);
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, TitleText);
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, ButtonText);

				try
				{
					main.mainActivity.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}
			}
		});

		platformConector.setQuitListner(new IQuit()
		{

			@Override
			public void Quit()
			{
				finish();
			}
		});

		platformConector.setGetApiKeyListner(new IGetApiKey()
		{

			@Override
			public void GetApiKey()
			{
				GetApiAuth();
			}
		});

		platformConector.setsetScreenLockTimeListner(new IsetScreenLockTime()
		{

			@Override
			public void setScreenLockTime(int value)
			{
				setScreenLockTimerNew(value);
			}
		});

		platformConector.setsetKeybordFocusListner(new IsetKeybordFocus()
		{

			@Override
			public void setKeybordFocus(boolean value)
			{
				// Iniitial HiddenTextField
				if (value)
				{
					try
					{
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								mTextField.setVisibility(View.VISIBLE);
								mTextField.requestFocus();
								((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextField,
										InputMethodManager.SHOW_FORCED);
								Timer timer = new Timer();
								TimerTask task = new TimerTask()
								{
									@Override
									public void run()
									{
										KeybordShown = true;
									}
								};
								timer.schedule(task, 500);

							}
						});
					}
					catch (Exception ex)
					{
						String s = ex.getMessage();
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
									mTextField.getWindowToken(), 0);
							KeybordShown = false;
						}
					});

				}
			}
		});
	}

	IgetFileReturnListner getFileReturnListner = null;
	IgetFolderReturnListner getFolderReturnListner = null;

	// #########################################################

	// ########### Reload CacheInfo ##########################

	private static ProgressDialog waitPD;

	private void reloadSelectedCacheInfo()
	{
		Thread thread = new Thread()
		{
			public void run()
			{

				String accessToken = Config.GetAccessToken();
				if (!CB_Core.Api.GroundspeakAPI.CacheStatusValid)
				{
					int result = CB_Core.Api.GroundspeakAPI.GetCacheLimits(accessToken);
					if (result != 0)
					{
						onlineReloadReadyHandler.sendMessage(onlineReloadReadyHandler.obtainMessage(1));
						return;
					}
				}
				if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0)
				{
					String s = "Download limit is reached!\n";
					s += "You have downloaded the full cache details of " + CB_Core.Api.GroundspeakAPI.MaxCacheCount
							+ " caches in the last 24 hours.\n";
					if (CB_Core.Api.GroundspeakAPI.MaxCacheCount < 10) s += "If you want to download the full cache details of 6000 caches per day you can upgrade to Premium Member at \nwww.geocaching.com!";

					message = s;

					onlineReloadReadyHandler.sendMessage(onlineReloadReadyHandler.obtainMessage(2));

					return;
				}

				if (!CB_Core.Api.GroundspeakAPI.IsPremiumMember(accessToken))
				{
					String s = "Download Details of this cache?\n";
					s += "Full Downloads left: " + CB_Core.Api.GroundspeakAPI.CachesLeft + "\n";
					s += "Actual Downloads: " + CB_Core.Api.GroundspeakAPI.CurrentCacheCount + "\n";
					s += "Max. Downloads in 24h: " + CB_Core.Api.GroundspeakAPI.MaxCacheCount;
					message = s;
					onlineReloadReadyHandler.sendMessage(onlineReloadReadyHandler.obtainMessage(3));
					return;
				}
				else
				{
					// call the download directly
					onlineReloadReadyHandler.sendMessage(onlineReloadReadyHandler.obtainMessage(4));
					return;
				}
			}
		};
		waitPD = ProgressDialog.show(this, "", "Download Description", true);

		thread.start();
	}

	private String message = "";
	private Handler onlineReloadReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				waitPD.dismiss();
				break;
			}
			case 2:
			{
				waitPD.dismiss();
				GL_MsgBox.Show(message, GlobalCore.Translations.Get("GC_title"), MessageBoxButtons.OKCancel,
						MessageBoxIcon.Powerd_by_GC_Live, null);
				break;
			}
			case 3:
			{
				waitPD.dismiss();
				GL_MsgBox.Show(message, GlobalCore.Translations.Get("GC_title"), MessageBoxButtons.OKCancel,
						MessageBoxIcon.Powerd_by_GC_Live, DownloadCacheDialogResult);
				break;
			}
			case 4:
			{
				waitPD.dismiss();
				DownloadCacheDialogResult.onClick(-1);
				break;
			}
			}
		}
	};

	private OnMsgBoxClickListener DownloadCacheDialogResult = new OnMsgBoxClickListener()
	{
		@Override
		public boolean onClick(int button)
		{
			switch (button)
			{
			case -1:
				CacheDAO dao = new CacheDAO();
				Cache newCache = dao.LoadApiDetails(GlobalCore.SelectedCache());
				if (newCache != null)
				{
					GlobalCore.SelectedCache(newCache);

					// hier ist kein AccessToke mehr notwendig, da diese Info
					// bereits im Cache sein muss!
					if (!CB_Core.Api.GroundspeakAPI.IsPremiumMember(""))
					{
						String s = "Download successful!\n";
						s += "Downloads left for today: " + CB_Core.Api.GroundspeakAPI.CachesLeft + "\n";
						s += "If you upgrade to Premium Member you are allowed to download the full cache details of 6000 caches per day and you can search not only for traditional caches (www.geocaching.com).";

						GL_MsgBox.Show(s, GlobalCore.Translations.Get("GC_title"), MessageBoxButtons.OKCancel,
								MessageBoxIcon.Powerd_by_GC_Live, null);
					}
				}
				break;
			}
			return true;
		}
	};

	// #########################################################
	public void GetApiAuth()
	{
		Intent gcApiLogin = new Intent().setClass(mainActivity, GcApiLogin.class);
		mainActivity.startActivityForResult(gcApiLogin, Global.REQUEST_CODE_GET_API_KEY);
	}

	// ###########################################################

	private class hiddenTextField extends EditText
	{
		public hiddenTextField(Context context)
		{
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawColor(Color.TRANSPARENT);

			// Debug
			// canvas.drawColor(Color.argb(100, 255, 0, 0));
		}
	}

	private String beforeS;
	private int beforeStart;
	private int beforeCount;
	private int beforeAfter;
	private boolean KeyboardWasClosed = false;

	private void initialHiddenEditText()
	{
		// mTextField = new hiddenTextField(this);

		mTextField = new hiddenTextField(inflater.getContext())
		{
			@Override
			public boolean onKeyPreIme(int keyCode, KeyEvent event)
			{
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				{
					GL.that.setKeyboardFocus(null);
					KeyboardWasClosed = true;
					return true;
				}
				return super.onKeyPreIme(keyCode, event);
			}
		};

		mTextField.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		mTextField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!mTextField.hasFocus()) mTextField.requestFocus();
			}
		});

		mTextField.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				// nach GL umleiten
				boolean handeld = false;

				if (event.getAction() == KeyEvent.ACTION_UP && getNumericValueFromKeyCode(keyCode) != null)
				{
					return CB_Core.Events.platformConector.sendKey((Character) getNumericValueFromKeyCode(keyCode));
				}

				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (keyCode == 66)// Enter
					{

						return true;
					}
					else if (keyCode == 67)// Enter
					{

						return true;
					}
					else
					{
						handeld = CB_Core.Events.platformConector.sendKeyDown(keyCode);
					}

				}
				else if (event.getAction() == KeyEvent.ACTION_UP)
				{
					if (keyCode == 66)// Enter
					{
						handeld = CB_Core.Events.platformConector.sendKey('\n');
						return handeld;
					}
					else if (keyCode == 67)// Enter
					{
						// Back
						char BACKSPACE = 8;
						return CB_Core.Events.platformConector.sendKey(BACKSPACE);
					}
					else
					{
						handeld = CB_Core.Events.platformConector.sendKeyUp(keyCode);
					}
				}

				return handeld;
			}

		});

		mTextField.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				try
				{
					boolean handeld = CB_Core.Events.platformConector.sendKeyDown(event.getKeyCode());
					// boolean handeld2 = CB_Core.Events.platformConector.sendKey(chr);
					return handeld;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			}
		});

		mTextField.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// int BreakPoint = 1;
				// if (BreakPoint == 1) BreakPoint++;
				String newText = s.toString().substring(start, start + count);
				String oldText = beforeS.substring(beforeStart, beforeStart + beforeCount);

				// OldText mit newText vergleichen. Alle Zeichen, die in oldText stehen, in newText aber nicht mehr drin sind im Editor
				// löschen
				for (int i = beforeCount; i >= 0; i--)
				{
					if (newText.length() < i)
					{
						// 1 Zeichen aus dem Editor muß mit Sicherheit gelöscht werden!
						char BACKSPACE = 8;
						CB_Core.Events.platformConector.sendKey(BACKSPACE);
						System.out.println("DEL");
					}
					else
					{
						// oldText mit newText vergleichen und zwar immer von Anfang an bis zu i
						String tmpNew = newText.substring(0, i);
						String tmpOld = oldText.substring(0, i);
						if (tmpOld.equals(tmpNew))
						{
							// bis i ist alles gleiche -> nichts mehr muß gelöscht werden
							// Neue Zeichen können eingefügt werden, und zwar ab dem Zeichen i in newText
							for (int j = i; j < newText.length(); j++)
							{
								System.out.println("NEW: " + newText.charAt(j));

								CB_Core.Events.platformConector.sendKey(newText.charAt(j));
							}
							// Fertig
							break;
						}
						else
						{
							// bis i sind noch Unterschiede -> ein Zeichen löschen
							System.out.println("DEL");
							char BACKSPACE = 8;
							CB_Core.Events.platformConector.sendKey(BACKSPACE);
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// int BreakPoint = 1;
				// if (BreakPoint == 1) BreakPoint++;
				beforeS = s.toString();
				beforeStart = start;
				beforeCount = count;
				beforeAfter = after;
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});

		mTextField.setBackgroundDrawable(null);
		mTextField.setClickable(false);

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutTextField);

		layout.addView(mTextField);

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTextField.getLayoutParams();
		params.height = 1;

		// params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		mTextField.setLayoutParams(params);

	}

	protected Object getNumericValueFromKeyCode(int keyCode)
	{
		if (keyCode == KeyEvent.KEYCODE_0) return '0';
		if (keyCode == KeyEvent.KEYCODE_1) return '1';
		if (keyCode == KeyEvent.KEYCODE_2) return '2';
		if (keyCode == KeyEvent.KEYCODE_3) return '3';
		if (keyCode == KeyEvent.KEYCODE_4) return '4';
		if (keyCode == KeyEvent.KEYCODE_5) return '5';
		if (keyCode == KeyEvent.KEYCODE_6) return '6';
		if (keyCode == KeyEvent.KEYCODE_7) return '7';
		if (keyCode == KeyEvent.KEYCODE_8) return '8';
		if (keyCode == KeyEvent.KEYCODE_9) return '9';

		return null;
	}

	@Override
	public void KeyboardFocusChanged(final EditTextFieldBase focus)
	{
		this.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if (focus != null)
				{
					// ;
				}
				else
				{
					mTextField.setVisibility(View.GONE);
				}
			}
		});

	}

}

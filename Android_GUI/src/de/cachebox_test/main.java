/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.FilterProperties;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.GpsStrength;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Locator.CompassType;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.TrackRecorder;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog.searchMode;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.splash;
import CB_UI.Settings.CB_UI_Settings;
import CB_UI_Base.Energy;
import CB_UI_Base.Plattform;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.ICallUrl;
import CB_UI_Base.Events.platformConector.IGetApiKey;
import CB_UI_Base.Events.platformConector.IHardwarStateListner;
import CB_UI_Base.Events.platformConector.IQuit;
import CB_UI_Base.Events.platformConector.IShowViewListner;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import CB_UI_Base.Events.platformConector.IsetScreenLockTime;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.ViewID;
import CB_UI_Base.GL_UI.ViewID.UI_Pos;
import CB_UI_Base.GL_UI.ViewID.UI_Type;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.Math.devicesSizes;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.ILog;
import CB_Utils.Log.Logger;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.iPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.iChanged;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
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
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;

import de.CB_PlugIn.IPlugIn;
import de.cachebox_test.NotifyService.LocalBinder;
import de.cachebox_test.CB_Texturepacker.Android_Packer;
import de.cachebox_test.Components.CacheNameView;
import de.cachebox_test.Custom_Controls.DebugInfoPanel;
import de.cachebox_test.Custom_Controls.Mic_On_Flash;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.QuickButtonList.HorizontalListView;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AndroidClipboard;
import de.cachebox_test.Views.DescriptionView;
import de.cachebox_test.Views.JokerView;
import de.cachebox_test.Views.SolverView;
import de.cachebox_test.Views.SpoilerView;
import de.cachebox_test.Views.ViewGL;
import de.cachebox_test.Views.Forms.GcApiLogin;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;

/**
 * @author Longri
 * @author ging-buh
 * @author arbor95
 */
@SuppressLint("Wakelock")
@SuppressWarnings("deprecation")
public class main extends AndroidApplication implements SelectedCacheEvent, LocationListener, CB_Core.Events.CacheListChangedEventListner,
		GpsStatus.NmeaListener, GpsStatus.Listener, ILog
{

	private static ServiceConnection mConnection;
	private static Intent serviceIntent;
	private static Service myNotifyService;
	private static BroadcastReceiver mReceiver;
	public HorizontalListView QuickButtonList;

	/*
	 * private static member
	 */

	public static ViewID aktViewId = null;
	public static ViewID aktTabViewId = null;

	public static DescriptionView descriptionView = null; // ID 4
	private static SpoilerView spoilerView = null; // ID 5
	private static SolverView solverView = null; // ID 7
	private static JokerView jokerView = null; // ID 12

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
	private static CB_Locator.Location mediaCoordinate = null;

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

	private String ExtSearch_GcCode = null;
	private String ExtSearch_GpxPath = null;

	private boolean mustRunSearch = false;

	private Mic_On_Flash Mic_Icon;
	private static DebugInfoPanel debugInfoPanel;

	// Views
	private ViewOptionsMenu aktView = null;
	private ViewOptionsMenu aktTabView = null;
	private CacheNameView cacheNameView;

	private ArrayList<ViewOptionsMenu> ViewList = new ArrayList<ViewOptionsMenu>();

	// private Threads
	Thread threadReceiveShortLog;

	Thread threadReloadSelectedCacheInfo;

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

		savedInstanceState.putInt("WindowWidth", UI_Size_Base.that.ui.Window.width);
		savedInstanceState.putInt("WindowHeight", UI_Size_Base.that.ui.Window.height);

		if (GlobalCore.getSelectedCache() != null) savedInstanceState.putString("selectedCacheID", GlobalCore.getSelectedCache()
				.getGcCode());
		if (GlobalCore.getSelectedWaypoint() != null) savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWaypoint()
				.getGcCode());

		// TODO onSaveInstanceState => save more

		super.onSaveInstanceState(savedInstanceState);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		GL.resetIsInitial();

		if (GlobalCore.RunFromSplash)
		{
			Log.d("CACHEBOX", "main-OnCreate Run from Splash");
		}
		else
		{
			Log.d("CACHEBOX", "main-OnCreate illegal-Run");
			// run splash
			Intent mainIntent = new Intent().setClass(main.this, de.cachebox_test.splash.class);
			startActivity(mainIntent);
			finish();
			return;
		}

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
			if (!FileIO.createDirectory(Config.WorkPath + "/User")) return;
			Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");
			// initialize Database
			Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
			Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

			Config.AcceptChanges();

			Resources res = this.getResources();
			devicesSizes ui = new devicesSizes();

			ui.Window = new Size(savedInstanceState.getInt("WindowWidth"), savedInstanceState.getInt("WindowHeight"));
			ui.Density = res.getDisplayMetrics().density;
			ui.RefSize = res.getDimensionPixelSize(R.dimen.RefSize);
			ui.TextSize_Normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
			ui.ButtonTextSize = res.getDimensionPixelSize(R.dimen.BtnTextSize);
			ui.IconSize = res.getDimensionPixelSize(R.dimen.IconSize);
			ui.Margin = res.getDimensionPixelSize(R.dimen.Margin);
			ui.ArrowSizeList = res.getDimensionPixelSize(R.dimen.ArrowSize_List);
			ui.ArrowSizeMap = res.getDimensionPixelSize(R.dimen.ArrowSize_Map);
			ui.TB_IconSize = res.getDimensionPixelSize(R.dimen.TB_icon_Size);
			ui.isLandscape = false;

			new UiSizes();

			UI_Size_Base.that.initial(ui);

			Global.Paints.init(this);
			Global.InitIcons(this);

			GlobalCore.restartCache = savedInstanceState.getString("selectedCacheID");
			GlobalCore.restartWaypoint = savedInstanceState.getString("selectedWayPoint");

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

		// N = Config.nightMode.getValue();

		setContentView(GlobalCore.isTab ? R.layout.tab_main : R.layout.main);

		findViewsById();
		initialPlatformConector();

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mainActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// create new splash
		splash sp = new splash(0, 0, UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight(), "Splash");

		// create new mainView
		TabMainView ma = new TabMainView(0, 0, UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight(), "mainView");

		glListener = new GL(UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight(), sp, ma);

		int Time = Config.ScreenLock.getValue();
		counter = new ScreenLockTimer(Time, Time);
		counter.start();

		// add Event Handler
		SelectedCacheEventList.Add(this);
		CachListChangedEventList.Add(this);
		// GpsStateChangeEventList.Add(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Config.DebugMode.setValue(false);
		Config.AcceptChanges();

		// Initial Android TexturePacker
		new Android_Packer();

		initialLocationManager();

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

		int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.that
				.getQuickButtonListHeight() : 0;

		setQuickButtonHeight(sollHeight);

		if (isFirstStart)
		{
			// ask for API key only if Rev-Number changed, like at new
			// installation and API Key is Empty
			if (Config.newInstall.getValue() && Config.GetAccessToken().equals(""))
			{
				askToGetApiKey();
			}
			else
			{
				if (!GlobalCore.restartAfterKill) chkGpsIsOn();
			}

			if (Config.newInstall.getValue())
			{
				// wait for Copy Asset is closed
				CheckTranslationIsLoaded();
				Timer tim = new Timer();
				TimerTask timTask = new TimerTask()
				{

					@Override
					public void run()
					{

						mainActivity.runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								String Welcome = "";
								String LangId = getString(R.string.langId);
								try
								{
									Welcome = Translation.GetTextFile("welcome", LangId);

									Welcome += Translation.GetTextFile("changelog", LangId);
								}
								catch (IOException e1)
								{
									e1.printStackTrace();
								}

								MessageBox.Show(Welcome, Translation.Get("welcome"), MessageBoxIcon.None);
							}
						});

					}
				};

				tim.schedule(timTask, 5000);

			}

			if (input == null)
			{
				AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
				graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
						: config.resolutionStrategy);

				input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), config);

			}

		}

		if (aktView != null) ((View) aktView).setVisibility(View.INVISIBLE);
		if (aktTabView != null) ((View) aktTabView).setVisibility(View.INVISIBLE);
		if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.INVISIBLE);
		if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.INVISIBLE);

		initialViewGL();
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
		if (ExtSearch_GcCode != null)
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
						SearchDialog.that.addSearch(ExtSearch_GcCode, searchMode.GcCode);
						ExtSearch_GcCode = null;
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
	}

	CancelWaitDialog wd;

	private void startGPXImport()
	{
		Logger.LogCat("startGPXImport");
		if (ExtSearch_GpxPath != null)
		{

			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					Logger.LogCat("startGPXImport:Timer startet");
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							wd = CancelWaitDialog.ShowWait(Translation.Get("ImportGPX"), new IcancelListner()
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

									importer.importGpx(ExtSearch_GpxPath, ip);

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

									ExtSearch_GpxPath = null;

									GL.that.Toast(Msg, 3000);
								}
							});

						}
					});

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

	private CB_Locator.Location CB_location = new CB_Locator.Location(0, 0, 0);

	@Override
	public void onLocationChanged(Location location)
	{
		ProviderType provider = ProviderType.NULL;

		if (location.getProvider().toLowerCase(new Locale("en")).contains("gps")) provider = ProviderType.GPS;
		if (location.getProvider().toLowerCase(new Locale("en")).contains("network")) provider = ProviderType.Network;

		CB_location = new CB_Locator.Location(location.getLatitude(), location.getLongitude(), location.getAccuracy());

		CB_location.setHasSpeed(location.hasSpeed());
		CB_location.setSpeed(location.getSpeed());
		CB_location.setHasBearing(location.hasBearing());
		CB_location.setBearing(location.getBearing());
		CB_location.setAltitude(location.getAltitude());
		CB_location.setProvider(provider);

		CB_Locator.Locator.setNewLocation(CB_location);
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
				GL.that.RunIfInitial(new IRunOnGL()
				{

					@Override
					public void run()
					{
						if (GlobalCore.getSelectedCache() != null) GlobalCore.getSelectedCache().ReloadSpoilerRessources();
						String MediaFolder = Config.UserImageFolder.getValue();
						String TrackFolder = Config.TrackFolder.getValue();
						String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
						// Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und
						// die Koordinaten nach der Aufnahme verwendet werden.
						mediaTimeString = Global.GetTrackDateTimeString();
						TrackRecorder.AnnotateMedia(basename + ".jpg", relativPath + "/" + basename + ".jpg",
								CB_Locator.Locator.getLastSavedFineLocation(), mediaTimeString);

						TabMainView.that.reloadSprites(false);

					}
				});

			}

			return;
		}

		if (requestCode == Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR)
		{
			CB_Android_FileExplorer.onActivityResult(requestCode, resultCode, data);
			return;
		}

		// Intent Result Record Video
		if (requestCode == Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				GL.that.RunIfInitial(new IRunOnGL()
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
						String MediaFolder = Config.UserImageFolder.getValue();

						// Video in Media-Ordner verschieben
						File source = new File(recordedVideoFilePath);
						File destination = new File(MediaFolder + "/" + basename + "." + ext);
						// Datei wird umbenannt/verschoben
						if (!source.renameTo(destination))
						{
							// Log.d("DroidCachebox", "Fehler beim Umbenennen der Datei: " + source.getName());
						}

						String TrackFolder = Config.TrackFolder.getValue();
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
			GL.that.RunIfInitial(new IRunOnGL()
			{

				@Override
				public void run()
				{
					SettingsActivity.resortList();
				}
			});

			Config.AcceptChanges();

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

		try
		{
			if (this.mWakeLock != null) this.mWakeLock.release();
		}
		catch (Exception e)
		{
			// dann ebend nicht!
		}
	}

	Dialog pWaitD;
	private boolean stopped = false;

	private AtomicBoolean waitForGL = new AtomicBoolean(false);

	private void showWaitToRenderStartet()
	{
		if (!GL.isInitial()) return;

		if (pWaitD == null)
		{

			pWaitD = PleaseWaitMessageBox.Show(Translation.Get("waitForGL"), "", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
			stopped = false;

			waitForGL.set(true);

			GL.that.RunOnGL(new IRunOnGL()
			{
				@Override
				public void run()
				{
					pWaitD.dismiss();
					pWaitD = null;
					waitForGL.set(false);
				}
			});

			Thread chkThread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					while (waitForGL.get())
					{
						GL.that.renderOnce(true);
						try
						{
							Thread.sleep(200);
						}
						catch (InterruptedException e)
						{

						}
					}
				}
			});
			chkThread.start();
		}
	}

	@Override
	protected void onResume()
	{
		viewGL.RenderContinous();
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

		int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.that
				.getQuickButtonListHeight() : 0;
		((main) main.mainActivity).setQuickButtonHeight(sollHeight);
		downSlider.isInitial = false;
		InfoDownSlider.invalidate();

		// Ausschalten verhindern
		/*
		 * This code together with the one in onDestroy() will make the screen be always on until this Activity gets destroyed.
		 */
		if (Config.SuppressPowerSaving.getValue())
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

		// Initial PlugIn
		fillPluginList();
		bindPluginServices();

		final Bundle extras = getIntent().getExtras();
		if (!GlobalCore.restartAfterKill || extras != null)
		{
			ExtSearch_GcCode = extras.getString("GcCode");
			ExtSearch_GpxPath = extras.getString("GpxPath");
			if (ExtSearch_GpxPath != null) Logger.LogCat("GPX found: " + ExtSearch_GpxPath);

			if (ExtSearch_GcCode != null || ExtSearch_GpxPath != null)
			{
				mustRunSearch = true;

				// ACB running call search
				if (TabMainView.that.isInitial())
				{
					platformConector.FirstShow();
				}
			}

			// delete handled extras
			getIntent().removeExtra("GcCode");
			getIntent().removeExtra("GpxPath");
		}
		GL.that.RestartRender();
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
		if (Config.SuppressPowerSaving.getValue())
		{
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
			this.mWakeLock.acquire();
		}
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
				if (GlobalCore.RunFromSplash)
				{
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
					SelectedCacheEventList.list.clear();
					SelectedCacheEventList.list.clear();
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
					jokerView = null;
					descriptionView = null;
					mainActivity = null;
					debugInfoPanel.OnFree();
					debugInfoPanel = null;
					InfoDownSlider = null;

					Config.AcceptChanges();

					Database.Data.Close();
					Database.FieldNotes.Close();

					SpriteCacheBase.destroyCache();

					Database.Settings.Close();

				}
				super.onDestroy();
				if (GlobalCore.RunFromSplash) System.exit(0);
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
				Energy.setDisplayOff();
				CB_Locator.Locator.setDisplayOff();
				wasScreenOn = false;
			}
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				Energy.setDisplayOn();
				CB_Locator.Locator.setDisplayOn();
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
			if ((Config.ScreenLock.getValue() / 1000 < 10)) return;
		}

		// TODO move/create ScreenLock on GDX
	}

	/*
	 * Handler
	 */

	private float compassHeading = -1;

	private final SensorEventListener mListener = new SensorEventListener()
	{
		public void onSensorChanged(SensorEvent event)
		{
			try
			{
				mCompassValues = event.values;
				compassHeading = mCompassValues[0];

				CB_Locator.Locator.setHeading(compassHeading, CompassType.Magnetic);
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

		if (ID == ViewConst.JOKER_VIEW) return jokerView = new JokerView(this, this);
		else if (ID == ViewConst.SOLVER_VIEW) return solverView = new SolverView(this, inflater);
		else if (ID == ViewConst.SPOILER_VIEW)
		{
			if (spoilerView == null) spoilerView = new SpoilerView(this, inflater);

			return spoilerView;
		}

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

			if (aktView.equals(jokerView))
			{
				// Instanz löschenn
				aktView = null;
				jokerView.OnFree();
				jokerView = null;
			}
			else if (aktView.equals(solverView))
			{
				// Instanz löschenn
				aktView = null;
				solverView.OnFree();
				solverView = null;
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

		if (aktView != null) ((View) aktView).setVisibility(View.VISIBLE);
		if (aktTabView != null) ((View) aktTabView).setVisibility(View.VISIBLE);
		if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.VISIBLE);
		if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.VISIBLE);

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
		if (Config.vibrateFeedback.getValue()) vibrator.vibrate(Config.VibrateTime.getValue());
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

			/*
			 * Longri: Ich habe die Zeiten und Distanzen der Location Updates angepasst. Der Network Provider hat eine schlechte
			 * genauigkeit, darher reicht es wenn er alle 10sec einen wert liefert, wen der alte um 500m abweicht. Beim GPS Provider habe
			 * ich die aktualiesierungs Zeit verkürzt, damit bei deaktiviertem Hardware Kompass aber die Werte trotzdem noch in einem
			 * gesunden Verhältnis zwichen Performance und Stromverbrauch, geliefert werden. Andere apps haben hier 0.
			 */

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 500, this);

			locationManager.addNmeaListener(this);
			locationManager.addGpsStatusListener(this);
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
			// boolean GL20 = checkGL20Support(this);
			//
			// if (gdxView != null) Logger.DEBUG("gdxView war initialisiert=" + gdxView.toString());
			// gdxView = initializeForView(glListener, GL20);

			AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
			cfg.numSamples = 16;

			gdxView = initializeForView(glListener, cfg);

			Logger.DEBUG("Initial new gdxView=" + gdxView.toString());

			int GlSurfaceType = -1;
			if (gdxView instanceof GLSurfaceView20) GlSurfaceType = ViewGL.GLSURFACE_VIEW20;
			else if (gdxView instanceof GLSurfaceView) GlSurfaceType = ViewGL.GLSURFACE_GLSURFACE;

			ViewGL.setSurfaceType(GlSurfaceType);

			Logger.DEBUG("InitializeForView...");

			switch (GlSurfaceType)
			{
			case ViewGL.GLSURFACE_VIEW20:
				((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
				break;

			case ViewGL.GLSURFACE_GLSURFACE:
				((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
				break;
			}

			initialOnTouchListner();

			if (viewGL == null) viewGL = new ViewGL(this, inflater, gdxView, glListener);

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

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

		try
		{
			switch (action & MotionEvent.ACTION_MASK)
			{
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				glListener.onTouchDownBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex),
						event.getPointerId(pointerIndex), 0);
				break;
			case MotionEvent.ACTION_MOVE:
				glListener.onTouchDraggedBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex),
						event.getPointerId(pointerIndex));
				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				glListener.onTouchUpBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex),
						0);
				break;
			}
		}
		catch (Exception e)
		{
			Logger.Error("gdxView.OnTouchListener", "", e);
			return true;
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
		String directory = Config.UserImageFolder.getValue();
		if (!FileIO.createDirectory(directory))
		{
			// Log.d("DroidCachebox", "Media-Folder does not exist...");
			return;
		}

		basename = Global.GetDateTimeString();

		if (GlobalCore.getSelectedCache() != null)
		{
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-"
					+ GlobalCore.getSelectedCache().getName());
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
		String directory = Config.UserImageFolder.getValue();
		if (!FileIO.createDirectory(directory))
		{
			// Log.d("DroidCachebox", "Media-Folder does not exist...");
			return;
		}

		basename = Global.GetDateTimeString();

		if (GlobalCore.getSelectedCache() != null)
		{
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-"
					+ GlobalCore.getSelectedCache().getName());
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
		mediaCoordinate = Locator.getLocation(ProviderType.GPS);

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
			String directory = Config.UserImageFolder.getValue();
			if (!FileIO.createDirectory(directory))
			{
				// Log.d("DroidCachebox", "Media-Folder does not exist...");
				return;
			}

			basename = Global.GetDateTimeString();

			if (GlobalCore.getSelectedCache() != null)
			{
				String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-"
						+ GlobalCore.getSelectedCache().getName());
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

			String MediaFolder = Config.UserImageFolder.getValue();
			String TrackFolder = Config.TrackFolder.getValue();
			String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
			// Da eine Voice keine Momentaufnahme ist, muss die Zeit und die
			// Koordinaten beim Start der Aufnahme verwendet werden.
			TrackRecorder.AnnotateMedia(basename + ".wav", relativPath + "/" + basename + ".wav", Locator.getLocation(ProviderType.GPS),
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
						URL url = new URL("http://www.gcjoker.de/cachebox.php?md5=" + Config.GcJoker.getValue() + "&wpt="
								+ GlobalCore.getSelectedCache().getGcCode());
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
						GL_MsgBox.Show(Translation.Get("internetError"));
					}
					catch (Exception ex)
					{
						Logger.Error("main.initialBtnInfoContextMenu()", "HTTP response Jokers", ex);
						// Log.d("DroidCachebox", ex.getMessage());
					}
				}

				if (Global.Jokers.isEmpty())
				{
					GL_MsgBox.Show(Translation.Get("noJokers"));
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

	private void NavigateTo()
	{
		if (GlobalCore.getSelectedCache() != null)
		{
			double lat = GlobalCore.getSelectedCache().Latitude();
			double lon = GlobalCore.getSelectedCache().Pos.getLongitude();

			if (GlobalCore.getSelectedWaypoint() != null)
			{
				lat = GlobalCore.getSelectedWaypoint().Pos.getLatitude();
				lon = GlobalCore.getSelectedWaypoint().Pos.getLongitude();
			}

			String selectedNavi = Config.Navis.getValue();
			if (selectedNavi.equals("Ask"))
			{
				// todo : Spinner for Selection
				for (String navi : CB_UI_Settings.navis)
				{
					if (!navi.equalsIgnoreCase("Ask"))
					{
						selectedNavi = navi;
					}
				}
				// todo : remove if Spinner done
				selectedNavi = Config.Navis.getValue(); // =Ask=do nothing
			}

			if (selectedNavi.equals("Navigon"))
			{
				startNavigon(lat, lon);
			}
			else if (selectedNavi.equals("OsmAnd"))
			{
				startNaviActivity("geo:" + lat + "," + lon);
			}
			else if (selectedNavi.equals("OsmAnd2"))
			{
				startNaviActivity("http://download.osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=14");
			}
			else if (selectedNavi.equals("Copilot") || selectedNavi.equals("Google"))
			{
				startNaviActivity("http://maps.google.com/maps?daddr=" + lat + "," + lon);
			}
			else if (selectedNavi.equals("Waze"))
			{
				startNaviActivity("waze://?ll=" + lat + "," + lon);
			}
		}
	}

	private void startNavigon(double lat, double lon)
	{

		Intent intent = null;
		try
		{
			intent = getPackageManager().getLaunchIntentForPackage("com.navigon.navigator");
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

		try
		{
			if (intent != null)
			{
				try
				{
					intent.putExtra("latitude", (float) lat);
					intent.putExtra("longitude", (float) lon);
					startActivity(intent);
				}
				catch (Exception e)
				{
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("main.NavigateTo()", "Start Navigon Fehler", e);
		}
	}

	private void startNaviActivity(String uri)
	{
		Intent intent = null;
		try
		{
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (intent != null)
		{
			startActivity(intent);
		}

	}

	/*
	 * Setter
	 */

	public void setDebugVisible()
	{
		if (Config.DebugShowPanel.getValue())
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

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				debugInfoPanel.setMsg(debugMsg);
			}
		});

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
					Locator.setAltCorrection(altCorrection);
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
		if (threadReceiveShortLog == null) threadReceiveShortLog = new Thread()
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

		threadReceiveShortLog.run();

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

	/**
	 * Überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung ausgegeben.
	 */
	private void chkGpsIsOn()
	{
		try
		{
			if (Config.Ask_Switch_GPS_ON.getValue() && !GpsOn())
			{
				CheckTranslationIsLoaded();
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						MessageBox.Show(Translation.Get("GPSon?"), Translation.Get("GPSoff"), MessageBoxButtons.YesNo,
								MessageBoxIcon.Question, new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int button)
									{
										// Behandle das ergebniss
										switch (button)
										{
										case -1:
											// yes open gps settings
											startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
		}
		catch (Exception e)
		{
			Logger.Error("main.chkGpsIsOn()", "", e);
			e.printStackTrace();
		}
	}

	private void CheckTranslationIsLoaded()
	{
		if (!Translation.isInitial())
		{
			new Translation(Config.WorkPath, FileType.Internal);
			try
			{
				Translation.LoadTranslation(Config.Sel_LanguagePath.getValue());
			}
			catch (Exception e)
			{
				try
				{
					Translation.LoadTranslation(Config.Sel_LanguagePath.getDefaultValue());
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	private boolean GpsOn()
	{
		LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean GpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return GpsOn;
	}

	private void askToGetApiKey()
	{
		MessageBox.Show(Translation.Get("wantApi"), Translation.Get("welcome"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live,
				new DialogInterface.OnClickListener()
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

		Config.hasFTF_PlugIn.setValue(false);
		Config.hasPQ_PlugIn.setValue(false);
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
						Config.hasFTF_PlugIn.setValue(true);
					}
					else if (sinfo.packageName.contains("de.CB_PQ_PlugIn"))// Don't bind, is an Widget
					{
						Config.hasPQ_PlugIn.setValue(true);
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
			t.run();

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
					Config.hasCallPermission.setValue(true);
				}
				else
				{
					Config.hasCallPermission.setValue(false);
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

		initialLocatorBase();

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

		});

		platformConector.setShowViewListner(new IShowViewListner()
		{

			@Override
			public void show(final ViewID viewID, final int left, final int top, final int right, final int bottom)
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
								RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) frame.getLayoutParams();
								paramsLeft.setMargins(left, top, right, bottom);
								frame.setLayoutParams(paramsLeft);
								frame.requestLayout();
							}
							else
							{
								if (tabFrame != null)
								{
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabFrame.getLayoutParams();

									int versatz = 0;
									if (TabMainView.LeftTab != null)
									{
										versatz = (int) (TabMainView.LeftTab.getWidth() - frame.getWidth());
									}

									params.setMargins(versatz + left, top, right, bottom);
									tabFrame.setLayoutParams(params);
									tabFrame.requestLayout();
								}
							}
						}

						if (InfoDownSlider != null)
						{
							InfoDownSlider.ActionUp();
							((View) InfoDownSlider).setVisibility(View.VISIBLE);
						}

						if (aktView != null) ((View) aktView).setVisibility(View.VISIBLE);
						if (aktTabView != null) ((View) aktTabView).setVisibility(View.VISIBLE);
						if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.VISIBLE);

						if (viewID == ViewConst.JOKER_VIEW)
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
								if (!(aktTabView == null) && viewID == aktTabViewId)
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

						Logger.DEBUG("Hide Android view");
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
						// chk for timer conflict (releay set invisible)
						// only if showing Dialog or Activity
						if (!GL.that.isShownDialogActivity()) return;

						if (aktView != null) ((View) aktView).setVisibility(View.INVISIBLE);
						if (aktTabView != null) ((View) aktTabView).setVisibility(View.INVISIBLE);
						if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.INVISIBLE);
						if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.INVISIBLE);
						Logger.DEBUG("Show AndroidView");
					}
				});
			}

			@Override
			public void hideForDialog()
			{

				Timer timer = new Timer();
				TimerTask task = new TimerTask()
				{
					@Override
					public void run()
					{
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{

								// chk for timer conflict (releay set invisible)
								// only if not showing Dialog or Activity
								if (!GL.that.isShownDialogActivity())
								{
									if (aktView != null) ((View) aktView).setVisibility(View.VISIBLE);
									if (aktTabView != null) ((View) aktTabView).setVisibility(View.VISIBLE);
									if (InfoDownSlider != null) ((View) InfoDownSlider).setVisibility(View.VISIBLE);
									if (cacheNameView != null) ((View) cacheNameView).setVisibility(View.VISIBLE);
								}
								// set position of slider
								downSlider.ButtonShowStateChanged();
							}
						});

					}
				};
				timer.schedule(task, 50);

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
					if (ExtSearch_GcCode != null) startSearchTimer();
					if (ExtSearch_GpxPath != null) startGPXImport();
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

		CB_Android_FileExplorer fileExplorer = new CB_Android_FileExplorer(this);
		platformConector.setGetFileListner(fileExplorer);
		platformConector.setGetFolderListner(fileExplorer);

		platformConector.setQuitListner(new IQuit()
		{

			@Override
			public void Quit()
			{
				if (GlobalCore.getSelectedCache() != null)
				{
					// speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
					Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
					Config.AcceptChanges();
					Logger.DEBUG("LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
				}
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

		platformConector.setCallUrlListner(new ICallUrl()
		{

			@Override
			public void call(String url)
			{
				try
				{
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
					main.mainActivity.startActivity(browserIntent);
				}
				catch (Exception exc)
				{
					Toast.makeText(main.mainActivity,
							Translation.Get("Cann_not_open_cache_browser") + " (" + GlobalCore.getSelectedCache().getUrl().trim() + ")",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		PlatformSettings.setPlatformSettings(new iPlatformSettings()
		{

			@Override
			public void Write(SettingBase<?> setting)
			{
				if (androidSetting == null) androidSetting = main.this.getSharedPreferences(Global.PREFS_NAME, 0);
				if (androidSettingEditor == null) androidSettingEditor = androidSetting.edit();

				if (setting instanceof SettingBool)
				{
					androidSettingEditor.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
				}

				else if (setting instanceof SettingString)
				{
					androidSettingEditor.putString(setting.getName(), ((SettingString) setting).getValue());
				}
				else if (setting instanceof SettingInt)
				{
					androidSettingEditor.putInt(setting.getName(), ((SettingInt) setting).getValue());
				}

				// Commit the edits!
				androidSettingEditor.commit();
			}

			@Override
			public SettingBase<?> Read(SettingBase<?> setting)
			{
				if (androidSetting == null) androidSetting = main.this.getSharedPreferences(Global.PREFS_NAME, 0);

				if (setting instanceof SettingString)
				{
					String value = androidSetting.getString(setting.getName(), ((SettingString) setting).getDefaultValue());
					((SettingString) setting).setValue(value);
				}
				else if (setting instanceof SettingBool)
				{
					boolean value = androidSetting.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
					((SettingBool) setting).setValue(value);
				}
				else if (setting instanceof SettingInt)
				{
					int value = androidSetting.getInt(setting.getName(), ((SettingInt) setting).getDefaultValue());
					((SettingInt) setting).setValue(value);
				}
				setting.clearDirty();
				return setting;
			}
		});

	}

	IgetFileReturnListner getFileReturnListner = null;
	IgetFolderReturnListner getFolderReturnListner = null;

	// #########################################################

	// ########### Reload CacheInfo ##########################

	private SharedPreferences androidSetting;
	private SharedPreferences.Editor androidSettingEditor;

	// #########################################################
	public void GetApiAuth()
	{
		Intent gcApiLogin = new Intent().setClass(mainActivity, GcApiLogin.class);
		mainActivity.startActivityForResult(gcApiLogin, Global.REQUEST_CODE_GET_API_KEY);
	}

	// ###########################################################

	/**
	 * Initial all Locator functions
	 */
	private void initialLocatorBase()
	{
		// ##########################################################
		// initial Locator with saved Location
		// ##########################################################
		double latitude = -1000;
		double longitude = -1000;

		if (Config.settings != null)
		{
			try
			{
				latitude = Config.MapInitLatitude.getValue();
				longitude = Config.MapInitLongitude.getValue();
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			// reload config
			// TODO
		}

		ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;

		CB_Locator.Location initialLocation;

		if (provider == ProviderType.Saved)
		{
			initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
		}
		else
		{
			initialLocation = CB_Locator.Location.NULL_LOCATION;
		}

		new CB_Locator.Locator(initialLocation);

		// ##########################################################
		// initial settings changed handling
		// ##########################################################

		// Use Imperial units?
		CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
		Config.ImperialUnits.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});

		// GPS update time?
		CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
		Config.gpsUpdateTime.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
			}
		});

		// Use magnetic Compass?
		CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
		Config.HardwareCompass.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
			}
		});

		// Magnetic compass level
		CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
		Config.HardwareCompassLevel.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
			}
		});
	}

	private final CB_List<CB_Locator.GpsStrength> coreSatList = new CB_List<CB_Locator.GpsStrength>(14);

	@Override
	public void onGpsStatusChanged(int event)
	{
		if (locationManager == null) return;

		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
		{

			GpsStatus status = locationManager.getGpsStatus(null);
			Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

			int satellites = 0;
			int fixed = 0;
			coreSatList.clear();

			while (statusIterator.hasNext())
			{
				GpsSatellite sat = statusIterator.next();
				satellites++;

				// satellite signal strength

				if (sat.usedInFix())
				{
					fixed++;
					// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr() + " FIX");
					// SatList.add(new GpsStrength(true, sat.getSnr()));
					coreSatList.add(new GpsStrength(true, sat.getSnr()));
				}
				else
				{
					// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr());
					// SatList.add(new GpsStrength(false, sat.getSnr()));
					coreSatList.add(new GpsStrength(false, sat.getSnr()));
				}

			}

			// SatList.sort();
			coreSatList.sort();

			CB_Locator.GPS.setSatFixes(fixed);
			CB_Locator.GPS.setSatVisible(satellites);
			CB_Locator.GPS.setSatList(coreSatList);
			GpsStateChangeEventList.Call();
			if (fixed < 3 && (Locator.isFixed()))
			{

				if (!losseChek)
				{
					Timer timer = new Timer();
					TimerTask task = new TimerTask()
					{
						@Override
						public void run()
						{
							if (CB_Locator.GPS.getFixedSats() < 3) Locator.FallBack2Network();
							losseChek = false;
						}
					};
					timer.schedule(task, 1000);
				}

			}
		}

	}

	private boolean losseChek = false;
}

/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.Database.DatabaseType;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
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
import CB_UI.CB_UI_Settings;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog.searchMode;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.splash;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.ICallUrl;
import CB_UI_Base.Events.PlatformConnector.IGetApiKey;
import CB_UI_Base.Events.PlatformConnector.IHardwarStateListener;
import CB_UI_Base.Events.PlatformConnector.IQuit;
import CB_UI_Base.Events.PlatformConnector.IShowViewListener;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_UI_Base.Events.PlatformConnector.IsetScreenLockTime;
import CB_UI_Base.Events.PlatformConnector.iStartPictureApp;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.ViewID;
import CB_UI_Base.GL_UI.ViewID.UI_Pos;
import CB_UI_Base.GL_UI.ViewID.UI_Type;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Plattform;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.Log;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.IPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
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
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.CB_PlugIn.IPlugIn;
import de.cachebox_test.CB_Texturepacker.Android_Packer;
import de.cachebox_test.Components.CacheNameView;
import de.cachebox_test.Custom_Controls.Mic_On_Flash;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.QuickButtonList.HorizontalListView;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AndroidClipboard;
import de.cachebox_test.Views.DescriptionView;
import de.cachebox_test.Views.JokerView;
import de.cachebox_test.Views.SolverView;
import de.cachebox_test.Views.ViewGL;
import de.cachebox_test.Views.Forms.GcApiLogin;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;
import de.cb.sqlite.AndroidDB;

/**
 * @author Longri
 * @author ging-buh
 * @author arbor95
 */
@SuppressLint("Wakelock")
@SuppressWarnings("deprecation")
public class main extends AndroidApplication implements SelectedCacheEvent, LocationListener, CB_Core.CacheListChangedEventListener, GpsStatus.NmeaListener, GpsStatus.Listener, CB_UI_Settings {

	private static AndroidApplicationConfiguration gdxConfig = new AndroidApplicationConfiguration();

	static {
		gdxConfig.numSamples = 2;
		gdxConfig.useAccelerometer = true;
		gdxConfig.useCompass = true;
	}

	final static org.slf4j.Logger log = LoggerFactory.getLogger(main.class);
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
	// private static SpoilerView spoilerView = null; // ID 5
	private static SolverView solverView = null; // ID 7
	private static JokerView jokerView = null; // ID 12

	/**
	 * viewGl kann mehrere ID beinhalten, viewGL ist nur die Basis für alle
	 * Views auf Basis von GL_View_Base </br>
	 * TestView = 16 </br>
	 * CreditsView = 17 </br>
	 * MapView = 18 </br>
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

	// Views
	private ViewOptionsMenu aktView = null;
	private ViewOptionsMenu aktTabView = null;
	private CacheNameView cacheNameView;

	private final ArrayList<ViewOptionsMenu> ViewList = new ArrayList<ViewOptionsMenu>();

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

	public Boolean getVoiceRecIsStart() {
		return mVoiceRecIsStart;
	}

	// Screenlock Counter
	private ScreenLockTimer counter = null;
	private boolean counterStopped = false;

	/**
	 * For Use the Vibrator, Call vibrate(); So the User can switch of this in
	 * Settings
	 */
	private static Vibrator vibrator;

	/*
	 * Classes
	 */
	private class ScreenLockTimer extends CountDownTimer {
		public ScreenLockTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			Log.debug(log, "create ScreenLockTimer innstanz: " + millisInFuture + "/" + countDownInterval);
		}

		@Override
		public void onFinish() {
			Log.debug(log, "ScreenLockTimer => onFinish");

			startScreenLock();
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.debug(log, " => onSaveInstanceState");

		savedInstanceState.putBoolean("isTab", GlobalCore.isTab);
		savedInstanceState.putBoolean("useSmallSkin", GlobalCore.useSmallSkin);
		savedInstanceState.putString("WorkPath", Config.mWorkPath);

		savedInstanceState.putInt("WindowWidth", UI_Size_Base.that.getWindowWidth());
		savedInstanceState.putInt("WindowHeight", UI_Size_Base.that.getWindowHeight());

		if (GlobalCore.isSetSelectedCache())
			savedInstanceState.putString("selectedCacheID", GlobalCore.getSelectedCache().getGcCode());
		if (GlobalCore.getSelectedWaypoint() != null)
			savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWaypoint().getGcCode());

		// TODO onSaveInstanceState => save more

		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GL.resetIsInitial();

		if (GlobalCore.RunFromSplash) {
			Log.info(log, "CACHEBOX: " + "main-OnCreate Run from Splash");
		} else {
			Log.info(log, "CACHEBOX: " + "main-OnCreate illegal-Run");
			// run splash
			Intent mainIntent = new Intent().setClass(main.this, de.cachebox_test.splash.class);
			startActivity(mainIntent);
			finish();
			return;
		}

		mainActivity = this;

		if (savedInstanceState != null) {
			// restore ACB after Kill
			Log.debug(log, "restore ACB after Kill");

			GlobalCore.restartAfterKill = true;
			GlobalCore.isTab = savedInstanceState.getBoolean("isTab");
			GlobalCore.useSmallSkin = savedInstanceState.getBoolean("useSmallSkin");
			String workPath = savedInstanceState.getString("WorkPath");

			// Read Config
			Config.Initialize(workPath, workPath + "/cachebox.config");

			// hier muss die Config Db initialisiert werden
			Database.Settings = new AndroidDB(DatabaseType.Settings, this);
			if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
				return;
			Database.Settings.StartUp(Config.mWorkPath + "/User/Config.db3");
			// initialize Database
			Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
			Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

			Config.AcceptChanges();

			Resources res = this.getResources();
			DevicesSizes ui = new DevicesSizes();

			ui.Window = new Size(savedInstanceState.getInt("WindowWidth"), savedInstanceState.getInt("WindowHeight"));
			ui.Density = res.getDisplayMetrics().density;

			ui.isLandscape = false;

			new UiSizes();

			UI_Size_Base.that.initial(ui);

			Global.Paints.init(this);
			Global.InitIcons(this);

			GlobalCore.restartCache = savedInstanceState.getString("selectedCacheID");
			GlobalCore.restartWaypoint = savedInstanceState.getString("selectedWayPoint");

		} else {
			GlobalCore.restartAfterKill = false;
		}

		ActivityUtils.onActivityCreateSetTheme(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (GlobalCore.isTab) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// initialize receiver for screen switched on/off
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		if (mReceiver == null) {
			mReceiver = new ScreenReceiver();
			registerReceiver(mReceiver, filter);
		}

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
		CacheListChangedEventList.Add(this);
		// GpsStateChangeEventList.Add(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Config.AcceptChanges();

		AndroidSettings.RunOverLockScreen.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				setLockScreenProperty();
			}
		});

		// Initial Android TexturePacker
		new Android_Packer();

		initialLocationManager();

		initalMicIcon();

		glListener.onStart();
		if (tabFrame != null)
			tabFrame.setVisibility(View.INVISIBLE);
		if (frame != null)
			frame.setVisibility(View.INVISIBLE);

		InfoDownSlider.invalidate();

		CacheListChangedEvent();

		try {
			this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		} catch (Exception e) {
			// sometimes:
			// ERROR/ActivityThread(15416): Activity de.cachebox_test.main has
			// leaked IntentReceiver
			// de.cachebox_test.main$7@4745a0f0 that was originally registered
			// here.
			// Are you missing a call to unregisterReceiver()?
		}

		downSlider.isInitial = false;

		int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.that.getQuickButtonListHeight() : 0;

		setQuickButtonHeight(sollHeight);

		if (isFirstStart) {
			// ask for API key only if Rev-Number changed, like at new
			// installation and API Key is Empty
			if (Config.newInstall.getValue() && Config.GetAccessToken().equals("")) {
				askToGetApiKey();
			} else {
				if (!GlobalCore.restartAfterKill)
					chkGpsIsOn();
			}

			if (Config.newInstall.getValue()) {
				// wait for Copy Asset is closed
				CheckTranslationIsLoaded();
				Timer tim = new Timer();
				TimerTask timTask = new TimerTask() {

					@Override
					public void run() {

						mainActivity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								String Welcome = "";
								String LangId = getString(R.string.langId);
								try {
									Welcome = Translation.GetTextFile("welcome", LangId);

									Welcome += Translation.GetTextFile("changelog", LangId);
								} catch (IOException e1) {
									e1.printStackTrace();
								}

								MessageBox.Show(Welcome, Translation.Get("welcome"), MessageBoxIcon.None);
							}
						});

					}
				};

				tim.schedule(timTask, 5000);

			}

			if (input == null) {
				graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);

				input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), gdxConfig);

			}

		}

		if (aktView != null)
			((View) aktView).setVisibility(View.INVISIBLE);
		if (aktTabView != null)
			((View) aktTabView).setVisibility(View.INVISIBLE);
		if (InfoDownSlider != null)
			((View) InfoDownSlider).setVisibility(View.INVISIBLE);
		if (cacheNameView != null)
			((View) cacheNameView).setVisibility(View.INVISIBLE);

		initialViewGL();
	}

	/*
	@Override
	protected void onStart() {
		super.onStart();
	}
	*/

	private void setLockScreenProperty() {
		// add flags for run over lock screen

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Window window = main.this.getWindow();
				if (window != null) {
					// Log.info(log, "Setting flag from RunOverLockScreen: " +
					// AndroidSettings.RunOverLockScreen.getValue());
					if (AndroidSettings.RunOverLockScreen.getValue()) {
						window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
					} else {
						window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
					}
				}
			}
		});
	}

	boolean flag = false;

	private void startSearchTimer() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				startSearch();
			}
		};
		timer.schedule(task, 500);
	}

	private void startSearch() {
		if (ExtSearch_GcCode != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (flag) {
						flag = false;
						if (SearchDialog.that == null) {
							new SearchDialog();
						}
						SearchDialog.that.showNotCloseAutomaticly();
						SearchDialog.that.addSearch(ExtSearch_GcCode, searchMode.GcCode);
						ExtSearch_GcCode = null;
					} else {
						flag = true;
						TabMainView.that.showCacheList();
						startSearchTimer();
					}
				}
			});
		}
	}

	CancelWaitDialog wd;

	private void startGPXImport() {
		Log.info(log, "startGPXImport");
		if (ExtSearch_GpxPath != null) {

			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					Log.info(log, "startGPXImport:Timer startet");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							wd = CancelWaitDialog.ShowWait(Translation.Get("ImportGPX"), new IcancelListener() {

								@Override
								public void isCanceld() {
									wd.close();
								}
							}, new cancelRunnable() {

								@Override
								public void run() {
									Date ImportStart = new Date();
									Log.info(log, "startGPXImport:Timer startet");
									Importer importer = new Importer();
									ImporterProgress ip = new ImporterProgress();
									Database.Data.beginTransaction();

									importer.importGpx(ExtSearch_GpxPath, ip);

									Database.Data.setTransactionSuccessful();
									Database.Data.endTransaction();

									// Import ready
									wd.close();

									// finish close activity and notify changes

									CacheListChangedEventList.Call();

									Date Importfin = new Date();
									long ImportZeit = Importfin.getTime() - ImportStart.getTime();

									String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C " + String.valueOf(GPXFileImporter.LogCount) + "L in " + String.valueOf(ImportZeit);

									Log.debug(log, Msg);

									FilterProperties props = FilterInstances.getLastFilter();

									EditFilterSettings.ApplyFilter(props);

									ExtSearch_GpxPath = null;

									GL.that.Toast(Msg, 3000);
								}

								@Override
								public boolean cancel() {
									// TODO handle cancel
									return false;
								}
							});

						}
					});

				}
			};
			timer.schedule(task, 500);

		}
	}

	/**
	 * hook into menu button for activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (aktView == null)
			return false;
		menu.clear();
		int menuId = aktView.GetMenuId();
		if (menuId > 0) {
			getMenuInflater().inflate(menuId, menu);
		}
		aktView.BeforeShowMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * when menu button option selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (aktView != null)
			return aktView.ItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUserInteraction() {
		if (counterStopped)
			return;
		if (counter != null)
			counter.start();

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {

		setSelectedCache_onUI(cache, waypoint);
	}

	public void setSelectedCache_onUI(Cache cache, Waypoint waypoint) {

		float distance = cache.Distance(CalculationType.FAST, false);
		if (waypoint != null) {
			distance = GlobalCore.getSelectedWaypoint().Distance();
		}
		if (distance > Config.SoundApproachDistance.getValue()) {
			((main) main.mainActivity).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					GlobalCore.switchToCompassCompleted = false;
				}
			});
		}

	}

	private CB_Locator.Location CB_location = new CB_Locator.Location(0, 0, 0);

	@Override
	public void onLocationChanged(Location location) {
		ProviderType provider = ProviderType.NULL;

		if (location.getProvider().toLowerCase(new Locale("en")).contains("gps"))
			provider = ProviderType.GPS;
		if (location.getProvider().toLowerCase(new Locale("en")).contains("network"))
			provider = ProviderType.Network;

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
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void CacheListChangedEvent() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Back from Activitiy
		if (requestCode != Global.REQUEST_CODE_KEYBOARDACTIVITY && requestCode != Global.REQUEST_CODE_SCREENLOCK) {
			glListener.onStart();
		} else {
			return;
		}

		// Intent Result Take Photo
		if (requestCode == Global.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				GL.that.RunIfInitial(new IRunOnGL() {

					@Override
					public void run() {
						if (GlobalCore.isSetSelectedCache())
							GlobalCore.getSelectedCache().loadSpoilerRessources();
						String MediaFolder = Config.UserImageFolder.getValue();
						String TrackFolder = Config.TrackFolder.getValue();
						String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
						// Da ein Foto eine Momentaufnahme ist, kann hier die
						// Zeit und
						// die Koordinaten nach der Aufnahme verwendet werden.
						mediaTimeString = Global.GetTrackDateTimeString();

						CB_Locator.Location last = CB_Locator.Locator.getLastSavedFineLocation();

						if (last == null) {
							last = CB_Locator.Locator.getLocation(ProviderType.any);
						}

						if (last == null)
							return;

						TrackRecorder.AnnotateMedia(basename + ".jpg", relativPath + "/" + basename + ".jpg", last, mediaTimeString);

						TabMainView.that.reloadSprites(false);

					}
				});

			}

			return;
		}

		if (requestCode == Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR) {
			CB_Android_FileExplorer.onActivityResult(requestCode, resultCode, data);
			return;
		}

		// Intent Result Record Video
		if (requestCode == Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				GL.that.RunIfInitial(new IRunOnGL() {

					@Override
					public void run() {
						String[] projection = { MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE };
						Cursor cursor = managedQuery(cameraVideoURI, projection, null, null, null);
						int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
						cursor.moveToFirst();
						String recordedVideoFilePath = cursor.getString(column_index_data);

						String ext = FileIO.GetFileExtension(recordedVideoFilePath);
						String MediaFolder = Config.UserImageFolder.getValue();

						// Video in Media-Ordner verschieben
						File source = FileFactory.createFile(recordedVideoFilePath);
						File destination = FileFactory.createFile(MediaFolder + "/" + basename + "." + ext);
						// Datei wird umbenannt/verschoben
						if (!source.renameTo(destination)) {
							// Log.d("DroidCachebox", "Fehler beim Umbenennen
							// der Datei: " + source.getName());
						}

						String TrackFolder = Config.TrackFolder.getValue();
						String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
						TrackRecorder.AnnotateMedia(basename + "." + ext, relativPath + "/" + basename + "." + ext, mediaCoordinate, mediaTimeString);
					}
				});

				return;
			} else {
				// Log.d("DroidCachebox", "Video NOT taken!!!");
				return;
			}
		}

		// Intent Result get API key
		if (requestCode == Global.REQUEST_CODE_GET_API_KEY) {
			GL.that.RunIfInitial(new IRunOnGL() {

				@Override
				public void run() {
					SettingsActivity.resortList();
				}
			});

			Config.AcceptChanges();

		}

		if (aktView != null)
			aktView.ActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();

		if (v instanceof ViewOptionsMenu) {
			int id = ((ViewOptionsMenu) v).GetContextMenuId();
			if (id > 0) {
				inflater.inflate(id, menu);
				((ViewOptionsMenu) v).BeforeShowContextMenu(menu);
			}
			return;
		}

	}

	@Override
	protected void onPause() {
		Log.info(log, "Main=> onPause");

		stopped = true;

		unbindPluginServices();

		if (input == null) {
			graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
			input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), gdxConfig);
		}

		if (isFinishing()) {
			if (mConnection != null) {
				unbindService(mConnection);
				mConnection = null;
			}

		}

		super.onPause();

		Log.debug(log, "Main=> onPause release SuppressPowerSaving");

		try {
			if (this.mWakeLock != null)
				this.mWakeLock.release();
		} catch (Exception e) {
			// dann ebend nicht!
		}
	}

	Dialog pWaitD;
	private boolean stopped = false;

	private final AtomicBoolean waitForGL = new AtomicBoolean(false);

	private void showWaitToRenderStartet() {
		if (!GL.isInitial())
			return;

		if (pWaitD == null) {

			pWaitD = PleaseWaitMessageBox.Show(Translation.Get("waitForGL"), "", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
			stopped = false;

			waitForGL.set(true);

			GL.that.RunOnGL(new IRunOnGL() {
				@Override
				public void run() {
					pWaitD.dismiss();
					pWaitD = null;
					waitForGL.set(false);
				}
			});

			Thread chkThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (waitForGL.get()) {
						GL.that.renderOnce(true);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {

						}
					}
				}
			});
			chkThread.start();
		}
	}

	@Override
	protected void onResume() {
		viewGL.RenderContinous();
		if (stopped) {
			showWaitToRenderStartet();
			invalidateTextureEventList.Call();
		}

		Log.debug(log, "Main=> onResume");
		if (input == null) {
			Log.debug(log, "Main=> onResume input== null");

			graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
			input = new AndroidInput(this, this.inflater.getContext(), graphics.getView(), gdxConfig);
		}

		super.onResume();

		if (counter != null) {
			if (runsWithAkku) {
				counter.start();
			} else {
				counter.cancel();
			}
		}

		if (mSensorManager != null)
			mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI);
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.that.getQuickButtonListHeight() : 0;
		((main) main.mainActivity).setQuickButtonHeight(sollHeight);
		downSlider.isInitial = false;
		InfoDownSlider.invalidate();

		// Ausschalten verhindern
		/*
		 * This code together with the one in onDestroy() will make the screen
		 * be always on until this Activity gets destroyed.
		 */
		if (Config.SuppressPowerSaving.getValue()) {
			Log.debug(log, "Main=> onResume SuppressPowerSaving");
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			int flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
			this.mWakeLock = pm.newWakeLock(flags, "Cachebox");
			this.mWakeLock.acquire();
		}

		Config.SuppressPowerSaving.addChangedEventListener(new IChanged() {

			@Override
			public void isChanged() {
				if (Config.SuppressPowerSaving.getValue()) {
					Log.debug(log, "Main=> onResume SuppressPowerSaving");
					final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					int flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
					main.this.mWakeLock = pm.newWakeLock(flags, "Cachebox");
					main.this.mWakeLock.acquire();

				} else {
					final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					main.this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Cachebox non Powersave");
					main.this.mWakeLock.acquire();
				}
			}
		});

		try {
			initialOnTouchListener();
		} catch (Exception e) {
			Log.err(log, "onResume", "initialOnTouchListener", e);
		}

		// Initial PlugIn
		fillPluginList();
		bindPluginServices();

		final Bundle extras = getIntent().getExtras();
		if (!GlobalCore.restartAfterKill || extras != null) {
			ExtSearch_GcCode = extras.getString("GcCode");
			ExtSearch_GpxPath = extras.getString("GpxPath");
			if (ExtSearch_GpxPath != null)
				Log.info(log, "GPX found: " + ExtSearch_GpxPath);

			if (ExtSearch_GcCode != null || ExtSearch_GpxPath != null) {
				mustRunSearch = true;

				// ACB running call search
				if (TabMainView.that.isInitial()) {
					PlatformConnector.FirstShow();
				}
			}

			// delete handled extras
			getIntent().removeExtra("GcCode");
			getIntent().removeExtra("GpxPath");
		}
		GL.that.RestartRender();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onStop() {
		Log.debug(log, "Main=> onStop");

		if (mSensorManager != null)
			mSensorManager.unregisterListener(mListener);

		try {
			this.unregisterReceiver(this.mBatInfoReceiver);
		} catch (Exception e) {
			Log.err(log, "Main=> onStop", "unregisterReceiver", e);
		}
		counter.cancel();
		super.onStop();

		// Ausschalten wieder zulassen!
		/*
		 * This code together with the one in onDestroy() will make the screen
		 * be always on until this Activity gets destroyed.
		 */
		if (Config.SuppressPowerSaving.getValue()) {
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Cachebox non Powersave");
			this.mWakeLock.acquire();
		}
	}

	@Override
	public void onDestroy() {
		try {
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			// File f = FileFactory.createFile(Config.FieldNotesGarminPath.getValue());
			intent.setData(Uri.fromFile(new java.io.File(Config.FieldNotesGarminPath.getValue())));
			sendBroadcast(intent);
			intent.setData(Uri.fromFile(new java.io.File(CB_SLF4J.logfile)));
			sendBroadcast(intent);
			Log.info(log, "Sent files to MediaScanner");
		} catch (Exception e) {
			Log.err(log, "Send files to MediaScanner: " + e.getMessage());
		}

		try {
			if (mReceiver != null)
				this.unregisterReceiver(mReceiver);
		} catch (Exception e1) {
		}
		mReceiver = null;

		Log.info(log, "CACHEBOX: " + "Main=> onDestroy");
		// frame.removeAllViews();
		if (isRestart) {
			Log.debug(log, "Main=> onDestroy isRestart");
			super.onDestroy();
			isRestart = false;
		} else {
			if (isFinishing()) {
				Log.info(log, "Main=> onDestroy isFinishing");
				if (GlobalCore.RunFromSplash) {
					Config.settings.WriteToDB();

					try {
						if (this.mWakeLock != null)
							this.mWakeLock.release();
					} catch (Exception e) {
						e.printStackTrace();
					}
					counter.cancel();
					TrackRecorder.StopRecording();
					// GPS Verbindung beenden
					locationManager.removeUpdates(this);
					// Voice Recorder stoppen
					if (extAudioRecorder != null) {
						extAudioRecorder.stop();
						extAudioRecorder.release();
						extAudioRecorder = null;
					}
					SelectedCacheEventList.list.clear();
					SelectedCacheEventList.list.clear();
					CacheListChangedEventList.list.clear();
					if (aktView != null) {
						aktView.OnHide();
						aktView.OnFree();
					}
					aktView = null;

					if (aktTabView != null) {
						aktTabView.OnHide();
						aktTabView.OnFree();
					}
					aktTabView = null;

					for (ViewOptionsMenu vom : ViewList) {
						vom.OnFree();
					}
					ViewList.clear();
					viewGL = null;
					jokerView = null;
					descriptionView = null;
					mainActivity = null;

					InfoDownSlider = null;

					Config.AcceptChanges();

					Database.Data.Close();
					Database.FieldNotes.Close();

					Sprites.destroyCache();

					Database.Settings.Close();

				}

				super.onDestroy();
				if (GlobalCore.RunFromSplash)
					System.exit(0);
			} else {
				Log.info(log, "Main=> onDestroy isFinishing==false");

				if (aktView != null)
					aktView.OnHide();
				if (aktTabView != null)
					aktTabView.OnHide();

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
	 * @author -jwei
	 *         http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-
	 *         and-screen-on-intents/
	 */
	public static class ScreenReceiver extends BroadcastReceiver {

		// thanks Jason
		public static boolean wasScreenOn = true;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Energy.setDisplayOff();
				CB_Locator.Locator.setDisplayOff();
				wasScreenOn = false;
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Energy.setDisplayOn();
				CB_Locator.Locator.setDisplayOn();
				wasScreenOn = true;
			}
		}

	}

	/**
	 * Startet die Bildschirm Sperre
	 */
	public void startScreenLock() {
		startScreenLock(false);
	}

	/**
	 * Startet die Bildschirm Sperre. Mit der der Übergabe von force = true,
	 * werden abfragen ob im Akkubetrieb oder die Zeit Einstellungen ignoriert.
	 *
	 * @param force
	 */
	public void startScreenLock(boolean force) {
		Log.debug(log, "Start Screenlock (force:" + force + ")");

		if (!force) {
			if (!runsWithAkku)
				return;
			counter.cancel();
			counterStopped = true;
			// ScreenLock nur Starten, wenn der Config Wert größer 10 sec ist.
			// Das verhindert das selber aussperren!
			if ((Config.ScreenLock.getValue() / 1000 < 10))
				return;
		}

		// TODO move/create ScreenLock on GDX
	}

	/*
	 * Handler
	 */

	private float compassHeading = -1;

	private final SensorEventListener mListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			try {
				mCompassValues = event.values;
				compassHeading = mCompassValues[0];
				CB_Locator.Locator.setHeading(compassHeading, CompassType.Magnetic);
			} catch (Exception e) {
				Log.err(log, "main.mListener.onSensorChanged()", "", e);
				e.printStackTrace();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	public static boolean ScreenLockOff = false;

	/**
	 * Gibt die zur ViewID gehörige View zurück und erstellst eine Instanz, wenn
	 * sie nicht exestiert.
	 *
	 * @param ID ViewID
	 * @return View
	 */
	private ViewOptionsMenu getView(ViewID ID) {
		// first chek if view on List
		if (ID.getID() < ViewList.size()) {
			return ViewList.get(ID.getID());
		}

		if (ID == ViewConst.JOKER_VIEW)
			return jokerView = new JokerView(this, this);
		else if (ID == ViewConst.SOLVER_VIEW)
			return solverView = new SolverView(this, inflater);
		else if (ID == ViewConst.SPOILER_VIEW) {
			// if (spoilerView == null) spoilerView = new SpoilerView(this,
			// inflater);

			// return spoilerView;
		} else if (ID == ViewConst.DESCRIPTION_VIEW) {
			if (descriptionView != null) {
				return descriptionView;
			} else {
				return descriptionView = new DescriptionView(this, inflater);
			}

		}

		return null;
	}

	private void showActivity(ViewID ID) {
		if (ID == ViewConst.NAVIGATE_TO) {
			NavigateTo();
		} else if (ID == ViewConst.VOICE_REC) {
			recVoice();
		} else if (ID == ViewConst.TAKE_PHOTO) {
			takePhoto();
		} else if (ID == ViewConst.VIDEO_REC) {
			recVideo();
		} else if (ID == ViewConst.LOCK) {
			startScreenLock(true);
		}

	}

	private void showView(ViewOptionsMenu view, ViewID ID) {
		if (GlobalCore.isTab) {
			if (ID.getPos() == ViewID.UI_Pos.Right) {
				showTabletView(view, ID);
				return;
			}
		}

		if (aktView != null) {
			aktView.OnHide();

			if (ID.getType() == UI_Type.OpenGl) {
				this.onPause();
			}

			if (aktView.equals(jokerView)) {
				// Instanz löschen
				aktView = null;
				jokerView.OnFree();
				jokerView = null;
			} else if (aktView.equals(solverView)) {
				// Instanz löschen
				aktView = null;
				solverView.OnFree();
				solverView = null;
			} else if (aktView.equals(descriptionView)) {
				// Instanz löschen
				aktView = null;
				descriptionView.OnHide();

			}

		}

		if (ID.getType() == UI_Type.OpenGl) {

			ShowViewGL(ID);
			return;

		}

		aktView = view;
		((View) aktView).setDrawingCacheEnabled(true);

		frame.removeAllViews();
		ViewParent parent = ((View) aktView).getParent();
		if (parent != null) {
			// aktView ist noch gebunden, also lösen
			((FrameLayout) parent).removeAllViews();
		}
		frame.addView((View) aktView);
		aktView.OnShow();

		InfoDownSlider.invalidate();
		((View) aktView).forceLayout();

		if (aktView != null)
			((View) aktView).setVisibility(View.VISIBLE);
		if (aktTabView != null)
			((View) aktTabView).setVisibility(View.VISIBLE);
		if (InfoDownSlider != null)
			((View) InfoDownSlider).setVisibility(View.INVISIBLE);
		if (cacheNameView != null)
			((View) cacheNameView).setVisibility(View.INVISIBLE);

	}

	private void ShowViewGL(ViewID Id) {
		Log.debug(log, "ShowViewGL " + GlFrame.getMeasuredWidth() + "/" + GlFrame.getMeasuredHeight());

		initialViewGL();

		glListener.onStart();
		glListener.setGLViewID(Id);

		if (aktTabViewId != null && aktTabViewId.getType() == ViewID.UI_Type.OpenGl) {
			tabFrame.setVisibility(View.INVISIBLE);
		}

		if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.OpenGl) {
			frame.setVisibility(View.INVISIBLE);
		}

		InfoDownSlider.invalidate();

	}

	private void showTabletView(ViewOptionsMenu view, ViewID Id) {

		if (aktTabView != null) {
			aktTabView.OnHide();

		}

		aktTabView = view;
		try {
			tabFrame.removeAllViews();
			ViewParent parent = ((View) aktTabView).getParent();
			if (parent != null) {
				// aktView ist noch gebunden, also lösen
				((FrameLayout) parent).removeAllViews();
			}
		} catch (Exception e) {

		}
		tabFrame.addView((View) aktTabView);
		aktTabView.OnShow();
		aktTabViewId = Id;
		InfoDownSlider.invalidate();
		((View) aktTabView).forceLayout();

	}

	public static void vibrate() {
		if (Config.vibrateFeedback.getValue())
			vibrator.vibrate(Config.VibrateTime.getValue());
	}

	/*
	 * Initial Methods
	 */

	private void findViewsById() {
		QuickButtonList = (HorizontalListView) this.findViewById(R.id.main_quick_button_list);
		TopLayout = (LinearLayout) this.findViewById(R.id.layoutTop);
		frame = (FrameLayout) this.findViewById(R.id.layoutContent);
		tabFrame = (FrameLayout) this.findViewById(R.id.tabletLayoutContent);
		GlFrame = (FrameLayout) this.findViewById(R.id.layoutGlContent);

		InfoDownSlider = (downSlider) this.findViewById(R.id.downSlider);

		Mic_Icon = (Mic_On_Flash) this.findViewById(R.id.mic_flash);

		cacheNameView = (CacheNameView) this.findViewById(R.id.main_cache_name_view);

		strengthLayout = (LinearLayout) this.findViewById(R.id.main_strength_control);

	}

	private void initialLocationManager() {

		try {
			if (locationManager != null) {
				// ist schon initialisiert
				return;
			}

			// GPS
			// Get the location manager
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// // Define the criteria how to select the locatioin provider ->
			// use
			// // default
			// Criteria criteria = new Criteria(); // noch nötig ???
			// criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// criteria.setAltitudeRequired(false);
			// criteria.setBearingRequired(false);
			// criteria.setCostAllowed(true);
			// criteria.setPowerRequirement(Criteria.POWER_LOW);

			/*
			 * Longri: Ich habe die Zeiten und Distanzen der Location Updates
			 * angepasst. Der Network Provider hat eine schlechte genauigkeit,
			 * darher reicht es wenn er alle 10sec einen wert liefert, wen der
			 * alte um 500m abweicht. Beim GPS Provider habe ich die
			 * aktualiesierungs Zeit verkürzt, damit bei deaktiviertem Hardware
			 * Kompass aber die Werte trotzdem noch in einem gesunden Verhältnis
			 * zwichen Performance und Stromverbrauch, geliefert werden. Andere
			 * apps haben hier 0.
			 */

			int updateTime = Config.gpsUpdateTime.getValue();

			Config.gpsUpdateTime.addChangedEventListener(new IChanged() {

				@Override
				public void isChanged() {
					int updateTime = Config.gpsUpdateTime.getValue();
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, main.this);
				}
			});

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 300, this);

			locationManager.addNmeaListener(this);
			locationManager.addGpsStatusListener(this);
		} catch (Exception e) {
			Log.err(log, "main.initialLocationManager()", "", e);
			e.printStackTrace();
		}

	}

	private void initialViewGL() {
		try {
			gdxView = initializeForView(glListener, gdxConfig);

			Log.debug(log, "Initial new gdxView=" + gdxView.toString());

			int GlSurfaceType = -1;
			if (gdxView instanceof GLSurfaceView20)
				GlSurfaceType = ViewGL.GLSURFACE_VIEW20;
			else if (gdxView instanceof GLSurfaceView)
				GlSurfaceType = ViewGL.GLSURFACE_GLSURFACE;

			ViewGL.setSurfaceType(GlSurfaceType);

			Log.debug(log, "InitializeForView...");

			switch (GlSurfaceType) {
			case ViewGL.GLSURFACE_VIEW20:
				((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
				break;

			case ViewGL.GLSURFACE_GLSURFACE:
				((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
				break;
			}

			initialOnTouchListener();

			if (viewGL == null)
				viewGL = new ViewGL(this, inflater, gdxView, glListener);

			viewGL.InitializeMap();

			GlFrame.removeAllViews();
			ViewParent parent = gdxView.getParent();
			if (parent != null) {
				// aktView ist noch gebunden, also lösen
				((RelativeLayout) parent).removeAllViews();
			}
			GlFrame.addView(gdxView);

			// }
		} catch (Exception e) {
			Log.err(log, "main.initialViewGL()", "", e);
			e.printStackTrace();
		}

	}

	private void initialOnTouchListener() throws Exception {

		if (gdxView == null)
			throw new Exception("gdx view nicht initialisiert");

		gdxView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				return sendMotionEvent(event);
			}
		});
	}

	public boolean sendMotionEvent(final MotionEvent event) {

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

		try {
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				glListener.onTouchDownBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
				break;
			case MotionEvent.ACTION_MOVE:
				glListener.onTouchDraggedBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex));
				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				glListener.onTouchUpBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
				break;
			}
		} catch (Exception e) {
			Log.err(log, "gdxView.OnTouchListener", "", e);
			return true;
		}
		return true;
	}

	private void initalMicIcon() {
		Mic_Icon.SetOff();
		Mic_Icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Stoppe Aufnahme durch klick auf Mikrofon-Icon
				setVoiceRecIsStart(false);
			}
		});
	}

	private void takePhoto() {
		Log.info(log, "takePhoto start " + GlobalCore.getSelectedCache());

		// define the file-name to save photo taken by Camera activity
		String directory = Config.UserImageFolder.getValue();
		if (!FileIO.createDirectory(directory)) {
			return;
		}
		basename = Global.GetDateTimeString();
		if (GlobalCore.isSetSelectedCache()) {
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
			mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
			// Title = Global.SelectedCache().Name;
		} else {
			mediaCacheName = "Image";
		}
		basename = basename + " " + mediaCacheName;
		mediafile = FileFactory.createFile(directory + "/" + basename + ".jpg");

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new java.io.File(mediafile.getAbsolutePath())));
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, Global.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

		// for the photo to show within spoilers
		Log.info(log, "SpoilerForceReEvaluate");
		if (GlobalCore.isSetSelectedCache()) {
			GlobalCore.getSelectedCache().SpoilerForceReEvaluate();
		}
		if (TabMainView.spoilerView != null) {
			TabMainView.spoilerView.ForceReload();
		}
	}

	private void recVideo() {

		// define the file-name to save video taken by Camera activity
		String directory = Config.UserImageFolder.getValue();
		if (!FileIO.createDirectory(directory)) {
			return;
		}

		basename = Global.GetDateTimeString();

		if (GlobalCore.isSetSelectedCache()) {
			String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
			mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
			// Title = Global.SelectedCache().Name;
		} else {
			mediaCacheName = "Video";
		}

		basename += " " + mediaCacheName;
		mediafile = FileFactory.createFile(directory + "/" + basename + ".3gp");

		// Da ein Video keine Momentaufnahme ist, muss die Zeit und die
		// Koordinaten beim Start der Aufnahme verwendet werden.
		mediaTimeString = Global.GetTrackDateTimeString();
		mediaCoordinate = Locator.getLocation(ProviderType.GPS);

		ContentValues values = new ContentValues();
		values.put(MediaStore.Video.Media.TITLE, "captureTemp.mp4");
		cameraVideoURI = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

		final Intent videointent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		videointent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new java.io.File(mediafile.getAbsolutePath())));
		videointent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		// videointent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,
		// MAXIMUM_VIDEO_SIZE);

		startActivityForResult(videointent, Global.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	private void recVoice() {
		if (!getVoiceRecIsStart()) // Voice Recorder starten
		{
			// Log.d("DroidCachebox", "Starting voice recorder on the
			// phone...");

			// define the file-name to save voice taken by activity
			String directory = Config.UserImageFolder.getValue();
			if (!FileIO.createDirectory(directory)) {
				// Log.d("DroidCachebox", "Media-Folder does not exist...");
				return;
			}

			basename = Global.GetDateTimeString();

			if (GlobalCore.isSetSelectedCache()) {
				String validName = FileIO.RemoveInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
				mediaCacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
				// Title = Global.SelectedCache().Name;
			} else {
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
			TrackRecorder.AnnotateMedia(basename + ".wav", relativPath + "/" + basename + ".wav", Locator.getLocation(ProviderType.GPS), Global.GetTrackDateTimeString());
			Toast.makeText(mainActivity, "Start Voice Recorder", Toast.LENGTH_SHORT).show();

			setVoiceRecIsStart(true);
			counter.cancel(); // Während der Aufnahme Screen-Lock-Counter
			// stoppen
			counterStopped = true;

			return;
		} else { // Voice Recorder stoppen
			// Log.d("DroidCachebox", "Stoping voice recorder on the
			// phone...");
			// Stop recording
			setVoiceRecIsStart(false);
			return;
		}
	}

	private void showJoker() {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// Debug add Joker
				// Global.Jokers.AddJoker("Andre", "Höpfner", "Katipa", "12",
				// "030 ++++++", "24/7");
				// Global.Jokers.AddJoker("Andre", "Höpfner", "Katipa", "12",
				// "030 ++++++", "24/7");

				if (Global.Jokers.isEmpty()) { // Wenn Telefonjoker-Liste leer
					// neu laden

					try {
						URL url = new URL("http://www.gcjoker.de/cachebox.php?md5=" + Config.GcJoker.getValue() + "&wpt=" + GlobalCore.getSelectedCache().getGcCode());
						URLConnection urlConnection = url.openConnection();
						HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;

						// Get the HTTP response code
						if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
							InputStream inputStream = httpConnection.getInputStream();
							if (inputStream != null) {
								String line;
								try {
									BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
									while ((line = reader.readLine()) != null) {
										String[] s = line.split(";", 7);
										try {
											if (s[0].equals("2")) // 2
											// entspricht
											// Fehler,
											// Fehlerursache ist
											// in S[1]
											{
												GL_MsgBox.Show(s[1]);
												break;
											}
											if (s[0].equals("1")) // 1
											// entspricht
											// Warnung, Ursache
											// ist in S[1]
											{ // es können aber noch gültige
												// Einträge
												// folgen
												GL_MsgBox.Show(s[1]);
											}
											if (s[0].equals("0")) // Normaler
											// Eintrag
											{
												Global.Jokers.AddJoker(s[1], s[2], s[3], s[4], s[5], s[6]);
											}
										} catch (Exception exc) {
											Log.err(log, "main.initialBtnInfoContextMenu()", "HTTP response Jokers", exc);
											return;
										}
									}

								} finally {
									inputStream.close();
								}
							}
						}
					} catch (MalformedURLException urlEx) {
						Log.err(log, "main.initialBtnInfoContextMenu()", "MalformedURLException HTTP response Jokers", urlEx);
						// Log.d("DroidCachebox", urlEx.getMessage());
					} catch (IOException ioEx) {
						Log.err(log, "main.initialBtnInfoContextMenu()", "IOException HTTP response Jokers", ioEx);
						// Log.d("DroidCachebox", ioEx.getMessage());
						GL_MsgBox.Show(Translation.Get("internetError"));
					} catch (Exception ex) {
						Log.err(log, "main.initialBtnInfoContextMenu()", "HTTP response Jokers", ex);
						// Log.d("DroidCachebox", ex.getMessage());
					}
				}

				if (Global.Jokers.isEmpty()) {
					GL_MsgBox.Show(Translation.Get("noJokers"));
				} else {
					Log.info(log, "Open JokerView...");

					main.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showView(ViewConst.JOKER_VIEW);
						}
					});
				}
			}
		});
		thread.start();

	}

	private void NavigateTo() {
		if (GlobalCore.isSetSelectedCache()) {
			double lat = GlobalCore.getSelectedCache().Latitude();
			double lon = GlobalCore.getSelectedCache().Pos.getLongitude();
			String targetName = GlobalCore.getSelectedCache().getGcCode();

			if (GlobalCore.getSelectedWaypoint() != null) {
				lat = GlobalCore.getSelectedWaypoint().Pos.getLatitude();
				lon = GlobalCore.getSelectedWaypoint().Pos.getLongitude();
				targetName = GlobalCore.getSelectedWaypoint().getGcCode();
			}

			String selectedNavi = Config.Navis.getValue();
			if (selectedNavi.equals("Ask")) {
				// todo : Spinner for Selection
				for (String navi : CB_UI_Settings.navis) {
					if (!navi.equalsIgnoreCase("Ask")) {
						selectedNavi = navi;
					}
				}
				// todo : remove if Spinner done
				selectedNavi = Config.Navis.getValue(); // =Ask=do nothing
			}

			Intent intent = null;
			if (selectedNavi.equals("Navigon")) {
				intent = getIntent("com.navigon.navigator", "android.intent.action.navigon.START_PUBLIC");
				if (intent != null) {
					intent.putExtra("latitude", (float) lat);
					intent.putExtra("longitude", (float) lon);
				}
			} else if (selectedNavi.equals("Orux")) {
				intent = getIntent("", "com.oruxmaps.VIEW_MAP_ONLINE");
				// from http://www.oruxmaps.com/oruxmapsmanual_en.pdf
				if (intent != null) {
					double[] targetLat = { lat };
					double[] targetLon = { lon };
					String[] targetNames = { targetName };
					intent.putExtra("targetLat", targetLat);
					intent.putExtra("targetLon", targetLon);
					intent.putExtra("targetName", targetNames);
					intent.putExtra("navigatetoindex", 1);
				}
			} else if (selectedNavi.equals("OsmAnd")) {
				intent = getIntent(Intent.ACTION_VIEW, "geo:" + lat + "," + lon);
			} else if (selectedNavi.equals("OsmAnd2")) {
				intent = getIntent(Intent.ACTION_VIEW, "http://download.osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=14");
			} else if (selectedNavi.equals("Copilot") || selectedNavi.equals("Google")) {
				intent = getIntent(Intent.ACTION_VIEW, "http://maps.google.com/maps?daddr=" + lat + "," + lon);
			} else if (selectedNavi.equals("Waze")) {
				intent = getIntent(Intent.ACTION_VIEW, "waze://?ll=" + lat + "," + lon);
			}
			try {
				if (intent != null)
					startActivity(intent);
			} catch (Exception e) {
				Log.err(log, "main.NavigateTo()", "Error Start " + selectedNavi, e);
			}
		}
	}

	private Intent getIntent(String mode, String from) {
		Intent intent = null;
		try {
			if (mode != null) {
				if (mode.equals(Intent.ACTION_VIEW)) {
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(from));
					return intent;
				} else {
					intent = getPackageManager().getLaunchIntentForPackage(mode);
				}
			}
			if (intent == null) {
				intent = new Intent(from);
			}
		} catch (Exception e) {
			intent = null;
		}
		return intent;
	}

	public void setVoiceRecIsStart(Boolean value) {
		mVoiceRecIsStart = value;
		if (mVoiceRecIsStart) {
			Mic_Icon.SetOn();
		} else { // Aufnahme stoppen
			Mic_Icon.SetOff();
			if (extAudioRecorder != null) {
				extAudioRecorder.stop();
				extAudioRecorder.release();
				extAudioRecorder = null;
				Toast.makeText(mainActivity, "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
			}
			if (runsWithAkku) {
				counterStopped = false; // ScreenLock-Counter wieder starten
				counter.start();
			}
		}

	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {
		try {
			if (nmea.length() >= 6 && nmea.substring(0, 6).equalsIgnoreCase("$GPGGA")) {
				String[] s = nmea.split(",");
				try {
					if (s[11].equals(""))
						return;
					if (!s[6].equals("1") & !s[6].equals("2"))
						return; // Fix ungültig
					double altCorrection = Double.valueOf(s[11]);
					if (altCorrection == 0)
						return;
					Log.info(log, "AltCorrection: " + String.valueOf(altCorrection));
					Locator.setAltCorrection(altCorrection);
					// Höhenkorrektur ändert sich normalerweise nicht, einmal
					// auslesen reicht...
					locationManager.removeNmeaListener(this);
				} catch (Exception exc) {
					// keine Höhenkorrektur vorhanden
				}
			}
		} catch (Exception e) {
			Log.err(log, "main.onNmeaReceived()", "", e);
			e.printStackTrace();
		}
	}

	public void setScreenLockTimerNew(int value) {
		Log.debug(log, "setScreenLockTimerNew");
		counter.cancel();
		counter = new ScreenLockTimer(value, value);
		if (runsWithAkku)
			counter.start();
	}

	static class LockClass {
	}

	;

	static LockClass lockObject = new LockClass();

	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			try {
				int plugged = intent.getIntExtra("plugged", -1);

				if (runsWithAkku != (plugged == 0)) {
					// if loading status has changed
					runsWithAkku = plugged == 0;
					if (!runsWithAkku) {
						// activate counter when device runs with accu
						counter.cancel();
						counterStopped = true;
					} else {
						// deactivate counter when device is plugged in
						counter.start();
						counterStopped = false;
					}
				}
			} catch (Exception e) {
				Log.err(log, "main.mBatInfoReceiver.onReceive()", "", e);
				e.printStackTrace();
			}
		}

	};

	int horizontalListViewHeigt;

	public void setQuickButtonHeight(int value) {
		horizontalListViewHeigt = value;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				QuickButtonList.setHeight(horizontalListViewHeigt);
				QuickButtonList.invalidate();
				TopLayout.requestLayout();
				frame.requestLayout();
			}
		});

	}

	/**
	 * überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung
	 * ausgegeben.
	 */
	private void chkGpsIsOn() {
		try {
			if (Config.Ask_Switch_GPS_ON.getValue() && !GpsOn()) {
				CheckTranslationIsLoaded();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						MessageBox.Show(Translation.Get("GPSon?"), Translation.Get("GPSoff"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int button) {
								// Behandle das ergebniss
								switch (button) {
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
		} catch (Exception e) {
			Log.err(log, "main.chkGpsIsOn()", "", e);
			e.printStackTrace();
		}
	}

	private void CheckTranslationIsLoaded() {
		if (!Translation.isInitial()) {
			new Translation(Config.mWorkPath, FileType.Internal);
			try {
				Translation.LoadTranslation(Config.Sel_LanguagePath.getValue());
			} catch (Exception e) {
				try {
					Translation.LoadTranslation(Config.Sel_LanguagePath.getDefaultValue());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private boolean GpsOn() {
		LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean GpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return GpsOn;
	}

	private void askToGetApiKey() {
		MessageBox.Show(Translation.Get("wantApi"), Translation.Get("welcome"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int button) {
				// Behandle das ergebniss
				switch (button) {
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

	public static void Toast(String Msg) {
		Toast.makeText(mainActivity, Msg, Toast.LENGTH_SHORT).show();
	}

	public void showView(ViewID ID) {
		if (ID == null) {
			Log.info(log, "main.showView(is NULL)");
			return;// keine Action
		}

		String Pos = "null";
		String Type = "null";
		if (ID.getPos() != null) {
			Pos = ID.getPos().toString();
			Type = ID.getType().toString();
		}

		Log.info(log, "main.showView(" + ID.getID() + "/" + Pos + "/" + Type + ")");

		if (ID.getType() == ViewID.UI_Type.Activity) {
			showActivity(ID);
			return;
		}

		if (GlobalCore.isTab) {
			if (ID.getPos() == UI_Pos.Left) {
				if (!(aktView == null) && ID == aktViewId) {
					aktView.OnShow();
					return;
				}
			} else {
				if (!(aktTabView == null) && ID == aktViewId) {
					aktTabView.OnShow();
					return;
				}
			}
		} else {
			if (!(aktView == null) && ID == aktViewId) {
				aktView.OnShow();
				return;
			}
		}

		if (ID.getPos() == UI_Pos.Left) {
			aktViewId = ID;
		} else {
			aktTabViewId = ID;
		}

		if (aktTabViewId != null && aktTabViewId.getType() == ViewID.UI_Type.Android) {
			if (tabFrame != null)
				tabFrame.setVisibility(View.VISIBLE);
		}

		if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.Android) {
			frame.setVisibility(View.VISIBLE);
		}

		if (ID.getType() == UI_Type.Android) {
			showView(getView(ID), ID);
		}

		if (ID.getType() == UI_Type.OpenGl) {
			ShowViewGL(ID);
		}

		if (ID == ViewConst.MAP_VIEW) {
			ScreenLockOff = true;
			startScreenLock(true);
		}

	}

	// ########### PlugIn Method ##########################

	// PlugIn Methodes
	private static final String ACTION_PICK_PLUGIN = "de.cachebox.action.PICK_PLUGIN";
	private static final String KEY_PKG = "pkg";
	private static final String KEY_SERVICENAME = "servicename";
	private ArrayList<HashMap<String, String>> services;
	private final PluginServiceConnection pluginServiceConnection[] = new PluginServiceConnection[4];

	private void fillPluginList() {
		services = new ArrayList<HashMap<String, String>>();
		PackageManager packageManager = getPackageManager();
		Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
		// Log.d(LOG_TAG, "fillPluginList: " + list);

		Config.hasFTF_PlugIn.setValue(false);
		Config.hasPQ_PlugIn.setValue(false);
		int i;
		try {
			for (i = 0; i < list.size(); ++i) {
				ResolveInfo info = list.get(i);
				ServiceInfo sinfo = info.serviceInfo;
				// Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " +
				// sinfo);
				if (sinfo != null) {

					if (sinfo.packageName.contains("de.CB_FTF_PlugIn")) // Don't
					// bind,
					// is an
					// Widget
					{
						Config.hasFTF_PlugIn.setValue(true);
					} else if (sinfo.packageName.contains("de.CB_PQ_PlugIn"))// Don't
					// bind,
					// is
					// an
					// Widget
					{
						Config.hasPQ_PlugIn.setValue(true);
					} else
					// PlugIn for Bind
					{
						HashMap<String, String> item = new HashMap<String, String>();
						item.put(KEY_PKG, sinfo.packageName);
						item.put(KEY_SERVICENAME, sinfo.name);
						services.add(item);
						if (i <= 4) {
							inflateToView(i, packageManager, sinfo.packageName);

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Config.AcceptChanges();
	}

	@SuppressWarnings("unused")
	private void inflateToView(int rowCtr, PackageManager packageManager, String packageName) {
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
			Resources res = packageManager.getResourcesForApplication(info);

			XmlResourceParser xres = res.getLayout(0x7f030000);

		} catch (NameNotFoundException ex) {
			Log.err(log, "NameNotFoundException", ex);
		}
	}

	private void bindPluginServices() {

		for (int i = 0; i < services.size(); ++i) {
			final PluginServiceConnection con = new PluginServiceConnection();

			pluginServiceConnection[i] = con;
			final Intent intent = new Intent();
			final HashMap<String, String> data = services.get(i);
			intent.setClassName(data.get(KEY_PKG), data.get(KEY_SERVICENAME));
			// Log.d(LOG_TAG, "bindPluginServices: " + intent);
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						bindService(intent, con, Context.BIND_AUTO_CREATE);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			};
			t.run();

		}

	}

	private void unbindPluginServices() {
		if (services != null) {
			for (int i = 0; i < services.size(); ++i) {

				for (PluginServiceConnection con : pluginServiceConnection) {
					if (con != null) {
						unbindService(con);
					}
				}
			}
		}
	}

	class PluginServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder boundService) {
			int idx = getServiceConnectionIndex();
			Log.info(log, "onServiceConnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0) {
				Global.iPlugin[idx] = IPlugIn.Stub.asInterface(boundService);

				// set Joker PlugIn

				if (Global.iPlugin != null && Global.iPlugin[0] != null) {
					Config.hasCallPermission.setValue(true);
				} else {
					Config.hasCallPermission.setValue(false);
				}

				Config.AcceptChanges();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			int idx = getServiceConnectionIndex();
			Log.info(log, "onServiceDisconnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0)
				Global.iPlugin[idx] = null;
		}

		private int getServiceConnectionIndex() {
			for (int i = 0; i < pluginServiceConnection.length; ++i)
				if (this == pluginServiceConnection[i])
					return i;
			return -1;
		}
	}

	;

	// #########################################################

	// ########### Platform Conector ##########################

	private void initialPlatformConector() {

		Plattform.used = Plattform.Android;

		initialLocatorBase();

		PlatformConnector.setisOnlineListener(new IHardwarStateListener() {
			/*
			 * isOnline Liefert TRUE wenn die Möglichkeit besteht auf das
			 * Internet zuzugreifen
			 */

			@Override
			public boolean isOnline() {
				ConnectivityManager cm = (ConnectivityManager) main.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				if (netInfo != null && netInfo.isConnectedOrConnecting()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean isGPSon() {
				return GpsOn();
			}

			@Override
			public void vibrate() {
				main.vibrate();
			}

			private AtomicBoolean torchAvailable = null;
			private Camera deviceCamera;

			@Override
			public boolean isTorchAvailable() {
				if (torchAvailable == null) {
					torchAvailable = new AtomicBoolean();
					torchAvailable.set(getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH));
				}

				return torchAvailable.get();
			}

			@Override
			public boolean isTorchOn() {
				if (deviceCamera != null)
					return true;
				return false;
			}

			@Override
			public void switchTorch() {
				if (deviceCamera == null) {
					deviceCamera = Camera.open();
					Parameters p = deviceCamera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					deviceCamera.setParameters(p);
					deviceCamera.startPreview();
				} else {
					deviceCamera.stopPreview();
					deviceCamera.release();
					deviceCamera = null;
				}
			}

			@Override
			public void switchToGpsMeasure() {
				Log.info(log, "switchToGpsMeasure()");
				int updateTime = Config.gpsUpdateTime.getValue();
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 0, main.this);
			}

			@Override
			public void switchtoGpsDefault() {
				Log.info(log, "switchtoGpsDefault()");
				int updateTime = Config.gpsUpdateTime.getValue();
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, main.this);
			}

		});

		PlatformConnector.setShowViewListener(new IShowViewListener() {

			@Override
			public void show(final ViewID viewID, final int left, final int top, final int right, final int bottom) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.info(log, "Show View from GL =>" + viewID.getID());

						// set Content size

						if (viewID.getType() != ViewID.UI_Type.Activity) {
							if (viewID.getPos() == UI_Pos.Left) {
								RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) frame.getLayoutParams();
								paramsLeft.setMargins(left, top, right, bottom);
								frame.setLayoutParams(paramsLeft);
								frame.requestLayout();
							} else {
								if (tabFrame != null) {
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabFrame.getLayoutParams();

									int versatz = 0;
									if (TabMainView.leftTab != null) {
										versatz = (int) (TabMainView.leftTab.getWidth() - frame.getWidth());
									}

									params.setMargins(versatz + left, top, right, bottom);
									tabFrame.setLayoutParams(params);
									tabFrame.requestLayout();
								}
							}
						}

						if (InfoDownSlider != null) {
							InfoDownSlider.ActionUp();
							((View) InfoDownSlider).setVisibility(View.INVISIBLE);
						}

						if (aktView != null)
							((View) aktView).setVisibility(View.VISIBLE);
						if (aktTabView != null)
							((View) aktTabView).setVisibility(View.VISIBLE);
						if (cacheNameView != null)
							((View) cacheNameView).setVisibility(View.INVISIBLE);

						if (viewID == ViewConst.JOKER_VIEW) {
							showJoker();
						} else {
							showView(viewID);
						}

					}
				});

			}

			@Override
			public void hide(final ViewID viewID) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (GlobalCore.isTab) {
							if (viewID.getPos() == UI_Pos.Left) {
								if (!(aktView == null) && viewID == aktViewId) {
									aktView.OnHide();
								}
							} else {
								if (!(aktTabView == null) && viewID == aktTabViewId) {
									aktTabView.OnHide();
								}
							}
						} else {
							if (!(aktView == null) && viewID == aktViewId) {
								aktView.OnHide();
							}
						}
						if (aktTabViewId != null && aktTabViewId == viewID && aktTabViewId.getPos() == UI_Pos.Right) {
							tabFrame.setVisibility(View.INVISIBLE);
							aktTabViewId = null;
							aktTabView = null;
						}

						if (aktViewId != null && aktViewId == viewID && aktViewId.getPos() == UI_Pos.Left) {
							frame.setVisibility(View.INVISIBLE);
							aktViewId = null;
							aktView = null;
						}

						Log.debug(log, "Hide Android view");
					}
				});

			}

			@Override
			public void showForDialog() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// chk for timer conflict (releay set invisible)
						// only if showing Dialog or Activity
						if (!GL.that.isShownDialogActivity())
							return;

						if (aktView != null)
							((View) aktView).setVisibility(View.INVISIBLE);
						if (aktTabView != null)
							((View) aktTabView).setVisibility(View.INVISIBLE);
						if (InfoDownSlider != null)
							((View) InfoDownSlider).setVisibility(View.INVISIBLE);
						if (cacheNameView != null)
							((View) cacheNameView).setVisibility(View.INVISIBLE);
						setLockScreenProperty();
						// Log.debug(log, "Show AndroidView");
					}
				});
			}

			@Override
			public void hideForDialog() {

				Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								// chk for timer conflict (releay set invisible)
								// only if not showing Dialog or Activity
								if (!GL.that.isShownDialogActivity()) {
									if (aktView != null)
										((View) aktView).setVisibility(View.VISIBLE);
									if (aktTabView != null)
										((View) aktTabView).setVisibility(View.VISIBLE);
									if (InfoDownSlider != null)
										((View) InfoDownSlider).setVisibility(View.INVISIBLE);
									if (cacheNameView != null)
										((View) cacheNameView).setVisibility(View.INVISIBLE);
								}
							}
						});

					}
				};
				timer.schedule(task, 50);

			}

			@Override
			public void firstShow() {
				if (mustRunSearch) {
					Log.info(log, "mustRunSearch");
					if (ExtSearch_GcCode != null)
						startSearchTimer();
					if (ExtSearch_GpxPath != null)
						startGPXImport();
				}

			}

			@Override
			public void dayNightSwitched() {

				Global.InitIcons(mainActivity);
				Global.initTheme(mainActivity);
				if (aktViewId == ViewConst.DESCRIPTION_VIEW || aktTabViewId == ViewConst.DESCRIPTION_VIEW) {
					if (descriptionView.getVisibility() == View.VISIBLE) {
						if (aktView == descriptionView) {
							hide(ViewConst.DESCRIPTION_VIEW);
							descriptionView = null;

						}
					}

				}

			}

			@Override
			public void setContentSize(final int left, final int top, final int right, final int bottom) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						Log.debug(log, "Set Android Content Sizeleft/top/right/bottom :" + String.valueOf(left) + "/" + String.valueOf(top) + "/" + String.valueOf(right) + "/" + String.valueOf(bottom));

						// set Content size

						if (aktView != null) {
							RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) frame.getLayoutParams();
							paramsLeft.setMargins(left, top, right, bottom);
							frame.setLayoutParams(paramsLeft);
							frame.requestLayout();
						} else if (aktTabView != null) {
							if (tabFrame != null) {
								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabFrame.getLayoutParams();

								LinearLayout.LayoutParams paramsLeft = (LinearLayout.LayoutParams) frame.getLayoutParams();
								params.setMargins(left - paramsLeft.width, top, right, bottom);
								tabFrame.setLayoutParams(params);
								tabFrame.requestLayout();
							}
						} else {
							Log.debug(log, "ActView & aktTabView == NULL");
						}
					}
				});
			}

		});

		// set AndroidClipboard
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		AndroidClipboard acb = new AndroidClipboard(cm);

		if (cm != null)
			GlobalCore.setDefaultClipboard(acb);

		CB_Android_FileExplorer fileExplorer = new CB_Android_FileExplorer(this);
		PlatformConnector.setGetFileListener(fileExplorer);
		PlatformConnector.setGetFolderListener(fileExplorer);

		PlatformConnector.setQuitListener(new IQuit() {

			@Override
			public void Quit() {
				if (GlobalCore.isSetSelectedCache()) {
					// speichere selektierten Cache, da nicht alles über die
					// SelectedCacheEventList läuft
					Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
					Config.AcceptChanges();
					Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
				}
				finish();
			}
		});

		PlatformConnector.setGetApiKeyListener(new IGetApiKey() {

			@Override
			public void getApiKey() {
				GetApiAuth();
			}
		});

		PlatformConnector.setsetScreenLockTimeListener(new IsetScreenLockTime() {

			@Override
			public void setScreenLockTime(int value) {
				setScreenLockTimerNew(value);
			}
		});

		PlatformConnector.setCallUrlListener(new ICallUrl() {

			/**
			 * call
			 */
			@Override
			public void call(String url) {
				try {
					if (url.startsWith("www.")) {
						url = "http://" + url;
					}
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
					main.mainActivity.startActivity(browserIntent);
				} catch (Exception exc) {
					Toast.makeText(main.mainActivity, Translation.Get("Cann_not_open_cache_browser") + " (" + url.trim() + ")", Toast.LENGTH_LONG).show();
				}
			}
		});

		PlatformConnector.setStartPictureApp(new iStartPictureApp() {
			@Override
			public void Start(String file) {
				Uri uriToImage = Uri.fromFile(new java.io.File(file));
				Intent shareIntent = new Intent(Intent.ACTION_VIEW);
				shareIntent.setDataAndType(uriToImage, "image/*");
				main.mainActivity.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.app_name)));
			}
		});

		PlatformSettings.setPlatformSettings(new IPlatformSettings() {

			@Override
			public void Write(SettingBase<?> setting) {
				if (androidSetting == null)
					androidSetting = main.this.getSharedPreferences(Global.PREFS_NAME, 0);
				if (androidSettingEditor == null)
					androidSettingEditor = androidSetting.edit();

				if (setting instanceof SettingBool) {
					androidSettingEditor.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
				} else if (setting instanceof SettingString) {
					androidSettingEditor.putString(setting.getName(), ((SettingString) setting).getValue());
				} else if (setting instanceof SettingInt) {
					androidSettingEditor.putInt(setting.getName(), ((SettingInt) setting).getValue());
				}

				// Commit the edits!
				androidSettingEditor.commit();
			}

			@Override
			public SettingBase<?> Read(SettingBase<?> setting) {
				if (androidSetting == null)
					androidSetting = main.this.getSharedPreferences(Global.PREFS_NAME, 0);

				if (setting instanceof SettingString) {
					String value = androidSetting.getString(setting.getName(), ((SettingString) setting).getDefaultValue());
					((SettingString) setting).setValue(value);
				} else if (setting instanceof SettingBool) {
					boolean value = androidSetting.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
					((SettingBool) setting).setValue(value);
				} else if (setting instanceof SettingInt) {
					int value = androidSetting.getInt(setting.getName(), ((SettingInt) setting).getDefaultValue());
					((SettingInt) setting).setValue(value);
				}
				setting.clearDirty();
				return setting;
			}
		});

	}

	IgetFileReturnListener getFileReturnListener = null;
	IgetFolderReturnListener getFolderReturnListener = null;

	// ########### Reload CacheInfo ##########################

	private SharedPreferences androidSetting;
	private SharedPreferences.Editor androidSettingEditor;

	// #########################################################
	public void GetApiAuth() {
		Intent gcApiLogin = new Intent().setClass(mainActivity, GcApiLogin.class);
		mainActivity.startActivityForResult(gcApiLogin, Global.REQUEST_CODE_GET_API_KEY);
	}

	// ###########################################################

	/**
	 * Initial all Locator functions
	 */
	private void initialLocatorBase() {
		// ##########################################################
		// initial Locator with saved Location
		// ##########################################################
		double latitude = -1000;
		double longitude = -1000;

		if (Config.settings != null) {
			try {
				latitude = Config.MapInitLatitude.getValue();
				longitude = Config.MapInitLongitude.getValue();
			} catch (Exception e) {
			}
		} else {
			// reload config
			// TODO
		}

		ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;

		CB_Locator.Location initialLocation;

		if (provider == ProviderType.Saved) {
			initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
		} else {
			initialLocation = CB_Locator.Location.NULL_LOCATION;
		}

		new CB_Locator.Locator(initialLocation);

		// ##########################################################
		// initial settings changed handling
		// ##########################################################

		// Use Imperial units?
		CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
		Config.ImperialUnits.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});

		// GPS update time?
		CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
		Config.gpsUpdateTime.addChangedEventListener(new IChanged() {

			@Override
			public void isChanged() {
				CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
			}
		});

		// Use magnetic Compass?
		CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
		Config.HardwareCompass.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
			}
		});

		// Magnetic compass level
		CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
		Config.HardwareCompassLevel.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
			}
		});
	}

	private final CB_List<CB_Locator.GpsStrength> coreSatList = new CB_List<CB_Locator.GpsStrength>(14);

	@Override
	public void onGpsStatusChanged(int event) {
		if (locationManager == null)
			return;

		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {

			GpsStatus status = locationManager.getGpsStatus(null);
			Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

			int satellites = 0;
			int fixed = 0;
			coreSatList.clear();

			while (statusIterator.hasNext()) {
				GpsSatellite sat = statusIterator.next();
				satellites++;

				// satellite signal strength

				if (sat.usedInFix()) {
					fixed++;
					// Log.d("Cachbox satellite signal strength", "Sat #" +
					// satellites + ": " + sat.getSnr() + " FIX");
					// SatList.add(new GpsStrength(true, sat.getSnr()));
					coreSatList.add(new GpsStrength(true, sat.getSnr()));
				} else {
					// Log.d("Cachbox satellite signal strength", "Sat #" +
					// satellites + ": " + sat.getSnr());
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
			if (fixed < 1 && (Locator.isFixed())) {

				if (!losseChek) {
					Timer timer = new Timer();
					TimerTask task = new TimerTask() {
						@Override
						public void run() {
							if (CB_Locator.GPS.getFixedSats() < 1)
								Locator.FallBack2Network();
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

/*
 * Copyright (C) 2014-2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox;

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
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Locator.GpsStrength;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Locator.CompassType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.*;
import CB_UI.GL_UI.Activitys.FZKDownload;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog.SearchMode;
import CB_UI.GL_UI.Main.Actions.Action_MapDownload;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GL_UI.Views.MainViewInit;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.*;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Input;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.ViewID;
import CB_UI_Base.GL_UI.ViewID.UI_Pos;
import CB_UI_Base.GL_UI.ViewID.UI_Type;
import CB_UI_Base.Math.*;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.Log;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Plattform;
import CB_Utils.Settings.*;
import CB_Utils.Settings.PlatformSettings.IPlatformSettings;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.hardware.*;
import android.hardware.Camera.Parameters;
import android.location.*;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.android.*;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import de.cb.sqlite.AndroidDB;
import de.cb.sqlite.SQLiteClass;
import de.cb.sqlite.SQLiteInterface;
import de.droidcachebox.CB_Texturepacker.Android_Packer;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Custom_Controls.DownSlider;
import de.droidcachebox.Custom_Controls.Mic_On_Flash;
import de.droidcachebox.Custom_Controls.QuickButtonList.HorizontalListView;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AndroidContentClipboard;
import de.droidcachebox.Ui.AndroidTextClipboard;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.Forms.GcApiLogin;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.PleaseWaitMessageBox;
import de.droidcachebox.Views.ViewGL;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static CB_Core.Api.GroundspeakAPI.GetSettingsAccessToken;
import static android.content.Intent.ACTION_VIEW;

@SuppressWarnings("deprecation")
public class Main extends AndroidApplication implements SelectedCacheChangedEventListener, LocationListener, GpsStatus.NmeaListener, GpsStatus.Listener, CB_UI_Settings {
    public static final int REQUEST_FROM_SPLASH = 987654322;
    private static final String sKlasse = "Main";
    private static final int REQUEST_CAPTURE_IMAGE = 61216516;
    private static final int REQUEST_CAPTURE_VIDEO = 61216517;
    private static final int REQUEST_GET_APIKEY = 987654321;
    public static AndroidApplication mainActivity;
    public static LinearLayout strengthLayout;
    private static DescriptionView descriptionView = null;
    private static Boolean isRestart = false;
    private static ViewID aktViewId = null;
    private static ViewID aktTabViewId = null;
    private static LocationManager locationManager;
    private static CB_Locator.Location recordingStartCoordinate;
    private static boolean mVoiceRecIsStart = false;
    private final ArrayList<ViewOptionsMenu> ViewList = new ArrayList<>();
    private final AtomicBoolean waitForGL = new AtomicBoolean(false);
    private final CB_List<CB_Locator.GpsStrength> coreSatList = new CB_List<>(14);
    private SensorEventListener mSensorEventListener;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private AndroidApplicationConfiguration gdxConfig;
    private Uri videoUri;
    private AndroidEventListener handlingRecordedVideo, handlingTakePhoto, handlingGetApiAuth;
    private OnTouchListener onTouchListener;
    private HorizontalListView QuickButtonList;
    private DownSlider downSlider;
    private PowerManager.WakeLock wakeLock;
    private CancelWaitDialog wd;
    private int horizontalListViewHeigt;
    private boolean mustShowCacheList = true;
    private View gdxView;
    private String recordingStartTime;
    private String mediaFileNameWithoutExtension;
    private String tempMediaPath;
    private LayoutInflater inflater;
    private ExtAudioRecorder extAudioRecorder = null;
    private FrameLayout frame;
    private FrameLayout tabFrame;
    private FrameLayout GlFrame;
    private LinearLayout TopLayout;
    private String ExternalRequestGCCode;
    private String ExternalRequestGpxPath;
    private String ExternalRequestMapDownloadPath;
    private String ExternalRequestLatLon;
    private String ExternalRequestGuid;
    private String ExternalRequestName;
    private Mic_On_Flash Mic_Icon;
    private ViewOptionsMenu aktView = null;
    private ViewOptionsMenu aktTabView = null;
    private CacheNameView cacheNameView;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SharedPreferences androidSetting;
    private SharedPreferences.Editor androidSettingEditor;
    private boolean lostCheck = false;
    private IChanged handleSuppressPowerSavingConfigChanged, handleRunOverLockScreenConfigChanged, handleGpsUpdateTimeConfigChanged, handleImperialUnitsConfigChanged;
    private Dialog pWaitD;
    private LastState lastState;

    public Main() {
        gdxConfig = new AndroidApplicationConfiguration();
        gdxConfig.numSamples = 2;
        gdxConfig.useAccelerometer = true;
        gdxConfig.useCompass = true;
        screenBroadcastReceiver = new ScreenBroadcastReceiver();
        mSensorEventListener = new SensorEventListener() {
            private final float orientationValues[] = new float[3];
            private final float R[] = new float[9];
            private final float I[] = new float[9];
            private final float minChange = 0.5f;
            private final long updateTime = 15;
            private final RingBufferFloat ringBuffer = new RingBufferFloat(15);
            private float[] gravity;
            private float[] geomagnetic;
            private long lastUpdateTime = 0;
            private float orientation;
            private float lastOrientation;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                long now = System.currentTimeMillis();
                if (lastUpdateTime == 0 || lastUpdateTime + updateTime < now) {
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        geomagnetic = event.values;
                        if (gravity != null && geomagnetic != null) {
                            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                                SensorManager.getOrientation(R, orientationValues);
                                orientation = ringBuffer.add((float) Math.toDegrees(orientationValues[0]));
                                while (orientation < 0) {
                                    orientation += 360;
                                }

                                while (orientation > 360) {
                                    orientation -= 360;
                                }

                                if (Math.abs(lastOrientation - orientation) > minChange) {
                                    Locator.getInstance().setHeading(orientation, CompassType.Magnetic);
                                    // sKlasse.debug("orientation: {}", orientation);
                                    lastOrientation = orientation;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mainActivity = this;

        handleImperialUnitsConfigChanged = () -> Locator.getInstance().setUseImperialUnits(Config.ImperialUnits.getValue());
        handleRunOverLockScreenConfigChanged = this::handleRunOverLockScreenConfig;
        handleGpsUpdateTimeConfigChanged = () -> {
            int updateTime1 = Config.gpsUpdateTime.getValue();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime1, 1, Main.this);
            } catch (SecurityException sex) {
                Log.err(sKlasse, "Config.gpsUpdateTime changed: " + sex.getLocalizedMessage());
            }
        };
        handleSuppressPowerSavingConfigChanged = () -> {
            if (Config.SuppressPowerSaving.getValue()) {
                setWakeLockLevelToScreenBright();
            } else {
                setWakeLockLevelToOnlyCPUOn();
            }
        };
        onTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                v.performClick();
                return sendMotionEvent(event);
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GlobalCore.RunFromSplash) {
            if (savedInstanceState != null) {
                GlobalCore.restartAfterKill = true;
                if (savedInstanceState.isEmpty()) {
                    Log.info(sKlasse, "=> onCreate; savedInstanceState is empty");
                } else {
                    Log.info(sKlasse, "=> onCreate; initializations from savedInstanceState");
                    // ? everything, that is initialized in Splash
                    GlobalCore.useSmallSkin = savedInstanceState.getBoolean("useSmallSkin");
                    new Config(savedInstanceState.getString("WorkPath"));
                    if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
                        return;
                    Database.Settings = new AndroidDB(DatabaseType.Settings, this);
                    Database.Settings.StartUp(Config.mWorkPath + "/User/Config.db3");
                    Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
                    Database.Drafts = new AndroidDB(DatabaseType.Drafts, this);

                    Resources res = getResources();
                    DevicesSizes ui = new DevicesSizes();
                    ui.Window = new Size(savedInstanceState.getInt("WindowWidth"), savedInstanceState.getInt("WindowHeight"));
                    ui.Density = res.getDisplayMetrics().density;
                    ui.isLandscape = false;
                    UiSizes.getInstance().initialize(ui);

                    Global.Paints.init(this);
                    Global.initIcons(this);

                    GlobalCore.restartCache = savedInstanceState.getString("selectedCacheID");
                    GlobalCore.restartWaypoint = savedInstanceState.getString("selectedWayPoint");
                }
            } else {
                Log.info(sKlasse, "=> onCreate first start");
                GlobalCore.restartAfterKill = false;
            }

            // registerReceiver receiver for screen switched on/off
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenBroadcastReceiver, intentFilter);

            ActivityUtils.onActivityCreateSetTheme(this);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            setContentView(R.layout.main);

            findViewsById();

            Config.SuppressPowerSaving.addSettingChangedListener(handleSuppressPowerSavingConfigChanged);
            Config.RunOverLockScreen.addSettingChangedListener(handleRunOverLockScreenConfigChanged);
            Config.gpsUpdateTime.addSettingChangedListener(handleGpsUpdateTimeConfigChanged);
            Config.ImperialUnits.addSettingChangedListener(handleImperialUnitsConfigChanged);
            initPlatformConnector();

            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            // initialize GL the gdx ApplicationListener (Window Size, load Sprites, ...)
            int width = UI_Size_Base.ui_size_base.getWindowWidth();
            int height = UI_Size_Base.ui_size_base.getWindowHeight();
            CB_RectF rec = new CB_RectF(0, 0, width, height);
            new GL(width, height, new MainViewInit(rec), new ViewManager(rec));
            GL.that.textInput = new Android_TextInput(this);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            assert mSensorManager != null;
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            // Initial Android TexturePacker
            new Android_Packer();

            initialLocationManager();

            initalMicIcon();

            GL.that.onStart();

            if (tabFrame != null)
                tabFrame.setVisibility(View.INVISIBLE);
            if (frame != null)
                frame.setVisibility(View.INVISIBLE);

            downSlider.invalidate();

            DownSlider.isInitialized = false;

            int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;

            setQuickButtonHeight(sollHeight);

            // ask for API key only if Rev-Number changed, like at new installation and API Key is Empty
            if (Config.newInstall.getValue() && GetSettingsAccessToken().length() == 0) {
                askToGetApiKey();
            }
            if (!GlobalCore.restartAfterKill)
                if (!GpsOn()) askToSwitchGpsOn();

            if (Config.newInstall.getValue()) {
                // wait for Copy Asset is closed
                CheckTranslationIsLoaded();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            String Welcome = "";
                            String LangId = getString(R.string.langId);
                            try {
                                Welcome = Translation.GetTextFile("welcome", LangId);

                                Welcome += Translation.GetTextFile("changelog", LangId);
                            } catch (IOException ignored) {
                            }
                            MessageBox.Show(Welcome, Translation.get("welcome"), MessageBoxIcon.None);
                        });
                    }
                }, 5000);

            }


            initializeGDXAndroidApplication();
            if (input == null) {
                Log.info(sKlasse, "initialize input");
                // should be != null : initialized by gdxview = initializeForView(GL.that, gdxConfig); in initializeGDXAndroidApplication();
                graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
                input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
            }

            if (aktView != null)
                ((View) aktView).setVisibility(View.INVISIBLE);
            if (aktTabView != null)
                ((View) aktTabView).setVisibility(View.INVISIBLE);
            if (downSlider != null)
                (downSlider).setVisibility(View.INVISIBLE);
            if (cacheNameView != null)
                (cacheNameView).setVisibility(View.INVISIBLE);

            checkExternalRequest();

            // static Event Lists
            SelectedCacheChangedEventListeners.getInstance().add(this);

            if (Config.SuppressPowerSaving.getValue()) {
                setWakeLockLevelToScreenBright();
            }

            Config.AcceptChanges();

        } else {
            restartFromSplash();
        }
        Log.info(sKlasse, "onCreate <=");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.info(sKlasse, "=> onSaveInstanceState");
        savedInstanceState.putBoolean("useSmallSkin", GlobalCore.useSmallSkin);
        savedInstanceState.putString("WorkPath", Config.mWorkPath);

        savedInstanceState.putInt("WindowWidth", UI_Size_Base.ui_size_base.getWindowWidth());
        savedInstanceState.putInt("WindowHeight", UI_Size_Base.ui_size_base.getWindowHeight());

        if (GlobalCore.isSetSelectedCache())
            savedInstanceState.putString("selectedCacheID", GlobalCore.getSelectedCache().getGcCode());
        if (GlobalCore.getSelectedWaypoint() != null)
            savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWaypoint().getGcCode());

        super.onSaveInstanceState(savedInstanceState);
        Log.info(sKlasse, "onSaveInstanceState <=");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // setIntent(intent) here to make future calls (from external) to getIntent() get the most recent Intent data
        // is not necessary for us, I think
        checkExternalRequest();
        Log.info(sKlasse, "=> onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        Log.info(sKlasse, "=> onStop");

        if (mSensorManager != null)
            mSensorManager.unregisterListener(mSensorEventListener);

        super.onStop();

        if (Config.SuppressPowerSaving.getValue()) {
            setWakeLockLevelToOnlyCPUOn();
        }

        Log.info(sKlasse, "onStop <=");
        lastState = LastState.onStop;
    }

    @Override
    protected void onResume() {
        Log.info(sKlasse, "=> onResume");

        if (GL.that == null) {
            Log.err("onResume", "GL.that == null");
            restartFromSplash();
        } else {
            if (GL.that.mGL_Listener_Interface == null) {
                Log.err("onResume", "mGL_Listener_Interface == null");
                restartFromSplash();
            }
            GL.that.restartRendering(); // does ViewGL.RenderContinous();
            if (lastState == LastState.onStop) {
                Log.info(sKlasse, "Resume from Stop");
                showWaitToRenderStarted();
                invalidateTextureEventList.Call();
            }
        }

        if (input == null) {
            Log.info(sKlasse, "(input == null) : init input needed for super.onPause()");
            graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
            input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
        }

        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(mSensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

        int lQuickButtonHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
        setQuickButtonHeight(lQuickButtonHeight);
        DownSlider.isInitialized = false;
        downSlider.invalidate();

        if (wakeLock != null) wakeLock.acquire();

        if (gdxView == null) {
            Log.err("", "gdx view nicht initialisiert");
        } else {
            gdxView.setOnTouchListener(onTouchListener);
        }

        Log.info(sKlasse, "onResume <=");
        lastState = LastState.onResume;

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.info(sKlasse, "=> onPause");

        if (isFinishing()) {
            Log.info(sKlasse, "is completely Finishing()");
        }

        if (wakeLock != null) wakeLock.release();

        if (input == null) {
            Log.info(sKlasse, "(input == null) : init input needed for super.onPause()");
            graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
            input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
        }
        super.onPause();
        Log.info(sKlasse, "onPause <=");
    }

    @Override
    public void onDestroy() {
        Log.info(sKlasse, "=> onDestroy AndroidApplication");
        try {
            PlatformConnector.addToMediaScannerList(Config.DraftsGarminPath.getValue());
            PlatformConnector.addToMediaScannerList(CB_SLF4J.logfile);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            for (String fn : PlatformConnector.getMediaScannerList()) {
                intent.setData(Uri.fromFile(new java.io.File(fn)));
                sendBroadcast(intent);
                Log.info(sKlasse, "Send " + fn + " to MediaScanner.");
            }
        } catch (Exception e) {
            Log.err(sKlasse, "Send files to MediaScanner: " + e.getMessage());
        }

        try {
            if (screenBroadcastReceiver != null)
                unregisterReceiver(screenBroadcastReceiver);
        } catch (Exception ignored) {
        }
        screenBroadcastReceiver = null;

        if (isRestart) {
            Log.info(sKlasse, "isRestart");
            super.onDestroy();
            isRestart = false;
        } else {
            if (isFinishing()) {
                Log.info(sKlasse, "isFinishing");
                if (GlobalCore.RunFromSplash) {
                    Config.settings.WriteToDB();

                    if (wakeLock != null) wakeLock.release();

                    TrackRecorder.StopRecording();
                    // GPS Verbindung beenden
                    locationManager.removeUpdates(this);
                    // Voice Recorder stoppen
                    if (extAudioRecorder != null) {
                        extAudioRecorder.stop();
                        extAudioRecorder.release();
                        extAudioRecorder = null;
                    }
                    SelectedCacheChangedEventListeners.getInstance().clear();
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
                    descriptionView = null;
                    mainActivity = null;

                    downSlider = null;

                    Config.AcceptChanges();

                    Database.Data.sql.close();
                    Database.Drafts.sql.close();

                    Sprites.destroyCache();

                    Database.Settings.sql.close();

                }

                super.onDestroy();
                if (GlobalCore.RunFromSplash)
                    System.exit(0);
            } else {
                Log.info(sKlasse, "isFinishing==false");

                if (aktView != null)
                    aktView.OnHide();
                if (aktTabView != null)
                    aktTabView.OnHide();

                Database.Settings.sql.close();
                Database.Data.sql.close();
                Database.Drafts.sql.close();

                super.onDestroy();
            }
        }
        Log.info(sKlasse, "onDestroy AndroidApplication <=");
        lastState = LastState.onDestroy;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.info(sKlasse, "=> onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.info(sKlasse, "=> onPrepareOptionsMenu");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.info(sKlasse, "=> onOptionsItemSelected");
        if (aktView != null)
            return aktView.ItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        Log.info(sKlasse, "=> onCreateContextMenu");
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
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        Log.info(sKlasse, "=> selectedCacheChanged");
        float distance = cache.Distance(CalculationType.FAST, false);
        if (waypoint != null) {
            distance = waypoint.Distance();
        }
        if (distance > Config.SoundApproachDistance.getValue()) {
            runOnUiThread(() -> GlobalCore.switchToCompassCompleted = false);
        }
    }

    @Override
    public void onLocationChanged(Location androidLocation) {
        // Log.info(sKlasse, "=> onLocationChanged"); // is fired often from Android LocationListener
        ProviderType provider = ProviderType.NULL;

        if (androidLocation.getProvider().toLowerCase(new Locale("en")).contains("gps"))
            provider = ProviderType.GPS;
        if (androidLocation.getProvider().toLowerCase(new Locale("en")).contains("network"))
            provider = ProviderType.Network;

        CB_Locator.Location cbLocation = new CB_Locator.Location(androidLocation.getLatitude(), androidLocation.getLongitude(), androidLocation.getAccuracy());
        cbLocation.setHasSpeed(androidLocation.hasSpeed());
        cbLocation.setSpeed(androidLocation.getSpeed());
        cbLocation.setHasBearing(androidLocation.hasBearing());
        cbLocation.setBearing(androidLocation.getBearing());
        cbLocation.setAltitude(androidLocation.getAltitude());
        cbLocation.setProvider(provider);

        Locator.getInstance().setNewLocation(cbLocation);
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
    public void onNmeaReceived(long timestamp, String nmea) {
        // Override onNmeaReceived in GpsStatus package android.location;
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
                    // Log.info(sKlasse, "AltCorrection: " + String.valueOf(altCorrection));
                    Locator.getInstance().setAltCorrection(altCorrection);
                    // Höhenkorrektur ändert sich normalerweise nicht, einmal auslesen reicht...
                    locationManager.removeNmeaListener(this);
                } catch (Exception ignored) {
                    // keine Höhenkorrektur vorhanden
                }
            }
        } catch (Exception e) {
            Log.err(sKlasse, "main.onNmeaReceived()", "", e);
        }
    }

    private void restartFromSplash() {
        mainActivity = null;
        Log.info(sKlasse, "=> Must restart from splash!");
        Intent splashIntent = new Intent().setClass(this, Splash.class);
        startActivity(splashIntent);
        finish();
    }

    private void askToGetApiKey() {
        MessageBox.Show(Translation.get("wantApi"), Translation.get("welcome"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live,
                (dialog, button) -> {
                    switch (button) {
                        case -1:
                            // yes get Api key
                            getApiKey();
                            break;
                        case -2:
                            // now, we check GPS
                            askToSwitchGpsOn();
                            break;
                        case -3:

                            break;
                    }
                    dialog.dismiss();
                });
    }

    private void checkExternalRequest() {
        Log.info(sKlasse, "prepared Request from splash");
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ExternalRequestGCCode = extras.getString("GcCode");
            ExternalRequestGpxPath = extras.getString("GpxPath");
            ExternalRequestMapDownloadPath = extras.getString("MapDownloadPath");
            ExternalRequestLatLon = extras.getString("LatLon");
            ExternalRequestGuid = extras.getString("Guid");
            ExternalRequestName = extras.getString("Name");

            if (ExternalRequestGCCode != null
                    || ExternalRequestGpxPath != null
                    || ExternalRequestGuid != null
                    || ExternalRequestLatLon != null
                    || ExternalRequestMapDownloadPath != null
                    || ExternalRequestName != null) {
                if (ViewManager.that.isInitialized()) {
                    // calls handleExternalRequest()
                    PlatformConnector.handleExternalRequest();
                }
            }
            // delete handled extras
            getIntent().removeExtra("GcCode");
            getIntent().removeExtra("GpxPath");
            getIntent().removeExtra("MapDownloadPath");
            getIntent().removeExtra("LatLon");
            getIntent().removeExtra("Guid");
            getIntent().removeExtra("Name");
        }
    }

    @SuppressLint("WakelockTimeout")
    private void setWakeLockLevelToScreenBright() {
        // Keep the device awake until OnStop() (=destroy) is called. remove there
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CacheBox:WakeLock");
        wakeLock.acquire();
    }

    @SuppressLint("WakelockTimeout")
    private void setWakeLockLevelToOnlyCPUOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CacheBox:PartialWakeLock");
        wakeLock.acquire();
    }

    private void showWaitToRenderStarted() {
        if (!GL.that.getAllisInitialized())
            return;

        if (pWaitD == null) {

            pWaitD = PleaseWaitMessageBox.Show(Translation.get("waitForGL"), "", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);

            waitForGL.set(true);

            GL.that.RunOnGL(() -> {
                pWaitD.dismiss();
                pWaitD = null;
                waitForGL.set(false);
            });

            new Thread(() -> {
                while (waitForGL.get()) {
                    GL.that.renderOnce(true);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
    }

    private void handleRunOverLockScreenConfig() {
        // add flags for run over lock screen
        runOnUiThread(() -> {
            Window window = getWindow();
            if (window != null) {
                if (Config.RunOverLockScreen.getValue()) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                }
            }
        });
    }

    /*
    private void logDBInfo() {
        if (Database.Data != null) {
            if (Database.Data.cacheList != null) {
                int no = Database.Data.cacheList.size();
                Log.info(sKlasse, "Number of geocaches: " + no + " in " + Database.Data.getDatabasePath());
            } else {
                Log.info(sKlasse, "Number of geocaches: 0 (null)" + " in " + Database.Data.getDatabasePath());
            }
        } else {
            Log.info(sKlasse, "Database not initialized.");
        }
    }
     */

    private ViewOptionsMenu getView(ViewID ID) {
        // first check if view on List
        if (ID.getID() < ViewList.size()) {
            return ViewList.get(ID.getID());
        }

        if (ID == ViewConst.DESCRIPTION_VIEW) {
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
        }
    }

    private void showView(ViewOptionsMenu view, ViewID ID) {

        if (aktView != null) {
            aktView.OnHide();

            if (ID.getType() == UI_Type.OpenGl) {
                Log.info(sKlasse, "showView OpenGl onPause");
                onPause();
            }

            if (aktView.equals(descriptionView)) {
                aktView = null;
                descriptionView.OnHide();
            }

        }

        if (ID.getType() == UI_Type.OpenGl) {

            ShowViewGL();
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

        downSlider.invalidate();
        ((View) aktView).forceLayout();

        if (aktView != null)
            ((View) aktView).setVisibility(View.VISIBLE);
        if (aktTabView != null)
            ((View) aktTabView).setVisibility(View.VISIBLE);
        if (downSlider != null)
            ((View) downSlider).setVisibility(View.INVISIBLE);
        if (cacheNameView != null)
            ((View) cacheNameView).setVisibility(View.INVISIBLE);

    }

    private void ShowViewGL() {
        Log.info(sKlasse, "ShowViewGL " + GlFrame.getMeasuredWidth() + "/" + GlFrame.getMeasuredHeight());

        initializeGDXAndroidApplication();

        GL.that.onStart();
        GL.that.setGLViewID();

        if (aktTabViewId != null && aktTabViewId.getType() == ViewID.UI_Type.OpenGl) {
            tabFrame.setVisibility(View.INVISIBLE);
        }

        if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.OpenGl) {
            frame.setVisibility(View.INVISIBLE);
        }

        downSlider.invalidate();

    }

    private void findViewsById() {
        QuickButtonList = (HorizontalListView) findViewById(R.id.main_quick_button_list);
        TopLayout = (LinearLayout) findViewById(R.id.layoutTop);
        frame = (FrameLayout) findViewById(R.id.layoutContent);
        tabFrame = (FrameLayout) findViewById(R.id.tabletLayoutContent);
        GlFrame = (FrameLayout) findViewById(R.id.layoutGlContent);

        downSlider = (DownSlider) findViewById(R.id.downSlider);

        Mic_Icon = (Mic_On_Flash) findViewById(R.id.mic_flash);

        cacheNameView = (CacheNameView) findViewById(R.id.main_cache_name_view);

        strengthLayout = (LinearLayout) findViewById(R.id.main_strength_control);

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
             * darher reicht es wenn er alle 10sec einen wert liefert, wenn der
             * alte um 500m abweicht. Beim GPS Provider habe ich die
             * Aktualiesierungszeit verkürzt, damit bei deaktiviertem Hardware
             * Kompass aber die Werte trotzdem noch in einem gesunden Verhältnis
             * zwichen Performance und Stromverbrauch, geliefert werden. Andere
             * apps haben hier 0.
             */

            int updateTime = Config.gpsUpdateTime.getValue();

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 300, this);
                locationManager.addNmeaListener(this); //
                locationManager.addGpsStatusListener(this);
            } catch (SecurityException sex) {
                Log.err(sKlasse, "Config.gpsUpdateTime changed: " + sex.getLocalizedMessage());
            }

        } catch (Exception e) {
            Log.err(sKlasse, "main.initialLocationManager()", "", e);
            e.printStackTrace();
        }

    }

    private void initializeGDXAndroidApplication() {
        try {
            Log.info(sKlasse, "initialize GDXAndroidApplication (gdxView = graphics.getView()");
            gdxView = initializeForView(GL.that, gdxConfig);
            int GlSurfaceType = -1;
            if (gdxView instanceof GLSurfaceView20)
                GlSurfaceType = ViewGL.GLSURFACE_VIEW20;
            else if (gdxView instanceof GLSurfaceView)
                GlSurfaceType = ViewGL.GLSURFACE_GLSURFACE;
            ViewGL.setSurfaceType(GlSurfaceType);
            switch (GlSurfaceType) {
                case ViewGL.GLSURFACE_VIEW20:
                    ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                    break;
                case ViewGL.GLSURFACE_GLSURFACE:
                    ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                    break;
            }

            gdxView.setOnTouchListener(onTouchListener);

            if (GL.that != null) {
                if (GL.that.mGL_Listener_Interface == null)
                    new ViewGL(this, inflater, gdxView, GL.that);
            }
            else {
                Log.err(sKlasse, "GL is null");
                // todo shutdown?
            }

            GlFrame.removeAllViews();
            ViewParent parent = gdxView.getParent();
            if (parent != null) {
                // aktView ist noch gebunden, also lösen
                ((RelativeLayout) parent).removeAllViews();
            }
            GlFrame.addView(gdxView);

            // }
        } catch (Exception e) {
            Log.err(sKlasse, "main.initialViewGL()", "", e);
        }

    }

    public boolean sendMotionEvent(final MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

        try {
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_DOWN:
                    GL_Input.that.onTouchDownBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    GL_Input.that.onTouchDraggedBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex));
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    GL_Input.that.onTouchUpBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
                    break;
            }
        } catch (Exception e) {
            Log.err(sKlasse, "sendMotionEvent", e);
            return true;
        }
        return true;
    }

    public boolean getVoiceRecIsStart() {
        return mVoiceRecIsStart;
    }

    public void setVoiceRecIsStart(boolean value) {
        mVoiceRecIsStart = value;
        if (mVoiceRecIsStart) {
            Mic_Icon.SetOn();
        } else { // Aufnahme stoppen
            Mic_Icon.SetOff();
            if (extAudioRecorder != null) {
                extAudioRecorder.stop();
                extAudioRecorder.release();
                extAudioRecorder = null;
                Toast.makeText(this, "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
            }
        }
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
        Log.info(sKlasse, "takePhoto start " + GlobalCore.getSelectedCache());
        try {
            // define the file-name to save photo taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sKlasse, "can't create " + directory);
                return;
            }
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
                cacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
            } else {
                cacheName = "Image";
            }
            mediaFileNameWithoutExtension = Global.GetDateTimeString() + " " + cacheName;
            tempMediaPath = getExternalFilesDir("User/Media").getAbsolutePath() + "/"; // oder Environment.DIRECTORY_PICTURES
            if (!FileIO.createDirectory(tempMediaPath)) {
                Log.err(sKlasse, "can't create " + tempMediaPath);
                return;
            }
            String tempMediaPathAndName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
            try {
                FileFactory.createFile(tempMediaPathAndName).createNewFile();
            } catch (Exception e) {
                Log.err(sKlasse, "can't create " + tempMediaPathAndName + "\r" + e.getLocalizedMessage());
                return;
            }

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this, "de.droidcachebox.android.fileprovider", new java.io.File(tempMediaPathAndName));
            } else {
                uri = Uri.fromFile(new java.io.File(tempMediaPathAndName));
            }
            Log.info(sKlasse, uri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            if (intent.resolveActivity(getPackageManager()) != null) {
                if (handlingTakePhoto == null) {
                    handlingTakePhoto = (requestCode, resultCode, data) -> {
                        Main.this.removeAndroidEventListener(handlingTakePhoto);
                        // Intent Result Take Photo
                        if (requestCode == REQUEST_CAPTURE_IMAGE) {
                            if (resultCode == RESULT_OK) {
                                GL.that.RunIfInitial(() -> {
                                    Log.info(sKlasse, "Photo taken");
                                    try {
                                        // move the photo from temp to UserImageFolder
                                        String sourceName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
                                        String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + ".jpg";
                                        if (!sourceName.equals(destinationName)) {
                                            File source = FileFactory.createFile(sourceName);
                                            File destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sKlasse, "move from " + sourceName + " to " + destinationName + " failed");
                                            }
                                        }

                                        // for the photo to show within spoilers
                                        if (GlobalCore.isSetSelectedCache()) {
                                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                                            SpoilerView.getInstance().ForceReload();
                                        }

                                        ViewManager.that.reloadSprites(false);

                                        // track annotation
                                        String TrackFolder = Config.TrackFolder.getValue();
                                        String relativPath = FileIO.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                                        CB_Locator.Location lastLocation = Locator.getInstance().getLastSavedFineLocation();
                                        if (lastLocation == null) {
                                            lastLocation = Locator.getInstance().getLocation(ProviderType.any);
                                            if (lastLocation == null) {
                                                Log.info(sKlasse, "No (GPS)-Location for Trackrecording.");
                                                return;
                                            }
                                        }
                                        // Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und die Koordinaten nach der Aufnahme verwendet werden.
                                        TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + ".jpg", relativPath + "/" + mediaFileNameWithoutExtension + ".jpg", lastLocation, Global.GetTrackDateTimeString());
                                    } catch (Exception e) {
                                        Log.err(sKlasse, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sKlasse, "Intent Take Photo resultCode: " + resultCode);
                            }
                        }
                    };
                }
                addAndroidEventListener(handlingTakePhoto);
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            } else {
                Log.err(sKlasse, MediaStore.ACTION_IMAGE_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sKlasse, e.getLocalizedMessage());
        }
    }

    private void recVideo() {

        try {
            Log.info(sKlasse, "recVideo start " + GlobalCore.getSelectedCache());
            // define the file-name to save video taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sKlasse, "can't create " + directory);
                return;
            }
            mediaFileNameWithoutExtension = Global.GetDateTimeString();
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
                cacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
            } else {
                cacheName = "Video";
            }
            mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;

            // Da ein Video keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
            recordingStartTime = Global.GetTrackDateTimeString();
            recordingStartCoordinate = Locator.getInstance().getLocation(ProviderType.GPS);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "");
            videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            // Log.info(uri.toString());
            final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAXIMUM_VIDEO_SIZE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                if (handlingRecordedVideo == null)
                    handlingRecordedVideo = (requestCode, resultCode, data) -> {
                        Main.this.removeAndroidEventListener(handlingRecordedVideo);
                        // Intent Result Record Video
                        if (requestCode == REQUEST_CAPTURE_VIDEO) {
                            if (resultCode == RESULT_OK) {
                                GL.that.RunIfInitial(() -> {
                                    Log.info(sKlasse, "Video recorded.");
                                    String ext = "";
                                    try {
                                        // move Video from temp (recordedVideoFilePath) in UserImageFolder and rename
                                        String recordedVideoFilePath = "";
                                        // first get the tempfile pathAndName (recordedVideoFilePath)
                                        String[] proj = {MediaStore.Images.Media.DATA}; // want to get Path to the file on disk.

                                        Cursor cursor = Main.this.getContentResolver().query(videoUri, proj, null, null, null); // result set
                                        if (cursor != null && cursor.getCount() != 0) {
                                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); // my meaning: if only one element index is 0
                                            cursor.moveToFirst(); // first row ( here we should have only one row )
                                            recordedVideoFilePath = cursor.getString(columnIndex);
                                        }
                                        if (cursor != null) {
                                            cursor.close();
                                        }

                                        if (recordedVideoFilePath.length() > 0) {
                                            ext = FileIO.getFileExtension(recordedVideoFilePath);

                                            File source = FileFactory.createFile(recordedVideoFilePath);
                                            String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + "." + ext;
                                            File destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sKlasse, "move from " + recordedVideoFilePath + " to " + destinationName + " failed");
                                            } else {
                                                Log.info(sKlasse, "Video saved at " + destinationName);
                                                // track annotation
                                                String TrackFolder = Config.TrackFolder.getValue();
                                                String relativPath = FileIO.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                                                TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + "." + ext, relativPath + "/" + mediaFileNameWithoutExtension + "." + ext, recordingStartCoordinate, recordingStartTime);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.err(sKlasse, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sKlasse, "Intent Record Video resultCode: " + resultCode);
                            }
                        }
                    };
                addAndroidEventListener(handlingRecordedVideo);
                startActivityForResult(intent, REQUEST_CAPTURE_VIDEO);
            } else {
                Log.err(sKlasse, MediaStore.ACTION_VIDEO_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sKlasse, e.toString());
        }
    }

    private void recVoice() {
        try {
            if (!getVoiceRecIsStart()) // Voice Recorder starten
            {
                // define the file-name to save voice taken by activity
                String directory = Config.UserImageFolder.getValue();
                if (!FileIO.createDirectory(directory)) {
                    Log.err(sKlasse, "can't create " + directory);
                    return;
                }

                mediaFileNameWithoutExtension = Global.GetDateTimeString();

                String cacheName;
                if (GlobalCore.isSetSelectedCache()) {
                    String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGcCode() + "-" + GlobalCore.getSelectedCache().getName());
                    cacheName = validName.substring(0, (validName.length() > 32) ? 32 : validName.length());
                } else {
                    cacheName = "Voice";
                }

                mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;
                extAudioRecorder = ExtAudioRecorder.getInstance(false);
                extAudioRecorder.setOutputFile(directory + "/" + mediaFileNameWithoutExtension + ".wav");
                extAudioRecorder.prepare();
                extAudioRecorder.start();

                String MediaFolder = Config.UserImageFolder.getValue();
                String TrackFolder = Config.TrackFolder.getValue();
                String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
                // Da eine Voice keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
                TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + ".wav", relativPath + "/" + mediaFileNameWithoutExtension + ".wav", Locator.getInstance().getLocation(ProviderType.GPS), Global.GetTrackDateTimeString());
                Toast.makeText(this, "Start Voice Recorder", Toast.LENGTH_SHORT).show();

                setVoiceRecIsStart(true);

            } else { // Voice Recorder stoppen
                setVoiceRecIsStart(false);
            }
        } catch (Exception ignored) {
        }
    }

    private void NavigateTo() {
        if (GlobalCore.isSetSelectedCache()) {
            double lat;
            double lon;
            String targetName;

            if (GlobalCore.getSelectedWaypoint() == null) {
                lat = GlobalCore.getSelectedCache().Latitude();
                lon = GlobalCore.getSelectedCache().Pos.getLongitude();
                targetName = GlobalCore.getSelectedCache().getGcCode();
            } else {
                lat = GlobalCore.getSelectedWaypoint().Pos.getLatitude();
                lon = GlobalCore.getSelectedWaypoint().Pos.getLongitude();
                targetName = GlobalCore.getSelectedWaypoint().getGcCode();
            }

            String selectedNavi = Config.Navis.getValue();

            Intent intent = null;
            if (selectedNavi.equals("Navigon")) {
                intent = getIntent("android.intent.action.navigon.START_PUBLIC", "");
                if (intent == null) {
                    intent = getIntent("", "com.navigon.navigator"); // get the launch-intent from package
                }
                if (intent != null) {
                    intent.putExtra("latitude", (float) lat);
                    intent.putExtra("longitude", (float) lon);
                }
            } else if (selectedNavi.equals("Orux")) {
                intent = getIntent("com.oruxmaps.VIEW_MAP_ONLINE", "");
                // from http://www.oruxmaps.com/oruxmapsmanual_en.pdf
                if (intent != null) {
                    double[] targetLat = {lat};
                    double[] targetLon = {lon};
                    String[] targetNames = {targetName};
                    intent.putExtra("targetLat", targetLat);
                    intent.putExtra("targetLon", targetLon);
                    intent.putExtra("targetName", targetNames);
                    intent.putExtra("navigatetoindex", 1);
                }
            } else if (selectedNavi.equals("OsmAnd")) {
                intent = getIntent(ACTION_VIEW, "geo:" + lat + "," + lon);
            } else if (selectedNavi.equals("OsmAnd2")) {
                intent = getIntent(ACTION_VIEW, "http://download.osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=14");
            } else if (selectedNavi.equals("Waze")) {
                intent = getIntent(ACTION_VIEW, "waze://?ll=" + lat + "," + lon);
            } else if (selectedNavi.equals("Sygic")) {
                intent = getIntent(ACTION_VIEW, "com.sygic.aura://coordinate|" + lon + "|" + lat + "|drive");
            }
            if (intent == null) {
                // "default" or "no longer existing selection" or "fallback" to google
                intent = getIntent(ACTION_VIEW, "http://maps.google.com/maps?daddr=" + lat + "," + lon);
            }
            try {
                if (intent != null)
                    startActivity(intent);
            } catch (Exception e) {
                Log.err(sKlasse, "Error Start " + selectedNavi, e);
            }
        }
    }

    private Intent getIntent(String action, String data) {
        Intent intent = null;
        try {
            if (action.length() > 0) {
                if (data.length() > 0) {
                    intent = new Intent(action, Uri.parse(data));
                } else {
                    intent = new Intent(action);
                }
            } else {
                intent = getPackageManager().getLaunchIntentForPackage(data);
            }
            if (intent != null) {
                // check if there is an activity that can handle the desired intent
                if (intent.resolveActivity(getPackageManager()) == null) {
                    intent = null;
                }
            }
            if (intent == null) {
                Log.err(sKlasse, "No intent for " + action + " , " + data);
            }
        } catch (Exception e) {
            intent = null;
            Log.err(sKlasse, "Exception: No intent for " + action + " , " + data, e);
        }
        return intent;
    }

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
    private void askToSwitchGpsOn() {
        try {
            if (Config.Ask_Switch_GPS_ON.getValue()) {
                CheckTranslationIsLoaded();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.Show(Translation.get("GPSon?"), Translation.get("GPSoff"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, new DialogInterface.OnClickListener() {
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
            Log.err(sKlasse, "main.chkGpsIsOn()", "", e);
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

    public void showView(ViewID ID) {
        if (ID == null) {
            return;// keine Action
        }

        if (ID.getPos() != null) {
            //Pos = ID.getPos().toString();
            //Type = ID.getType().toString();
        }

        if (ID.getType() == ViewID.UI_Type.Activity) {
            showActivity(ID);
            return;
        }


        if (!(aktView == null) && ID == aktViewId) {
            aktView.OnShow();
            return;
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
            ShowViewGL();
        }

    }

    private void initPlatformConnector() {

        Plattform.used = Plattform.Android;
        PlatformConnector.AndroidVersion = Build.VERSION.SDK_INT;

        initLocatorBase();

        PlatformConnector.setisOnlineListener(new IHardwarStateListener() {

            private AtomicBoolean torchAvailable = null;
            private Camera deviceCamera;

            @Override
            /**
             * isOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen
             */
            public boolean isOnline() {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                if (Config.vibrateFeedback.getValue())
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(Config.VibrateTime.getValue());
            }

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
                Log.info(sKlasse, "switchToGpsMeasure()");
                int updateTime = Config.gpsUpdateTime.getValue();
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 0, Main.this);
                } catch (SecurityException sex) {
                    Log.err(sKlasse, "switchToGpsMeasure: " + sex.getLocalizedMessage());
                }
            }

            @Override
            public void switchtoGpsDefault() {
                Log.info(sKlasse, "switchtoGpsDefault()");
                int updateTime = Config.gpsUpdateTime.getValue();
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, Main.this);
                } catch (SecurityException sex) {
                    Log.err(sKlasse, "switchtoGpsDefault: " + sex.getLocalizedMessage());
                }
            }

        });

        PlatformConnector.setShowViewListener(new IShowViewListener() {

            int lastLeft, lastTop, lastRight, lastBottom;

            @Override
            public void show(final ViewID viewID, final int left, final int top, final int right, final int bottom) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.info(sKlasse, "Show View with ID = " + viewID.getID());

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
                                    if (ViewManager.leftTab != null) {
                                        versatz = (int) (ViewManager.leftTab.getWidth() - frame.getWidth());
                                    }

                                    params.setMargins(versatz + left, top, right, bottom);
                                    tabFrame.setLayoutParams(params);
                                    tabFrame.requestLayout();
                                }
                            }
                        }

                        if (downSlider != null) {
                            downSlider.ActionUp();
                            ((View) downSlider).setVisibility(View.INVISIBLE);
                        }

                        if (aktView != null)
                            ((View) aktView).setVisibility(View.VISIBLE);
                        if (aktTabView != null)
                            ((View) aktTabView).setVisibility(View.VISIBLE);
                        if (cacheNameView != null)
                            ((View) cacheNameView).setVisibility(View.INVISIBLE);

                        showView(viewID);

                    }
                });

            }

            @Override
            public void hideView(final ViewID viewID) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.info(sKlasse, "Hide View with ID = " + viewID.getID());

                        if (!(aktView == null) && viewID == aktViewId) {
                            aktView.OnHide();
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
                        if (!GL.that.isShownDialogOrActivity())
                            return;

                        if (aktView != null)
                            ((View) aktView).setVisibility(View.INVISIBLE);
                        if (aktTabView != null)
                            ((View) aktTabView).setVisibility(View.INVISIBLE);
                        if (downSlider != null)
                            ((View) downSlider).setVisibility(View.INVISIBLE);
                        if (cacheNameView != null)
                            ((View) cacheNameView).setVisibility(View.INVISIBLE);
                        handleRunOverLockScreenConfig();
                        // Log.info(sKlasse, "Show AndroidView");
                    }
                });
            }

            @Override
            public void hideForDialog() {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            // chk for timer conflict (releay set invisible)
                            // only if not showing Dialog or Activity
                            if (!GL.that.isShownDialogOrActivity()) {
                                if (aktView != null) {
                                    ((View) aktView).setVisibility(View.VISIBLE);
                                    aktView.OnShow();
                                    setContentSize(lastLeft, lastTop, lastRight, lastBottom);
                                }
                                if (aktTabView != null) {
                                    ((View) aktTabView).setVisibility(View.VISIBLE);
                                    aktTabView.OnShow();
                                    setContentSize(lastLeft, lastTop, lastRight, lastBottom);
                                }
                                if (downSlider != null)
                                    (downSlider).setVisibility(View.INVISIBLE);
                                if (cacheNameView != null)
                                    (cacheNameView).setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }, 50);
            }

            @Override
            public void handleExternalRequest() {
                if (ExternalRequestGCCode != null)
                    importCacheByGCCode();
                if (ExternalRequestGpxPath != null)
                    importGPXFile();
                if (ExternalRequestGuid != null)
                    // importCacheByGuid();
                    ;
                if (ExternalRequestLatLon != null)
                    // positionLatLon();
                    ;
                if (ExternalRequestMapDownloadPath != null) {
                    FZKDownload.getInstance().importByUrl(ExternalRequestMapDownloadPath);
                    Action_MapDownload.getInstance().Execute();
                    FZKDownload.getInstance().importByUrlFinished();
                }
                if (ExternalRequestName != null)
                    //importCacheByName();
                    ;
            }

            @Override
            public void dayNightSwitched() {

                Global.initIcons(mainActivity);
                Global.initTheme(mainActivity);
                if (aktViewId == ViewConst.DESCRIPTION_VIEW || aktTabViewId == ViewConst.DESCRIPTION_VIEW) {
                    if (descriptionView.getVisibility() == View.VISIBLE) {
                        if (aktView == descriptionView) {
                            hideView(ViewConst.DESCRIPTION_VIEW);
                            descriptionView = null;

                        }
                    }

                }

            }

            @Override
            public void setContentSize(final int left, final int top, final int right, final int bottom) {

                lastLeft = left;
                lastRight = right;
                lastTop = top;
                lastBottom = bottom;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.info(sKlasse, "Set Android Content Sizeleft/top/right/bottom :" + String.valueOf(left) + "/" + String.valueOf(top) + "/" + String.valueOf(right) + "/" + String.valueOf(bottom));

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
                            Log.info(sKlasse, "ActView & aktTabView == NULL");
                        }
                    }
                });
            }

        });

        PlatformConnector.setConnection(new IConnection() {
            @Override
            public SQLiteInterface getSQLInstance() {
                return new SQLiteClass(mainActivity);
            }

            @Override
            public void freeSQLInstance(SQLiteInterface sqlInstance) {
                sqlInstance = null;
            }
        });

        // set AndroidContentClipboard
        Object cm = getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            if (cm instanceof android.content.ClipboardManager) {
                AndroidContentClipboard acb = new AndroidContentClipboard((android.content.ClipboardManager) cm);
                GlobalCore.setDefaultClipboard(acb);
                Log.info(sKlasse, "got AndroidContentClipboard");
            } else if (cm instanceof android.text.ClipboardManager) {
                AndroidTextClipboard acb = new AndroidTextClipboard((android.text.ClipboardManager) cm);
                GlobalCore.setDefaultClipboard(acb);
                Log.info(sKlasse, "got AndroidTextClipboard");
            }
        }

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
                    Log.info(sKlasse, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
                }
                finish();
            }
        });

        PlatformConnector.setGetApiKeyListener(new IGetApiKey() {
            @Override
            public void getApiKey() {
                Main.this.getApiKey();
            }
        });

        PlatformConnector.setCallUrlListener(new ICallUrl() {

            /**
             * call
             */
            @Override
            public void call(String url) {
                try {
                    url = url.trim();
                    if (url.startsWith("www.")) {
                        url = "http://" + url;
                    }
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setDataAndType(uri, "text/html");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        Log.info(sKlasse, "Start activity for " + uri.toString());
                        mainActivity.startActivity(intent);
                    } else {
                        Log.err(sKlasse, "Activity for " + url + " not installed.");
                        Toast.makeText(mainActivity, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception exc) {
                    Log.err(sKlasse, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", exc);
                }
            }
        });

        PlatformConnector.setStartPictureApp(new iStartPictureApp() {
            @Override
            public void Start(String file) {
                Uri uriToImage = Uri.fromFile(new java.io.File(file));
                Intent shareIntent = new Intent(ACTION_VIEW);
                shareIntent.setDataAndType(uriToImage, "image/*");
                Main.mainActivity.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.app_name)));
            }
        });

        PlatformSettings.setPlatformSettings(new IPlatformSettings() {

            @Override
            public void Write(SettingBase<?> setting) {
                if (androidSetting == null)
                    androidSetting = Main.this.getSharedPreferences(Global.PreferencesNAME, 0);
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
                    androidSetting = Main.this.getSharedPreferences(Global.PreferencesNAME, 0);

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

    private void importCacheByGCCode() {
        TimerTask runTheSearchTasks = new TimerTask() {
            @Override
            public void run() {
                if (ExternalRequestGCCode != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mustShowCacheList) {
                                // show cachelist first then search dialog
                                mustShowCacheList = false;
                                ViewManager.leftTab.ShowView(CacheListView.getInstance());
                                importCacheByGCCode(); // now the search can start (doSearchOnline)
                            } else {
                                mustShowCacheList = true;
                                if (SearchDialog.that == null) {
                                    new SearchDialog();
                                }
                                SearchDialog.that.showNotCloseAutomaticly();
                                SearchDialog.that.doSearchOnline(ExternalRequestGCCode, SearchMode.GcCode);
                                ExternalRequestGCCode = null;
                            }
                        }
                    });
                }
            }
        };
        new Timer().schedule(runTheSearchTasks, 500);
    }

    private void importGPXFile() {
        TimerTask gpxImportTask = new TimerTask() {
            @Override
            public void run() {
                Log.info(sKlasse, "ImportGPXFile");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wd = CancelWaitDialog.ShowWait(Translation.get("ImportGPX"), new IcancelListener() {
                            @Override
                            public void isCanceled() {
                                wd.close();
                            }
                        }, new ICancelRunnable() {
                            @Override
                            public void run() {
                                Log.info(sKlasse, "Import GPXFile from " + ExternalRequestGpxPath + " startet");
                                Date ImportStart = new Date();
                                Importer importer = new Importer();
                                ImporterProgress ip = new ImporterProgress();

                                Database.Data.sql.beginTransaction();
                                try {
                                    importer.importGpx(ExternalRequestGpxPath, ip);
                                } catch (Exception ignored) {
                                }
                                Database.Data.sql.setTransactionSuccessful();
                                Database.Data.sql.endTransaction();

                                wd.close();
                                CacheListChangedEventList.Call();
                                FilterProperties props = FilterInstances.getLastFilter();
                                EditFilterSettings.ApplyFilter(props);

                                long ImportZeit = new Date().getTime() - ImportStart.getTime();
                                String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "Caches\n" + String.valueOf(GPXFileImporter.LogCount) + "Logs\n in " + String.valueOf(ImportZeit);
                                Log.info(sKlasse, Msg.replace("\n", "\n\r") + " from " + ExternalRequestGpxPath);
                                GL.that.Toast(Msg, 3000);
                                ExternalRequestGpxPath = null;
                            }

                            @Override
                            public boolean doCancel() {
                                // TODO handle cancel
                                return false;
                            }
                        });

                    }
                });

            }
        };

        new Timer().schedule(gpxImportTask, 500);
    }

    private void getApiKey() {
        Intent intent = new Intent().setClass(mainActivity, GcApiLogin.class);
        if (intent.resolveActivity(getPackageManager()) != null) {
            if (handlingGetApiAuth == null)
                handlingGetApiAuth = (requestCode, resultCode, data) -> {
                    Main.this.removeAndroidEventListener(handlingGetApiAuth);
                    if (requestCode == REQUEST_GET_APIKEY) {
                        GL.that.RunIfInitial(() -> SettingsActivity.resortList());
                        Config.AcceptChanges();
                    }
                };
            addAndroidEventListener(handlingGetApiAuth);
            mainActivity.startActivityForResult(intent, REQUEST_GET_APIKEY);
        } else {
            Log.err(sKlasse, "GcApiLogin class not found");
        }
    }

    private void initLocatorBase() {
        // initial Locator with saved Location
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

        Locator.getInstance().setNewLocation(initialLocation);

        // ##########################################################
        // initial settings changed handling
        // ##########################################################

        // Use Imperial units?
        try {
            Locator.getInstance().setUseImperialUnits(Config.ImperialUnits.getValue());
        } catch (Exception e) {
            Log.err(sKlasse, "Error Initial Locator.UseImperialUnits");
        }

        // GPS update time?
        try {
            Locator.getInstance().setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
            Config.gpsUpdateTime.addSettingChangedListener(() -> Locator.getInstance().setMinUpdateTime((long) Config.gpsUpdateTime.getValue()));
        } catch (Exception e) {
            Log.err(sKlasse, "Error Initial Locator.MinUpdateTime");
        }

        // Use magnetic Compass?
        try {
            Locator.getInstance().setUseHardwareCompass(Config.HardwareCompass.getValue());
            Config.HardwareCompass.addSettingChangedListener(() -> Locator.getInstance().setUseHardwareCompass(Config.HardwareCompass.getValue()));
        } catch (Exception e) {
            Log.err(sKlasse, "Error Initial Locator.UseHardwareCompass");
        }

        // Magnetic compass level
        try {
            Locator.getInstance().setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
            Config.HardwareCompassLevel.addSettingChangedListener(() -> Locator.getInstance().setHardwareCompassLevel(Config.HardwareCompassLevel.getValue()));
        } catch (Exception e) {
            Log.err(sKlasse, "Error Initial Locator.HardwareCompassLevel");
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (locationManager == null)
            return;

        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            GpsStatus status;
            try {
                status = locationManager.getGpsStatus(null);
            } catch (SecurityException sex) {
                Log.err(sKlasse, "onGpsStatusChanged: " + sex.getLocalizedMessage());
                return;
            }

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
                    // Log.d("Cachebox satellite signal strength", "Sat #" +
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
            if (fixed < 1 && (Locator.getInstance().isFixed())) {
                if (!lostCheck) {
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (CB_Locator.GPS.getFixedSats() < 1)
                                Locator.getInstance().FallBack2Network();
                            lostCheck = false;
                        }
                    };
                    timer.schedule(task, 1000);
                }

            }
        }

    }

    private enum LastState {
        onResume, onStop, onDestroy
    }

    private static class ScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Energy.setDisplayOff();
                Locator.getInstance().setDisplayOff();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Energy.setDisplayOn();
                Locator.getInstance().setDisplayOn();
            }
        }
    }

}

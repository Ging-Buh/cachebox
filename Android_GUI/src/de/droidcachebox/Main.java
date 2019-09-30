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
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GL_UI.Views.MainViewInit;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IPlatformListener;
import CB_UI_Base.Events.PlatformConnector.IShowViewListener;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.Log;
import CB_Utils.Log.LogLevel;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Plattform;
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
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.*;
import android.hardware.Camera.Parameters;
import android.location.*;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import de.cb.sqlite.AndroidDB;
import de.cb.sqlite.SQLiteClass;
import de.cb.sqlite.SQLiteInterface;
import de.droidcachebox.CB_Texturepacker.AndroidTexturePacker;
import de.droidcachebox.Custom_Controls.QuickButtonList.HorizontalListView;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AndroidContentClipboard;
import de.droidcachebox.Ui.AndroidTextClipboard;
import de.droidcachebox.Views.Forms.GcApiLogin;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.PleaseWaitMessageBox;
import de.droidcachebox.Views.ShowViewListener;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static CB_Core.Api.GroundspeakAPI.GetSettingsAccessToken;
import static android.content.Intent.ACTION_VIEW;

@SuppressWarnings("deprecation")
public class Main extends AndroidApplication implements SelectedCacheChangedEventListener, LocationListener, GpsStatus.NmeaListener, GpsStatus.Listener, CB_UI_Settings {
    private static final String sKlasse = "Main";
    private static final int REQUEST_GET_APIKEY = 987654321;
    public static Main mainActivity;
    private static Boolean isRestart = false;
    private static LocationManager locationManager;
    private final AtomicBoolean waitForGL = new AtomicBoolean(false);
    private final CB_List<CB_Locator.GpsStrength> coreSatList = new CB_List<>(14);
    private SensorEventListener mSensorEventListener;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private AndroidEventListener handlingGetApiAuth;
    private HorizontalListView quickButtonListView;
    private PowerManager.WakeLock wakeLock;
    private int horizontalListViewHeight;
    private LinearLayout layoutTop;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SharedPreferences.Editor androidSettingEditor;
    private boolean lostCheck = false;
    private Dialog pWaitD;
    private LastState lastState;
    private IChanged handleSuppressPowerSavingConfigChanged, handleRunOverLockScreenConfigChanged, handleGpsUpdateTimeConfigChanged, handleImperialUnitsConfigChanged;
    private IShowViewListener showViewListener;
    private IPlatformListener platformListener;
    private boolean mustShowCacheList = true;
    private CancelWaitDialog wd;

    public Main() {

        screenBroadcastReceiver = new ScreenBroadcastReceiver();
        mSensorEventListener = new SensorEventListener() {
            private final float[] orientationValues = new float[3];
            private final float[] R = new float[9];
            private final float[] I = new float[9];
            private final RingBufferFloat ringBuffer = new RingBufferFloat(15);
            private float[] gravity;
            private float lastOrientation;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                long now = System.currentTimeMillis();
                long lastUpdateTime = 0;
                long updateTime = 15;
                // if (lastUpdateTime == 0 ||
                if (lastUpdateTime + updateTime < now) {
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        float[] geomagnetic = event.values;
                        if (gravity != null && geomagnetic != null) {
                            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                                SensorManager.getOrientation(R, orientationValues);
                                float orientation = ringBuffer.add((float) Math.toDegrees(orientationValues[0]));
                                while (orientation < 0) {
                                    orientation += 360;
                                }

                                while (orientation > 360) {
                                    orientation -= 360;
                                }

                                float minChange = 0.5f;
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

        platformListener = new IPlatformListener() {
            private AtomicBoolean torchAvailable = null;
            private Camera deviceCamera;
            private SharedPreferences androidSetting;

            @Override
            public void writeSetting(SettingBase<?> setting) {
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
                androidSettingEditor.apply();
            }

            @Override
            public SettingBase<?> readSetting(SettingBase<?> setting) {
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

            @Override
            public void setScreenLockTime(int value) {
            }

            @Override
            public boolean isOnline() {
                // isOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                assert cm != null;
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnectedOrConnecting();
            }

            @Override
            public boolean isGPSon() {
                return GpsOn();
            }

            @Override
            public void vibrate() {
                if (Config.vibrateFeedback.getValue())
                    ((Vibrator) Objects.requireNonNull(getSystemService(Context.VIBRATOR_SERVICE))).vibrate(Config.VibrateTime.getValue());
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
                return deviceCamera != null;
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

            @Override
            public void getApiKey() {
                Main.this.getApiKey();
            }

            @Override
            public void callUrl(String url) {
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
                    if (intent.resolveActivity(Main.this.getPackageManager()) != null) {
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

            @Override
            public void handleExternalRequest() {
                checkExternalRequest();
            }

            @Override
            public void startPictureApp(String fileName) {
                Uri uriToImage = Uri.fromFile(new java.io.File(fileName));
                Intent shareIntent = new Intent(ACTION_VIEW);
                shareIntent.setDataAndType(uriToImage, "image/*");
                Main.mainActivity.startActivity(Intent.createChooser(shareIntent, Main.this.getResources().getText(R.string.app_name)));
            }

            @Override
            public SQLiteInterface getSQLInstance() {
                return new SQLiteClass(mainActivity);
            }

            @Override
            public void freeSQLInstance(SQLiteInterface sqlInstance) {
                // sqlInstance = null;
            }

            @Override
            public void getFile(String initialPath, String extension, String TitleText, String ButtonText, PlatformConnector.IgetFileReturnListener returnListener) {
                File mPath = FileFactory.createFile(initialPath);
                Android_FileExplorer fileDialog = new Android_FileExplorer(mainActivity, mPath, TitleText, ButtonText);
                fileDialog.setFileReturnListener(returnListener);
                fileDialog.showDialog();
            }

            @Override
            public void getFolder(String initialPath, String TitleText, String ButtonText, PlatformConnector.IgetFolderReturnListener returnListener) {
                File mPath = FileFactory.createFile(initialPath);
                Android_FileExplorer folderDialog = new Android_FileExplorer(mainActivity, mPath, TitleText, ButtonText);
                folderDialog.setSelectDirectoryOption();
                folderDialog.setFolderReturnListener(returnListener);
                folderDialog.showDialog();
            }

            @Override
            public void quit() {
                if (GlobalCore.isSetSelectedCache()) {
                    // speichere selektierten Cache, da nicht alles über die
                    // SelectedCacheEventList läuft
                    Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
                    Config.AcceptChanges();
                    Log.info(sKlasse, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
                }
                finish();
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

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.main);
            quickButtonListView = findViewById(R.id.quickButtonListView);
            layoutTop = findViewById(R.id.layoutTop);

            // initialize GL the gdx ApplicationListener (Window Size, load Sprites, ...)
            int width = UiSizes.getInstance().getWindowWidth();
            int height = UiSizes.getInstance().getWindowHeight();
            CB_RectF rec = new CB_RectF(0, 0, width, height);
            new GL(width, height, new MainViewInit(rec), new ViewManager(rec));
            GL.that.textInput = new Android_TextInput(this);

            showViewListener = new ShowViewListener(this);

            // registerReceiver receiver for screen switched on/off
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenBroadcastReceiver, intentFilter);

            ActivityUtils.onActivityCreateSetTheme(this);

            Config.SuppressPowerSaving.addSettingChangedListener(handleSuppressPowerSavingConfigChanged);
            Config.RunOverLockScreen.addSettingChangedListener(handleRunOverLockScreenConfigChanged);
            Config.gpsUpdateTime.addSettingChangedListener(handleGpsUpdateTimeConfigChanged);
            Config.ImperialUnits.addSettingChangedListener(handleImperialUnitsConfigChanged);

            initLocatorBase();

            Plattform.used = Plattform.Android;
            PlatformConnector.AndroidVersion = Build.VERSION.SDK_INT;
            PlatformConnector.setShowViewListener(showViewListener);
            PlatformConnector.setPlatformListener(platformListener);

            // init Clipboard
            Object clipboardService = getSystemService(CLIPBOARD_SERVICE);
            if (clipboardService != null) {
                if (clipboardService instanceof android.content.ClipboardManager) {
                    GlobalCore.setDefaultClipboard(new AndroidContentClipboard((android.content.ClipboardManager) clipboardService));
                    Log.info(sKlasse, "got AndroidContentClipboard");
                } else if (clipboardService instanceof android.text.ClipboardManager) {
                    GlobalCore.setDefaultClipboard(new AndroidTextClipboard((android.text.ClipboardManager) clipboardService));
                    Log.info(sKlasse, "got AndroidTextClipboard");
                }
            }

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            assert mSensorManager != null;
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            new AndroidTexturePacker();

            initialLocationManager();

            GL.that.onStart();

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
                checkTranslationIsLoaded();

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
                            MessageBox.show(Main.mainActivity, Welcome, Translation.get("welcome"), MessageBoxIcon.None);
                        });
                    }
                }, 5000);

            }

            // static Event Lists
            SelectedCacheChangedEventListeners.getInstance().add(this);

            if (Config.SuppressPowerSaving.getValue()) {
                setWakeLockLevelToScreenBright();
            }

            Config.AcceptChanges();

        } else {
            restartFromSplash();
        }

        if (input == null) {
            Log.info(sKlasse, "gdx input not yet initialized");
                /*
                // should be != null : initialized by gdxview = initializeForView(GL.that, gdxConfig); in initializeGDXAndroidApplication();
                graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
                 */
        }

        Log.info(sKlasse, "onCreate <=");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.info(sKlasse, "=> onSaveInstanceState");
        savedInstanceState.putBoolean("useSmallSkin", GlobalCore.useSmallSkin);
        savedInstanceState.putString("WorkPath", Config.mWorkPath);

        savedInstanceState.putInt("WindowWidth", UiSizes.getInstance().getWindowWidth());
        savedInstanceState.putInt("WindowHeight", UiSizes.getInstance().getWindowHeight());

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
        Log.info(sKlasse, "=> onNewIntent");
        super.onNewIntent(intent);
    }

    private void checkExternalRequest() {
        final Bundle extras = mainActivity.getIntent().getExtras();
        if (extras != null) {
            Log.info(sKlasse, "prepared Request from splash");
            if (ViewManager.that.isInitialized()) {
                String ExternalRequestGCCode = extras.getString("GcCode");
                if (ExternalRequestGCCode != null) {
                    Log.info(sKlasse, "importCacheByGCCode");
                    mainActivity.getIntent().removeExtra("GcCode");
                    importCacheByGCCode(ExternalRequestGCCode);
                }
                String ExternalRequestGpxPath = extras.getString("GpxPath");
                if (ExternalRequestGpxPath != null) {
                    Log.info(sKlasse, "importGPXFile");
                    mainActivity.getIntent().removeExtra("GpxPath");
                    importGPXFile(ExternalRequestGpxPath);
                }
                String ExternalRequestGuid = extras.getString("Guid");
                if (ExternalRequestGuid != null) {
                    Log.info(sKlasse, "importCacheByGuid");
                    mainActivity.getIntent().removeExtra("Guid");
                    importCacheByGuid();
                }
                String ExternalRequestLatLon = extras.getString("LatLon");
                if (ExternalRequestLatLon != null) {
                    Log.info(sKlasse, "positionLatLon");
                    mainActivity.getIntent().removeExtra("LatLon");
                    positionLatLon();
                }
                String ExternalRequestMapDownloadPath = extras.getString("MapDownloadPath");
                if (ExternalRequestMapDownloadPath != null) {
                    Log.info(sKlasse, "MapDownload");
                    mainActivity.getIntent().removeExtra("MapDownloadPath");
                    FZKDownload.getInstance().importByUrl(ExternalRequestMapDownloadPath);
                    GL.that.showActivity(FZKDownload.getInstance());
                    FZKDownload.getInstance().importByUrlFinished();
                }
                String ExternalRequestName = extras.getString("Name");
                if (ExternalRequestName != null) {
                    Log.info(sKlasse, "importCacheByName");
                    mainActivity.getIntent().removeExtra("Name");
                    importCacheByName();
                }
            }
        }
    }

    private void positionLatLon() {
    }

    private void importCacheByGuid() {
    }

    private void importCacheByGCCode(final String ExternalRequestGCCode) {
        TimerTask runTheSearchTasks = new TimerTask() {
            @Override
            public void run() {
                if (ExternalRequestGCCode != null) {
                    mainActivity.runOnUiThread(() -> {
                        if (mustShowCacheList) {
                            // show cachelist first then search dialog
                            mustShowCacheList = false;
                            ViewManager.leftTab.ShowView(CacheListView.getInstance());
                            importCacheByGCCode(ExternalRequestGCCode); // now the search can start (doSearchOnline)
                        } else {
                            mustShowCacheList = true;
                            if (SearchDialog.that == null) {
                                new SearchDialog();
                            }
                            SearchDialog.that.showNotCloseAutomaticly();
                            SearchDialog.that.doSearchOnline(ExternalRequestGCCode, SearchDialog.SearchMode.GcCode);
                        }
                    });
                }
            }
        };
        new Timer().schedule(runTheSearchTasks, 500);
    }

    private void importGPXFile(final String ExternalRequestGpxPath) {
        TimerTask gpxImportTask = new TimerTask() {
            @Override
            public void run() {
                Log.info(sKlasse, "ImportGPXFile");
                mainActivity.runOnUiThread(() -> wd = CancelWaitDialog.ShowWait(Translation.get("ImportGPX"), () -> wd.close(), new ICancelRunnable() {
                    @Override
                    public void run() {
                        Log.info(sKlasse, "Import GPXFile from " + ExternalRequestGpxPath + " started");
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
                        String Msg = "Import " + GPXFileImporter.CacheCount + "Caches\n" + GPXFileImporter.LogCount + "Logs\n in " + ImportZeit;
                        Log.info(sKlasse, Msg.replace("\n", "\n\r") + " from " + ExternalRequestGpxPath);
                        GL.that.Toast(Msg, 3000);
                    }

                    @Override
                    public boolean doCancel() {
                        return false;
                    }
                }));

            }
        };

        new Timer().schedule(gpxImportTask, 500);
    }

    private void importCacheByName() {
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

    @SuppressLint("WakelockTimeout")
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
            Log.info(sKlasse, "(input == null) : init input needed for super.onResume()");
            /*
            graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
             */
        }

        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(mSensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

        int lQuickButtonHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
        setQuickButtonHeight(lQuickButtonHeight);

        showViewListener.onResume();

        if (wakeLock != null) wakeLock.acquire();

        Log.info(sKlasse, "checkExternalRequest from onResume");
        checkExternalRequest();

        Log.info(sKlasse, "onResume <=");
        lastState = LastState.onResume;
        // to have a protokoll of the program start independant of Config.AktLogLevel
        CB_SLF4J.getInstance(Config.mWorkPath).setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());


        super.onResume();
    }

    public void pause() {
        onPause();
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
            /*
            graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
             */
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
                    SelectedCacheChangedEventListeners.getInstance().clear();
                    CacheListChangedEventList.list.clear();
                    mainActivity = null;
                    showViewListener.onDestroyWithFinishing();

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
                showViewListener.onDestroyWithoutFinishing();

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.info(sKlasse, "=> onPrepareOptionsMenu");
        int menuId = showViewListener.getAktViewId();
        if (menuId != 0) {
            menu.clear();
            getMenuInflater().inflate(menuId, menu);
        }
        return super.onPrepareOptionsMenu(menu);
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
                    double altCorrection = Double.parseDouble(s[11]);
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

    public void restartFromSplash() {
        mainActivity = null;
        Log.info(sKlasse, "=> Must restart from splash!");
        Intent splashIntent = new Intent().setClass(this, Splash.class);
        startActivity(splashIntent);
        finish();
    }

    private void askToGetApiKey() {
        MessageBox.show(this, Translation.get("wantApi"), Translation.get("welcome"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live,
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

    public void handleRunOverLockScreenConfig() {
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
            Log.err(sKlasse, "Main.initialLocationManager()", "", e);
            e.printStackTrace();
        }

    }

    public void setQuickButtonHeight(int value) {
        horizontalListViewHeight = value;
        runOnUiThread(() -> {
            quickButtonListView.setHeight(horizontalListViewHeight);
            quickButtonListView.invalidate();
            layoutTop.requestLayout();
            showViewListener.requestLayout();
        });

    }

    /**
     * überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung
     * ausgegeben.
     */
    private void askToSwitchGpsOn() {
        try {
            if (Config.Ask_Switch_GPS_ON.getValue()) {
                checkTranslationIsLoaded();
                runOnUiThread(() -> MessageBox.show(Main.mainActivity, Translation.get("GPSon?"), Translation.get("GPSoff"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, (dialog, button) -> {
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
                }));

            }
        } catch (Exception e) {
            Log.err(sKlasse, "main.chkGpsIsOn()", "", e);
            e.printStackTrace();
        }
    }

    private void checkTranslationIsLoaded() {
        if (!Translation.isInitialized()) {

            try {
                new Translation(Config.mWorkPath, FileType.Internal).loadTranslation(Config.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                Translation.that.loadTranslation(Config.Sel_LanguagePath.getDefaultValue());
            }
        }
    }

    private boolean GpsOn() {
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert locManager != null;
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getApiKey() {
        Intent intent = new Intent().setClass(mainActivity, GcApiLogin.class);
        if (intent.resolveActivity(getPackageManager()) != null) {
            if (handlingGetApiAuth == null)
                handlingGetApiAuth = (requestCode, resultCode, data) -> {
                    Main.this.removeAndroidEventListener(handlingGetApiAuth);
                    if (requestCode == REQUEST_GET_APIKEY) {
                        GL.that.RunIfInitial(SettingsActivity::resortList);
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
            } catch (Exception ignored) {
            }
        }

        ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;
        CB_Locator.Location initialLocation;
        if (provider == ProviderType.Saved) {
            initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = CB_Locator.Location.NULL_LOCATION;
        }
        Locator.getInstance().setNewLocation(initialLocation);

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
            if (intent != null) {
                if (intent.getAction() != null) {
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
    }

}

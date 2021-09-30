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

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.activities.Splash;
import de.droidcachebox.controls.HorizontalListView;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheboxDB;
import de.droidcachebox.database.DraftsDB;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.SettingsDB;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.controls.Android_TextInput;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.AndroidTexturePacker;
import de.droidcachebox.gdx.utils.AndroidContentClipboard;
import de.droidcachebox.locator.AndroidLocatorBaseMethods;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.Locator.CompassType;
import de.droidcachebox.locator.LocatorBasePlatFormMethods;
import de.droidcachebox.maps.BRouter;
import de.droidcachebox.menu.MainViewInit;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ActivityUtils;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.log.CB_SLF4J;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.LogLevel;
import de.droidcachebox.views.forms.MessageBox;
import de.droidcachebox.views.forms.PleaseWaitMessageBox;

public class Main extends AndroidApplication implements CacheSelectionChangedListeners.CacheSelectionChangedListener, GpsStatus.NmeaListener, CB_UI_Settings {
    private static final String sKlasse = "Main";
    public static boolean isCreated = false;
    public static int Request_ForLocationManager = 11052016;
    public static int Request_ServiceOption = 11052017;
    public static int Request_takePhoto = 11052018;
    public static int Request_recordVideo = 11052019;
    public static int Request_recordVoice = 11052020;
    public static int Request_getLocationIfInBackground = 11052021;
    private static boolean isRestart = false; // ???
    private final AtomicBoolean waitForGL = new AtomicBoolean(false);
    private final SensorEventListener mSensorEventListener;
    private final IChanged handleSuppressPowerSavingConfigChanged;
    private final IChanged handleImperialUnitsConfigChanged;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private HorizontalListView quickButtonListView;
    private PowerManager.WakeLock wakeLock;
    private int horizontalListViewHeight;
    private LinearLayout layoutTop;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Dialog pWaitD;
    private LastState lastState;
    private ShowViewListener showViewListener;
    private AndroidUIBaseMethods androidUIBaseMethods;

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
                long updateInterval = 15;
                // if (lastUpdateTime == 0 ||
                if (lastUpdateTime + updateInterval < now) {
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

        handleImperialUnitsConfigChanged = () -> Locator.getInstance().setUseImperialUnits(Config.ImperialUnits.getValue());
        handleSuppressPowerSavingConfigChanged = () -> setWakeLock(Config.SuppressPowerSaving.getValue());

        ShowMap.setRouter(new BRouter(this));

    }

    public static Activity getInstance() {
        if (isCreated) {
            if (Gdx.app == null) {
                Log.err(sKlasse, "Gdx.app is null");
            } else {
                if (Gdx.app instanceof Activity)
                    return (Activity) Gdx.app;
                Log.err(sKlasse, "Gdx.app is not an Activity");
            }
        }
        return null;
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
                    if (savedInstanceState.getBoolean("useSmallSkin"))
                        GlobalCore.displayType = DisplayType.Small;
                    new Config(savedInstanceState.getString("WorkPath"));
                    if (!FileIO.createDirectory(Config.workPath + "/User"))
                        return;
                    new SettingsDB(this);
                    SettingsDB.Settings.startUp(Config.workPath + "/User/Config.db3");
                    new CacheboxDB(this);
                    new DraftsDB(this);

                    Resources res = getResources();
                    DevicesSizes ui = new DevicesSizes();
                    ui.Window = new Size(savedInstanceState.getInt("WindowWidth"), savedInstanceState.getInt("WindowHeight"));
                    ui.Density = res.getDisplayMetrics().density;
                    ui.isLandscape = false;
                    UiSizes.getInstance().initialize(ui);

                    Global.Paints.init(this);
                    Global.initIcons(this);

                    GlobalCore.restartCache = savedInstanceState.getString("selectedCacheID");
                    GlobalCore.restartWayPoint = savedInstanceState.getString("selectedWayPoint");
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
            GL.that.setTextInput(new Android_TextInput(this));

            // registerReceiver receiver for screen switched on/off
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenBroadcastReceiver, intentFilter);

            ActivityUtils.onActivityCreateSetTheme(this);

            Config.SuppressPowerSaving.addSettingChangedListener(handleSuppressPowerSavingConfigChanged);
            Config.ImperialUnits.addSettingChangedListener(handleImperialUnitsConfigChanged);

            Log.debug(sKlasse, "initLocatorBase");
            initLocatorBase();

            Plattform.used = Plattform.Android;
            PlatformUIBase.AndroidVersion = Build.VERSION.SDK_INT;
            showViewListener = new ShowViewListener(this);
            PlatformUIBase.setShowViewListener(showViewListener);
            androidUIBaseMethods = new AndroidUIBaseMethods(this);
            PlatformUIBase.setMethods(androidUIBaseMethods);
            Object clipboardService = getSystemService(CLIPBOARD_SERVICE);
            if (clipboardService != null) {
                if (clipboardService instanceof android.content.ClipboardManager) {
                    PlatformUIBase.setClipboard(new AndroidContentClipboard((android.content.ClipboardManager) clipboardService));
                    Log.info(sKlasse, "got AndroidContentClipboard");
                } /*
                else {
                    if (clipboardService instanceof android.text.ClipboardManager) {
                        PlatformUIBase.setClipboard(new AndroidTextClipboard((android.text.ClipboardManager) clipboardService));
                        Log.info(sKlasse, "got AndroidTextClipboard");
                    }
                }
                */
            }

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager != null) {
                accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }

            new AndroidTexturePacker();

            GL.that.onStart();

            int sollHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
            setQuickButtonHeight(sollHeight);

            CacheSelectionChangedListeners.getInstance().addListener(this);

            Log.debug(sKlasse, "initializeLocatorBaseMethods");
            initializeLocatorBaseMethods();
            androidUIBaseMethods.getLocationManager(); // now the onLocationChanged is called (LocationListener) or no permission
            androidUIBaseMethods.startService();

            isCreated = true;
            Log.info(sKlasse, "onCreate <=");

        } else {
            Log.info(sKlasse, "restartFromSplash: cannot start Main without previous Splash");
            restartFromSplash();
        }
        // do no dialogs in create
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.info(sKlasse, "=> onSaveInstanceState");
        savedInstanceState.putBoolean("useSmallSkin", GlobalCore.displayType == DisplayType.Small);
        savedInstanceState.putString("WorkPath", Config.workPath);

        savedInstanceState.putInt("WindowWidth", UiSizes.getInstance().getWindowWidth());
        savedInstanceState.putInt("WindowHeight", UiSizes.getInstance().getWindowHeight());

        if (GlobalCore.isSetSelectedCache())
            savedInstanceState.putString("selectedCacheID", GlobalCore.getSelectedCache().getGeoCacheCode());
        if (GlobalCore.getSelectedWayPoint() != null)
            savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWayPoint().getWaypointCode());

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

    @Override
    protected void onStop() {
        Log.info(sKlasse, "=> onStop");

        if (mSensorManager != null)
            mSensorManager.unregisterListener(mSensorEventListener);

        setWakeLock(false);

        super.onStop();

        Log.info(sKlasse, "onStop <=");
        lastState = LastState.onStop;
    }

    @Override
    protected void onResume() {
        if (GL.that == null) {
            Log.err("onResume", "GL.that == null");
            restartFromSplash();
        } else {
            if (GL.that.getGlListener() == null) {
                Log.err("onResume", "mGL_Listener_Interface == null");
                restartFromSplash();
            } else {
                Log.debug(sKlasse, "onResume");
            }
        }

        GL.that.restartRendering(); // does ViewGL.renderContinous();

        if (lastState == LastState.onStop) {
            Log.info(sKlasse, "=> Resume from Stop");
            showWaitToRenderStarted();
            InvalidateTextureListeners.getInstance().invalidateTexture();
        } else {
            Log.info(sKlasse, "=> onResume");
        }

        OnResumeListeners.getInstance().onResume();

        if (mSensorManager != null) {
            if (accelerometer != null)
                mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            if (magnetometer != null)
                mSensorManager.registerListener(mSensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

        int lQuickButtonHeight = (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
        setQuickButtonHeight(lQuickButtonHeight);

        setWakeLock(Config.SuppressPowerSaving.getValue());

        Log.info(sKlasse, "onResume <=");
        lastState = LastState.onResume;
        // having a protokoll of the program start: but now reset to Config.AktLogLevel but >= LogLevel.ERROR
        if (Config.AktLogLevel.getEnumValue() == LogLevel.OFF)
            Config.AktLogLevel.setEnumValue(LogLevel.ERROR);
        CB_SLF4J.getInstance(Config.workPath).setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());

        boolean ret = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(GPS_PROVIDER);
        if (!ret) {
            runOnUiThread(() -> {
                // can't use GL MsgBox here, cause Fonts ara not loaded yet
                MessageBox.show(this,
                        Translation.get("GPSon?"),
                        Translation.get("GPSoff"),
                        MsgBoxButton.YesNo,
                        MsgBoxIcon.Information, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (which == BUTTON_POSITIVE) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                        }

                );
            });
        }

        // perhaps move to about
        if (androidUIBaseMethods.askForLocationPermission()) {
            androidUIBaseMethods.resetAskForLocationPermission();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runOnUiThread(() -> {
                    MessageBox.show(this, Translation.get("GPSDisclosureText"), Translation.get("GPSDisclosureTitle"), MsgBoxButton.YesNo, MsgBoxIcon.Information, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == Dialog.BUTTON_POSITIVE) {
                                final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                                ActivityCompat.requestPermissions(Main.this, locationPermissions, Request_ForLocationManager);
                            }
                        }
                    });
                });
            }
            else {
                final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, locationPermissions, Request_ForLocationManager);
            }
        }

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

        super.onPause();
        Log.info(sKlasse, "onPause <=");
    }

    @Override
    public void onDestroy() {
        Log.info(sKlasse, "=> onDestroy AndroidApplication");
        try {
            PlatformUIBase.addToMediaScannerList(Config.DraftsGarminPath.getValue());
            PlatformUIBase.addToMediaScannerList(CB_SLF4J.logfile);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            for (String fn : PlatformUIBase.getMediaScannerList()) {
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

                    if (wakeLock != null) {
                        wakeLock.release();
                    }

                    TrackRecorder.stopRecording();
                    // GPS Verbindung beenden
                    androidUIBaseMethods.removeFromGPS();
                    CacheSelectionChangedListeners.getInstance().clear();
                    CacheListChangedListeners.getInstance().clear();
                    showViewListener.onDestroyWithFinishing();

                    Config.AcceptChanges();

                    CBDB.Data.sql.close();
                    DraftsDatabase.Drafts.sql.close();

                    Sprites.destroyCache();

                    SettingsDatabase.Settings.sql.close();

                }

                super.onDestroy();
                if (GlobalCore.RunFromSplash)
                    System.exit(0);
            } else {
                Log.info(sKlasse, "isFinishing==false");
                showViewListener.onDestroyWithoutFinishing();

                SettingsDatabase.Settings.sql.close();
                CBDB.Data.sql.close();
                DraftsDatabase.Drafts.sql.close();

                super.onDestroy();
            }
        }
        Log.info(sKlasse, "onDestroy AndroidApplication <=");
        lastState = LastState.onDestroy;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, int[] grantResults) {
        if (permissions.length > 0) {
            if (requestCode == Request_ForLocationManager) {
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PERMISSION_GRANTED) {
                    androidUIBaseMethods.getLocationManager(true); // now the onLocationChanged is called (LocationListener)
                }
            } else if (requestCode == Request_takePhoto) {
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PERMISSION_GRANTED) {
                    PlatformUIBase.showView(ViewConst.TAKE_PHOTO, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_recordVideo) {
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PERMISSION_GRANTED) {
                    PlatformUIBase.showView(ViewConst.VIDEO_REC, 0, 0, 0, 0, 0, 0);
                }
                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PERMISSION_GRANTED) {
                    PlatformUIBase.showView(ViewConst.VIDEO_REC, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_recordVoice) {
                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PERMISSION_GRANTED) {
                    PlatformUIBase.showView(ViewConst.VOICE_REC, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_getLocationIfInBackground) {
                // result is only when requested (API level 29)
                if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[0] == PERMISSION_GRANTED) {
                    Log.debug(sKlasse, "onRequestPermissionsResult granted " + Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    // TrackRecorder and others are started anyhow
                }
            } else if (requestCode == Request_ServiceOption) {
                if (permissions[0].equals(Manifest.permission.FOREGROUND_SERVICE) && grantResults[0] == PERMISSION_GRANTED) {
                    androidUIBaseMethods.serviceCanBeStarted();
                }
            }
        }
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
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        if (cache != null) {
            float distance = cache.recalculateAndGetDistance(CalculationType.FAST, false, Locator.getInstance().getMyPosition());
            if (waypoint != null) {
                distance = waypoint.getDistance();
            }
            if (distance > Config.SoundApproachDistance.getValue()) {
                runOnUiThread(() -> GlobalCore.switchToCompassCompleted = false);
            }
        }
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
                    androidUIBaseMethods.getLocationManager().removeNmeaListener(this);
                } catch (Exception ignored) {
                    // keine Höhenkorrektur vorhanden
                }
            }
        } catch (Exception e) {
            Log.err(sKlasse, "main.onNmeaReceived()", "", e);
        }
    }

    void restartFromSplash() {
        Log.info(sKlasse, "=> Must restart from splash!");
        Intent splashIntent = new Intent().setClass(this, Splash.class);
        startActivity(splashIntent);
        finish();
    }

    @SuppressLint("WakelockTimeout")
    private void setWakeLock(boolean suppress) {
        // Keep the device awake until destroy is called. remove there
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            if (suppress) {
                wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CacheBox:WakeLock");
            } else {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CacheBox:PartialWakeLock");
            }
            if (wakeLock != null) {
                Log.info(sKlasse, "wakeLock.acquire()");
                // even if not held, you must acquire to change behavior of powerservice
                wakeLock.acquire();
            }
        }
    }

    private void showWaitToRenderStarted() {
        if (!GL.that.getAllisInitialized())
            return;

        if (pWaitD == null) {

            pWaitD = PleaseWaitMessageBox.show(Translation.get("waitForGL"), "", MsgBoxButton.NOTHING, MsgBoxIcon.None, null);

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

    public void setQuickButtonHeight(int value) {
        horizontalListViewHeight = value;
        runOnUiThread(() -> {
            quickButtonListView.setHeight(horizontalListViewHeight);
            quickButtonListView.invalidate();
            layoutTop.requestLayout();
            showViewListener.requestLayout();
        });
    }

    private void checkTranslationIsLoaded() {
        if (!Translation.isInitialized()) {
            Translation trans = new Translation(Config.workPath);
            try {
                trans.loadTranslation(Config.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                trans.loadTranslation(Config.Sel_LanguagePath.getDefaultValue());
            }
        }
    }

    private void initLocatorBase() {
        // initial Locator with saved Location
        double latitude = -1000;
        double longitude = -1000;

        if (Config.settings != null) {
            try {
                latitude = Config.mapInitLatitude.getValue();
                longitude = Config.mapInitLongitude.getValue();
            } catch (Exception ignored) {
            }
        }

        ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;
        CBLocation initialLocation;
        if (provider == ProviderType.Saved) {
            initialLocation = new CBLocation(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = CBLocation.NULL_LOCATION;
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

    private void initializeLocatorBaseMethods() {
        LocatorBasePlatFormMethods.setMethods(new AndroidLocatorBaseMethods(this));
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
                    } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        Energy.setDisplayOn();
                    }
                }
            }
        }
    }

}

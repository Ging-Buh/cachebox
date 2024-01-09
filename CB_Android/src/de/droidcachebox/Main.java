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
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.controls.Android_TextInput;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.AndroidTexturePacker;
import de.droidcachebox.gdx.utils.AndroidContentClipboard;
import de.droidcachebox.locator.AndroidLocatorMethods;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.Locator.CompassType;
import de.droidcachebox.locator.LocatorMethods;
import de.droidcachebox.maps.BRouter;
import de.droidcachebox.menu.MainView;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn3.executes.TrackRecorder;
import de.droidcachebox.settings.Settings;
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

public class Main extends AndroidApplication implements CacheSelectionChangedListeners.CacheSelectionChangedListener, GpsStatus.NmeaListener {
    private static final String sClass = "Main";
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
    private ShowViewMethods showViewListener;
    private AndroidPlatformMethods androidUIBaseMethods;

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
                                    // sClass.debug("orientation: {}", orientation);
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

        handleImperialUnitsConfigChanged = () -> Locator.getInstance().setUseImperialUnits(Settings.ImperialUnits.getValue());
        handleSuppressPowerSavingConfigChanged = () -> setWakeLock(Settings.SuppressPowerSaving.getValue());

        ShowMap.setRouter(new BRouter(this));

    }

    public static Activity getInstance() {
        if (isCreated) {
            if (Gdx.app == null) {
                Log.err(sClass, "Gdx.app is null");
            } else {
                if (Gdx.app instanceof Activity)
                    return (Activity) Gdx.app;
                Log.err(sClass, "Gdx.app is not an Activity");
            }
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Plattform.used = Plattform.Android;
        Platform.AndroidVersion = Build.VERSION.SDK_INT;

        if (GlobalCore.RunFromSplash) {
            if (savedInstanceState != null) {
                GlobalCore.restartAfterKill = true;
                if (savedInstanceState.isEmpty()) {
                    Log.debug(sClass, "=> onCreate; savedInstanceState is empty");
                } else {
                    Log.debug(sClass, "=> onCreate; initializations from savedInstanceState");
                    // ? everything, that is initialized in Splash
                    if (savedInstanceState.getBoolean("useSmallSkin"))
                        GlobalCore.displayType = DisplayType.Small;
                    GlobalCore.workPath = savedInstanceState.getString("WorkPath");
                    if (!FileIO.createDirectory(GlobalCore.workPath + "/User"))
                        return;

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
                Log.debug(sClass, "=> onCreate first start");
                GlobalCore.restartAfterKill = false;
            }

            androidUIBaseMethods = new AndroidPlatformMethods(this);
            Platform.init(androidUIBaseMethods);

            LocatorMethods.init(new AndroidLocatorMethods(this));

            SettingsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/Config.db3");

            Object clipboardService = getSystemService(CLIPBOARD_SERVICE);
            if (clipboardService != null) {
                if (clipboardService instanceof android.content.ClipboardManager) {
                    Platform.setClipboard(new AndroidContentClipboard((android.content.ClipboardManager) clipboardService));
                    Log.debug(sClass, "got AndroidContentClipboard");
                } /*
                else {
                    if (clipboardService instanceof android.text.ClipboardManager) {
                        PlatformUIBase.setClipboard(new AndroidTextClipboard((android.text.ClipboardManager) clipboardService));
                        Log.debug(sClass, "got AndroidTextClipboard");
                    }
                }
                */
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
            new GL(width, height, new MainView(rec), new ViewManager(rec));
            GL.that.setTextInput(new Android_TextInput(this));

            showViewListener = new ShowViewMethods(this);
            Platform.initShowViewMethods(showViewListener);

            // registerReceiver receiver for screen switched on/off
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenBroadcastReceiver, intentFilter);

            ActivityUtils.onActivityCreateSetTheme(this);

            Settings.SuppressPowerSaving.addSettingChangedListener(handleSuppressPowerSavingConfigChanged);
            Settings.ImperialUnits.addSettingChangedListener(handleImperialUnitsConfigChanged);

            Log.debug(sClass, "initLocatorBase");
            initLocatorBase();

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager != null) {
                accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }

            new AndroidTexturePacker();

            GL.that.onStart();

            int sollHeight = (Settings.quickButtonShow.getValue() && Settings.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
            setQuickButtonHeight(sollHeight);

            CacheSelectionChangedListeners.getInstance().addListener(this);

            androidUIBaseMethods.getLocationManager(); // now the onLocationChanged is called (LocationListener) or no permission
            androidUIBaseMethods.startService();

            isCreated = true;
            Log.debug(sClass, "onCreate <=");

        } else {
            Log.debug(sClass, "restartFromSplash: cannot start Main without previous Splash");
            restartFromSplash();
        }
        // do no dialogs in create
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.debug(sClass, "=> onSaveInstanceState");
        savedInstanceState.putBoolean("useSmallSkin", GlobalCore.displayType == DisplayType.Small);
        savedInstanceState.putString("WorkPath", GlobalCore.workPath);

        savedInstanceState.putInt("WindowWidth", UiSizes.getInstance().getWindowWidth());
        savedInstanceState.putInt("WindowHeight", UiSizes.getInstance().getWindowHeight());

        if (GlobalCore.isSetSelectedCache())
            savedInstanceState.putString("selectedCacheID", GlobalCore.getSelectedCache().getGeoCacheCode());
        if (GlobalCore.getSelectedWayPoint() != null)
            savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWayPoint().getWaypointCode());

        super.onSaveInstanceState(savedInstanceState);
        Log.debug(sClass, "onSaveInstanceState <=");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // setIntent(intent) here to make future calls (from external) to getIntent() get the most recent Intent data
        // is not necessary for us, I think
        Log.debug(sClass, "=> onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        Log.debug(sClass, "=> onStop");

        if (mSensorManager != null)
            mSensorManager.unregisterListener(mSensorEventListener);

        setWakeLock(false);

        super.onStop();

        Log.debug(sClass, "onStop <=");
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
                Log.debug(sClass, "onResume");
            }
        }

        GL.that.restartRendering(); // does ViewGL.renderContinous();

        if (lastState == LastState.onStop) {
            Log.debug(sClass, "=> Resume from Stop");
            showWaitToRenderStarted();
            InvalidateTextureListeners.getInstance().fireInvalidateTexture();
        } else {
            Log.debug(sClass, "=> onResume");
        }

        OnResumeListeners.getInstance().onResume();

        if (mSensorManager != null) {
            if (accelerometer != null)
                mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            if (magnetometer != null)
                mSensorManager.registerListener(mSensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

        int lQuickButtonHeight = (Settings.quickButtonShow.getValue() && Settings.quickButtonLastShow.getValue()) ? UiSizes.getInstance().getQuickButtonListHeight() : 0;
        setQuickButtonHeight(lQuickButtonHeight);

        setWakeLock(Settings.SuppressPowerSaving.getValue());

        Log.debug(sClass, "onResume <=");
        lastState = LastState.onResume;
        // having a protokoll of the program start: but now reset to SettingsClass.AktLogLevel but >= LogLevel.ERROR
        if (Settings.AktLogLevel.getEnumValue() == LogLevel.OFF)
            Settings.AktLogLevel.setEnumValue(LogLevel.ERROR);
        CB_SLF4J.getInstance(GlobalCore.workPath).setLogLevel((LogLevel) Settings.AktLogLevel.getEnumValue());

        boolean ret = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(GPS_PROVIDER);
        if (!ret && Settings.Ask_Switch_GPS_ON.getValue()) {
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
                runOnUiThread(() -> MessageBox.show(this, Translation.get("GPSDisclosureText"), Translation.get("GPSDisclosureTitle"), MsgBoxButton.YesNo, MsgBoxIcon.Information, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                            ActivityCompat.requestPermissions(Main.this, locationPermissions, Request_ForLocationManager);
                        }
                    }
                }));
            } else {
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
        Log.debug(sClass, "=> onPause");

        if (isFinishing()) {
            Log.debug(sClass, "is completely Finishing()");
        }

        super.onPause();
        Log.debug(sClass, "onPause <=");
    }

    @Override
    public void onDestroy() {
        Log.info(sClass, "=> onDestroy AndroidApplication");
        try {
            Platform.addToMediaScannerList(Settings.DraftsGarminPath.getValue());
            Platform.addToMediaScannerList(CB_SLF4J.logfile);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            for (String fn : Platform.getMediaScannerList()) {
                intent.setData(Uri.fromFile(new java.io.File(fn)));
                sendBroadcast(intent);
                Log.debug(sClass, "Send " + fn + " to MediaScanner.");
            }
        } catch (Exception e) {
            Log.err(sClass, "Send files to MediaScanner: " + e.getMessage());
        }

        try {
            if (screenBroadcastReceiver != null)
                unregisterReceiver(screenBroadcastReceiver);
        } catch (Exception ignored) {
        }
        screenBroadcastReceiver = null;

        if (isRestart) {
            Log.debug(sClass, "isRestart");
            super.onDestroy();
            isRestart = false;
        } else {
            if (isFinishing()) {
                Log.debug(sClass, "isFinishing");
                if (GlobalCore.RunFromSplash) {

                    if (wakeLock != null) {
                        wakeLock.release();
                    }

                    TrackRecorder.getInstance().stopRecording();
                    // GPS Verbindung beenden
                    if (androidUIBaseMethods != null) androidUIBaseMethods.removeFromGPS();
                    CacheSelectionChangedListeners.getInstance().clear();
                    CacheListChangedListeners.getInstance().clear();
                    if (showViewListener != null)
                        showViewListener.onDestroyWithFinishing();

                    Settings.getInstance().acceptChanges(); // same as Config.settings.writeToDatabases();

                    Sprites.destroyCache();

                    CBDB.getInstance().close();
                    DraftsDatabase.getInstance().close();
                    SettingsDatabase.getInstance().close();

                }

                super.onDestroy();
                if (GlobalCore.RunFromSplash)
                    System.exit(0);
            } else {
                Log.debug(sClass, "isFinishing==false");
                showViewListener.onDestroyWithoutFinishing();

                SettingsDatabase.getInstance().close();
                CBDB.getInstance().close();
                DraftsDatabase.getInstance().close();

                super.onDestroy();
            }
        }
        Log.debug(sClass, "onDestroy AndroidApplication <=");
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
                    Platform.showView(ViewConst.TAKE_PHOTO, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_recordVideo) {
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PERMISSION_GRANTED) {
                    Platform.showView(ViewConst.VIDEO_REC, 0, 0, 0, 0, 0, 0);
                }
                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PERMISSION_GRANTED) {
                    Platform.showView(ViewConst.VIDEO_REC, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_recordVoice) {
                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PERMISSION_GRANTED) {
                    Platform.showView(ViewConst.VOICE_REC, 0, 0, 0, 0, 0, 0);
                }
            } else if (requestCode == Request_getLocationIfInBackground) {
                // result is only when requested (API level 29)
                if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[0] == PERMISSION_GRANTED) {
                    Log.debug(sClass, "onRequestPermissionsResult granted " + Manifest.permission.ACCESS_BACKGROUND_LOCATION);
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
        Log.debug(sClass, "=> onPrepareOptionsMenu");
        int menuId = showViewListener.getCurrentViewId();
        if (menuId != 0) {
            menu.clear();
            getMenuInflater().inflate(menuId, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void handleCacheSelectionChanged(Cache cache, Waypoint selectedWaypoint) {
        if (cache != null) {
            float distance;
            if (selectedWaypoint != null) {
                distance = selectedWaypoint.recalculateAndGetDistance();
            } else {
                distance = cache.recalculateAndGetDistance(CalculationType.FAST, false, Locator.getInstance().getMyPosition());
            }
            if (distance > Settings.SoundApproachDistance.getValue()) {
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
                    // Log.debug(sClass, "AltCorrection: " + String.valueOf(altCorrection));
                    Locator.getInstance().setAltCorrection(altCorrection);
                    // Höhenkorrektur ändert sich normalerweise nicht, einmal auslesen reicht...
                    androidUIBaseMethods.getLocationManager().removeNmeaListener(this);
                } catch (Exception ignored) {
                    // keine Höhenkorrektur vorhanden
                }
            }
        } catch (Exception e) {
            Log.err(sClass, "main.onNmeaReceived()", "", e);
        }
    }

    void restartFromSplash() {
        Log.debug(sClass, "=> Must restart from splash!");
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
                Log.debug(sClass, "wakeLock.acquire()");
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

            GL.that.runOnGL(() -> {
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
                Log.debug(sClass, "Number of geocaches: " + no + " in " + Database.Data.getDatabasePath());
            } else {
                Log.debug(sClass, "Number of geocaches: 0 (null)" + " in " + Database.Data.getDatabasePath());
            }
        } else {
            Log.debug(sClass, "Database not initialized.");
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
            Translation trans = new Translation(GlobalCore.workPath);
            try {
                trans.loadTranslation(Settings.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                trans.loadTranslation(Settings.Sel_LanguagePath.getDefaultValue());
            }
        }
    }

    private void initLocatorBase() {
        // initial Locator with saved Location
        double latitude = -1000;
        double longitude = -1000;

        latitude = Settings.mapInitLatitude.getValue();
        longitude = Settings.mapInitLongitude.getValue();

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
            Locator.getInstance().setUseImperialUnits(Settings.ImperialUnits.getValue());
        } catch (Exception e) {
            Log.err(sClass, "Error Initial Locator.UseImperialUnits");
        }

        // GPS update time?
        try {
            Locator.getInstance().setMinUpdateTime((long) Settings.gpsUpdateTime.getValue());
            Settings.gpsUpdateTime.addSettingChangedListener(() -> Locator.getInstance().setMinUpdateTime((long) Settings.gpsUpdateTime.getValue()));
        } catch (Exception e) {
            Log.err(sClass, "Error Initial Locator.MinUpdateTime");
        }

        // Use magnetic Compass?
        try {
            Locator.getInstance().setUseHardwareCompass(Settings.HardwareCompass.getValue());
            Settings.HardwareCompass.addSettingChangedListener(() -> Locator.getInstance().setUseHardwareCompass(Settings.HardwareCompass.getValue()));
        } catch (Exception e) {
            Log.err(sClass, "Error Initial Locator.UseHardwareCompass");
        }

        // Magnetic compass level
        try {
            Locator.getInstance().setHardwareCompassLevel(Settings.HardwareCompassLevel.getValue());
            Settings.HardwareCompassLevel.addSettingChangedListener(() -> Locator.getInstance().setHardwareCompassLevel(Settings.HardwareCompassLevel.getValue()));
        } catch (Exception e) {
            Log.err(sClass, "Error Initial Locator.HardwareCompassLevel");
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
                    } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        Energy.setDisplayOn();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }
}

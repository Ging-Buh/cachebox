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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.*;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.Window;
import android.widget.LinearLayout;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import de.droidcachebox.activities.CBForeground;
import de.droidcachebox.activities.Splash;
import de.droidcachebox.controls.HorizontalListView;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.AndroidDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Database.DatabaseType;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Android_TextInput;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.AndroidTexturePacker;
import de.droidcachebox.gdx.utils.AndroidContentClipboard;
import de.droidcachebox.gdx.utils.AndroidTextClipboard;
import de.droidcachebox.locator.*;
import de.droidcachebox.locator.Location.ProviderType;
import de.droidcachebox.locator.Locator.CompassType;
import de.droidcachebox.maps.BRouter;
import de.droidcachebox.menu.MainViewInit;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.*;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.CB_SLF4J;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.LogLevel;
import de.droidcachebox.views.forms.MessageBox;
import de.droidcachebox.views.forms.PleaseWaitMessageBox;

import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.droidcachebox.core.GroundspeakAPI.getSettingsAccessToken;

@SuppressWarnings("deprecation")
public class Main extends AndroidApplication implements SelectedCacheChangedEventListener, LocationListener, GpsStatus.NmeaListener, GpsStatus.Listener, CB_UI_Settings {
    private static final String sKlasse = "Main";
    public static boolean isCreated = false;
    private static boolean isRestart = false; // ???
    private final AtomicBoolean waitForGL = new AtomicBoolean(false);
    private final CB_List<de.droidcachebox.locator.GpsStrength> coreSatList = new CB_List<>(14);
    private SensorEventListener mSensorEventListener;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private HorizontalListView quickButtonListView;
    private PowerManager.WakeLock wakeLock;
    private int horizontalListViewHeight;
    private LinearLayout layoutTop;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private boolean lostCheck = false;
    private Dialog pWaitD;
    private LastState lastState;
    private IChanged handleSuppressPowerSavingConfigChanged, handleGpsUpdateTimeConfigChanged, handleImperialUnitsConfigChanged;
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
        handleGpsUpdateTimeConfigChanged = () -> {
            int updateTime1 = Config.gpsUpdateTime.getValue();
            try {
                androidUIBaseMethods.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime1, 1, Main.this);
            } catch (SecurityException sex) {
                Log.err(sKlasse, "Config.gpsUpdateTime changed: " + sex.getLocalizedMessage());
            }
        };
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
                    Database.Settings = new AndroidDB(DatabaseType.Settings, this);
                    Database.Settings.startUp(Config.workPath + "/User/Config.db3");
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
            Config.gpsUpdateTime.addSettingChangedListener(handleGpsUpdateTimeConfigChanged);
            Config.ImperialUnits.addSettingChangedListener(handleImperialUnitsConfigChanged);

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
                } else if (clipboardService instanceof android.text.ClipboardManager) {
                    PlatformUIBase.setClipboard(new AndroidTextClipboard((android.text.ClipboardManager) clipboardService));
                    Log.info(sKlasse, "got AndroidTextClipboard");
                }
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

            // ask for API key only if Rev-Number changed, like at new installation and API Key is Empty
            if (Config.newInstall.getValue() && getSettingsAccessToken().length() == 0) {
                askToGetApiKey();
            }
            if (!GlobalCore.restartAfterKill)
                if (!androidUIBaseMethods.isGPSon()) askToSwitchGpsOn();

            if (Config.newInstall.getValue()) {
                // wait for Copy Asset is closed
                checkTranslationIsLoaded();
                final Activity me = this;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            String langId = getString(R.string.langId);
                            String Welcome = Translation.that.getTextFile("welcome", langId) + Translation.that.getTextFile("changelog", langId);
                            MessageBox.show(me, Welcome, Translation.get("welcome"), MessageBoxIcon.None);
                        });
                    }
                }, 5000);

            }

            // static Event Lists
            SelectedCacheChangedEventListeners.getInstance().add(this);

            Config.AcceptChanges();

        } else {
            Log.info(sKlasse, "restartFromSplash: cannot start Main without previous Splash");
            restartFromSplash();
        }

        if (input == null) {
            Log.err(sKlasse, "gdx input not yet / no longer initialized");
                /*
                // should be != null : initialized by gdxview = initializeForView(GL.that, gdxConfig); in initializeGDXAndroidApplication();
                graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
                 */
        }

        initializeLocatorBaseMethods();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, CBForeground.class));
        }
        /*
        // not necessary
        else {
            // startService(new Intent(this, CBForeground.class));
        }
         */

        isCreated = true;
        Log.info(sKlasse, "onCreate <=");
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
            savedInstanceState.putString("selectedWayPoint", GlobalCore.getSelectedWayPoint().getGcCode());

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
            }
        }

        GL.that.restartRendering(); // does ViewGL.renderContinous();

        if (lastState == LastState.onStop) {
            Log.info(sKlasse, "=> Resume from Stop");
            showWaitToRenderStarted();
            InvalidateTextureEventList.Call();
        } else {
            Log.info(sKlasse, "=> onResume");
        }

        OnResumeListeners.getInstance().onResume();

        if (input == null) {
            Log.err(sKlasse, "(input == null) : init input needed for super.onResume()");
            /*
            graphics = new AndroidGraphics(this, gdxConfig, gdxConfig.resolutionStrategy == null ? new FillResolutionStrategy() : gdxConfig.resolutionStrategy);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            input = new AndroidInput(this, inflater.getContext(), graphics.getView(), gdxConfig);
             */
        }

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
        if (Config.AktLogLevel.getEnumValue() == LogLevel.OFF) Config.AktLogLevel.setEnumValue(LogLevel.ERROR);
        CB_SLF4J.getInstance(Config.workPath).setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());

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

        if (input == null) {
            Log.err(sKlasse, "(input == null) : init input needed for super.onPause()");
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
                    androidUIBaseMethods.getLocationManager().removeUpdates(this);
                    SelectedCacheChangedEventListeners.getInstance().clear();
                    CacheListChangedListeners.getInstance().clear();
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
        float distance = cache.recalculateAndGetDistance(CalculationType.FAST, false, Locator.getInstance().getMyPosition());
        if (waypoint != null) {
            distance = waypoint.getDistance();
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

        de.droidcachebox.locator.Location cbLocation = new de.droidcachebox.locator.Location(androidLocation.getLatitude(), androidLocation.getLongitude(), androidLocation.getAccuracy());
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

    private void askToGetApiKey() {
        MessageBox.show(this, Translation.get("wantApi"), Translation.get("welcome"), MessageBoxButton.YesNo, MessageBoxIcon.GC_Live,
                (dialog, button) -> {
                    switch (button) {
                        case -1:
                            // yes get Api key
                            androidUIBaseMethods.getApiKey();
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
                if (wakeLock.isHeld()) {
                    wakeLock.acquire();
                } else {
                    // even if not held, you must acquire to change behavior of powerservice
                    wakeLock.acquire();
                }
            }
        }
    }

    private void showWaitToRenderStarted() {
        if (!GL.that.getAllisInitialized())
            return;

        if (pWaitD == null) {

            pWaitD = PleaseWaitMessageBox.show(Translation.get("waitForGL"), "", MessageBoxButton.NOTHING, MessageBoxIcon.None, null);

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

    /**
     * überprüft ob das GPS eingeschaltet ist. Wenn nicht, wird eine Meldung
     * ausgegeben.
     */
    private void askToSwitchGpsOn() {
        try {
            if (Config.Ask_Switch_GPS_ON.getValue()) {
                checkTranslationIsLoaded();
                final Activity me = this;
                runOnUiThread(() -> MessageBox.show(me, Translation.get("GPSon?"), Translation.get("GPSoff"), MessageBoxButton.YesNo, MessageBoxIcon.Question, (dialog, button) -> {
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
            Translation trans = new Translation(Config.workPath, FileType.Internal);
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
                latitude = Config.MapInitLatitude.getValue();
                longitude = Config.MapInitLongitude.getValue();
            } catch (Exception ignored) {
            }
        }

        ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;
        de.droidcachebox.locator.Location initialLocation;
        if (provider == ProviderType.Saved) {
            initialLocation = new de.droidcachebox.locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
        } else {
            initialLocation = de.droidcachebox.locator.Location.NULL_LOCATION;
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
        if (androidUIBaseMethods.getLocationManager() == null)
            return;

        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            GpsStatus status;
            try {
                status = androidUIBaseMethods.getLocationManager().getGpsStatus(null);
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

            GPS.setSatFixes(fixed);
            GPS.setSatVisible(satellites);
            GPS.setSatList(coreSatList);
            GpsStateChangeEventList.Call();
            if (fixed < 1 && (Locator.getInstance().isFixed())) {
                if (!lostCheck) {
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (GPS.getFixedSats() < 1)
                                Locator.getInstance().FallBack2Network();
                            lostCheck = false;
                        }
                    };
                    timer.schedule(task, 1000);
                }

            }
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

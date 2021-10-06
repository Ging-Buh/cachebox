package de.droidcachebox;

import static android.content.Intent.ACTION_VIEW;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.activities.CBForeground;
import de.droidcachebox.activities.GcApiLogin;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.SQLiteClass;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.ex_import.GPXFileImporter;
import de.droidcachebox.ex_import.Importer;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.activities.FZKDownload;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.views.GeoCacheListListView;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.GPS;
import de.droidcachebox.locator.GpsStateChangeEventList;
import de.droidcachebox.locator.GpsStrength;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.LayerManager;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.SettingInt;
import de.droidcachebox.settings.SettingString;
import de.droidcachebox.settings.SettingsActivity;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.log.Log;

public class AndroidUIBaseMethods implements PlatformUIBase.Methods, LocationListener {
    private static final String sClass = "PlatformListener";
    private static final int REQUEST_GET_APIKEY = 987654321;
    private static final int ACTION_OPEN_DOCUMENT_TREE = 6518;
    private static final int useGNSS = Build.VERSION_CODES.N;
    private final AndroidApplication androidApplication;
    private final Activity mainActivity;
    private final Main mainMain;
    private final String defaultBrowserPackageName;
    private final CB_List<GpsStrength> coreSatList = new CB_List<>(14);
    private AtomicBoolean torchAvailable = null;
    private Camera deviceCamera;
    private SharedPreferences androidSetting;
    private SharedPreferences.Editor androidSettingEditor;
    private AndroidEventListener handlingGetApiAuth;
    private boolean mustShowCacheList = true;
    private CancelWaitDialog wd;
    private LocationManager locationManager;
    private AndroidEventListener handlingGetDirectoryAccess;
    private Intent locationServiceIntent;
    private GnssStatus.Callback gnssStatusCallback;
    private android.location.GpsStatus.Listener gpsStatusListener;
    private boolean lostCheck = false;
    private boolean askForLocationPermission;

    AndroidUIBaseMethods(Main main) {
        androidApplication = main;
        mainActivity = main;
        mainMain = main;
        OnResumeListeners.getInstance().addListener(AndroidUIBaseMethods.this::handleExternalRequest);
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        final ResolveInfo resolveInfo = mainActivity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null)
            defaultBrowserPackageName = resolveInfo.activityInfo.packageName;
        else
            defaultBrowserPackageName = "android";

        IChanged handleAllowLocationServiceConfigChanged = this::changeLocationService;
        Config.allowLocationService.addSettingChangedListener(handleAllowLocationServiceConfigChanged);
        IChanged handleGpsUpdateTimeConfigChanged = () -> {
            int updateTime1 = Config.gpsUpdateTime.getValue();
            try {
                getLocationManager().requestLocationUpdates(GPS_PROVIDER, updateTime1, 1, this);
            } catch (SecurityException sex) {
                Log.err(sClass, "Config.gpsUpdateTime changed: " + sex.getLocalizedMessage());
            }
        };
        Config.gpsUpdateTime.addSettingChangedListener(handleGpsUpdateTimeConfigChanged);

        if (Build.VERSION.SDK_INT >= useGNSS) {
            gnssStatusCallback = new GnssStatus.Callback() {

                @Override
                public void onSatelliteStatusChanged(final GnssStatus status) {
                    final int satellites = status.getSatelliteCount();
                    int fixed = 0;
                    coreSatList.clear();
                    for (int satelliteNr = 0; satelliteNr < satellites; satelliteNr++) {
                        if (status.usedInFix(satelliteNr)) {
                            fixed++;
                            coreSatList.add(new GpsStrength(true, satelliteNr));
                        } else {
                            coreSatList.add(new GpsStrength(false, satelliteNr));
                        }
                    }
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

                @Override
                public void onStarted() {
                    // GPS has startet
                    Log.debug(sClass, "Gnss started");
                }

                @Override
                public void onStopped() {
                    // GPS has stopped
                    Log.debug(sClass, "Gnss stopped");
                }
            };
        } else {
            gpsStatusListener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {

                    if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        final GpsStatus status = getLocationManager().getGpsStatus(null);

                        int satellites = 0;
                        int fixed = 0;
                        coreSatList.clear();
                        for (final GpsSatellite satellite : status.getSatellites()) {
                            satellites++;
                            if (satellite.usedInFix()) {
                                fixed++;
                                coreSatList.add(new GpsStrength(true, satellite.getSnr()));
                            } else {
                                coreSatList.add(new GpsStrength(false, satellite.getSnr()));
                            }
                        }

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
            };
        }

    }

    @Override
    public void writePlatformSetting(SettingBase<?> setting) {
        if (androidSetting == null)
            androidSetting = mainActivity.getSharedPreferences(Global.PreferencesNAME, 0);
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
    public SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        if (androidSetting == null)
            androidSetting = mainActivity.getSharedPreferences(Global.PreferencesNAME, 0);
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
    public boolean isOnline() {
        // isOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    @Override
    public boolean isGPSon() {
        boolean ret = getLocationManager().isProviderEnabled(GPS_PROVIDER);
        if (!ret && Config.Ask_Switch_GPS_ON.getValue())
            mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // dialog gps ein
        return ret;
    }

    @Override
    public void vibrate() {
        if (Config.vibrateFeedback.getValue())
            ((Vibrator) Objects.requireNonNull(mainActivity.getSystemService(Context.VIBRATOR_SERVICE))).vibrate(Config.VibrateTime.getValue());
    }

    @Override
    public boolean isTorchAvailable() {
        if (torchAvailable == null) {
            torchAvailable = new AtomicBoolean();
            torchAvailable.set(mainActivity.getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH));
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
            Camera.Parameters p = deviceCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
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
        try {
            Log.info(sClass, "switchToGpsMeasure()");
            int updateTime = Config.gpsUpdateTime.getValue();
            getLocationManager().requestLocationUpdates(GPS_PROVIDER, updateTime, 0, this);
        } catch (SecurityException ex) {
            Log.err(sClass, "switchToGpsMeasure: ", ex);
        }
    }

    @Override
    public void switchToGpsDefault() {
        Log.info(sClass, "switchtoGpsDefault()");
        int updateTime = Config.gpsUpdateTime.getValue();
        try {
            getLocationManager().requestLocationUpdates(GPS_PROVIDER, updateTime, 1, this);
        } catch (SecurityException sex) {
            Log.err(sClass, "switchtoGpsDefault: " + sex.getLocalizedMessage());
        }
    }

    @Override
    public void getApiKey() {
        Intent intent = new Intent().setClass(mainActivity, GcApiLogin.class);
        if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
            if (handlingGetApiAuth == null)
                handlingGetApiAuth = (requestCode, resultCode, data) -> {
                    androidApplication.removeAndroidEventListener(handlingGetApiAuth);
                    if (requestCode == REQUEST_GET_APIKEY) {
                        GL.that.RunIfInitial(SettingsActivity::resortList);
                        Config.acceptChanges();
                    }
                };
            androidApplication.addAndroidEventListener(handlingGetApiAuth);
            mainActivity.startActivityForResult(intent, REQUEST_GET_APIKEY);
        } else {
            Log.err(sClass, "GcApiLogin class not found");
        }
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
            intent.setDataAndType(uri, "text/html");
            if (defaultBrowserPackageName.equals("android")) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // The BROWSABLE category is required to get links from web pages.
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
            } else {
                intent.setPackage(defaultBrowserPackageName);
            }
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                Log.info(sClass, "Start activity for " + uri.toString());
            } else {
                Log.err(sClass, "Activity for " + url + " not visible. (" + defaultBrowserPackageName + "). Try startActivity(intent) anyway.");
                // Toast.makeText(mainActivity, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", Toast.LENGTH_LONG).show();
                // start independent from visibility ( Android 11 hides, even if invisible, a browser starts! )
            }
            mainActivity.startActivity(intent);
        } catch (Exception ex) {
            Log.err(sClass, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", ex);
        }
    }

    @Override
    public void startPictureApp(String fileName) {
        Uri uriToImage = Uri.fromFile(new java.io.File(fileName));
        Intent shareIntent = new Intent(ACTION_VIEW);
        shareIntent.setDataAndType(uriToImage, "image/*");
        mainActivity.startActivity(Intent.createChooser(shareIntent, mainActivity.getResources().getText(R.string.app_name)));
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
    public void quit() {
        if (GlobalCore.isSetSelectedCache()) {
            // speichere selektierten Cache, da nicht alles über die
            // SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Config.acceptChanges();
            Log.info(sClass, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGeoCacheCode());
        }
        CBDB.getInstance().close();
        mainActivity.finish();
    }

    @Override
    public void handleExternalRequest() {
        // viewmanager must have been initialized
        final Bundle extras = mainActivity.getIntent().getExtras();
        if (extras != null) {
            // Log.trace(sKlasse, "prepared Request from splash");
            if (ViewManager.that.isInitialized()) {
                String externalRequestGCCode = extras.getString("GcCode");
                if (externalRequestGCCode != null) {
                    Log.info(sClass, "importCacheByGCCode");
                    mainActivity.getIntent().removeExtra("GcCode");
                    importCacheByGCCode(externalRequestGCCode);
                }
                String externalRequestGpxPath = extras.getString("GpxPath");
                if (externalRequestGpxPath != null) {
                    mainActivity.getIntent().removeExtra("GpxPath");
                    if (externalRequestGpxPath.endsWith(".map")) {
                        AbstractFile sourceFile = FileFactory.createFile(externalRequestGpxPath);
                        AbstractFile destinationFile = FileFactory.createFile(FZKDownload.getInstance().getPathForMapFile(), sourceFile.getName());
                        boolean result = sourceFile.renameTo(destinationFile);
                        String sResult = result ? " ok!" : " no success!";
                        Log.info(sClass, "Move map-file " + destinationFile.getPath() + sResult);
                        if (result) LayerManager.getInstance().initLayers();
                    } else {
                        Log.info(sClass, "importGPXFile (*.gpx or *.zip)");
                        importGPXFile(externalRequestGpxPath);
                    }
                }
                String externalRequestGuid = extras.getString("Guid");
                if (externalRequestGuid != null) {
                    Log.info(sClass, "importCacheByGuid");
                    mainActivity.getIntent().removeExtra("Guid");
                    importCacheByGuid();
                }
                String externalRequestLatLon = extras.getString("LatLon");
                if (externalRequestLatLon != null) {
                    Log.info(sClass, "positionLatLon");
                    mainActivity.getIntent().removeExtra("LatLon");
                    positionLatLon(externalRequestLatLon);
                }
                String externalRequestMapDownloadPath = extras.getString("MapDownloadPath");
                if (externalRequestMapDownloadPath != null) {
                    Log.info(sClass, "MapDownload");
                    mainActivity.getIntent().removeExtra("MapDownloadPath");
                    FZKDownload.getInstance().importByUrl(externalRequestMapDownloadPath);
                    GL.that.showActivity(FZKDownload.getInstance());
                    FZKDownload.getInstance().importByUrlFinished();
                }
                String externalRequestName = extras.getString("Name");
                if (externalRequestName != null) {
                    Log.info(sClass, "importCacheByName");
                    mainActivity.getIntent().removeExtra("Name");
                    importCacheByName();
                }
            }
        }
    }

    @Override
    public String removeHtmlEntyties(String text) {
        /*
        if (android.os.Build.VERSION.SDK_INT >= N)
            return android.text.Html.fromHtml(text, FROM_HTML_MODE_LEGACY).toString();
        else return text.replaceAll("\\<[^>]*>","");
         */
        return HtmlCompat.fromHtml(text, FROM_HTML_MODE_LEGACY).toString();
    }

    @Override
    public String getFileProviderContentUrl(String localFileName) {
        return FileProvider.getUriForFile(mainActivity, "de.droidcachebox.android.fileprovider", new File(localFileName)).toString();
    }

    @Override
    public void getDirectoryAccess(String _DirectoryToAccess) {
        // Choose a directory using the system's file picker.
        final Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            // Optionally, specify a URI for the directory that should be opened in the system file picker when it loads.
            intent.putExtra(DocumentsContract.EXTRA_INFO, _DirectoryToAccess);
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                if (handlingGetDirectoryAccess == null)
                    handlingGetDirectoryAccess = (requestCode, resultCode, resultData) -> {
                        androidApplication.removeAndroidEventListener(handlingGetDirectoryAccess);
                        // Intent Result Record Video
                        if (requestCode == ACTION_OPEN_DOCUMENT_TREE) {
                            if (resultCode == Activity.RESULT_OK) {
                                // The result data contains a URI for the document or directory that the user selected.
                                GlobalCore.selectedUri = null;
                                if (resultData != null) {
                                    GlobalCore.selectedUri = resultData.getData();
                                    // Perform actions using its URI.
                                }
                            }
                        }
                    };
            }
            androidApplication.addAndroidEventListener(handlingGetDirectoryAccess);
            mainActivity.startActivityForResult(intent, ACTION_OPEN_DOCUMENT_TREE);
        }
    }

    @Override
    public void startRecordTrack() {
        request_getLocationIfInBackground();
        // if permission is not granted, recording must be done with cb always in foreground
        TrackRecorder.startRecording();
    }

    @Override
    public boolean request_getLocationIfInBackground() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && Config.showGPSDisclosure.getValue()) {
                String permissionlabel = "";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    permissionlabel = mainActivity.getPackageManager().getBackgroundPermissionOptionLabel().toString();
                }

                // hint & explanation
                MsgBox.show(
                        Translation.get("PleaseConfirm") + "\n\n" + permissionlabel + "\n\n" + Translation.get("GPSDisclosureText"),
                        Translation.get("GPSDisclosureTitle"),
                        MsgBoxButton.YesNo,
                        MsgBoxIcon.Information,
                        new MsgBox.OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int btnNumber, Object data) {
                                if (btnNumber == MsgBox.BTN_LEFT_POSITIVE) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                        final String[] requestedPermissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                                        ActivityCompat.requestPermissions(mainActivity, requestedPermissions, Main.Request_getLocationIfInBackground);
                                    }
                                    else {
                                        // frage trotzdem, aber es popt nicht mehr auf. Daher
                                        mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // dialog gps ein
                                    }
                                } else {
                                    // if you don't want, never ask again
                                    Config.showGPSDisclosure.setValue(false);
                                }
                                return true; // click is handled
                            }
                        }
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCacheCountInDB(String absolutePath) {
        try {
            SQLiteDatabase myDB = SQLiteDatabase.openDatabase(absolutePath, null, SQLiteDatabase.OPEN_READONLY);
            Cursor c = myDB.rawQuery("select count(*) from Caches", null);
            c.moveToFirst();
            int count = c.getInt(0);
            c.close();
            myDB.close();
            return count;
        } catch (Exception ignored) {
        }
        return 0;
    }

    private void positionLatLon(String externalRequestLatLon) {
        String[] s = externalRequestLatLon.split(",");
        Coordinate coordinate = new Coordinate(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        Log.info(sClass, "" + externalRequestLatLon + " " + s[0] + " , " + s[1] + "\n" + coordinate);
        if (coordinate.isValid()) {
            ShowMap.getInstance().execute();
            ShowMap.getInstance().normalMapView.setBtnMapStateToFree(); // btn
            // ShowMap.getInstance().normalMapView.setMapState(MapViewBase.MapState.FREE);
            ShowMap.getInstance().normalMapView.setCenter(new CoordinateGPS(coordinate.latitude, coordinate.longitude));
        }
    }

    private void importCacheByGuid() {
    }

    private void importCacheByGCCode(final String externalRequestGCCode) {
        TimerTask runTheSearchTasks = new TimerTask() {
            @Override
            public void run() {
                if (externalRequestGCCode != null) {
                    mainActivity.runOnUiThread(() -> {
                        if (mustShowCacheList) {
                            // show cachelist first then search dialog
                            mustShowCacheList = false;
                            ViewManager.leftTab.showView(GeoCacheListListView.getInstance());
                            importCacheByGCCode(externalRequestGCCode); // now the search can start (doSearchOnline)
                        } else {
                            mustShowCacheList = true;
                            if (SearchDialog.that == null) {
                                new SearchDialog();
                            }
                            SearchDialog.that.showNotCloseAutomaticly();
                            SearchDialog.that.doSearchOnline(externalRequestGCCode, SearchDialog.SearchMode.GcCode);
                        }
                    });
                }
            }
        };
        new Timer().schedule(runTheSearchTasks, 500);
    }

    private void importGPXFile(final String externalRequestGpxPath) {
        TimerTask gpxImportTask = new TimerTask() {
            @Override
            public void run() {
                Log.info(sClass, "ImportGPXFile");
                mainActivity.runOnUiThread(() -> wd = CancelWaitDialog.ShowWait(Translation.get("ImportGPX"), () -> wd.close(), new ICancelRunnable() {
                    @Override
                    public void run() {
                        Log.info(sClass, "Import GPXFile from " + externalRequestGpxPath + " started");
                        Date ImportStart = new Date();
                        Importer importer = new Importer();
                        ImporterProgress ip = new ImporterProgress();

                        CBDB.getInstance().getSql().beginTransaction();
                        try {
                            importer.importGpx(externalRequestGpxPath, ip);
                        } catch (Exception ignored) {
                        }
                        CBDB.getInstance().getSql().setTransactionSuccessful();
                        CBDB.getInstance().getSql().endTransaction();

                        wd.close();
                        CacheListChangedListeners.getInstance().cacheListChanged();
                        FilterProperties props = FilterInstances.getLastFilter();
                        EditFilterSettings.applyFilter(props);

                        long ImportZeit = new Date().getTime() - ImportStart.getTime();
                        String msg = "Import " + GPXFileImporter.CacheCount + "Caches\n" + GPXFileImporter.LogCount + "Logs\n in " + ImportZeit;
                        Log.info(sClass, msg.replace("\n", "\n\r") + " from " + externalRequestGpxPath);
                        GL.that.toast(msg);
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

    LocationManager getLocationManager() {
        return getLocationManager(false);
    }

    LocationManager getLocationManager(boolean forceInitialization) {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
            askForLocationPermission = true;
            return locationManager;
        }
        askForLocationPermission = false;
        if (locationManager == null || forceInitialization) {
            Log.info(sClass, "Initialise  location manager");

            // Get the (gps) location manager
            locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

            /*
            Longri: Ich habe die Zeiten und Distanzen der Locationupdates angepasst.
            Der Network Provider hat eine schlechte Genauigkeit,
            daher reicht es wenn er alle 10sec einen wert liefert und der alte um 300m abweicht.
            Beim GPS Provider habe ich die Aktualisierungszeit verkürzt.
            Bei deaktiviertem Hardware Kompass bleiben trotzdem die Werte noch in einem gesunden Verhältnis zwischen Performance und Stromverbrauch.
            Andere apps haben hier 0.
             */

            int updateTime = Config.gpsUpdateTime.getValue();

            try {
                locationManager.requestLocationUpdates(GPS_PROVIDER, updateTime, 1, this);
                locationManager.requestLocationUpdates(NETWORK_PROVIDER, 10000, 300, this);
                locationManager.addNmeaListener(mainMain); // for altitude correction: removed after first achieve (onNmeaReceived in main)

                if (Build.VERSION.SDK_INT >= useGNSS) {
                    locationManager.registerGnssStatusCallback(gnssStatusCallback);
                } else {
                    locationManager.addGpsStatusListener(gpsStatusListener);
                }

            } catch (SecurityException ex) {
                Log.err(sClass, "Config.gpsUpdateTime changed: ", ex);
            }
        }
        return locationManager;
    }

    void removeFromGPS() {
        if (Build.VERSION.SDK_INT >= useGNSS) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
        } else {
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }

    public boolean askForLocationPermission() {
        return askForLocationPermission;
    }

    public void resetAskForLocationPermission() {
        askForLocationPermission = false;
    }

    public void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Config.allowLocationService.getValue()) {
                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                    final String[] requestedPermissions = new String[]{Manifest.permission.FOREGROUND_SERVICE};
                    ActivityCompat.requestPermissions(mainActivity, requestedPermissions, Main.Request_ServiceOption);
                }
                else {
                    serviceCanBeStarted();
                }
            }
        }
    }

    public void serviceCanBeStarted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            locationServiceIntent = new Intent(androidApplication, CBForeground.class);
            androidApplication.startForegroundService(locationServiceIntent);
        }
    }

    public void stopService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!Config.allowLocationService.getValue()) {
                androidApplication.stopService(locationServiceIntent);
            }
        }
    }

    private void changeLocationService() {
        if (Config.allowLocationService.getValue()) {
            startService();
        } else {
            stopService();
        }
    }

    @Override
    public void onLocationChanged(Location receivedLocation) {
        // is fired from Android LocationListener: see getLocationManager in AndroidUIBaseMethods
        CBLocation.ProviderType provider;
        if (receivedLocation.getProvider().toLowerCase(new Locale("en")).contains("gps"))
            provider = CBLocation.ProviderType.GPS;
        else if (receivedLocation.getProvider().toLowerCase(new Locale("en")).contains("network"))
            provider = CBLocation.ProviderType.Network;
        else {
            provider = CBLocation.ProviderType.NULL;
        }
        Locator.getInstance().setNewLocation(new CBLocation(
                receivedLocation.getLatitude(),
                receivedLocation.getLongitude(),
                receivedLocation.getAccuracy(),
                receivedLocation.hasSpeed(),
                receivedLocation.getSpeed(),
                receivedLocation.hasBearing(),
                receivedLocation.getBearing(),
                receivedLocation.getAltitude(),
                provider));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ist obsolete, aber braucht eine leere Implementierung
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}

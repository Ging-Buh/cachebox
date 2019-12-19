package de.droidcachebox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import de.droidcachebox.activities.GcApiLogin;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.SQLiteClass;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.ex_import.GPXFileImporter;
import de.droidcachebox.ex_import.Importer;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.activities.FZKDownload;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.views.CacheListView;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.MapViewBase;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.settings.*;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.forms.Android_FileExplorer;

import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Intent.ACTION_VIEW;

public class AndroidUIBaseMethods implements PlatformUIBase.Methods {
    private static final String sKlasse = "PlatformListener";
    private static final int REQUEST_GET_APIKEY = 987654321;
    private AndroidApplication androidApplication;
    private Activity mainActivity;
    private Main mainMain;
    private AtomicBoolean torchAvailable = null;
    private Camera deviceCamera;
    private SharedPreferences androidSetting;
    private SharedPreferences.Editor androidSettingEditor;
    private AndroidEventListener handlingGetApiAuth;
    private boolean mustShowCacheList = true;
    private CancelWaitDialog wd;
    private LocationManager locationManager;

    AndroidUIBaseMethods(Main main) {
        androidApplication = main;
        mainActivity = main;
        mainMain = main;
        OnResumeListeners.getInstance().addListener(AndroidUIBaseMethods.this::handleExternalRequest);
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
        return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
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
        Log.info(sKlasse, "switchToGpsMeasure()");
        int updateTime = Config.gpsUpdateTime.getValue();
        try {
            getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 0, mainMain);
        } catch (SecurityException sex) {
            Log.err(sKlasse, "switchToGpsMeasure: " + sex.getLocalizedMessage());
        }
    }

    @Override
    public void switchtoGpsDefault() {
        Log.info(sKlasse, "switchtoGpsDefault()");
        int updateTime = Config.gpsUpdateTime.getValue();
        try {
            getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, mainMain);
        } catch (SecurityException sex) {
            Log.err(sKlasse, "switchtoGpsDefault: " + sex.getLocalizedMessage());
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
                        Config.AcceptChanges();
                    }
                };
            androidApplication.addAndroidEventListener(handlingGetApiAuth);
            mainActivity.startActivityForResult(intent, REQUEST_GET_APIKEY);
        } else {
            Log.err(sKlasse, "GcApiLogin class not found");
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
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setDataAndType(uri, "text/html");
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                Log.info(sKlasse, "Start activity for " + uri.toString());
                mainActivity.startActivity(intent);
            } else {
                Log.err(sKlasse, "Activity for " + url + " not installed.");
                Toast.makeText(mainActivity, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Log.err(sKlasse, Translation.get("Cann_not_open_cache_browser") + " (" + url + ")", ex);
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
    public void getFile(String initialPath, String extension, String TitleText, String ButtonText, PlatformUIBase.IgetFileReturnListener returnListener) {
        File mPath = FileFactory.createFile(initialPath);
        Android_FileExplorer fileDialog = new Android_FileExplorer(mainActivity, mPath, TitleText, ButtonText);
        fileDialog.setFileReturnListener(returnListener);
        fileDialog.showDialog();
    }

    @Override
    public void getFolder(String initialPath, String TitleText, String ButtonText, PlatformUIBase.IgetFolderReturnListener returnListener) {
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
        Database.Data.sql.close();
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
                    Log.info(sKlasse, "importCacheByGCCode");
                    mainActivity.getIntent().removeExtra("GcCode");
                    importCacheByGCCode(externalRequestGCCode);
                }
                String externalRequestGpxPath = extras.getString("GpxPath");
                if (externalRequestGpxPath != null) {
                    Log.info(sKlasse, "importGPXFile");
                    mainActivity.getIntent().removeExtra("GpxPath");
                    importGPXFile(externalRequestGpxPath);
                }
                String externalRequestGuid = extras.getString("Guid");
                if (externalRequestGuid != null) {
                    Log.info(sKlasse, "importCacheByGuid");
                    mainActivity.getIntent().removeExtra("Guid");
                    importCacheByGuid();
                }
                String externalRequestLatLon = extras.getString("LatLon");
                if (externalRequestLatLon != null) {
                    Log.info(sKlasse, "positionLatLon");
                    mainActivity.getIntent().removeExtra("LatLon");
                    positionLatLon(externalRequestLatLon);
                }
                String externalRequestMapDownloadPath = extras.getString("MapDownloadPath");
                if (externalRequestMapDownloadPath != null) {
                    Log.info(sKlasse, "MapDownload");
                    mainActivity.getIntent().removeExtra("MapDownloadPath");
                    FZKDownload.getInstance().importByUrl(externalRequestMapDownloadPath);
                    GL.that.showActivity(FZKDownload.getInstance());
                    FZKDownload.getInstance().importByUrlFinished();
                }
                String externalRequestName = extras.getString("Name");
                if (externalRequestName != null) {
                    Log.info(sKlasse, "importCacheByName");
                    mainActivity.getIntent().removeExtra("Name");
                    importCacheByName();
                }
            }
        }
    }

    private void positionLatLon(String externalRequestLatLon) {
        String[] s = externalRequestLatLon.split(",");
        Coordinate coordinate = new Coordinate(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        Log.info(sKlasse, "" + externalRequestLatLon + " " + s[0] + " , " + s[1] + "\n" + coordinate);
        if (coordinate.isValid()) {
            ShowMap.getInstance().execute();
            ShowMap.getInstance().normalMapView.setMapStateFree(); // btn
            ShowMap.getInstance().normalMapView.setMapState(MapViewBase.MapState.FREE);
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
                            ViewManager.leftTab.showView(CacheListView.getInstance());
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
                Log.info(sKlasse, "ImportGPXFile");
                mainActivity.runOnUiThread(() -> wd = CancelWaitDialog.ShowWait(Translation.get("ImportGPX"), () -> wd.close(), new ICancelRunnable() {
                    @Override
                    public void run() {
                        Log.info(sKlasse, "Import GPXFile from " + externalRequestGpxPath + " started");
                        Date ImportStart = new Date();
                        Importer importer = new Importer();
                        ImporterProgress ip = new ImporterProgress();

                        Database.Data.sql.beginTransaction();
                        try {
                            importer.importGpx(externalRequestGpxPath, ip);
                        } catch (Exception ignored) {
                        }
                        Database.Data.sql.setTransactionSuccessful();
                        Database.Data.sql.endTransaction();

                        wd.close();
                        CacheListChangedListeners.getInstance().cacheListChanged();
                        FilterProperties props = FilterInstances.getLastFilter();
                        EditFilterSettings.applyFilter(props);

                        long ImportZeit = new Date().getTime() - ImportStart.getTime();
                        String Msg = "Import " + GPXFileImporter.CacheCount + "Caches\n" + GPXFileImporter.LogCount + "Logs\n in " + ImportZeit;
                        Log.info(sKlasse, Msg.replace("\n", "\n\r") + " from " + externalRequestGpxPath);
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


    LocationManager getLocationManager() {

        try {
            if (locationManager != null) {
                return locationManager;
            }

            // GPS
            // Get the location manager
            locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, mainMain);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 300, mainMain);
                locationManager.addNmeaListener(mainMain); //
                locationManager.addGpsStatusListener(mainMain);
            } catch (SecurityException sex) {
                Log.err(sKlasse, "Config.gpsUpdateTime changed: " + sex.getLocalizedMessage());
            }

        } catch (Exception e) {
            Log.err(sKlasse, "getLocationManager()", "", e);
        }
        return locationManager;
    }

}

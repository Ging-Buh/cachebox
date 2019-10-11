package de.droidcachebox;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FZKDownload;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.OnResumeListeners;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingString;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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
import de.cb.sqlite.SQLiteClass;
import de.cb.sqlite.SQLiteInterface;
import de.droidcachebox.Activities.GcApiLogin;
import org.mapsforge.map.android.graphics.ext_AndroidGraphicFactory;

import java.io.ByteArrayOutputStream;
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

    public AndroidUIBaseMethods(Main main) {
        androidApplication = main;
        mainActivity = main;
        mainMain = main;
        OnResumeListeners.getInstance().addListener(this::handleExternalRequest);
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
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
        mainActivity.finish();
    }

    @Override
    public void handleExternalRequest() {
        // viewmanager must have been initialized
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


    @Override
    public byte[] getImageFromFile(String cachedTileFilename) {
        android.graphics.Bitmap result = BitmapFactory.decodeFile(cachedTileFilename);
        if (result != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            return b;
        }
        return null;
    }


    @Override
    public PlatformUIBase.ImageData getImagePixel(byte[] img) {
        android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
        // Buffer dst = null;
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        // bitmap.getPixels(pixels, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        PlatformUIBase.ImageData imgData = new PlatformUIBase.ImageData();
        imgData.width = bitmap.getWidth();
        imgData.height = bitmap.getHeight();
        imgData.PixelColorArray = pixels;

        return imgData;
    }

    @Override
    public byte[] getImageFromData(PlatformUIBase.ImageData imgData) {
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(imgData.PixelColorArray, imgData.width, imgData.height, android.graphics.Bitmap.Config.RGB_565);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public ext_GraphicFactory getGraphicFactory(float Scalefactor) {
        return ext_AndroidGraphicFactory.getInstance(Scalefactor);
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

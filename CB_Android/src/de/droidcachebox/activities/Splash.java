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
package de.droidcachebox.activities;

import static de.droidcachebox.settings.Config_Core.displayDensity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.DefaultAndroidFiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import de.droidcachebox.Global;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Main;
import de.droidcachebox.R;
import de.droidcachebox.components.CopyAssetFolder;
import de.droidcachebox.database.SQLiteClass;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.Handler;
import de.droidcachebox.gdx.math.DevicesSizes;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AndroidFileFactory;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.StringH;
import de.droidcachebox.utils.log.CB_SLF4J;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.LogLevel;

/**
 * what is this good for:
 * + write some values to the config database: installedRev, newInstall
 * + initialising some classes with good values:
 * + check if this (Intent) is called with "Params" in the Extras Bundle: if pass them to main
 * + at last starting the gdx AndroidApplication Main
 */
public class Splash extends Activity {
    private static final String sClass = "CB2 Splash";
    private boolean logIsInitialized;
    private Bitmap bitmap;
    private AlertDialog pleaseWaitDialog;
    private String workPath;
    private SharedPreferences androidSetting;
    private Bundle bundledData;
    private FrameLayout frame;
    private Activity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Plattform.used = Plattform.Android;
        androidSetting = getSharedPreferences(Global.PreferencesNAME, MODE_PRIVATE);
        logIsInitialized = false;
        if (!FileFactory.isInitialized()) {
            // so Main has not been started
            new AndroidFileFactory(); // used by CB_SLF4J, Translation, ...
        }
        // read workpath from Android Preferences (defValue = none)
        workPath = androidSetting.getString("WorkPath", "none");
        if (!workPath.equals("none")) {
            CB_SLF4J l = CB_SLF4J.getInstance(workPath);
            l.setLogLevel(LogLevel.INFO);
            logIsInitialized = true;
            info("Logging initialized");
        }
        info("onCreate called");

        bundledData = new Bundle();
        prepareBundledData();

        // settings for this class Activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Porträt erzwingen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        frame = findViewById(R.id.frameLayout1);

        // loadImages
        findViewById(R.id.splash_textViewDesc).setVisibility(View.INVISIBLE);
        findViewById(R.id.splash_textViewVersion).setVisibility(View.INVISIBLE);
        findViewById(R.id.splash_TextView).setVisibility(View.INVISIBLE);

        info("onCreate done.");
    }

    private void info(String text) {
        if (logIsInitialized) {
            Log.info(sClass, text);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        info("onStart");

        main = Main.getInstance();

        if (main == null) {
            initializationStep1();
        } else {
            startMain();
        }
    }

    private void startMain() {
        saveSDCardPathsToGlobalCore();
        GlobalCore.RunFromSplash = true;
        Intent mainIntent;
        if (main == null) {
            Log.info(sClass, "Start Main");
            mainIntent = new Intent().setClass(this, Main.class);
        } else {
            Log.info(sClass, "Connect to Main to onNewIntent(Intent)");
            mainIntent = main.getIntent();
        }
        int width = frame.getMeasuredWidth();
        int height = frame.getMeasuredHeight();
        int delaytime; // give splash the time to show: width/height != 0
        if (width == 0 || height == 0)
            delaytime = 1000;
        else
            delaytime = 0;
        new Handler().postDelayed(() -> {
            // could bundle utils too, but the (static) classes are initialized directly
            initializeSomeUiSettings(); // don't know, if it must be done here : frame is the space, where everything is shown
            if (SettingsDatabase.getInstance().isDatabaseNew()) {
                Settings.mapViewDPIFaktor.setValue(displayDensity);
            }
            Global.Paints.init(this);
            mainIntent.putExtras(bundledData); // the prepared Data
            Settings.getInstance().writeToDatabases(); // saving to SettingsDatabase
            SettingsDatabase.getInstance().close();
            startActivity(mainIntent);
            setResult(RESULT_OK); // for the calling App (setResult(resultCode, dataIntent));
            finish(); // this activity can be closed and back to the calling activity in onActivityResult
        }, delaytime);
    }

    private void prepareBundledData() {

        String GcCode = null;
        String guid = null;
        String name = null;
        String GpxPath = null;
        String LatLon = null;
        String downloadPath = null;

        final Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            // ask André what is the intention for this
            GcCode = intentExtras.getString("geocode");
            name = intentExtras.getString("name");
            guid = intentExtras.getString("guid");
        }

        final Uri uri = getIntent().getData();
        if (uri != null) {
            Log.info(sClass, "Intent Data:" + uri.toString());
            String scheme = uri.getScheme();
            if (scheme != null) {
                scheme = scheme.toLowerCase();
                switch (scheme) {
                    case "file":
                        // get this if created by the download manager
                        if (uri.getPath() != null) {
                            if (uri.getPath().endsWith(".gpx") || uri.getPath().endsWith(".zip")) {
                                GpxPath = uri.getPath();
                            } else if (uri.getPath().endsWith(".map")) {
                                GpxPath = uri.getPath();
                            }
                        }
                        break;
                    case "http":
                    case "https":
                        if (uri.getHost() != null && uri.getPath() != null) {
                            String uriHost = uri.getHost().toLowerCase(Locale.US);
                            String uriPath = uri.getPath().toLowerCase(Locale.US);
                            if (uriHost.contains("geocaching.com")) {
                                GcCode = uri.getQueryParameter("wp");
                                if (StringH.isEmpty(GcCode)) {
                                    int i1 = uriPath.indexOf("/gc") + 1;
                                    int i2 = uriPath.indexOf("_");
                                    if (i2 > i1) {
                                        GcCode = uriPath.substring(i1, i2);
                                    } else if (i1 > 0) {
                                        GcCode = uriPath.substring(i1);
                                    }
                                }
                                if (StringH.isEmpty(GcCode)) {
                                    guid = uri.getQueryParameter("guid");
                                    if (!StringH.isEmpty(guid)) {
                                        guid = guid.toLowerCase(Locale.US);
                                        if (guid.endsWith("#")) {
                                            guid = guid.substring(0, guid.length() - 1);
                                        }
                                    }
                                }
                            } else if (uriHost.contains("coord.info")) {
                                if (uriPath.startsWith("/gc")) {
                                    GcCode = uriPath.substring(1).toUpperCase(Locale.US);
                                }
                            } else if (uriHost.contains("download.openandromaps.org") || uriHost.contains("download.freizeitkarte-osm.de")) {
                                downloadPath = uri.toString();
                                if (!downloadPath.endsWith("zip")) downloadPath = null;
                            } else if (uri.toString().endsWith("map")) {
                                // mapsforge + mirror
                                downloadPath = uri.toString();
                            }
                        }
                        break;
                    case "geo":
                        LatLon = uri.getSchemeSpecificPart(); // will copy to clipboard
                        break;
                    case "content":
                        // to do
                        break;
                    default:
                        // download.openandromaps.org -> orux-map, backcountrynav-action-map, bikecomputer-map
                        downloadPath = uri.getSchemeSpecificPart();
                        if (downloadPath != null) {
                            downloadPath = "http:" + downloadPath;
                            if (!downloadPath.endsWith("zip")) downloadPath = null;
                        }
                }
            }
        }

        if (GcCode != null)
            bundledData.putSerializable("GcCode", GcCode);
        if (name != null)
            bundledData.putSerializable("Name", name);
        if (guid != null)
            bundledData.putSerializable("Guid", guid);
        if (GpxPath != null)
            bundledData.putSerializable("GpxPath", GpxPath);
        if (LatLon != null)
            bundledData.putSerializable("LatLon", LatLon);
        if (downloadPath != null)
            bundledData.putSerializable("MapDownloadPath", downloadPath);

    }

    private void initializeSomeUiSettings() {
        if (!UiSizes.getInstance().isInitialized()) {
            // class GlobalCore: displayDensity(Default for MapViewDPIFaktor, displayType, useSmallSkin
            int width = frame.getMeasuredWidth();
            int height = frame.getMeasuredHeight();
            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            if (height == 0 || width == 0) {
                Log.info(sClass, "Width/Height still 0, so calc from displaymetrics");
                height = displaymetrics.heightPixels;
                width = displaymetrics.widthPixels;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                int heightOfStatusBar = getResources().getDimensionPixelSize(resourceId);
                if (resourceId > 0) {
                    height = height - heightOfStatusBar;
                }
            }
            displayDensity = displaymetrics.density;
            int dpH = (int) (height / displayDensity + 0.5);
            int dpW = (int) (width / displayDensity + 0.5);
            if (dpH * dpW >= 960 * 720)
                GlobalCore.displayType = DisplayType.xLarge;
            else if (dpH * dpW >= 640 * 480)
                GlobalCore.displayType = DisplayType.Large;
            else if (dpH * dpW >= 470 * 320)
                GlobalCore.displayType = DisplayType.Normal;
            else
                GlobalCore.displayType = DisplayType.Small;

            // class UiSizes
            DevicesSizes ui = new DevicesSizes();
            ui.Density = displaymetrics.density;
            ui.isLandscape = false;
            ui.Window = new Size(width, height);
            UiSizes.getInstance().initialize(ui);
            // class GL_UISizes
            GL_UISizes.defaultDPI = displaymetrics.density;

            Log.info(sClass, "Screen width/height: " + ui.Window.width + "/" + ui.Window.height);
        }
    }

    // permissions
    private void initializationStep1() {
        ArrayList<String> neededPermissions = new ArrayList<>(
                Arrays.asList(
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            neededPermissions.add(Manifest.permission.CAMERA);
            neededPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), 170421);
        initializationStep2();
    }

    // workpath
    private void initializationStep2() {
        Global.initTheme(this);
        Global.initIcons(this);

        this.getFilesDir();// workaround for Android bug #10515463
        // String privateFilesDirectory = filesDirectory.getAbsolutePath(); // /data/data/de.droidcachebox/files
        Gdx.files = new DefaultAndroidFiles(this.getAssets(), this, true); // will be set automatically to this values in init of gdx's AndroidApplication
        String languagePath = androidSetting.getString("Sel_LanguagePath", "");
        if (languagePath.length() == 0) {
            // very  first start
            String locale = Locale.getDefault().getLanguage(); // en
            if (locale.equalsIgnoreCase("en")) {
                locale = "en-GB";
            } else if (locale.equalsIgnoreCase("pt")) {
                locale = "pt-PT";
            }
            languagePath = "data/lang/" + locale + "/strings.ini";
        }

        try {
            new Translation(workPath).loadTranslation(languagePath);
        } catch (Exception ex) {
            info(workPath);
            info(languagePath);
        }

        if (workPath.equals("none") || androidSetting.getBoolean("AskAgain", false) || !FileIO.directoryExists(workPath))
            // first Start || askOnEveryStart || workPath doesn't exist
            askForWorkPath();
        else {
            boolean hasWritePermission = FileIO.createDirectory(workPath + "/dummy");
            if (hasWritePermission)
                FileIO.deleteDirectory(FileFactory.createFile(workPath + "/dummy"));
            if (!hasWritePermission || FileIO.fileExists(workPath + "/askAgain.txt")) {
                askForWorkPath();
            } else {
                initializationStep3();
            }
        }
    }

    private void askForWorkPath() {
        try {
            Dialog dialog = createDialog();
            setSelectInternalSDButton(dialog);
            java.io.File[] dirs = getExternalFilesDirs(null);
            if (dirs.length > 1) {
                setSelectExternalSDButton(dialog);
            } else {
                Button btnExternalSandbox = dialog.findViewById(R.id.btnExternalSandbox);
                btnExternalSandbox.setVisibility(Button.INVISIBLE);
            }
            Button btnCreateWorkpath = dialog.findViewById(R.id.btnCreateWorkpath);
            btnCreateWorkpath.setVisibility(Button.INVISIBLE);
            dialog.show();
        } catch (Exception ex) {
            Log.err(sClass, "askForWorkPath Dialogs", ex);
        }
    }

    private Dialog createDialog() {
        final Dialog dialog = new Dialog(this) {
            @Override
            public boolean onKeyDown(int keyCode, @NotNull KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Splash.this.finishAffinity();
                }
                return super.onKeyDown(keyCode, event);
            }
        };
        dialog.setContentView(R.layout.sdselectdialog);

        TextView title = dialog.findViewById(R.id.select_sd_title);
        String titleText = Translation.get("selectWorkSpace") + "\n\n";
        title.setText(titleText);
        return dialog;
    }

    private void setSelectInternalSDButton(Dialog dialog) {
        Button btnInternal_SD = dialog.findViewById(R.id.btnInternal_SD);
        final String possibleWorkPath;
        String tmp = Environment.getExternalStorageDirectory().getPath() + "/CacheBox";
        boolean useOldCacheBoxDir = android.os.Build.VERSION.SDK_INT < 29;
        if (!useOldCacheBoxDir) {
            if (!FileIO.directoryExists(tmp)) {
                FileIO.createDirectory(tmp);
            }
            if (FileIO.directoryExists(tmp)) {
                useOldCacheBoxDir = FileIO.createDirectory(tmp + "/dummy");
                if (useOldCacheBoxDir)
                    FileIO.deleteDirectory(FileFactory.createFile(tmp + "/dummy"));
            }
        }
        if (useOldCacheBoxDir) {
            possibleWorkPath = tmp;
        } else {
            possibleWorkPath = getExternalFilesDir(null).getAbsolutePath();
        }
        String btnInternal_SDText = "Internal SD\n\n" + possibleWorkPath;
        btnInternal_SD.setText(btnInternal_SDText);
        btnInternal_SD.setOnClickListener(v -> {
            dialog.dismiss();
            new Thread() {
                @Override
                public void run() {
                    dialog.dismiss();
                    workPath = possibleWorkPath;
                    initializationStep3();
                }
            }.start();
        });
    }

    private void setSelectExternalSDButton(Dialog dialog) {
        Button btnExternalSandbox = dialog.findViewById(R.id.btnExternalSandbox);
        java.io.File[] dirs = getExternalFilesDirs(null);
        final String possibleWorkpath;
        possibleWorkpath = dirs[1].getPath();
        String btnExternalSandboxText = "External SD\n\n" + possibleWorkpath;
        btnExternalSandbox.setText(btnExternalSandboxText);
        btnExternalSandbox.setOnClickListener(v -> {
            dialog.dismiss();
            new Thread() {
                @Override
                public void run() {
                    dialog.dismiss();
                    workPath = possibleWorkpath;
                    initializationStep3();
                }
            }.start();
        });
    }

    private void initializationStep3() {
        GlobalCore.workPath = workPath;
        finishInitializationAndStartMain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug(sClass, "onDestroy");
        if (isFinishing()) {
            releaseImages();
        }
        super.onDestroy();
        if (pleaseWaitDialog != null && pleaseWaitDialog.isShowing()) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            this.finishAffinity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                this.finishAffinity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPleaseWaitDialog() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText("Copy resources");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(ll);

        pleaseWaitDialog = builder.create();
        pleaseWaitDialog.setTitle("Please wait! Bitte warten!"); // Copy resources
        pleaseWaitDialog.show();
        Window window = pleaseWaitDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(pleaseWaitDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            pleaseWaitDialog.getWindow().setAttributes(layoutParams);
        }
    }

    private void finishInitializationAndStartMain() {
        saveWorkPathToPreferences();

        Log.info(sClass, "Initialized for use with " + workPath);

        if (pleaseWaitDialog == null)
            runOnUiThread(this::showPleaseWaitDialog);

        CB_SLF4J.getInstance(workPath).setLogLevel(LogLevel.INFO);

        mediaInfo();

        boolean workPathFolderExists = FileIO.createDirectory(workPath);
        if (!workPathFolderExists) {
            Log.err(sClass, "Can't write to " + workPath);
            this.finishAffinity();
            return;
        }

        Log.info(sClass, "start Settings Database " + workPath + "/User/Config.db3");
        boolean userFolderExists = FileIO.createDirectory(workPath + "/User");
        if (!userFolderExists) {
            Log.err(sClass, "Can't create " + workPath + "/User");
            this.finishAffinity();
            return;
        }

        SettingsDatabase.getInstance().setSQL(new SQLiteClass(this));
        SettingsDatabase.getInstance().startUp(workPath + "/User/Config.db3");
        // Wenn die Settings DB neu erstellt wurde, müssen die Default Werte geschrieben werden.
        if (SettingsDatabase.getInstance().isDatabaseNew()) {
            Settings.getInstance().loadAllDefaultValues();
            Settings.getInstance().writeToDatabases();
            Log.info(sClass, "Default Settings written to new configDB.");
        } else {
            Settings.getInstance().readFromDB();
            Log.info(sClass, "Settings read from configDB.");
        }
        Settings.AktLogLevel.addSettingChangedListener(() -> CB_SLF4J.getInstance(workPath).setLogLevel((LogLevel) Settings.AktLogLevel.getEnumValue()));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // copy AssetFolder only if Rev-Number changed, like at new installation
        try {
            if (Settings.installedRev.getValue() < GlobalCore.getInstance().getCurrentRevision()) {
                String[] exclude = new String[]{"webkit", "sound", "sounds", "images", "skins", "lang", "kioskmode", "string-files", ""};
                CopyAssetFolder copyAssetFolder = new CopyAssetFolder();
                copyAssetFolder.copyAll(getAssets(), GlobalCore.workPath, exclude);

                Settings.installedRev.setValue(GlobalCore.getInstance().getCurrentRevision());
                Settings.newInstall.setValue(true);

                // create .nomedia Files
                FileIO.createFile(workPath + "/data/.nomedia");
                FileIO.createFile(workPath + "/skins/.nomedia");
                FileIO.createFile(workPath + "/repository/.nomedia");
                FileIO.createFile(workPath + "/Repositories/.nomedia");
                FileIO.createFile(workPath + "/cache/.nomedia");

            } else {
                Settings.newInstall.setValue(false);
            }
        } catch (Exception e) {
            Log.err(sClass, "Copy Asset", e);
        }

        Log.info(sClass, GlobalCore.getInstance().getVersionString());

        if (pleaseWaitDialog != null) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }

        startMain();
    }

    private void saveWorkPathToPreferences() {
        SharedPreferences settings = getSharedPreferences(Global.PreferencesNAME, 0);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("WorkPath", workPath);
        // Commit the edits!
        editor.apply();
    }

    private void releaseImages() {
        ((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

    }

    private void mediaInfo() {
        //<uses-permission android:name="android.permission.WRITE_MEDIA_STORAGE"></uses-permission> is only for system apps
        try {
            Log.info(sClass, "android.os.Build.VERSION.SDK_INT= " + android.os.Build.VERSION.SDK_INT);
            Log.info(sClass, "workPath set to " + workPath);
            Log.info(sClass, "getFilesDir()= " + getFilesDir());// user invisible
            Log.info(sClass, "Environment.getExternalStoragePublicDirectory()= " + Environment.getExternalStoragePublicDirectory("").getAbsolutePath());
            Log.info(sClass, "Environment.getExternalStorageDirectory()= " + Environment.getExternalStorageDirectory());
            Log.info(sClass, "getExternalFilesDir(null)= " + getExternalFilesDir(null));

            // normally [0] is the internal SD, [1] is the external SD
            File[] dirs = getExternalFilesDirs(null);
            for (int i = 0; i < dirs.length; i++) {
                Log.info(sClass, "get_ExternalFilesDirs[" + i + "]= " + dirs[i].getAbsolutePath());
            }
            // will be automatically created
				/*
				if (android.os.Build.VERSION.SDK_INT >= LOLLIPOP) {
					dirs = getExternalMediaDirs();
					for (int i = 0; i < dirs.length; i++) {
						Log.info(log, "getExternalMediaDirs[" + i + "]= " + dirs[i].getAbsolutePath());
					}
				}
				*/
        } catch (Exception e) {
            Log.err(sClass, e.getLocalizedMessage());
        }
    }

    private void saveSDCardPathsToGlobalCore() {
        java.io.File[] dirs = getExternalFilesDirs(null);
        String firstSDCard, secondSDCard;
        if (dirs.length > 0) {
            String tmp = dirs[0].getAbsolutePath();
            int pos = tmp.indexOf("Android") - 1;
            if (pos > 0)
                firstSDCard = tmp.substring(0, pos);
            else
                firstSDCard = "";
        } else {
            firstSDCard = "";
        }

        if (dirs.length > 1) {
            String tmp;
            try {
                tmp = dirs[1].getAbsolutePath();
                int pos = tmp.indexOf("Android") - 1;
                if (pos > 0)
                    secondSDCard = tmp.substring(0, pos);
                else
                    secondSDCard = "";
            } catch (Exception e) {
                secondSDCard = "";
            }
        } else {
            secondSDCard = "";
        }
        GlobalCore.firstSDCard = firstSDCard;
        GlobalCore.secondSDCard = secondSDCard;
    }

/*
    private Dialog msg;
    private int AdditionalWorkPathCount;
    private ArrayList<String> AdditionalWorkPathArray;

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);

                // set title
                alertDialogBuilder.setTitle("Sandbox");

                // set dialog message
                alertDialogBuilder.setMessage(Translation.get("Desc_Sandbox")).setCancelable(false).setPositiveButton(Translation.get("yes"),
                        (dialog12, id) -> {
                            // if this button is clicked, run Sandbox Path

                            showSandbox = true;
                            // Config.that.AcceptChanges();

                            // close select dialog
                            dialog12.dismiss();

                            // show please wait dialog
                            runOnUiThread(this::showPleaseWaitDialog);

                            // use external SD -> change workPath
                            new Thread() {
                                @Override
                                public void run() {
                                    workPath = externalSd2 + "/CacheBox";
                                    finishInitializationAndStartMain();
                                }
                            }.start();
                        }).setNegativeButton(Translation.get("no"), (dialog13, id) -> {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog13.cancel();
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();


    private void setWorkPathFromRedirectionFileIfExists() {
        // Zur Kompatibilität mit Älteren Installationen wird hier noch die redirection.txt abgefragt
        if (FileIO.fileExists(workPath + "/redirection.txt")) {
            BufferedReader Filereader;
            try {
                Filereader = new BufferedReader(new FileReader(workPath + "/redirection.txt"));
                String line;
                while ((line = Filereader.readLine()) != null) {
                    // chk ob der umleitungs Ordner existiert
                    if (FileIO.fileExists(line)) {
                        workPath = line;
                    }
                }
                Filereader.close();
            } catch (IOException e) {
                Log.err(log, "read redirection getWorkPathFromFile", e);
            }
        }
    }

            LinearLayout ll = dialog.findViewById(R.id.scrollViewLinearLayout);

            // add all Buttons for created Workpaths

            AdditionalWorkPathArray = getAdditionalWorkPathArray();

            for (final String _AdditionalWorkPath : AdditionalWorkPathArray) {
                final String Name = FileIO.getFileNameWithoutExtension(_AdditionalWorkPath);
                if (!FileFactory.createFile(_AdditionalWorkPath).exists()) {
                    deleteWorkPath(_AdditionalWorkPath);
                    continue;
                }
                if (!FileIO.canWrite(_AdditionalWorkPath)) {
                    deleteWorkPath(_AdditionalWorkPath);
                    continue;
                }

                Button btnAdditionalWorkpath = new Button(Splash.this);
                String btnAdditionalWorkpathText = Name + "\n\n" + _AdditionalWorkPath;
                btnAdditionalWorkpath.setText(btnAdditionalWorkpathText);
                btnAdditionalWorkpath.setOnLongClickListener(v -> {

                    // setting the MessageBox then the UI_sizes are not initial in this moment
                    Resources res = Splash.this.getResources();
                    float scale = res.getDisplayMetrics().density;
                    float calcBase = 533.333f * scale;

                    FrameLayout frame = findViewById(R.id.frameLayout1);
                    int width = frame.getMeasuredWidth();
                    int height = frame.getMeasuredHeight();

                    MessageBox.Builder.WindowWidth = width;
                    MessageBox.Builder.WindowHeight = height;
                    MessageBox.Builder.textSize = (calcBase / res.getDimensionPixelSize(R.dimen.BtnTextSize)) * scale;
                    MessageBox.Builder.ButtonHeight = (int) (50 * scale);

                    // Ask before delete
                    msg = MessageBox.show(this, Translation.get("shuredeleteWorkspace", Name), Translation.get("deleteWorkspace"), MsgBoxButton.YesNo, MsgBoxIcon.Question,
                            (dialog1, which) -> {
                                if (which == Dialog.BUTTON_POSITIVE) {
                                    // Delete this Workpath only from Settings don't delete any File
                                    deleteWorkPath(_AdditionalWorkPath);
                                }
                                // Start again to exclude the old Folder
                                msg.dismiss();
                                onStart();
                            });

                    dialog.dismiss();
                    return true;
                });
                btnAdditionalWorkpath.setOnClickListener(v -> {
                    // close select dialog
                    dialog.dismiss();

                    // show please wait dialog
                    runOnUiThread(this::showPleaseWaitDialog);

                    // use external SD -> change workPath
                    new Thread() {
                        @Override
                        public void run() {
                            workPath = _AdditionalWorkPath;
                            // boolean useTabletLayout = rbTabletLayout.isChecked();
                            finishInitializationAndStartMain();
                        }
                    }.start();

                });

                ll.addView(btnAdditionalWorkpath);
            }

            // with the changed (own) folder  get, the workspace can not be created, only selected
            Button btnCreateWorkpath = dialog.findViewById(R.id.btnCreateWorkpath);
            btnCreateWorkpath.setText(Translation.get("createWorkSpace"));
            btnCreateWorkpath.setOnClickListener(v -> {
                // close select dialog
                dialog.dismiss();
                getFolder(Translation.get("select_folder"), Translation.get("select"), path -> {
                    if (path.canWrite()) {
                        AdditionalWorkPathArray.add(path.getAbsolutePath());
                        Splash.this.writeAdditionalWorkPathArray(AdditionalWorkPathArray);
                        // Start again to include the new Folder
                        Splash.this.onStart();
                    } else {
                        String WriteProtectionMsg = Translation.get("NoWriteAcces");
                        Toast.makeText(Splash.this, WriteProtectionMsg, Toast.LENGTH_LONG).show();
                    }
                });
            });

    private ArrayList<String> getAdditionalWorkPathArray() {
        ArrayList<String> retList = new ArrayList<>();
        AdditionalWorkPathCount = androidSetting.getInt("AdditionalWorkPathCount", 0);
        for (int i = 0; i < AdditionalWorkPathCount; i++) {
            retList.add(androidSetting.getString("AdditionalWorkPath" + i, ""));
        }
        return retList;
    }

    private void writeAdditionalWorkPathArray(ArrayList<String> list) {
        Editor editor = androidSetting.edit();

        // first remove all
        for (int i = 0; i < AdditionalWorkPathCount; i++) {
            String delWorkPath = "AdditionalWorkPath" + i;
            editor.remove(delWorkPath);
        }
        editor.apply();

        int index = 0;
        for (String workpath : list) {
            String addWorkPath = "AdditionalWorkPath" + index;
            editor.putString(addWorkPath, workpath);
            index++;
        }
        AdditionalWorkPathCount = index;
        editor.putInt("AdditionalWorkPathCount", AdditionalWorkPathCount);
        editor.commit();
    }

    private void deleteWorkPath(String addWorkPath) {
        int index = AdditionalWorkPathArray.indexOf(addWorkPath);
        if (index >= 0)
            AdditionalWorkPathArray.remove(index);
        writeAdditionalWorkPathArray(AdditionalWorkPathArray);
    }

    // don't want to implement PlatformConnector for Splash, for only need of getFolder
    private void getFolder(String TitleText, String ButtonText, FileOrFolderPicker.IReturnAbstractFile returnListener) {
        Android_FileExplorer folderDialog = new Android_FileExplorer(this, FileFactory.createFile(""), TitleText, ButtonText);
        folderDialog.setFolderReturn(returnListener);
        folderDialog.showDialog();
    }

*/


}

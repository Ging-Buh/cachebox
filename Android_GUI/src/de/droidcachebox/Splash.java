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
package de.droidcachebox;

import CB_Core.Database;
import CB_Core.Database.DatabaseType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.DisplayType;
import CB_UI_Base.GL_UI.Handler;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.Log;
import CB_Utils.Log.LogLevel;
import CB_Utils.StringH;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFiles;
import de.CB_Utils.fileProvider.AndroidFileFactory;
import de.cb.sqlite.AndroidDB;
import de.droidcachebox.Components.copyAssetFolder;
import de.droidcachebox.Views.Forms.MessageBox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * what is this good for:
 * + write some values to the config database: installedRev, newInstall
 * + initialising some classes with good values:
 * + check if this (Intent) is called with "Params" in the Extras Bundle: if pass them to main
 * + at last starting the gdx AndroidApplication Main
 */
public class Splash extends Activity {
    private static final String log = "CB2 Splash";
    private Bitmap bitmap;
    private Dialog pleaseWaitDialog;
    private String workPath;
    private int AdditionalWorkPathCount;
    private Dialog msg;
    private ArrayList<String> AdditionalWorkPathArray;
    private SharedPreferences androidSetting;
    private SharedPreferences.Editor androidSettingEditor;
    private Boolean showSandbox;
    private Bundle bundeledData;
    private boolean askForWorkpath;
    private FrameLayout frame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        androidSetting = getSharedPreferences(Global.PreferencesNAME, MODE_PRIVATE);
        if (!FileFactory.isInitialized()) {
            // so Main has not been started
            new AndroidFileFactory(); // used by CB_SLF4J
            // read workpath from Android Preferences
            workPath = androidSetting.getString("WorkPath", Environment.getExternalStorageDirectory().getPath() + "/CacheBox");
            CB_SLF4J.getInstance(workPath).setLogLevel(LogLevel.INFO); // perhaps put this into androidSetting,setting: a "startLogLevel"
            Log.info(log, "Logging initialized");
        }
        Log.info(log, "onCreate called");

        bundeledData = new Bundle();
        prepareBundledData();

        // settings for this class Activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Porträt erzwingen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        frame = findViewById(R.id.frameLayout1);
        loadImages();
        Log.info(log, "onCreate finished.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.info(log, "onStart");
        if (!Main.isCreated) {
            startInitialization();
            if (askForWorkpath) {
                askForWorkPath(); // does finishInitializationAndStartMain()
            } else {
                finishInitializationAndStartMain();
            }
        } else {
            startMain();
        }
    }

    private void startMain() {
        GlobalCore.RunFromSplash = true;
        Intent mainIntent;
        if (!Main.isCreated) {
            Log.info(log, "Start Main");
            mainIntent = new Intent().setClass(this, Main.class);
        } else {
            mainIntent = Main.getInstance().getIntent();
            Log.info(log, "Connect to Main to onNewIntent(Intent)");
        }
        int width = frame.getMeasuredWidth();
        int height = frame.getMeasuredHeight();
        int delaytime = 0; // give splash the time to show: width/height != 0
        if (width == 0 || height == 0) delaytime = 1000;
        new Handler().postDelayed(() -> {
            // could bundle ui too, but the (static) classes are initialized directly
            initializeSomeUiSettings(); // don't know, if it must be done here : frame is the space, where everything is shown
            Global.Paints.init(this);
            mainIntent.putExtras(bundeledData); // the prepared Data
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
            // todo ask André what is the intention for this
            GcCode = intentExtras.getString("geocode");
            name = intentExtras.getString("name");
            guid = intentExtras.getString("guid");
        }

        final Uri intentData = getIntent().getData();
        if (intentData != null) {
            final Uri uri = intentData;
            String scheme = intentData.getScheme();
            if (scheme != null) {
                scheme = scheme.toLowerCase();
                switch (scheme) {
                    case "file":
                        if (uri.getEncodedPath().endsWith(".gpx") || uri.getEncodedPath().endsWith(".zip")) {
                            GpxPath = uri.getEncodedPath();
                        }
                        break;
                    case "http":
                    case "https":
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
                                // todo guid not yet implemented : implement fetch cache by  guid in main
                                guid = uri.getQueryParameter("guid");
                                if (!StringH.isEmpty(guid)) {
                                    guid = guid.toLowerCase(Locale.US);
                                    if (guid.endsWith("#")) {
                                        guid = guid.substring(0, guid.length() - 1);
                                    }
                                }
                            }
                        } else if (uriHost.contains("coord.info")) {
                            if (uriPath != null && uriPath.startsWith("/gc")) {
                                GcCode = uriPath.substring(1).toUpperCase(Locale.US);
                            }
                        } else if (uriHost.contains("download.openandromaps.org") || uriHost.contains("download.freizeitkarte-osm.de")) {
                            downloadPath = uri.toString();
                            if (!downloadPath.endsWith("zip")) downloadPath = null;
                        }
                        break;
                    case "geo":
                        LatLon = uri.getSchemeSpecificPart();
                        // todo
                        // we have no navigation but we can
                        // create a tempory waypoint on the map and go there
                        // or
                        // show map and center map there
                        break;
                    default:
                        // download.openandromaps.org -> orux-map, backcountrynav-action-map, bikecomputer-map
                        downloadPath = uri.getEncodedSchemeSpecificPart();
                        if (downloadPath != null) {
                            downloadPath = "http:" + downloadPath;
                            if (!downloadPath.endsWith("zip")) downloadPath = null;
                        }
                }
            }
        }

        if (GcCode != null)
            bundeledData.putSerializable("GcCode", GcCode);
        if (name != null)
            bundeledData.putSerializable("Name", name);
        if (guid != null)
            bundeledData.putSerializable("Guid", guid);
        if (GpxPath != null)
            bundeledData.putSerializable("GpxPath", GpxPath);
        if (LatLon != null)
            bundeledData.putSerializable("LatLon", LatLon);
        if (downloadPath != null)
            bundeledData.putSerializable("MapDownloadPath", downloadPath);

    }

    private void initializeSomeUiSettings() {
        if (!UiSizes.getInstance().isInitialized()) {
            // class GlobalCore: displayDensity(Default for MapViewDPIFaktor, displayType, useSmallSkin
            int width = frame.getMeasuredWidth();
            int height = frame.getMeasuredHeight();
            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            if (height == 0 || width == 0) {
                Log.info(log, "Width/Height still 0, so calc from displaymetrics");
                height = displaymetrics.heightPixels;
                width = displaymetrics.widthPixels;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                int heightOfStatusBar = getResources().getDimensionPixelSize(resourceId);
                if (resourceId > 0) {
                    height = height - heightOfStatusBar;
                }
            }
            GlobalCore.displayDensity = displaymetrics.density;
            int dpH = (int) (height / GlobalCore.displayDensity + 0.5);
            int dpW = (int) (width / GlobalCore.displayDensity + 0.5);
            if (dpH * dpW >= 960 * 720)
                GlobalCore.displayType = DisplayType.xLarge;
            else if (dpH * dpW >= 640 * 480)
                GlobalCore.displayType = DisplayType.Large;
            else if (dpH * dpW >= 470 * 320)
                GlobalCore.displayType = DisplayType.Normal;
            else
                GlobalCore.displayType = DisplayType.Small;
            GlobalCore.useSmallSkin = GlobalCore.displayType == DisplayType.Small;

            // class UiSizes
            DevicesSizes ui = new DevicesSizes();
            ui.Density = displaymetrics.density;
            ui.isLandscape = false;
            ui.Window = new Size(width, height);
            UiSizes.getInstance().initialize(ui);
            // class GL_UISizes
            GL_UISizes.defaultDPI = displaymetrics.density;

            Log.info(log, "Screen width/height: " + ui.Window.width + "/" + ui.Window.height);
        }
    }

    private void startInitialization() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PermissionCheck.checkNeededPermissions(this);
        }

        String privateFilesDirectory = getFilesDir().getAbsolutePath(); // /data/data/de.droidcachebox/files
        Gdx.files = new AndroidFiles(getAssets(), privateFilesDirectory); // will be set automatically to this values in init of gdx's AndroidApplication

        // read some setting from Android Preferences (Platform
        workPath = androidSetting.getString("WorkPath", Environment.getExternalStorageDirectory().getPath() + "/CacheBox");
        setWorkPathFromRedirectionFileIfExists();
        askForWorkpath = androidSetting.getBoolean("AskAgain", false)
                || !FileIO.directoryExists((Environment.getExternalStorageDirectory().getPath() + "/CacheBox"))
                || FileIO.fileExists(workPath + "/askAgain.txt");
        showSandbox = androidSetting.getBoolean("showSandbox", false);

        CB_SLF4J.getInstance(workPath).setLogLevel(LogLevel.INFO);
        Log.info(log, "onStart called");

        Global.initTheme(this);
        Global.initIcons(this);

        String languagePath = androidSetting.getString("Sel_LanguagePath", ""); // ""
        if (languagePath.length() == 0) {
            String locale = Locale.getDefault().getLanguage();
            if (locale.equalsIgnoreCase("en")) {
                locale = "en-GB";
            } else if (locale.equalsIgnoreCase("pt")) {
                locale = "pt-PT";
            }
            languagePath = "data/lang/" + locale + "/strings.ini";
        }
        try {
            new Translation(workPath, FileType.Internal).loadTranslation(languagePath);
        } catch (Exception ignored) {
        }
    }

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

    private void askForWorkPath() {
        workPath = Environment.getExternalStorageDirectory().getPath() + "/CacheBox";

        String externalSd = getExternalSdPath();  // externalSd = null or sandboxPath
        final String externalSd2 = externalSd;

        try {
            final Dialog dialog = new Dialog(this) {
                @Override
                public boolean onKeyDown(int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Splash.this.finish();
                    }
                    return super.onKeyDown(keyCode, event);
                }
            };
            dialog.setContentView(R.layout.sdselectdialog);

            TextView title = dialog.findViewById(R.id.select_sd_title);
            title.setText(Translation.get("selectWorkSpace") + "\n\n");

            Button btnInternal_SD = dialog.findViewById(R.id.btnInternal_SD);
            btnInternal_SD.setText("Internal SD\n\n" + workPath);
            btnInternal_SD.setOnClickListener(v -> {
                // close select dialog
                dialog.dismiss();

                // show please wait dialog
                showPleaseWaitDialog();

                // use internal SD -> nothing to change
                new Thread() {
                    @Override
                    public void run() {
                        // boolean useTabletLayout = rbTabletLayout.isChecked();
                        saveWorkPath();
                        dialog.dismiss();
                        finishInitializationAndStartMain();
                    }
                }.start();
            });

            Button btnExternalSandbox = dialog.findViewById(R.id.btnExternalSandbox);
            boolean hasExtSd = false;
            if (externalSd != null)
                if (externalSd.length() > 0)
                    if (!externalSd.equalsIgnoreCase(workPath))
                        hasExtSd = true;
            final boolean isSandbox = externalSd != null && externalSd.contains("Android/data/de.droidcachebox");
            if (hasExtSd) {
                String extSdText = isSandbox ? "External SD SandBox\n\n" : "External SD\n\n";
                btnExternalSandbox.setText(extSdText + externalSd);
                btnExternalSandbox.setOnClickListener(v -> {
                    if (isSandbox && !showSandbox) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);

                        // set title
                        alertDialogBuilder.setTitle("Sandbox");

                        // set dialog message
                        alertDialogBuilder.setMessage(Translation.get("Desc_Sandbox")).setCancelable(false).setPositiveButton(Translation.get("yes"),
                                (dialog12, id) -> {
                                    // if this button is clicked, run Sandbox Path

                                    showSandbox = true;
                                    // Config.AcceptChanges();

                                    // close select dialog
                                    dialog12.dismiss();

                                    // show please wait dialog
                                    showPleaseWaitDialog();

                                    // use external SD -> change workPath
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            workPath = externalSd2 + "/CacheBox";
                                            saveWorkPath();
                                            finishInitializationAndStartMain();
                                        }
                                    }.start();
                                }).setNegativeButton(Translation.get("no"), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog13, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog13.cancel();
                            }
                        });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    } else {
                        // close select dialog
                        dialog.dismiss();

                        // show please wait dialog
                        showPleaseWaitDialog();

                        // use external SD -> change workPath
                        new Thread() {
                            @Override
                            public void run() {
                                workPath = externalSd2;
                                saveWorkPath();
                                finishInitializationAndStartMain();
                            }
                        }.start();
                    }
                });
            } else {
                btnExternalSandbox.setVisibility(Button.INVISIBLE);
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
                btnAdditionalWorkpath.setText(Name + "\n\n" + _AdditionalWorkPath);
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
                    msg = MessageBox.show(this, Translation.get("shuredeleteWorkspace", Name), Translation.get("deleteWorkspace"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
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
                    showPleaseWaitDialog();

                    // use external SD -> change workPath
                    new Thread() {
                        @Override
                        public void run() {
                            workPath = _AdditionalWorkPath;
                            // boolean useTabletLayout = rbTabletLayout.isChecked();
                            saveWorkPath();
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
                getFolder("", Translation.get("select_folder"), Translation.get("select"), new PlatformUIBase.IgetFolderReturnListener() {
                    @Override
                    public void returnFolder(String path) {
                        if (FileIO.canWrite(path)) {
                            AdditionalWorkPathArray.add(path);
                            Splash.this.writeAdditionalWorkPathArray(AdditionalWorkPathArray);
                            // Start again to include the new Folder
                            Splash.this.onStart();
                        } else {
                            String WriteProtectionMsg = Translation.get("NoWriteAcces");
                            Toast.makeText(Splash.this, WriteProtectionMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            });

            dialog.show();

        } catch (Exception ex) {
            Log.err(log, "askForWorkPath Dialogs", ex);
        }
    }

    // don't want to implement PlatformConnector for Splash, for only need of getFolder
    public void getFolder(String initialPath, String TitleText, String ButtonText, PlatformUIBase.IgetFolderReturnListener returnListener) {
        File mPath = FileFactory.createFile(initialPath);
        Android_FileExplorer folderDialog = new Android_FileExplorer(this, mPath, TitleText, ButtonText);
        folderDialog.setSelectDirectoryOption();
        folderDialog.setFolderReturnListener(returnListener);
        folderDialog.showDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.debug(log, "onDestroy");
        if (isFinishing()) {
            releaseImages();
        }
        super.onDestroy();
        if (pleaseWaitDialog != null && pleaseWaitDialog.isShowing()) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPleaseWaitDialog() {
        pleaseWaitDialog = ProgressDialog.show(Splash.this, "In progress", "Copy resources");
        pleaseWaitDialog.show();
        TextView tv1 = pleaseWaitDialog.findViewById(android.R.id.message);
        tv1.setTextColor(Color.WHITE);
    }

    private String testExtSdPath(String extPath) {
        // this will test whether the extPath is an existing path to an external sd card
        if (extPath.equalsIgnoreCase(workPath))
            return null; // if this extPath is the same than the actual workPath -> this is the
        // internal SD, not
        // the external!!!
        try {
            if (FileIO.fileExists(extPath)) {
                StatFs stat = new StatFs(extPath);
                @SuppressWarnings("deprecation")
                long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
                if (bytesAvailable == 0) {
                    return null; // ext SD-Card is not plugged in -> do not use it
                } else {
                    // Check can Read/Write

                    File f = FileFactory.createFile(extPath);
                    if (f.canWrite()) {
                        if (f.canRead()) {
                            return f.getAbsolutePath() + "/CacheBox"; // ext SD-Card is plugged in
                        }
                    }

                    // Check can Read/Write on Application Storage
                    String appPath = this.getApplication().getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
                    int Pos = appPath.indexOf("/Android/data/");
                    String p = appPath.substring(Pos);
                    File fi = FileFactory.createFile(extPath + p);// "/Android/data/de.droidcachebox/files");
                    fi.mkdirs();
                    if (fi.canWrite()) {
                        if (fi.canRead()) {
                            return fi.getAbsolutePath() + "/CacheBox";
                        }
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveWorkPath() {

        SharedPreferences settings = this.getSharedPreferences(Global.PreferencesNAME, 0);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("WorkPath", workPath);
        // Commit the edits!
        editor.commit();
    }

    private String getExternalSdPath() {
        // we have minsdk >= KITKAT
        File sandbox = getExternalSandbox();
        if (sandbox != null) {
            return sandbox.getAbsolutePath();
        } else {
            return null;
        }
    }

    private ArrayList<String> getAdditionalWorkPathArray() {
        ArrayList<String> retList = new ArrayList<String>();
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
        editor.commit();

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

    private void finishInitializationAndStartMain() {
        Log.info(log, "Initialized for use with " + workPath);

        // show wait dialog if not running
        if (pleaseWaitDialog == null)
            showPleaseWaitDialog();

        CB_SLF4J.getInstance(workPath).setLogLevel(LogLevel.INFO);

        Log.info(log, "mediaInfo");
        mediaInfo();

        new Config(workPath);
        Log.info(log, "start Settings Database " + workPath + "/User/Config.db3");
        boolean userFolderExists = FileIO.createDirectory(workPath + "/User");
        if (!userFolderExists)
            return;
        Database.Settings = new AndroidDB(DatabaseType.Settings, this);
        Database.Settings.StartUp(workPath + "/User/Config.db3");
        // Wenn die Settings DB neu erstellt wurde, müssen die Default Werte geschrieben werden.
        if (Database.Settings.isDbNew()) {
            Config.settings.LoadAllDefaultValues();
            Config.settings.WriteToDB();
            Log.info(log, "Default Settings written to new configDB.");
        } else {
            Config.settings.ReadFromDB();
            Log.info(log, "Settings read from configDB.");
        }
        Config.AktLogLevel.addSettingChangedListener(() -> CB_SLF4J.getInstance(workPath).setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue()));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Database.Data = new AndroidDB(DatabaseType.CacheBox, this);

        // copy AssetFolder only if Rev-Number changed, like at new installation
        try {
            if (Config.installedRev.getValue() < GlobalCore.getInstance().getCurrentRevision()) {

                String[] exclude = new String[]{"webkit", "sound", "sounds", "images", "skins", "lang", "kioskmode", "string-files", ""};
                copyAssetFolder myCopie = new copyAssetFolder();
                myCopie.copyAll(getAssets(), Config.mWorkPath, exclude);

                Config.installedRev.setValue(GlobalCore.getInstance().getCurrentRevision());
                Config.newInstall.setValue(true);
                Config.AcceptChanges();

                // create .nomedia Files
                FileIO.createFile(workPath + "/data/.nomedia");
                FileIO.createFile(workPath + "/skins/.nomedia");
                FileIO.createFile(workPath + "/repository/.nomedia");
                FileIO.createFile(workPath + "/Repositories/.nomedia");
                FileIO.createFile(workPath + "/cache/.nomedia");

            } else {
                Config.newInstall.setValue(false);
            }
        } catch (Exception e) {
            Log.err(log, "Copy Asset", e);
        }

        Config.showSandbox.setValue(showSandbox);
        Config.AcceptChanges();

        Log.info(log, GlobalCore.getInstance().getVersionString());

        Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
        Database.Drafts = new AndroidDB(DatabaseType.Drafts, this);

        Config.AcceptChanges();

        if (pleaseWaitDialog != null) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }

        startMain();
    }

    private void loadImages() {
        findViewById(R.id.splash_textViewDesc).setVisibility(View.INVISIBLE);
        findViewById(R.id.splash_textViewVersion).setVisibility(View.INVISIBLE);
        findViewById(R.id.splash_TextView).setVisibility(View.INVISIBLE);
    }

    private void releaseImages() {
        ((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionCheck.MY_PERMISSIONS_REQUEST: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial

                for (String permission : PermissionCheck.NEEDED_PERMISSIONS) {
                    perms.put(permission, PackageManager.PERMISSION_GRANTED);
                }

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // check all
                ArrayList<String> deniedList = new ArrayList<String>();
                for (String permission : PermissionCheck.NEEDED_PERMISSIONS) {
                    if (perms.get(permission) != PackageManager.PERMISSION_GRANTED)
                        deniedList.add(permission);
                }

                if (!deniedList.isEmpty()) {
                    // Permission Denied
                    String br = System.getProperty("line.separator");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Some Permission is Denied");
                    sb.append(br);

                    for (String denied : deniedList) {
                        sb.append(denied);
                        sb.append(br);
                    }
                    sb.append(br);

                    sb.append("Cachbox will close");

                    Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

                    // close
                    this.finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private File getExternalSandbox() {
        java.io.File[] dirs = getExternalFilesDirs(null);
        if (dirs.length > 1) {
            return FileFactory.createFile(dirs[1].getAbsolutePath());
        }
        return null;
    }

    private void mediaInfo() {
        //<uses-permission android:name="android.permission.WRITE_MEDIA_STORAGE"></uses-permission> is only for system apps
        try {
            Log.info(log, "android.os.Build.VERSION.SDK_INT= " + android.os.Build.VERSION.SDK_INT);
            Log.info(log, "workPath set to " + workPath);
            Log.info(log, "getFilesDir()= " + getFilesDir());// user invisible
            Log.info(log, "Environment.getExternalStoragePublicDirectory()= " + Environment.getExternalStoragePublicDirectory("").getAbsolutePath());
            Log.info(log, "Environment.getExternalStorageDirectory()= " + Environment.getExternalStorageDirectory());
            Log.info(log, "getExternalFilesDir(null)= " + getExternalFilesDir(null));

            java.io.File[] dirss = getExternalFilesDirs(null);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                // normally [0] is the internal SD, [1] is the external SD
                java.io.File[] dirs = getExternalFilesDirs(null);
                for (int i = 0; i < dirs.length; i++) {
                    Log.info(log, "get_ExternalFilesDirs[" + i + "]= " + dirs[i].getAbsolutePath());
                }
                // will be automatically created
				/*
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
					dirs = getExternalMediaDirs();
					for (int i = 0; i < dirs.length; i++) {
						Log.info(log, "getExternalMediaDirs[" + i + "]= " + dirs[i].getAbsolutePath());
					}
				}
				*/
            }
        } catch (Exception e) {
            Log.err(log, e.getLocalizedMessage());
        }
    }

}

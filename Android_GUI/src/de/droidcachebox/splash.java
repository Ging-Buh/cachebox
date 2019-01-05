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
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.DisplayType;
import CB_UI_Base.Math.DevicesSizes;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.CB_SLF4J;
import CB_Utils.Log.Log;
import CB_Utils.Log.LogLevel;
import CB_Utils.Settings.*;
import CB_Utils.Settings.PlatformSettings.IPlatformSettings;
import CB_Utils.StringH;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.*;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFiles;
import de.CB_Utils.fileProvider.AndroidFileFactory;
import de.cb.sqlite.AndroidDB;
import de.droidcachebox.Components.copyAssetFolder;
import de.droidcachebox.Views.Forms.MessageBox;
import org.mapsforge.map.android.graphics.ext_AndroidGraphicFactory;
import org.mapsforge.map.model.DisplayModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * what is this good for:
 * + write some values to the config database:
 * installedRev, newInstall
 * + initialising some classes with good values:
 * ... etc todo continue infos
 * + check if this (Intent) is called with "Params" in the Extras Bundle: if pass them to main
 * + at last starting the gdx AndroidApplication main
 */
public class splash extends Activity {
    private static final String log = "splash";
    public static Activity splashActivity;
    private static DevicesSizes ui;
    protected int height;
    protected int width;
    private Bitmap bitmap;
    private Dialog pleaseWaitDialog;
    private String GcCode = null;
    private String guid = null;
    private String name = null;
    private String GpxPath = null;
    private String workPath;
    private IgetFolderReturnListener getFolderReturnListener;
    private int AdditionalWorkPathCount;
    private MessageBox msg;
    private ArrayList<String> AdditionalWorkPathArray;
    private SharedPreferences androidSetting;
    private SharedPreferences.Editor androidSettingEditor;
    private Boolean showSandbox;
    private boolean mSelectDbIsStarted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CB_SLF4J.changeLogLevel(LogLevel.ALL);
        splashActivity = this;

        if (!FileFactory.isInitial()) {
            new AndroidFileFactory();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash);

        DisplayMetrics displaymetrics = this.getResources().getDisplayMetrics();

        GlobalCore.displayDensity = displaymetrics.density;
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;

        int dpH = (int) (h / GlobalCore.displayDensity + 0.5);
        int dpW = (int) (w / GlobalCore.displayDensity + 0.5);

        if (dpH * dpW >= 960 * 720)
            GlobalCore.displayType = DisplayType.xLarge;
        else if (dpH * dpW >= 640 * 480)
            GlobalCore.displayType = DisplayType.Large;
        else if (dpH * dpW >= 470 * 320)
            GlobalCore.displayType = DisplayType.Normal;
        else
            GlobalCore.displayType = DisplayType.Small;

        // Porträt erzwingen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Check if use small skin
        GlobalCore.useSmallSkin = GlobalCore.displayType == DisplayType.Small;

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            GcCode = extras.getString("geocode");
            name = extras.getString("name");
            guid = extras.getString("guid");
        }

        final Uri uri = getIntent().getData();
        String LatLon = "";
        if (uri != null) {
            if (uri.getEncodedPath().endsWith(".gpx")) {
                GpxPath = uri.getEncodedPath();
            }
            if (GcCode == null && guid == null) {
                String uriHost = uri.getHost().toLowerCase(Locale.US);
                String uriPath = uri.getPath().toLowerCase(Locale.US);
                if (uri.getScheme().toLowerCase().startsWith("geo")) {
                    LatLon = uri.getSchemeSpecificPart();
                    // todo preperation for to create a tempory waypoint on the map and go there
                } else {
                    if (uriHost.contains("geocaching.com")) {
                        GcCode = uri.getQueryParameter("wp");
                        if (StringH.isEmpty(GcCode)) {
                            int i1 = uriPath.indexOf("/gc") + 1;
                            int i2 = uriPath.indexOf("_");
                            if (i2 > i1) {
                                GcCode = uriPath.substring(i1, i2);
                                Log.debug(log, "GCCode='" + GcCode + "'");
                            }
                            else if (i1 > 0) {
                                GcCode = uriPath.substring(i1);
                                Log.debug(log, "GCCode='" + GcCode + "'");
                            }
                        }
                        // todo guid not yet implemented : implement fetch cache by  guid in main
                        guid = uri.getQueryParameter("guid");

                        if (GcCode != null && GcCode.length() > 0) {
                            GcCode = GcCode.toUpperCase(Locale.US);
                            guid = null;
                        } else if (guid != null && guid.length() > 0) {
                            GcCode = null;
                            guid = guid.toLowerCase(Locale.US);
                            if (guid.endsWith("#")) {
                                guid = guid.substring(0, guid.length() - 1);
                            }
                        } else {
                            // warning.showToast(res.getString(R.string.err_detail_open));
                            finish();
                            return;
                        }
                    } else if (uriHost.contains("coord.info")) {
                        if (uriPath != null && uriPath.startsWith("/gc")) {
                            GcCode = uriPath.substring(1).toUpperCase(Locale.US);
                        } else {
                            // warning.showToast(res.getString(R.string.err_detail_open));
                            finish();
                            return;
                        }
                    }
                }
            }
        }

        if (main.mainActivity != null) {
            Bundle b = new Bundle();
            if (GcCode != null) {
                b.putSerializable("GcCode", GcCode);
                b.putSerializable("name", name);
                b.putSerializable("guid", guid);
            }
            if (GpxPath != null) {
                b.putSerializable("GpxPath", GpxPath);
            }
            b.putSerializable("latlon", LatLon);
            Intent mainIntent = main.mainActivity.getIntent();
            mainIntent.putExtras(b);
            Log.info(log, "startActivity mainIntent from splash.onCreate for created main.mainActivity (com.badlogic.gdx.backends.android.AndroidApplication)");
            startActivity(mainIntent);
            finish();
        }

        LoadImages();

        if (savedInstanceState != null) {
            mSelectDbIsStarted = savedInstanceState.getBoolean("SelectDbIsStartet");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        splashActivity = this;
        // Log.* ist erst nach StartInitial möglich
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PermissionCheck.checkNeededPermissions(this);
        }

        // initial GDX
        Gdx.files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        // first, try to find stored preferences of workPath
        androidSetting = this.getSharedPreferences(Global.PREFS_NAME, 0);

        workPath = androidSetting.getString("WorkPath", Environment.getDataDirectory() + "/cachebox");
        boolean askAgain = androidSetting.getBoolean("AskAgain", true);
        showSandbox = androidSetting.getBoolean("showSandbox", false);

        Global.initTheme(this);
        Global.InitIcons(this);

        CB_Android_FileExplorer fileExplorer = new CB_Android_FileExplorer(this);
        PlatformConnector.setGetFileListener(fileExplorer);
        PlatformConnector.setGetFolderListener(fileExplorer);

        String LangPath = androidSetting.getString("Sel_LanguagePath", "");
        if (LangPath.length() == 0) {
            // set default lang

            String locale = Locale.getDefault().getLanguage();
            if (locale.contains("de")) {
                LangPath = "data/lang/de/strings.ini";
            } else if (locale.contains("cs")) {
                LangPath = "data/lang/cs/strings.ini";
            } else if (locale.contains("cs")) {
                LangPath = "data/lang/cs/strings.ini";
            } else if (locale.contains("fr")) {
                LangPath = "data/lang/fr/strings.ini";
            } else if (locale.contains("nl")) {
                LangPath = "data/lang/nl/strings.ini";
            } else if (locale.contains("pl")) {
                LangPath = "data/lang/pl/strings.ini";
            } else if (locale.contains("pt")) {
                LangPath = "data/lang/pt/strings.ini";
            } else if (locale.contains("hu")) {
                LangPath = "data/lang/hu/strings.ini";
            } else {
                LangPath = "data/lang/en-GB/strings.ini";
            }
        }

        new Translation(workPath, FileType.Internal);
        try {
            Translation.LoadTranslation(LangPath);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // no saved workPath found -> search sd-cards and if more than 1 is found give the user the possibility to select one
        if (askAgain) {

            String externalSd = getExternalSdPath("/CacheBox");

            boolean hasExtSd;
            final String externalSd2 = externalSd;

            if (externalSd != null) {
                hasExtSd = (externalSd.length() > 0) && (!externalSd.equalsIgnoreCase(workPath));
            } else {
                hasExtSd = false;
            }

            try {
                final Dialog dialog = new Dialog(splashActivity) {
                    @Override
                    public boolean onKeyDown(int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            splash.this.finish();
                        }
                        return super.onKeyDown(keyCode, event);
                    }
                };

                dialog.setContentView(R.layout.sdselectdialog);

                TextView title = (TextView) dialog.findViewById(R.id.select_sd_title);
                title.setText(Translation.Get("selectWorkSpace") + "\n\n");

                Button buttonI = (Button) dialog.findViewById(R.id.button1);
                buttonI.setText("Internal SD\n\n" + workPath);
                buttonI.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // close select dialog
                        dialog.dismiss();

                        // show please wait dialog
                        showPleaseWaitDialog();

                        // use internal SD -> nothing to change
                        Thread thread = new Thread() {
                            @Override
                            public void run() {

                                // boolean useTabletLayout = rbTabletLayout.isChecked();
                                saveWorkPath();
                                dialog.dismiss();
                                startInitial();
                                Log.info(log, "Initial for use internal SD");
                            }
                        };
                        thread.start();
                    }
                });

                Button buttonE = (Button) dialog.findViewById(R.id.button2);
                final boolean isSandbox = externalSd == null ? false : externalSd.contains("Android/data/de.droidcachebox");
                if (!hasExtSd) {
                    buttonE.setVisibility(Button.INVISIBLE);
                } else {
                    String extSdText = isSandbox ? "External SD SandBox\n\n" : "External SD\n\n";
                    buttonE.setText(extSdText + externalSd);
                }
                buttonE.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // show KitKat Massage?

                        if (isSandbox && !showSandbox) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(splashActivity);

                            // set title
                            alertDialogBuilder.setTitle("KitKat Sandbox");

                            // set dialog message
                            alertDialogBuilder.setMessage(Translation.Get("Desc_Sandbox")).setCancelable(false).setPositiveButton(Translation.Get("yes"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, run Sandbox Path

                                    showSandbox = true;
                                    // Config.AcceptChanges();

                                    // close select dialog
                                    dialog.dismiss();

                                    // show please wait dialog
                                    showPleaseWaitDialog();

                                    // use external SD -> change workPath
                                    Thread thread = new Thread() {
                                        @Override
                                        public void run() {
                                            workPath = externalSd2;

                                            // boolean useTabletLayout = rbTabletLayout.isChecked();
                                            saveWorkPath();
                                            startInitial();
                                            Log.info(log, "Initial for " + workPath);
                                        }
                                    };
                                    thread.start();
                                }
                            }).setNegativeButton(Translation.Get("no"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
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
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    workPath = externalSd2;
                                    saveWorkPath();
                                    startInitial();
                                    Log.info(log, "Initial for " + workPath);
                                }
                            };
                            thread.start();
                        }
                    }
                });

                LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.scrollViewLinearLayout);

                // add all Buttons for created Workspaces

                AdditionalWorkPathArray = getAdditionalWorkPathArray();

                for (final String _AdditionalWorkPath : AdditionalWorkPathArray) {
                    final String Name = FileIO.GetFileNameWithoutExtension(_AdditionalWorkPath);
                    if (!FileFactory.createFile(_AdditionalWorkPath).exists()) {
                        deleteWorkPath(_AdditionalWorkPath);
                        continue;
                    }
                    if (!FileIO.canWrite(_AdditionalWorkPath)) {
                        deleteWorkPath(_AdditionalWorkPath);
                        continue;
                    }

                    Button buttonW = new Button(splashActivity);
                    buttonW.setText(Name + "\n\n" + _AdditionalWorkPath);
                    buttonW.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            // setting the MessageBox then the UI_sizes are not initial in this moment
                            Resources res = splash.this.getResources();
                            float scale = res.getDisplayMetrics().density;
                            float calcBase = 533.333f * scale;

                            FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout1);
                            int width = frame.getMeasuredWidth();
                            int height = frame.getMeasuredHeight();

                            MessageBox.Builder.WindowWidth = width;
                            MessageBox.Builder.WindowHeight = height;
                            MessageBox.Builder.textSize = (calcBase / res.getDimensionPixelSize(R.dimen.BtnTextSize)) * scale;
                            MessageBox.Builder.ButtonHeight = (int) (50 * scale);

                            // Ask before delete
                            msg = (MessageBox) MessageBox.Show(Translation.Get("shuredeleteWorkspace", Name), Translation.Get("deleteWorkspace"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == MessageBox.BUTTON_POSITIVE) {
                                        // Delete this Workpath only from Settings don't delete any File
                                        deleteWorkPath(_AdditionalWorkPath);
                                    }
                                    // Start again to exclude the old Folder
                                    msg.dismiss();
                                    onStart();
                                }

                            });

                            dialog.dismiss();
                            return true;
                        }
                    });
                    buttonW.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // close select dialog
                            dialog.dismiss();

                            // show please wait dialog
                            showPleaseWaitDialog();

                            // use external SD -> change workPath
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    workPath = _AdditionalWorkPath;

                                    // boolean useTabletLayout = rbTabletLayout.isChecked();
                                    saveWorkPath();
                                    startInitial();
                                    Log.info(log, "Initial for " + workPath);
                                }
                            };
                            thread.start();

                        }
                    });

                    ll.addView(buttonW);
                }

                Button buttonC = (Button) dialog.findViewById(R.id.buttonCreateWorkspace);
                buttonC.setText(Translation.Get("createWorkSpace"));
                buttonC.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // close select dialog
                        dialog.dismiss();
                        getFolderReturnListener = new IgetFolderReturnListener() {

                            @Override
                            public void returnFolder(String Path) {
                                if (FileIO.canWrite(Path)) {

                                    AdditionalWorkPathArray.add(Path);
                                    writeAdditionalWorkPathArray(AdditionalWorkPathArray);
                                    // Start again to include the new Folder
                                    onStart();
                                } else {
                                    String WriteProtectionMsg = Translation.Get("NoWriteAcces");
                                    Toast.makeText(splash.this, WriteProtectionMsg, Toast.LENGTH_LONG).show();
                                }
                            }
                        };

                        PlatformConnector.getFolder("", Translation.Get("select_folder"), Translation.Get("select"), getFolderReturnListener);

                    }
                });

                dialog.show();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            startInitial();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.debug(log, "onActivityResult");
        splashActivity = this;

        if (resultCode == RESULT_OK && requestCode == Global.REQUEST_CODE_GET_WRITE_PERMISSION_ANDROID_5) {
            Uri treeUri = data.getData();

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Check for the freshest data.

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                ContentResolver cr = getContentResolver();
                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cr.takePersistableUriPermission(treeUri, takeFlags);
                List<UriPermission> permissionlist = cr.getPersistedUriPermissions();
            }

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    Initial(width, height);
                }
            });
            th.start();

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.debug(log, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        splashActivity = this;
        outState.putBoolean("SelectDbIsStartet", mSelectDbIsStarted);
        outState.putBoolean("OriantationRestart", true);
    }

    @Override
    public void onDestroy() {
        Log.debug(log, "onDestroy");
        if (isFinishing()) {
            ReleaseImages();
            // versionTextView = null;
            // myTextView = null;
            // descTextView = null;
            splashActivity = null;
        }
        super.onDestroy();
        if (pleaseWaitDialog != null && pleaseWaitDialog.isShowing()) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }

        ui = null;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPleaseWaitDialog() {
        pleaseWaitDialog = ProgressDialog.show(splash.this, "In progress", "Copy resources");
        pleaseWaitDialog.show();
        TextView tv1 = (TextView) pleaseWaitDialog.findViewById(android.R.id.message);
        tv1.setTextColor(Color.WHITE);
    }

    private String testExtSdPath(String extPath) {
        // this will test whether the extPath is an existing path to an external sd card
        if (extPath.equalsIgnoreCase(workPath))
            return null; // if this extPath is the same than the actual workPath -> this is the
        // internal SD, not
        // the external!!!
        try {
            if (FileIO.FileExists(extPath)) {
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
                            return f.getAbsolutePath(); // ext SD-Card is plugged in
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
                            return fi.getAbsolutePath();
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

        SharedPreferences settings = this.getSharedPreferences(Global.PREFS_NAME, 0);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("WorkPath", workPath);
        // Commit the edits!
        editor.commit();
    }

    private String getExternalSdPath(String Folder) {

        // check if Layout forced from User
        workPath = Environment.getExternalStorageDirectory() + Folder;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			/*
			MEDIA_UNKNOWN,
			MEDIA_REMOVED,
			MEDIA_UNMOUNTED,
			MEDIA_CHECKING,
			MEDIA_NOFS,
			MEDIA_MOUNTED,
			MEDIA_MOUNTED_READ_ONLY,
			MEDIA_SHARED,
			MEDIA_BAD_REMOVAL,
			MEDIA_UNMOUNTABLE.
			*/
        }

        // extract first part of path ("/mnt/" or "/storage/" ...)
        String prev = "/mnt";
        int pos = workPath.indexOf("/", 2); // search for the second /
        if (pos > 0) {
            prev = workPath.substring(0, pos);
        }
        // search for an external SD-Card
        String externalSd = "";

        // search for an external sd card on different devices

        if ((externalSd = testExtSdPath(prev + "/extSdCard")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/MicroSD")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/ext_sdcard")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard/ext_sd")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/ext_card")) != null) {
            // Sony Xperia sola
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/external")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard2")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard1")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard/_ExternalSD")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard-ext")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/external1")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard/external_sd")) != null) {
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/emmc")) != null) {
            // for CM9
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath("/Removable/MicroSD")) != null) {
            // Asus Transformer
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath("/mnt/ext_sd")) != null) {
            // ODYS Motion
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath("/sdcard/tflash")) != null) {
            // Car Radio
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath(prev + "/sdcard")) != null) {
            // on some devices it is possible that the SD-Card reported by getExternalStorageDirectory() is the extSd and the real
            // external SD is /mnt/sdcard (Faktor2 Tablet!!!)
            externalSd += Folder;
        } else if ((externalSd = testExtSdPath("/mnt/shared/ExtSD")) != null) {
            // GinyMotion Emulator
            externalSd += Folder;
        }

        final java.io.File[] externalCacheDirs = ContextCompat.getExternalCacheDirs(splashActivity);
        final List<String> result = new ArrayList<>();

        for (int i = 1; i < externalCacheDirs.length; ++i) {
            final java.io.File file = externalCacheDirs[i];
            if (file == null)
                continue;
            final String storageState = EnvironmentCompat.getStorageState(file);
            if (Environment.MEDIA_MOUNTED.equals(storageState))
                result.add(getRootOfInnerSdCardFolder(externalCacheDirs[i]));
        }
        if (!result.isEmpty()) {
            externalSd = result.get(0) + Folder;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            // check for Root permission

            File sandboxPath = null;
            String sandboxParentPath = "";
            try {
                String testFolderName = externalSd + "/Test";

                File testFolder = FileFactory.createFile(testFolderName);

                sandboxParentPath = FileFactory.createFile(externalSd).getParent() + "/Android/data/" + getPackageName();

                sandboxPath = FileFactory.createFile(sandboxParentPath + "/files");

                File test = FileFactory.createFile(testFolder + "/Test.txt");
                testFolder.mkdirs();
                test.createNewFile();
                if (!test.exists()) {
                    externalSd = null;
                }
                test.delete();
                testFolder.delete();
            } catch (Exception e) {
                externalSd = null;
            }

            if (externalSd == null && sandboxPath != null) {
                // Check Sandbox Path
                try {
                    // create Sandbox folder with getExternalFilesDir(null);
                    getExternalFilesDir(null); // FileFactory.createFile(sandboxParentPath).mkdirs(); dosen't work

                    String testFolderName = sandboxPath.getAbsolutePath() + "/Test";

                    File testFolder = FileFactory.createFile(testFolderName);
                    File test = FileFactory.createFile(testFolderName + "/Test.txt");
                    testFolder.mkdirs();
                    test.createNewFile();
                    if (!test.exists()) {
                        externalSd = null;
                    }
                    test.delete();
                    testFolder.delete();
                    externalSd = sandboxPath.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    externalSd = null;
                }
            }
        }

        return externalSd;
    }

    private String getRootOfInnerSdCardFolder(java.io.File file) {
        /**
         * Given any file/folder inside an sd card, this will return the path of the sd card
         */
        if (file == null)
            return null;
        final long totalSpace = file.getTotalSpace();
        while (true) {
            final java.io.File parentFile = file.getParentFile();
            if (parentFile == null || parentFile.getTotalSpace() != totalSpace)
                return file.getAbsolutePath();
            file = parentFile;
        }
    }

    private ArrayList<String> getAdditionalWorkPathArray() {
        ArrayList<String> retList = new ArrayList<String>();
        AdditionalWorkPathCount = androidSetting.getInt("AdditionalWorkPathCount", 0);
        for (int i = 0; i < AdditionalWorkPathCount; i++) {
            retList.add(androidSetting.getString("AdditionalWorkPath" + String.valueOf(i), ""));
        }
        return retList;
    }

    private void writeAdditionalWorkPathArray(ArrayList<String> list) {
        Editor editor = androidSetting.edit();

        // first remove all
        for (int i = 0; i < AdditionalWorkPathCount; i++) {
            String delWorkPath = "AdditionalWorkPath" + String.valueOf(i);
            editor.remove(delWorkPath);
        }
        editor.commit();

        int index = 0;
        for (String workpath : list) {
            String addWorkPath = "AdditionalWorkPath" + String.valueOf(index);
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

    private void startInitial() {

        // show wait dialog if not running
        if (pleaseWaitDialog == null)
            showPleaseWaitDialog();

        // saved workPath found -> use this
        Thread thread = new Thread() {

            @Override
            public void run() {
                // wait for measure layout

                final FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout1);
                width = frame.getMeasuredWidth();
                height = frame.getMeasuredHeight();

                while (width == 0 || height == 0) {
                    splash.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            frame.forceLayout();
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    width = frame.getMeasuredWidth();
                    height = frame.getMeasuredHeight();
                }


                Initial(width, height);

            }
        };

        thread.start();

    }

    private void Initial(int width, int height) {

        // Zur Kompatibilität mit Älteren Installationen wird hier noch die redirection.txt abgefragt
        if (FileIO.FileExists(workPath + "/redirection.txt")) {
            BufferedReader Filereader;

            try {
                Filereader = new BufferedReader(new FileReader(workPath + "/redirection.txt"));
                String line;

                while ((line = Filereader.readLine()) != null) {
                    // chk ob der umleitungs Ordner existiert
                    if (FileIO.FileExists(line)) {
                        workPath = line;
                    }
                }

                Filereader.close();
            } catch (IOException e) {
                Log.err(log, "read redirection", e);
            }

        }

        // init logging
        new CB_SLF4J(workPath);
        CB_SLF4J.setLogLevel(LogLevel.INFO);

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

        CB_SLF4J.changeLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());
        Config.AktLogLevel.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                CB_SLF4J.setLogLevel((LogLevel) Config.AktLogLevel.getEnumValue());
            }
        });

        Log.info(log, "initialisieren der PlattformSettings");
        PlatformSettings.setPlatformSettings(new IPlatformSettings() {

            @Override
            public void Write(SettingBase<?> setting) {
                if (androidSetting == null)
                    androidSetting = splash.this.getSharedPreferences(Global.PREFS_NAME, MODE_PRIVATE);
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
                    androidSetting = splash.this.getSharedPreferences(Global.PREFS_NAME, 0);

                if (setting instanceof SettingString) {
                    String value = androidSetting.getString(setting.getName(), "");
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
                createFile(workPath + "/data/.nomedia");
                createFile(workPath + "/skins/.nomedia");
                createFile(workPath + "/repository/.nomedia");
                createFile(workPath + "/Repositories/.nomedia");
                createFile(workPath + "/cache/.nomedia");

            } else {
                Config.newInstall.setValue(false);
            }
        } catch (Exception e) {
            Log.err(log, "Copy Asset", e);
        }

        Config.showSandbox.setValue(showSandbox);
        Config.AcceptChanges();

        // UiSize Structur für die Berechnung der Größen zusammen stellen!

        Log.info(log, GlobalCore.getInstance().getVersionString());
        Log.info(log, "Screen width/height:" + width + "/" + height);

        if (ui == null) {
            Resources res = splash.this.getResources();
            ui = new DevicesSizes();
            ui.Window = new Size(width, height);
            ui.Density = res.getDisplayMetrics().density;
            ui.isLandscape = false;
            Log.debug(log, "ui.Window: " + ui.Window.toString());
            Log.debug(log, "ui.Density: " + ui.Density);
        }

        new UiSizes();
        UiSizes.that.initial(ui);
        GL_UISizes.defaultDPI = ui.Density;

        Global.Paints.init(this);

        {// restrict MapsforgeScaleFactor to max 1.0f (TileSize 256x256)
            ext_AndroidGraphicFactory.createInstance(this.getApplication());

            float restrictedScaleFactor = 1f;
            DisplayModel.setDeviceScaleFactor(restrictedScaleFactor);
            new de.droidcachebox.Map.AndroidManager(new DisplayModel());
        }

        Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
        Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

        Config.AcceptChanges();

        GlobalCore.RunFromSplash = true;

        if (pleaseWaitDialog != null) {
            pleaseWaitDialog.dismiss();
            pleaseWaitDialog = null;
        }

        Bundle b = new Bundle();
        if (GcCode != null) {
            b.putSerializable("GcCode", GcCode);
            b.putSerializable("name", name);
            b.putSerializable("guid", guid);
        }
        if (GpxPath != null) {
            b.putSerializable("GpxPath", GpxPath);
        }
        b.putSerializable("UI", ui);
        // Log.info(log, "Intent putExtras" + " GcCode " + GcCode + " name " + name + " guid " + guid + " GpxPath " + GpxPath); // + " UI " + ui

        Intent mainIntent = new Intent().setClass(splash.this, main.class);
        mainIntent.putExtras(b);
        Log.info(log, "startActivity for main.class (com.badlogic.gdx.backends.android.AndroidApplication) from slash");
        startActivity(mainIntent);

        finish();

    }

    private void createFile(String path) {
        try {
            File CreateFile = FileFactory.createFile(path);
            CreateFile.getParentFile().mkdirs();
            CreateFile.createNewFile();
        } catch (IOException e) {
            Log.err(log, path + ": " + e.getLocalizedMessage());
        }
    }

    private void LoadImages() {
        ((TextView) findViewById(R.id.splash_textViewDesc)).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.splash_textViewVersion)).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.splash_TextView)).setVisibility(View.INVISIBLE);
    }

    private void ReleaseImages() {
        ((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        splashActivity = this;
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

    private void mediaInfo() {
        //<uses-permission android:name="android.permission.WRITE_MEDIA_STORAGE"></uses-permission> is only for system apps
        try {
            Log.info(log, "android.os.Build.VERSION.SDK_INT= " + android.os.Build.VERSION.SDK_INT);
            Log.info(log, "workPath set to " + workPath);
            Log.info(log, "getFilesDir()= " + getFilesDir());// user invisible
            Log.info(log, "Environment.getExternalStoragePublicDirectory()= " + Environment.getExternalStoragePublicDirectory("").getAbsolutePath());
            Log.info(log, "Environment.getExternalStorageDirectory()= " + Environment.getExternalStorageDirectory());
            Log.info(log, "getExternalFilesDir(null)= " + getExternalFilesDir(null));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                // normally [0] is the internal SD, [1] is the external SD
                java.io.File dirs[] = getExternalFilesDirs(null);
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

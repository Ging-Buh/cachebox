package de.droidcachebox;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Build.VERSION_CODES.O_MR1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.components.CacheNameView;
import de.droidcachebox.controls.DownSlider;
import de.droidcachebox.controls.MicrophoneView;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.ViewID;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.Formatter;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.ShowSpoiler;
import de.droidcachebox.menu.menuBtn3.executes.TrackRecorder;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.DescriptionView;
import de.droidcachebox.views.ViewGL;

public class ShowViewMethods implements Platform.ShowViewMethods {
    private final static String sClass = "ShowViewListener";
    private static final int REQUEST_CAPTURE_IMAGE = 6516;
    private static final int REQUEST_CAPTURE_VIDEO = 6517;
    private static boolean isVoiceRecordingStarted = false;
    private static CBLocation recordingStartCoordinate;
    private final ArrayList<ViewOptionsMenu> ViewList = new ArrayList<>();
    private int lastLeft, lastTop, lastRight, lastBottom;
    private final AndroidApplication androidApplication;
    private final Activity mainActivity;
    private final Main mainMain;
    private final AndroidApplicationConfiguration gdxConfig;
    private final FrameLayout layoutContent;
    private DownSlider downSlider;
    private ViewOptionsMenu currentView;
    private ViewID aktViewId;
    private ViewOptionsMenu aktTabView;
    private ViewID aktTabViewId;
    private final CacheNameView cacheNameView;
    private DescriptionView descriptionView;
    private final FrameLayout layoutGlContent;
    private final LayoutInflater inflater;
    private View gdxView;
    private Uri videoUri;
    private AndroidEventListener handlingRecordedVideo, handlingTakePhoto;
    private String recordingStartTime;
    private String mediaFileNameWithoutExtension;
    private String tempMediaPath;
    private MicrophoneView microphoneView;
    private ExtAudioRecorder extAudioRecorder;
    private final View.OnTouchListener onTouchListener;

    ShowViewMethods(Main main) {
        androidApplication = main;
        mainActivity = main;
        mainMain = main;

        gdxConfig = new AndroidApplicationConfiguration();
        gdxConfig.numSamples = 2;
        gdxConfig.useAccelerometer = true;
        gdxConfig.useCompass = true;

        inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutContent = main.findViewById(R.id.layoutContent);
        layoutContent.setVisibility(View.INVISIBLE);
        downSlider = main.findViewById(R.id.downSlider);
        downSlider.invalidate();
        downSlider.setVisibility(View.INVISIBLE);
        downSlider.setMain(mainMain);
        cacheNameView = main.findViewById(R.id.cacheNameView);
        cacheNameView.setVisibility(View.INVISIBLE);

        if (currentView != null)
            ((View) currentView).setVisibility(View.INVISIBLE);
        if (aktTabView != null)
            ((View) aktTabView).setVisibility(View.INVISIBLE);

        layoutGlContent = main.findViewById(R.id.layoutGlContent);

        initializeGDXAndroidApplication();
        initalizeMicrophone();

        onTouchListener = (v, event) -> {
            v.performClick();
            return sendMotionEvent(event);
        };

        Settings.RunOverLockScreen.addSettingChangedListener(this::handleRunOverLockScreenConfig);

        OnResumeListeners.getInstance().addListener(this::onResume);

    }

    // all you have to do on Main onResume
    private void onResume() {
        try {
            gdxView.setOnTouchListener(onTouchListener);
            downSlider.invalidate();
        } catch (Exception ex) {
            Log.err(sClass, "onResume", ex);
        }
    }

    // all you have to do on Main Destroy
    void onDestroyWithFinishing() {
        if (currentView != null) {
            currentView.onHide();
            currentView.onFree();
        }
        currentView = null;

        if (aktTabView != null) {
            aktTabView.onHide();
            aktTabView.onFree();
        }
        aktTabView = null;

        for (ViewOptionsMenu vom : ViewList) {
            vom.onFree();
        }
        ViewList.clear();

        downSlider = null;

        descriptionView = null;

        // Voice Recorder stoppen
        if (extAudioRecorder != null) {
            extAudioRecorder.stop();
            extAudioRecorder.release();
            extAudioRecorder = null;
        }

    }

    void onDestroyWithoutFinishing() {
        if (currentView != null)
            currentView.onHide();
        if (aktTabView != null)
            aktTabView.onHide();
    }

    public int getAktViewId() {
        if (currentView != null)
            return currentView.getMenuId();
        else
            return 0;
    }

    public void requestLayout() {
        if (layoutContent != null)
            layoutContent.requestLayout();
    }

    @Override
    public void showView(final ViewID viewID, final int left, final int top, final int right, final int bottom) {
        mainActivity.runOnUiThread(() -> {
            Log.debug(sClass, "Show View with ID = " + viewID.getID());

            // set Content size

            if (viewID.getType() != ViewID.UI_Type.Activity) {
                if (viewID.getPos() == ViewID.UI_Pos.Left) {
                    RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) layoutContent.getLayoutParams();
                    paramsLeft.setMargins(left, top, right, bottom);
                    layoutContent.setLayoutParams(paramsLeft);
                    layoutContent.requestLayout();
                }
            }

            if (downSlider != null) {
                downSlider.ActionUp();
                downSlider.setVisibility(View.INVISIBLE);
            }

            if (currentView != null)
                ((View) currentView).setVisibility(View.VISIBLE);
            if (aktTabView != null)
                ((View) aktTabView).setVisibility(View.VISIBLE);
            if (cacheNameView != null)
                cacheNameView.setVisibility(View.INVISIBLE);

            if (viewID.getType() == ViewID.UI_Type.Activity) {
                showActivity(viewID.getID());
            } else if (!(currentView == null) && viewID == aktViewId) {
                currentView.onShow();
            } else {
                if (viewID.getPos() == ViewID.UI_Pos.Left) {
                    aktViewId = viewID;
                } else {
                    aktTabViewId = viewID;
                }
                if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.Android) {
                    layoutContent.setVisibility(View.VISIBLE);
                }
                if (viewID.getType() == ViewID.UI_Type.Android) {
                    showAndroidView(getView(viewID), viewID);
                }
                if (viewID.getType() == ViewID.UI_Type.OpenGl) {
                    ShowGLView();
                }
            }

        });
    }

    private void showAndroidView(ViewOptionsMenu view, ViewID ID) {

        if (currentView != null) {
            currentView.onHide();

            if (ID.getType() == ViewID.UI_Type.OpenGl) {
                Log.debug(sClass, "showView OpenGl onPause");
                mainMain.pause();
            }

            if (currentView.equals(descriptionView)) {
                currentView = null;
                descriptionView.onHide();
            }
        }

        if (ID.getType() == ViewID.UI_Type.OpenGl) {
            ShowGLView();
            return;
        }

        currentView = view;
        ((View) currentView).setDrawingCacheEnabled(true);

        layoutContent.removeAllViews();
        ViewParent parent = ((View) currentView).getParent();
        if (parent != null) {
            // aktView ist noch gebunden, also lösen
            ((FrameLayout) parent).removeAllViews();
        }
        layoutContent.addView((View) currentView);
        currentView.onShow();

        downSlider.invalidate();
        ((View) currentView).forceLayout();

        if (currentView != null)
            ((View) currentView).setVisibility(View.VISIBLE);
        if (aktTabView != null)
            ((View) aktTabView).setVisibility(View.VISIBLE);
        if (downSlider != null)
            downSlider.setVisibility(View.INVISIBLE);
        if (cacheNameView != null)
            cacheNameView.setVisibility(View.INVISIBLE);

    }

    private void ShowGLView() {
        Log.debug(sClass, "ShowViewGL " + layoutGlContent.getMeasuredWidth() + "/" + layoutGlContent.getMeasuredHeight());

        initializeGDXAndroidApplication();

        GL.that.onStart();
        GL.that.setGLViewID();

        if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.OpenGl) {
            layoutContent.setVisibility(View.INVISIBLE);
        }

        downSlider.invalidate();

    }

    private ViewOptionsMenu getView(ViewID id) {
        // first check if view on List
        if (id.getID() < ViewList.size()) {
            return ViewList.get(id.getID());
        }

        if (id == ViewConst.DESCRIPTION_VIEW) {
            if (descriptionView != null) {
                return descriptionView;
            } else {
                return descriptionView = new DescriptionView(mainActivity, inflater);
            }
        }

        return null;
    }

    private void showActivity(int id) {
        if (id == ViewID.NAVIGATE_TO) {
            navigate();
        } else if (id == ViewID.VOICE_REC) {
            recVoice();
        } else if (id == ViewID.TAKE_PHOTO) {
            takePhoto();
        } else if (id == ViewID.VIDEO_REC) {
            recVideo();
        } else if (id == ViewID.WhatsApp) {
            shareInfos();
        }
    }

    @Override
    public void hideView(final ViewID viewID) {

        mainActivity.runOnUiThread(() -> {
            Log.debug(sClass, "Hide View with ID = " + viewID.getID());

            if (!(currentView == null) && viewID == aktViewId) {
                currentView.onHide();
            }

            if (aktTabViewId != null && aktTabViewId == viewID && aktTabViewId.getPos() == ViewID.UI_Pos.Right) {
                aktTabViewId = null;
                aktTabView = null;
            }

            if (aktViewId != null && aktViewId == viewID && aktViewId.getPos() == ViewID.UI_Pos.Left) {
                layoutContent.setVisibility(View.INVISIBLE);
                aktViewId = null;
                currentView = null;
            }
        });

    }

    @Override
    public void showForDialog() {

        mainActivity.runOnUiThread(() -> {
            // chk for timer conflict (releay set invisible)
            // only if showing Dialog or Activity
            if (GL.that.isNotShownDialogOrActivity())
                return;

            if (currentView != null)
                ((View) currentView).setVisibility(View.INVISIBLE);
            if (aktTabView != null)
                ((View) aktTabView).setVisibility(View.INVISIBLE);
            if (downSlider != null)
                downSlider.setVisibility(View.INVISIBLE);
            if (cacheNameView != null)
                cacheNameView.setVisibility(View.INVISIBLE);
            handleRunOverLockScreenConfig();
            // Log.debug(sClass, "Show AndroidView");
        });
    }

    @Override
    public void hideForDialog() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mainActivity.runOnUiThread(() -> {
                    // chk for timer conflict (releay set invisible)
                    // only if not showing Dialog or Activity
                    if (GL.that.isNotShownDialogOrActivity()) {
                        if (currentView != null) {
                            ((View) currentView).setVisibility(View.VISIBLE);
                            currentView.onShow();
                            setContentSize(lastLeft, lastTop, lastRight, lastBottom);
                        }
                        if (aktTabView != null) {
                            ((View) aktTabView).setVisibility(View.VISIBLE);
                            aktTabView.onShow();
                            setContentSize(lastLeft, lastTop, lastRight, lastBottom);
                        }
                        if (downSlider != null)
                            (downSlider).setVisibility(View.INVISIBLE);
                        if (cacheNameView != null)
                            (cacheNameView).setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 50);
    }

    @Override
    public void dayNightSwitched() {
        Global.initIcons(mainActivity);
        Global.initTheme(mainActivity);
        if (aktViewId == ViewConst.DESCRIPTION_VIEW || aktTabViewId == ViewConst.DESCRIPTION_VIEW) {
            if (descriptionView.getVisibility() == View.VISIBLE) {
                if (currentView == descriptionView) {
                    hideView(ViewConst.DESCRIPTION_VIEW);
                    descriptionView = null;
                }
            }
        }
    }

    @Override
    public void setContentSize(final int left, final int top, final int right, final int bottom) {
        lastLeft = left;
        lastRight = right;
        lastTop = top;
        lastBottom = bottom;
        mainActivity.runOnUiThread(() -> {
            Log.debug(sClass, "Set Android Content Sizeleft/top/right/bottom :" + left + "/" + top + "/" + right + "/" + bottom);
            if (currentView != null) {
                RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) layoutContent.getLayoutParams();
                paramsLeft.setMargins(left, top, right, bottom);
                layoutContent.setLayoutParams(paramsLeft);
                layoutContent.requestLayout();
            }
        });
    }

    private void initializeGDXAndroidApplication() {
        try {
            Log.debug(sClass, "initialize GDXAndroidApplication (gdxView = graphics.getView()");
            gdxView = androidApplication.initializeForView(GL.that, gdxConfig);
            int GlSurfaceType = -1;
            if (gdxView instanceof GLSurfaceView20)
                GlSurfaceType = ViewGL.GLSURFACE_VIEW20;
            else if (gdxView instanceof GLSurfaceView)
                GlSurfaceType = ViewGL.GLSURFACE_GLSURFACE;
            ViewGL.setSurfaceType(GlSurfaceType);
            switch (GlSurfaceType) {
                case ViewGL.GLSURFACE_VIEW20:
                    ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                    break;
                case ViewGL.GLSURFACE_GLSURFACE:
                    ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                    break;
            }

            gdxView.setOnTouchListener(onTouchListener);

            if (GL.that != null) {
                if (GL.that.getGlListener() == null)
                    new ViewGL(mainActivity, inflater, gdxView);
            } else {
                Log.err(sClass, "GL is null");
                mainMain.restartFromSplash();
            }

            if (layoutGlContent != null)
                layoutGlContent.removeAllViews();
            ViewParent parent = gdxView.getParent();
            if (parent != null) {
                // aktView ist noch gebunden, also lösen
                ((RelativeLayout) parent).removeAllViews();
            }
            layoutGlContent.addView(gdxView);

            // }
        } catch (Exception e) {
            Log.err(sClass, "main.initialViewGL()", "", e);
        }

    }

    private void navigate() {
        if (GlobalCore.isSetSelectedCache()) {
            double lat;
            double lon;
            String targetName;

            if (GlobalCore.getSelectedWayPoint() == null) {
                lat = GlobalCore.getSelectedCache().getLatitude();
                lon = GlobalCore.getSelectedCache().getCoordinate().getLongitude();
                targetName = GlobalCore.getSelectedCache().getGeoCacheCode();
            } else {
                lat = GlobalCore.getSelectedWayPoint().getLatitude();
                lon = GlobalCore.getSelectedWayPoint().getLongitude();
                targetName = GlobalCore.getSelectedWayPoint().getWaypointCode();
            }

            String selectedNavi = Settings.Navis.getValue();

            Intent intent = null;
            switch (selectedNavi) {
                case "Navigon":
                    intent = getNavigationIntent("android.intent.action.navigon.START_PUBLIC", "");
                    if (intent == null) {
                        intent = getNavigationIntent("", "com.navigon.navigator"); // get the launch-intent from package
                    }
                    if (intent != null) {
                        intent.putExtra("latitude", (float) lat);
                        intent.putExtra("longitude", (float) lon);
                    }
                    break;
                case "Orux":
                    intent = getNavigationIntent("com.oruxmaps.VIEW_MAP_ONLINE", "");
                    // from http://www.oruxmaps.com/oruxmapsmanual_en.pdf
                    if (intent != null) {
                        double[] targetLat = {lat};
                        double[] targetLon = {lon};
                        String[] targetNames = {targetName};
                        intent.putExtra("targetLat", targetLat);
                        intent.putExtra("targetLon", targetLon);
                        intent.putExtra("targetName", targetNames);
                        intent.putExtra("navigatetoindex", 1);
                    }
                    break;
                case "OsmAnd":
                    intent = getNavigationIntent(ACTION_VIEW, "geo:" + lat + "," + lon);
                    break;
                case "OsmAnd2":
                    intent = getNavigationIntent(ACTION_VIEW, "http://download.osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=14");
                    break;
                case "Waze":
                    intent = getNavigationIntent(ACTION_VIEW, "waze://?ll=" + lat + "," + lon);
                    break;
                case "Sygic":
                    intent = getNavigationIntent(ACTION_VIEW, "com.sygic.aura://coordinate|" + lon + "|" + lat + "|drive");
                    break;
            }
            if (intent == null) {
                // "default" or "no longer existing selection" or "fallback" to google
                intent = getNavigationIntent(ACTION_VIEW, "http://maps.google.com/maps?daddr=" + lat + "," + lon);
            }
            try {
                if (intent != null)
                    mainActivity.startActivity(intent);
            } catch (Exception e) {
                Log.err(sClass, "Error Start " + selectedNavi, e);
            }
        }
    }

    private Intent getNavigationIntent(String action, String data) {
        Intent intent;
        try {
            if (action.length() > 0) {
                if (data.length() > 0) {
                    intent = new Intent(action, Uri.parse(data));
                } else {
                    intent = new Intent(action);
                }
                if (intent != null) {
                    // check if there is an activity that can handle the desired intent (not needed: this is implicit done by startActivity(event))
                    if (intent.resolveActivity(mainActivity.getPackageManager()) == null) {
                        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                            Log.err(sClass, "Navigation: No App for " + action + " , " + data);
                            intent = null;
                        } else {
                            // from Build.VERSION_CODES.R onwards the Activity may exist even, if not visible
                            Log.debug(sClass, "Navigation: No visible App for " + action + " , " + data);
                        }
                    }
                }
            } else {
                intent = mainActivity.getPackageManager().getLaunchIntentForPackage(data);
                if (intent == null) {
                    Log.err(sClass, "Navigation: No package/App for " + data);
                }
            }
        } catch (Exception e) {
            intent = null;
            Log.err(sClass, "Exception: No intent for " + action + " , " + data, e);
        }
        return intent;
    }

    private void recVoice() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            final String[] recordAudioPermissions = {Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(mainActivity, recordAudioPermissions, Main.Request_recordVoice);
            return;
        }

        try {
            if (!isVoiceRecordingStarted()) // Voice Recorder starten
            {
                // define the file-name to save voice taken by activity
                String directory = Settings.UserImageFolder.getValue();
                if (!FileIO.createDirectory(directory)) {
                    Log.err(sClass, "can't create " + directory);
                    return;
                }

                mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date());

                String cacheName;
                if (GlobalCore.isSetSelectedCache()) {
                    String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGeoCacheCode() + "-" + GlobalCore.getSelectedCache().getGeoCacheName());
                    cacheName = validName.substring(0, Math.min(validName.length(), 32));
                } else {
                    cacheName = "Voice";
                }

                mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;
                extAudioRecorder = ExtAudioRecorder.getInstance(false);
                extAudioRecorder.setOutputFile(directory + "/" + mediaFileNameWithoutExtension + ".wav");
                extAudioRecorder.prepare();
                extAudioRecorder.start();

                String MediaFolder = Settings.UserImageFolder.getValue();
                String TrackFolder = Settings.TrackFolder.getValue();
                String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
                // Da eine Voice keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
                TrackRecorder.getInstance().annotateMedia(mediaFileNameWithoutExtension + ".wav", relativPath + "/" + mediaFileNameWithoutExtension + ".wav", Locator.getInstance().getLocation(CBLocation.ProviderType.GPS), getTrackDateTimeString());
                Toast.makeText(mainActivity, "Start Voice Recorder", Toast.LENGTH_SHORT).show();

                recordVoice(true);

            } else { // Voice Recorder stoppen
                recordVoice(false);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isVoiceRecordingStarted() {
        return isVoiceRecordingStarted;
    }

    private void recordVoice(boolean value) {
        isVoiceRecordingStarted = value;
        if (isVoiceRecordingStarted) {
            microphoneView.SetOn();
        } else { // Aufnahme stoppen
            microphoneView.SetOff();
            if (extAudioRecorder != null) {
                extAudioRecorder.stop();
                extAudioRecorder.release();
                extAudioRecorder = null;
                Toast.makeText(mainActivity, "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initalizeMicrophone() {
        microphoneView = mainActivity.findViewById(R.id.microphone);
        if (microphoneView != null) {
            microphoneView.SetOff();
            microphoneView.setOnClickListener(v -> {
                // Stoppe Aufnahme durch klick auf Mikrofon-Icon
                recordVoice(false);
            });
        }
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            final String[] takePhotoPermissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(mainActivity, takePhotoPermissions, Main.Request_takePhoto);
            return;
        }
        Log.debug(sClass, "takePhoto start " + GlobalCore.getSelectedCache());
        try {
            // define the file-name to save photo taken by Camera activity
            String directory = Settings.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sClass, "can't create " + directory);
                return;
            }
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGeoCacheCode() + "-" + GlobalCore.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Image";
            }
            mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date()) + " " + cacheName;
            tempMediaPath = Objects.requireNonNull(mainActivity.getExternalFilesDir("User/Media")).getAbsolutePath() + "/"; // oder Environment.DIRECTORY_PICTURES
            if (!FileIO.createDirectory(tempMediaPath)) {
                Log.err(sClass, "can't create " + tempMediaPath);
                return;
            }
            String tempMediaPathAndName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
            try {
                FileFactory.createFile(tempMediaPathAndName).createNewFile();
            } catch (Exception e) {
                Log.err(sClass, "can't create " + tempMediaPathAndName + "\r" + e.getLocalizedMessage());
                return;
            }

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(mainActivity, "de.droidcachebox.android.fileprovider", new java.io.File(tempMediaPathAndName));
            } else {
                uri = Uri.fromFile(new java.io.File(tempMediaPathAndName));
            }
            Log.debug(sClass, uri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                if (handlingTakePhoto == null) {
                    handlingTakePhoto = (requestCode, resultCode, data) -> {
                        androidApplication.removeAndroidEventListener(handlingTakePhoto);
                        // Intent Result Take Photo
                        if (requestCode == REQUEST_CAPTURE_IMAGE) {
                            if (resultCode == Activity.RESULT_OK) {
                                GL.that.runIfInitial(() -> {
                                    Log.debug(sClass, "Photo taken");
                                    try {
                                        // move the photo from temp to UserImageFolder
                                        String sourceName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
                                        String destinationName = Settings.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + ".jpg";
                                        if (!sourceName.equals(destinationName)) {
                                            AbstractFile source = FileFactory.createFile(sourceName);
                                            AbstractFile destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sClass, "move from " + sourceName + " to " + destinationName + " failed");
                                            }
                                        }

                                        // for the photo to show within spoilers
                                        if (GlobalCore.isSetSelectedCache()) {
                                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                                            ((ShowSpoiler)Action.ShowSpoiler.action).forceReloadSpoiler();
                                        }

                                        ViewManager.that.reloadSprites(false);

                                        // track annotation
                                        String TrackFolder = Settings.TrackFolder.getValue();
                                        String relativPath = FileIO.getRelativePath(Settings.UserImageFolder.getValue(), TrackFolder, "/");
                                        CBLocation lastLocation = Locator.getInstance().getLastSavedFineLocation();
                                        if (lastLocation == null) {
                                            lastLocation = Locator.getInstance().getLocation(CBLocation.ProviderType.any);
                                            if (lastLocation == null) {
                                                Log.debug(sClass, "No (GPS)-Location for Trackrecording.");
                                                return;
                                            }
                                        }
                                        // Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und die Koordinaten nach der Aufnahme verwendet werden.
                                        TrackRecorder.getInstance().annotateMedia(mediaFileNameWithoutExtension + ".jpg", relativPath + "/" + mediaFileNameWithoutExtension + ".jpg", lastLocation, getTrackDateTimeString());
                                    } catch (Exception e) {
                                        Log.err(sClass, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sClass, "Intent Take Photo resultCode: " + resultCode);
                            }
                        }
                    };
                }
                androidApplication.addAndroidEventListener(handlingTakePhoto);
                mainActivity.startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            } else {
                Log.err(sClass, MediaStore.ACTION_IMAGE_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sClass, e.getLocalizedMessage());
        }
    }

    private void recVideo() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            final String[] takePhotoPermissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(mainActivity, takePhotoPermissions, Main.Request_recordVideo);
            return;
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            final String[] recordAudioPermissions = {Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(mainActivity, recordAudioPermissions, Main.Request_recordVideo);
            return;
        }

        try {
            Log.debug(sClass, "recVideo start " + GlobalCore.getSelectedCache());
            // define the file-name to save video taken by Camera activity
            String directory = Settings.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sClass, "can't create " + directory);
                return;
            }
            mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date());
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGeoCacheCode() + "-" + GlobalCore.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Video";
            }
            mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;

            // Da ein Video keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
            recordingStartTime = getTrackDateTimeString();
            recordingStartCoordinate = Locator.getInstance().getLocation(CBLocation.ProviderType.GPS);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "");
            videoUri = mainActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            // Log.debug(uri.toString());
            final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAXIMUM_VIDEO_SIZE);
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                if (handlingRecordedVideo == null)
                    handlingRecordedVideo = (requestCode, resultCode, data) -> {
                        androidApplication.removeAndroidEventListener(handlingRecordedVideo);
                        // Intent Result Record Video
                        if (requestCode == REQUEST_CAPTURE_VIDEO) {
                            if (resultCode == Activity.RESULT_OK) {
                                GL.that.runIfInitial(() -> {
                                    Log.debug(sClass, "Video recorded.");
                                    String ext;
                                    try {
                                        // move Video from temp (recordedVideoFilePath) in UserImageFolder and rename
                                        String recordedVideoFilePath = "";
                                        // first get the tempfile pathAndName (recordedVideoFilePath)
                                        String[] proj = {MediaStore.Images.Media.DATA}; // want to get Path to the file on disk.

                                        Cursor cursor = mainActivity.getContentResolver().query(videoUri, proj, null, null, null); // result set
                                        if (cursor != null && cursor.getCount() != 0) {
                                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); // my meaning: if only one element index is 0
                                            cursor.moveToFirst(); // first row ( here we should have only one row )
                                            recordedVideoFilePath = cursor.getString(columnIndex);
                                        }
                                        if (cursor != null) {
                                            cursor.close();
                                        }

                                        if (recordedVideoFilePath.length() > 0) {
                                            ext = FileIO.getFileExtension(recordedVideoFilePath);

                                            AbstractFile source = FileFactory.createFile(recordedVideoFilePath);
                                            String destinationName = Settings.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + "." + ext;
                                            AbstractFile destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sClass, "move from " + recordedVideoFilePath + " to " + destinationName + " failed");
                                            } else {
                                                Log.debug(sClass, "Video saved at " + destinationName);
                                                // track annotation
                                                String TrackFolder = Settings.TrackFolder.getValue();
                                                String relativPath = FileIO.getRelativePath(Settings.UserImageFolder.getValue(), TrackFolder, "/");
                                                TrackRecorder.getInstance().annotateMedia(mediaFileNameWithoutExtension + "." + ext, relativPath + "/" + mediaFileNameWithoutExtension + "." + ext, recordingStartCoordinate, recordingStartTime);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.err(sClass, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sClass, "Intent Record Video resultCode: " + resultCode);
                            }
                        }
                    };
                androidApplication.addAndroidEventListener(handlingRecordedVideo);
                mainActivity.startActivityForResult(intent, REQUEST_CAPTURE_VIDEO);
            } else {
                Log.err(sClass, MediaStore.ACTION_VIDEO_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sClass, e.toString());
        }
    }

    private String getTrackDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return datFormat.format(timestamp).replace(" ", "T") + "Z";
    }

    private void shareInfos() {
        String smiley = ((char) new BigInteger("1F604", 16).intValue()) + " ";

        // PackageManager pm = mainActivity.getPackageManager();
        try {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            /*
            // PackageInfo info =
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code in catch block will be called
            waIntent.setPackage("com.whatsapp");
             */

            Cache cache = GlobalCore.getSelectedCache();
            String text = cache.getGeoCacheCode() + " - " + cache.getGeoCacheName() + ("\n" + "https://coord.info/" + cache.getGeoCacheCode());
            if (cache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                text = text + ("\n\n" + "Location (corrected)");
                if (cache.hasCorrectedCoordinates()) {
                    text = text + ("\n" + Formatter.FormatCoordinate(cache.getCoordinate(), ""));
                } else {
                    text = text + ("\n" + Formatter.FormatCoordinate(cache.getCorrectedFinal().getCoordinate(), ""));
                }
            } else {
                text = text + ("\n\n" + "Location");
                text = text + ("\n" + Formatter.FormatCoordinate(cache.getCoordinate(), ""));
            }
            if (Platform.getClipboard() != null)
                text = text + ("\n" + Platform.getClipboard().getContents());
            text = text + ("\n" + smiley + "AndroidCacheBox");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            mainActivity.startActivity(Intent.createChooser(shareIntent, Translation.get("ShareWith")));
            //} catch (PackageManager.NameNotFoundException e) {
            //    toast.makeText(mainActivity, "WhatsApp not Installed", toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(mainActivity, "Share App not installed", Toast.LENGTH_SHORT).show();
        }

    }

    private void handleRunOverLockScreenConfig() {
        mainActivity.runOnUiThread(() -> {
            // add flags for run over lock screen
            if (Build.VERSION.SDK_INT >= O_MR1) {
                mainActivity.setShowWhenLocked(true);
                mainActivity.setTurnScreenOn(true);
                /*
                // seems as if usage of cachebox is possible without following code
                // if locked: this code forces to unlock (with your selected unlock method).
                // if not locked: gives an onDismissError
                KeyguardManager keyguardManager = (KeyguardManager) mainActivity.getSystemService(Context.KEYGUARD_SERVICE);
                // to do use try catch for getting KeyguardManager: throws ServiceNotFoundException
                if (keyguardManager != null) {
                    keyguardManager.requestDismissKeyguard(mainActivity, new KeyguardManager.KeyguardDismissCallback() {
                        public void onDismissError() {
                            Log.err(sClass, "request Dismiss Keyguard: Error");
                        }
                        public void onDismissSucceeded() {
                            Log.err(sClass, "request Dismiss Keyguard: succeeded");
                        }
                        public void onDismissCancelled() {
                            Log.err(sClass, "request Dismiss Keyguard: cancelled");
                        }
                    });
                }
                else {
                    Log.err(sClass, "keyguardManager not available (null)");
                }
                 */
            } else {
                Window window = mainActivity.getWindow();
                if (window != null) {
                    if (Settings.RunOverLockScreen.getValue()) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    }
                }
            }
        });
    }

    private boolean sendMotionEvent(final MotionEvent event) {
        if (event != null) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
            try {
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_DOWN:
                        GL_Input.that.onTouchDownBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        GL_Input.that.onTouchDraggedBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex));
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        GL_Input.that.onTouchUpBase((int) event.getX(pointerIndex), (int) event.getY(pointerIndex), event.getPointerId(pointerIndex), 0);
                        break;
                }
            } catch (Exception e) {
                Log.err(sClass, "sendMotionEvent", e);
            }
        }
        return true;
    }

}

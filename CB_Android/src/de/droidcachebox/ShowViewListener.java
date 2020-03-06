package de.droidcachebox;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import de.droidcachebox.components.CacheNameView;
import de.droidcachebox.controls.DownSlider;
import de.droidcachebox.controls.MicrophoneView;
import de.droidcachebox.database.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.ViewID;
import de.droidcachebox.gdx.views.SpoilerView;
import de.droidcachebox.locator.Formatter;
import de.droidcachebox.locator.Location;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.DescriptionView;
import de.droidcachebox.views.ViewGL;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O_MR1;

public class ShowViewListener implements PlatformUIBase.IShowViewListener {
    private final static String sKlasse = "ShowViewListener";
    private static final int REQUEST_CAPTURE_IMAGE = 61216516;
    private static final int REQUEST_CAPTURE_VIDEO = 61216517;
    private static boolean isVoiceRecordingStarted = false;
    private static Location recordingStartCoordinate;
    private final ArrayList<ViewOptionsMenu> ViewList = new ArrayList<>();
    private int lastLeft, lastTop, lastRight, lastBottom;
    private AndroidApplication androidApplication;
    private Activity mainActivity;
    private Main mainMain;
    private AndroidApplicationConfiguration gdxConfig;
    private FrameLayout layoutContent;
    private DownSlider downSlider;
    private ViewOptionsMenu aktView;
    private ViewID aktViewId;
    private ViewOptionsMenu aktTabView;
    private ViewID aktTabViewId;
    private CacheNameView cacheNameView;
    private DescriptionView descriptionView;
    private FrameLayout layoutGlContent;
    private LayoutInflater inflater;
    private View gdxView;
    private Uri videoUri;
    private AndroidEventListener handlingRecordedVideo, handlingTakePhoto;
    private String recordingStartTime;
    private String mediaFileNameWithoutExtension;
    private String tempMediaPath;
    private MicrophoneView microphoneView;
    private ExtAudioRecorder extAudioRecorder;
    private View.OnTouchListener onTouchListener;

    ShowViewListener(Main main) {
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

        if (aktView != null)
            ((View) aktView).setVisibility(View.INVISIBLE);
        if (aktTabView != null)
            ((View) aktTabView).setVisibility(View.INVISIBLE);

        layoutGlContent = main.findViewById(R.id.layoutGlContent);

        initializeGDXAndroidApplication();
        initalizeMicrophone();

        onTouchListener = (v, event) -> {
            v.performClick();
            return sendMotionEvent(event);
        };

        Config.RunOverLockScreen.addSettingChangedListener(this::handleRunOverLockScreenConfig);

        OnResumeListeners.getInstance().addListener(this::onResume);

    }

    // all you have to do on Main onResume
    private void onResume() {
        try {
            gdxView.setOnTouchListener(onTouchListener);
            downSlider.invalidate();
        } catch (Exception ex) {
            Log.err(sKlasse, "onResume", ex);
        }
    }

    // all you have to do on Main Destroy
    void onDestroyWithFinishing() {
        if (aktView != null) {
            aktView.onHide();
            aktView.onFree();
        }
        aktView = null;

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
        if (aktView != null)
            aktView.onHide();
        if (aktTabView != null)
            aktTabView.onHide();
    }

    public int getAktViewId() {
        if (aktView != null)
            return aktView.getMenuId();
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
            Log.info(sKlasse, "Show View with ID = " + viewID.getID());

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

            if (aktView != null)
                ((View) aktView).setVisibility(View.VISIBLE);
            if (aktTabView != null)
                ((View) aktTabView).setVisibility(View.VISIBLE);
            if (cacheNameView != null)
                cacheNameView.setVisibility(View.INVISIBLE);

            showView(viewID);

        });

    }


    private void showView(ViewID viewID) {
        if (viewID != null) {
            if (viewID.getType() == ViewID.UI_Type.Activity) {
                showActivity(viewID);
            } else if (!(aktView == null) && viewID == aktViewId) {
                aktView.onShow();
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
        }
    }

    private void showAndroidView(ViewOptionsMenu view, ViewID ID) {

        if (aktView != null) {
            aktView.onHide();

            if (ID.getType() == ViewID.UI_Type.OpenGl) {
                Log.info(sKlasse, "showView OpenGl onPause");
                mainMain.pause();
            }

            if (aktView.equals(descriptionView)) {
                aktView = null;
                descriptionView.onHide();
            }

        }

        if (ID.getType() == ViewID.UI_Type.OpenGl) {

            ShowGLView();
            return;

        }

        aktView = view;
        ((View) aktView).setDrawingCacheEnabled(true);

        layoutContent.removeAllViews();
        ViewParent parent = ((View) aktView).getParent();
        if (parent != null) {
            // aktView ist noch gebunden, also lösen
            ((FrameLayout) parent).removeAllViews();
        }
        layoutContent.addView((View) aktView);
        aktView.onShow();

        downSlider.invalidate();
        ((View) aktView).forceLayout();

        if (aktView != null)
            ((View) aktView).setVisibility(View.VISIBLE);
        if (aktTabView != null)
            ((View) aktTabView).setVisibility(View.VISIBLE);
        if (downSlider != null)
            downSlider.setVisibility(View.INVISIBLE);
        if (cacheNameView != null)
            cacheNameView.setVisibility(View.INVISIBLE);

    }

    private void ShowGLView() {
        Log.info(sKlasse, "ShowViewGL " + layoutGlContent.getMeasuredWidth() + "/" + layoutGlContent.getMeasuredHeight());

        initializeGDXAndroidApplication();

        GL.that.onStart();
        GL.that.setGLViewID();

        if (aktViewId != null && aktViewId.getType() == ViewID.UI_Type.OpenGl) {
            layoutContent.setVisibility(View.INVISIBLE);
        }

        downSlider.invalidate();

    }

    private ViewOptionsMenu getView(ViewID ID) {
        // first check if view on List
        if (ID.getID() < ViewList.size()) {
            return ViewList.get(ID.getID());
        }

        if (ID == ViewConst.DESCRIPTION_VIEW) {
            if (descriptionView != null) {
                return descriptionView;
            } else {
                return descriptionView = new DescriptionView(mainActivity, inflater);
            }
        }

        return null;
    }

    private void showActivity(ViewID ID) {
        if (ID == ViewConst.NAVIGATE_TO) {
            NavigateTo();
        } else if (ID == ViewConst.VOICE_REC) {
            recVoice();
        } else if (ID == ViewConst.TAKE_PHOTO) {
            takePhoto();
        } else if (ID == ViewConst.VIDEO_REC) {
            recVideo();
        } else if (ID == ViewConst.Share) {
            shareInfos();
        }
    }

    @Override
    public void hideView(final ViewID viewID) {

        mainActivity.runOnUiThread(() -> {
            Log.info(sKlasse, "Hide View with ID = " + viewID.getID());

            if (!(aktView == null) && viewID == aktViewId) {
                aktView.onHide();
            }

            if (aktTabViewId != null && aktTabViewId == viewID && aktTabViewId.getPos() == ViewID.UI_Pos.Right) {
                aktTabViewId = null;
                aktTabView = null;
            }

            if (aktViewId != null && aktViewId == viewID && aktViewId.getPos() == ViewID.UI_Pos.Left) {
                layoutContent.setVisibility(View.INVISIBLE);
                aktViewId = null;
                aktView = null;
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

            if (aktView != null)
                ((View) aktView).setVisibility(View.INVISIBLE);
            if (aktTabView != null)
                ((View) aktTabView).setVisibility(View.INVISIBLE);
            if (downSlider != null)
                downSlider.setVisibility(View.INVISIBLE);
            if (cacheNameView != null)
                cacheNameView.setVisibility(View.INVISIBLE);
            handleRunOverLockScreenConfig();
            // Log.info(sKlasse, "Show AndroidView");
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
                        if (aktView != null) {
                            ((View) aktView).setVisibility(View.VISIBLE);
                            aktView.onShow();
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
                if (aktView == descriptionView) {
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
            Log.info(sKlasse, "Set Android Content Sizeleft/top/right/bottom :" + left + "/" + top + "/" + right + "/" + bottom);
            if (aktView != null) {
                RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) layoutContent.getLayoutParams();
                paramsLeft.setMargins(left, top, right, bottom);
                layoutContent.setLayoutParams(paramsLeft);
                layoutContent.requestLayout();
            }
        });
    }

    private void initializeGDXAndroidApplication() {
        try {
            Log.info(sKlasse, "initialize GDXAndroidApplication (gdxView = graphics.getView()");
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
                Log.err(sKlasse, "GL is null");
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
            Log.err(sKlasse, "main.initialViewGL()", "", e);
        }

    }

    private void NavigateTo() {
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
                targetName = GlobalCore.getSelectedWayPoint().getGcCode();
            }

            String selectedNavi = Config.Navis.getValue();

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
                Log.err(sKlasse, "Error Start " + selectedNavi, e);
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
            } else {
                intent = mainActivity.getPackageManager().getLaunchIntentForPackage(data);
            }
            if (intent != null) {
                // check if there is an activity that can handle the desired intent
                if (intent.resolveActivity(mainActivity.getPackageManager()) == null) {
                    intent = null;
                }
            }
            if (intent == null) {
                Log.err(sKlasse, "No intent for " + action + " , " + data);
            }
        } catch (Exception e) {
            intent = null;
            Log.err(sKlasse, "Exception: No intent for " + action + " , " + data, e);
        }
        return intent;
    }

    private void recVoice() {
        try {
            if (!isVoiceRecordingStarted()) // Voice Recorder starten
            {
                // define the file-name to save voice taken by activity
                String directory = Config.UserImageFolder.getValue();
                if (!FileIO.createDirectory(directory)) {
                    Log.err(sKlasse, "can't create " + directory);
                    return;
                }

                mediaFileNameWithoutExtension = Global.GetDateTimeString();

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

                String MediaFolder = Config.UserImageFolder.getValue();
                String TrackFolder = Config.TrackFolder.getValue();
                String relativPath = FileIO.getRelativePath(MediaFolder, TrackFolder, "/");
                // Da eine Voice keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
                TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + ".wav", relativPath + "/" + mediaFileNameWithoutExtension + ".wav", Locator.getInstance().getLocation(Location.ProviderType.GPS), Global.GetTrackDateTimeString());
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
        Log.info(sKlasse, "takePhoto start " + GlobalCore.getSelectedCache());
        try {
            // define the file-name to save photo taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sKlasse, "can't create " + directory);
                return;
            }
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGeoCacheCode() + "-" + GlobalCore.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Image";
            }
            mediaFileNameWithoutExtension = Global.GetDateTimeString() + " " + cacheName;
            tempMediaPath = Objects.requireNonNull(mainActivity.getExternalFilesDir("User/Media")).getAbsolutePath() + "/"; // oder Environment.DIRECTORY_PICTURES
            if (!FileIO.createDirectory(tempMediaPath)) {
                Log.err(sKlasse, "can't create " + tempMediaPath);
                return;
            }
            String tempMediaPathAndName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
            try {
                FileFactory.createFile(tempMediaPathAndName).createNewFile();
            } catch (Exception e) {
                Log.err(sKlasse, "can't create " + tempMediaPathAndName + "\r" + e.getLocalizedMessage());
                return;
            }

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= N) {
                uri = FileProvider.getUriForFile(mainActivity, "de.droidcachebox.android.fileprovider", new java.io.File(tempMediaPathAndName));
            } else {
                uri = Uri.fromFile(new java.io.File(tempMediaPathAndName));
            }
            Log.info(sKlasse, uri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
                if (handlingTakePhoto == null) {
                    handlingTakePhoto = (requestCode, resultCode, data) -> {
                        androidApplication.removeAndroidEventListener(handlingTakePhoto);
                        // Intent Result Take Photo
                        if (requestCode == REQUEST_CAPTURE_IMAGE) {
                            if (resultCode == Activity.RESULT_OK) {
                                GL.that.RunIfInitial(() -> {
                                    Log.info(sKlasse, "Photo taken");
                                    try {
                                        // move the photo from temp to UserImageFolder
                                        String sourceName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
                                        String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + ".jpg";
                                        if (!sourceName.equals(destinationName)) {
                                            AbstractFile source = FileFactory.createFile(sourceName);
                                            AbstractFile destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sKlasse, "move from " + sourceName + " to " + destinationName + " failed");
                                            }
                                        }

                                        // for the photo to show within spoilers
                                        if (GlobalCore.isSetSelectedCache()) {
                                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                                            SpoilerView.getInstance().ForceReload();
                                        }

                                        ViewManager.that.reloadSprites(false);

                                        // track annotation
                                        String TrackFolder = Config.TrackFolder.getValue();
                                        String relativPath = FileIO.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                                        Location lastLocation = Locator.getInstance().getLastSavedFineLocation();
                                        if (lastLocation == null) {
                                            lastLocation = Locator.getInstance().getLocation(Location.ProviderType.any);
                                            if (lastLocation == null) {
                                                Log.info(sKlasse, "No (GPS)-Location for Trackrecording.");
                                                return;
                                            }
                                        }
                                        // Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und die Koordinaten nach der Aufnahme verwendet werden.
                                        TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + ".jpg", relativPath + "/" + mediaFileNameWithoutExtension + ".jpg", lastLocation, Global.GetTrackDateTimeString());
                                    } catch (Exception e) {
                                        Log.err(sKlasse, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sKlasse, "Intent Take Photo resultCode: " + resultCode);
                            }
                        }
                    };
                }
                androidApplication.addAndroidEventListener(handlingTakePhoto);
                mainActivity.startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            } else {
                Log.err(sKlasse, MediaStore.ACTION_IMAGE_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sKlasse, e.getLocalizedMessage());
        }
    }

    private void recVideo() {

        try {
            Log.info(sKlasse, "recVideo start " + GlobalCore.getSelectedCache());
            // define the file-name to save video taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!FileIO.createDirectory(directory)) {
                Log.err(sKlasse, "can't create " + directory);
                return;
            }
            mediaFileNameWithoutExtension = Global.GetDateTimeString();
            String cacheName;
            if (GlobalCore.isSetSelectedCache()) {
                String validName = FileIO.removeInvalidFatChars(GlobalCore.getSelectedCache().getGeoCacheCode() + "-" + GlobalCore.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Video";
            }
            mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;

            // Da ein Video keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
            recordingStartTime = Global.GetTrackDateTimeString();
            recordingStartCoordinate = Locator.getInstance().getLocation(Location.ProviderType.GPS);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "");
            videoUri = mainActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            // Log.info(uri.toString());
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
                                GL.that.RunIfInitial(() -> {
                                    Log.info(sKlasse, "Video recorded.");
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
                                            String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + "." + ext;
                                            AbstractFile destination = FileFactory.createFile(destinationName);
                                            if (!source.renameTo(destination)) {
                                                Log.err(sKlasse, "move from " + recordedVideoFilePath + " to " + destinationName + " failed");
                                            } else {
                                                Log.info(sKlasse, "Video saved at " + destinationName);
                                                // track annotation
                                                String TrackFolder = Config.TrackFolder.getValue();
                                                String relativPath = FileIO.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                                                TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + "." + ext, relativPath + "/" + mediaFileNameWithoutExtension + "." + ext, recordingStartCoordinate, recordingStartTime);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.err(sKlasse, e.getLocalizedMessage());
                                    }
                                });
                            } else {
                                Log.err(sKlasse, "Intent Record Video resultCode: " + resultCode);
                            }
                        }
                    };
                androidApplication.addAndroidEventListener(handlingRecordedVideo);
                mainActivity.startActivityForResult(intent, REQUEST_CAPTURE_VIDEO);
            } else {
                Log.err(sKlasse, MediaStore.ACTION_VIDEO_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            Log.err(sKlasse, e.toString());
        }
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
            if (PlatformUIBase.getClipboard() != null)
                text = text + ("\n" + PlatformUIBase.getClipboard().getContents());
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
                            Log.err(sKlasse, "request Dismiss Keyguard: Error");
                        }
                        public void onDismissSucceeded() {
                            Log.err(sKlasse, "request Dismiss Keyguard: succeeded");
                        }
                        public void onDismissCancelled() {
                            Log.err(sKlasse, "request Dismiss Keyguard: cancelled");
                        }
                    });
                }
                else {
                    Log.err(sKlasse, "keyguardManager not available (null)");
                }
                 */
            } else {
                Window window = mainActivity.getWindow();
                if (window != null) {
                    if (Config.RunOverLockScreen.getValue()) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    }
                }
            }
        });
    }

    private boolean sendMotionEvent(final MotionEvent event) {
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
            Log.err(sKlasse, "sendMotionEvent", e);
            return true;
        }
        return true;
    }

}

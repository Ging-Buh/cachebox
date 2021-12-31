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
package de.droidcachebox.gdx;

import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;
import static de.droidcachebox.settings.AllSettings.fadeToGrayAfterXSeconds;
import static de.droidcachebox.settings.AllSettings.nightMode;
import static de.droidcachebox.settings.AllSettings.useAndroidKeyboard;
import static de.droidcachebox.settings.AllSettings.useGrayFader;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.Energy;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.Dialog;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.SelectionMarker;
import de.droidcachebox.gdx.controls.TextInputInterface;
import de.droidcachebox.gdx.controls.animation.Fader;
import de.droidcachebox.gdx.controls.dialogs.Toast;
import de.droidcachebox.gdx.controls.popups.PopUp_Base;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.Trace;

public class GL implements ApplicationListener {

    public static final int FRAME_RATE_IDLE = 200;
    public static final int FRAME_RATE_ACTION = 50;
    public static final int FRAME_RATE_FAST_ACTION = 40;
    public static final boolean isTestVersion = false;
    public static GL that;
    private TextInputInterface textInput;
    private GL_Listener_Interface glListener; // implementation in ../ViewGL : Desktop-Launcher/DesktopMain/start
    private AsyncExecutor asyncExecutor;
    private int width, height;
    private MainViewBase mSplash;
    private MainViewBase mMainView;
    private boolean allIsInitialized;
    private boolean renderingIsStopped;
    private boolean darknessAnimationRuns;
    private Fader grayFader;
    private long GL_ThreadId;
    private Timer myTimer;
    private long timerValue;
    private ArrayList<IRunOnGL> runIfInitial = new ArrayList<>();
    private AtomicBoolean started = new AtomicBoolean(false);
    private PolygonSpriteBatch mPolygonSpriteBatch;
    private int FpsInfoPos = 0;
    private ParentInfo prjMatrix;
    private Sprite FpsInfoSprite;
    private ArrayList<IRunOnGL> runOnGL_List = new ArrayList<>();
    private ArrayList<IRunOnGL> runOnGL_ListWaitPool = new ArrayList<>();
    private AtomicBoolean isWorkOnRunOnGL = new AtomicBoolean(false);
    // private RenderStarted renderStartedListener = null;
    private float stateTime = 0;
    private long FBO_RunBegin = System.currentTimeMillis();
    private boolean FBO_RunLapsed = false;
    private HashMap<String, Float> caller = new HashMap<>();
    private HashMap<String, Integer> callerCount = new HashMap<>();
    private ModelBatch modelBatch;
    private float lastRenderOnceTime = -1;
    private float lastTouchX = 0;
    private float lastTouchY = 0;
    private GrayScaleShaderProgram grayScaleShaderProgram;
    private Sprite mDarknessSprite;
    private Pixmap mDarknessPixmap;
    private Texture mDarknessTexture;
    private HashMap<GL_View_Base, Integer> renderViews = new HashMap<>();
    private MainViewBase child;
    private CB_View_Base mDialog;
    private Dialog currentDialog;
    private boolean currentDialogIsShown;
    private CB_View_Base mActivity;
    private ActivityBase currentActivity;
    private boolean currentActivityIsShown;
    private CB_View_Base mMarkerOverlay;
    private SelectionMarker selectionMarkerCenter, selectionMarkerLeft, selectionMarkerRight;
    private boolean MarkerIsShown;
    private CB_View_Base mToastOverlay;
    private Toast toastView;
    private boolean toastIsShown;
    private EditTextField focusedEditTextField;
    private ArrayList<Dialog> dialogHistory = new ArrayList<>();
    private ArrayList<ActivityBase> activityHistory = new ArrayList<>();
    private PopUp_Base aktPopUp;
    private float darknessAlpha = 0f;

    public GL(int _width, int _height, MainViewBase splash, MainViewBase mainView) {
        width = _width;
        height = _height;
        mSplash = splash;
        mMainView = mainView;
        toastIsShown = false;
        renderingIsStopped = false;
        darknessAnimationRuns = false;
        currentDialogIsShown = false;
        currentActivityIsShown = false;
        MarkerIsShown = false;
        aktPopUp = null;
        that = this;
        allIsInitialized = false;
    }

    @Override
    public void create() {
        // ApplicationListener Implementation create()
        GL_UISizes.initial(width, height);
        initialize();
        nightMode.addSettingChangedListener(() -> {
            mDarknessSprite = null;// for new creation with changed color
        });

        if (Gdx.input != null) {
            Gdx.input.setInputProcessor(new GL_Input());
            // Gdx.input.setCatchBackKey(true);
            Gdx.input.setCatchKey(Input.Keys.BACK, true);
        }
    }

    @Override
    public void resize(int Width, int Height) {
        // ApplicationListener Implementation resize()
        width = Width;
        height = Height;
        if (child != null)
            child.setSize(width, height);
        // camera = new OrthographicCamera(width, height);
        prjMatrix = new ParentInfo(new Matrix4().setToOrtho2D(0, 0, width, height), new Vector2(0, 0), new CB_RectF(0, 0, width, height));
    }

    @Override
    public void render() {
        // ApplicationListener Implementation render()
        if (Gdx.gl == null) {
            Gdx.app.error("CB_UI GL", "GL.render() without initialization.");
            Log.err("GL:render()", new Exception("GL.render() without initialization."));
            return;
        }

        if (Energy.isDisplayOff())
            return;

        if (!started.get() || renderingIsStopped)
            return;

        if (grayFader == null) {
            grayFader = new Fader(); // !!! is calling GL.that.GLrenderOnce(true)
            grayFader.setAlwaysOn(!useGrayFader.getValue());
            grayFader.setTimeToFadeOut(fadeToGrayAfterXSeconds.getValue() * 1000);
            IChanged ce = () -> {
                grayFader.setAlwaysOn(!useGrayFader.getValue());
                grayFader.setTimeToFadeOut(fadeToGrayAfterXSeconds.getValue() * 1000);
                grayFader.resetFadeOut();
            };
            useGrayFader.addSettingChangedListener(ce);
            fadeToGrayAfterXSeconds.addSettingChangedListener(ce);
        }
        setGrayscale(grayFader.getValue());

        GL_ThreadId = Thread.currentThread().getId();

        if (glListener != null && glListener.isContinous()) {
            glListener.renderDirty();
        }

        stateTime += Gdx.graphics.getDeltaTime();

        FBO_RunBegin();

        isWorkOnRunOnGL.set(true);

        synchronized (runOnGL_List) {
            if (runOnGL_List.size() > 0) {

                if (runOnGL_List.size() > 200) {
                    System.out.print("zuviel");
                }

                for (IRunOnGL run : runOnGL_List) {
                    if (run != null) {
                        // Run only MAX_FBO_RENDER_CALLS
                        if (run instanceof IRenderFBO) {
                            if (canFBO()) {
                                run.run();
                            } else {
                                runOnGL_ListWaitPool.add(run);
                            }
                        } else
                            run.run();
                    }
                }

                runOnGL_List.clear();
            }
        }
        isWorkOnRunOnGL.set(false);

        synchronized (runOnGL_ListWaitPool) {
            if (runOnGL_ListWaitPool != null && runOnGL_ListWaitPool.size() > 0) {
                for (IRunOnGL run : runOnGL_ListWaitPool) {
                    if (run != null) {
                        // Run only MAX_FBO_RENDER_CALLS
                        if (run instanceof IRenderFBO) {

                            if (canFBO()) {
                                run.run();
                            } else {
                                // Log.debug(log, "Max_FBO_Render_Calls" + run.toString());
                                runOnGL_List.add(run);
                            }
                        } else
                            run.run();
                    }
                }

                runOnGL_ListWaitPool.clear();

            }
        }

        if (allIsInitialized) {
            synchronized (runIfInitial) {
                if (runIfInitial.size() > 0) {
                    for (IRunOnGL run : runIfInitial) {
                        if (run != null)
                            run.run();
                    }
                    runIfInitial.clear();
                }
            }
        }

        if (nightMode.getValue()) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        } else {

            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        mPolygonSpriteBatch.setColor(Color.WHITE);

        try {
            mPolygonSpriteBatch.begin();
        } catch (java.lang.IllegalStateException e) {
            mPolygonSpriteBatch.flush();
            mPolygonSpriteBatch.end();
            mPolygonSpriteBatch.begin();
        }
        mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());

        if (currentActivityIsShown && mActivity.getChildCount() <= 0) {
            currentActivityIsShown = false;
            PlatformUIBase.hideForDialog();
            renderOnce();
        }
        if (currentDialogIsShown && mDialog.getChildCount() <= 0) {
            currentDialogIsShown = false;
            PlatformUIBase.hideForDialog();
            renderOnce();
        }

        if (currentDialog != null && currentDialog.isDisposed()) {
            closeDialog(currentDialog);
        }

        if (currentActivity != null && currentActivity.isDisposed()) {
            closeActivity();
        }

        if (currentActivityIsShown) {
            drawDarknessSprite();
            mActivity.renderChildren(mPolygonSpriteBatch, prjMatrix);
        }

        if (!currentActivityIsShown && child != null) {
            child.renderChildren(mPolygonSpriteBatch, prjMatrix);
            // reset child Matrix
            mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (currentDialogIsShown || toastIsShown || MarkerIsShown)
            mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());

        if (currentDialogIsShown && mDialog.getChildCount() > 0) {
            // Zeichne Transparentes Rec um den Hintergrund abzudunkeln.

            drawDarknessSprite();
            mDialog.renderChildren(mPolygonSpriteBatch, prjMatrix);
            mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (toastIsShown) {
            mToastOverlay.renderChildren(mPolygonSpriteBatch, prjMatrix);
            mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (MarkerIsShown) {
            mMarkerOverlay.renderChildren(mPolygonSpriteBatch, prjMatrix);
            mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (GL_View_Base.debug) {
            Point first = GL_Input.that.getTouchDownPos();
            if (first != null) {
                int x = first.x;
                int y = height - first.y;
                int pointSize = 20;
                if (lastTouchX != x || lastTouchY != y) {
                    lastTouchX = x;
                    lastTouchY = y;
                }
                mPolygonSpriteBatch.draw(Sprites.LogIcons.get(14), x - (pointSize >> 1), y - (pointSize >> 1), pointSize, pointSize);
            }
        }

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        mPolygonSpriteBatch.setProjectionMatrix(prjMatrix.Matrix());

        if (isTestVersion) {
            float FpsInfoSize = 4 * UiSizes.getInstance().getScale();
            if (FpsInfoSprite != null) {
                mPolygonSpriteBatch.draw(FpsInfoSprite, FpsInfoPos, 2, FpsInfoSize, FpsInfoSize);
            } else {
                if (Sprites.Stars != null)// SpriteCache is initial
                {
                    FpsInfoSprite = new Sprite(Sprites.getSprite("pixel2x2"));
                    FpsInfoSprite.setColor(1.0f, 1.0f, 0.0f, 1.0f);
                    FpsInfoSprite.setSize(FpsInfoSize, FpsInfoSize);
                }
            }

            FpsInfoPos += UiSizes.getInstance().getScale();
            if (FpsInfoPos > 60 * UiSizes.getInstance().getScale()) {
                FpsInfoPos = 0;
            }

        }

        try {
            mPolygonSpriteBatch.end();
        } catch (Exception ignored) {
        }

        Gdx.gl.glFlush();
        Gdx.gl.glFinish();

    }

    @Override
    public void pause() {
        // ApplicationListener Implementation pause()
        onStop();
    }

    @Override
    public void resume() {
        // ApplicationListener Implementation resume()
        onStart();
    }

    @Override
    public void dispose() {
        // ApplicationListener Implementation dispose()
        disposeTexture();
        Sprites.destroyCache();
        Translation.writeMissingStringsToFile();
    }

    private void setGrayscale(float value) {
        if (grayScaleShaderProgram != null) {
            grayScaleShaderProgram.begin();
            grayScaleShaderProgram.setUniformf("grayscale", value);
            grayScaleShaderProgram.end();
        }
    }

    public void onStop() {
        // App wird verkleinert oder Gerät ausgeschaltet
        // Log.debug(log, "GL_Listener => onStop");
        stopTimer();
        if (glListener != null)
            glListener.renderContinous();
        child.onStop();
        toastView = null; // regenerate toast control
    }

    public void onStart() {
        // App wird wiederhergestellt oder Gerät eingeschaltet
        started.set(true);
        if (glListener != null)
            glListener.renderDirty();

        if (currentActivityIsShown || currentDialogIsShown) {
            PlatformUIBase.showForDialog();
        } else if (child != null) {
            child.onShow();
        }

        if (currentActivityIsShown) {
            if (mActivity != null)
                mActivity.onShow();
        }
        if (currentDialogIsShown) {
            if (mDialog != null)
                mDialog.onShow();
        }

        renderOnce();

    }

    public void showDialog(final Dialog dialog) {
        showDialog(dialog, false);
    }

    public void showDialog(final Dialog dialog, boolean atTop) {
        try {
            setFocusedEditTextField(null);
            clearRenderViews();
            if (dialog.isDisposed())
                return;

            // Center Dialog on Screen
            float x = (width - dialog.getWidth()) / 2;
            float y;
            if (atTop)
                y = height - dialog.getHeight();// - (UI_Size_Base.that.getMargin() * 4);
            else
                y = (height - dialog.getHeight()) / 2;
            dialog.setPos(x, y);

            if (aktPopUp != null) {
                closePopUp(aktPopUp);
            }

            if (currentDialog != null && currentDialog != dialog) {
                currentDialog.onHide();
                currentDialog.setEnabled(false);
                // am Anfang der Liste einfügen
                dialogHistory.add(0, currentDialog);
                mDialog.removeChildDirect(currentDialog);
            }

            currentDialog = dialog;

            mDialog.addChildDirect(dialog);
            mDialog.setClickHandler((v, x1, y1, pointer, button) -> {
                // Sollte bei einem Click neben dem Dialog ausgelöst werden.
                // Dann soll der Dialog geschlossen werden, wenn es sich um ein Menü handelt.
                if (currentDialogIsShown) {
                    GL_View_Base vDialog = mDialog.getChild(0);
                    if (vDialog instanceof Menu)
                        closeDialog(currentDialog);
                    if (aktPopUp != null) {
                        closePopUp(aktPopUp);
                    }
                    return true;
                }

                if (aktPopUp != null) {
                    closePopUp(aktPopUp);
                    return true;
                }

                return false;
            });

            child.setClickable(false);
            currentDialogIsShown = true;
            darknessAnimationRuns = true;
            currentDialog.onShow();
            try {
                currentDialog.setEnabled(true);
                currentDialog.setVisible();
            } catch (Exception ignored) {
            }
            PlatformUIBase.showForDialog();
        } catch (Exception ex) {
            Log.err("GL", "ShowDialog", ex);
        }

        renderOnce();
    }

    public void clearRenderViews() {
        synchronized (renderViews) {
            stopTimer();
            renderViews.clear();
        }
    }

    public void closeDialog(CB_View_Base dialog) {
        if (dialog instanceof ActivityBase)
            throw new IllegalArgumentException("don't show an Activity as Dialog. Use \"GL_listener.showActivity()\"");
        closeDialog(dialog, true);
    }

    private void closeDialog(final CB_View_Base dialog, boolean msgToPlatformConector) {

        if (!currentDialogIsShown || !mDialog.getchilds().contains((dialog))) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (dialog == null || dialog.isDisposed())
                        return;
                    if (dialog.equals(mDialog))
                        throw new IllegalStateException("dialog can't dispose");
                    dialog.dispose();
                }
            };
            timer.schedule(task, 50);
        }

        if (msgToPlatformConector)
            PlatformUIBase.hideForDialog();
        if (currentDialog != null) {
            //check if KeyboardFocus on this Dialog
            if (focusedEditTextField != null && focusedEditTextField.getParent() == currentDialog) {
                setFocusedEditTextField(null);
            }
            currentDialog.onHide();
        }

        if (dialogHistory.size() > 0) {
            mDialog.removeChild(currentDialog);
            // letzten Dialog wiederherstellen
            currentDialog = dialogHistory.get(0);
            // currentDialog.onShow();
            // currentDialog.setEnabled(true);
            dialogHistory.remove(0);
            // currentDialogIsShown = true;
            // platformConector.showForDialog();
            showDialog(currentDialog);
        } else {
            currentDialog = null;
            mDialog.removeChildsDirect();
            child.setClickable(true);
            // child.invalidate();
            currentDialogIsShown = false;
            darknessAlpha = 0f;
        }

        if (dialog != null) {
            if (!dialog.isDisposed()) {
                dialog.dispose();
            }
        }

        clearRenderViews();
        if (currentActivityIsShown) {
            PlatformUIBase.showForDialog();

        }
        renderOnce();
    }

    public void closePopUp(PopUp_Base popUp) {
        CB_View_Base aktView = currentDialogIsShown ? mDialog : child;
        if (currentActivityIsShown)
            aktView = mActivity;

        aktView.removeChild(popUp);
        if (aktPopUp != null)
            aktPopUp.onHide();
        aktPopUp = null;
        if (popUp != null)
            popUp.dispose();

        renderOnce();
    }

    public void showActivity(final ActivityBase activity) {
        setFocusedEditTextField(null);
        clearRenderViews();
        PlatformUIBase.showForDialog();

        if (aktPopUp != null) {
            closePopUp(aktPopUp);
        }

        darknessAnimationRuns = true;

        // Center activity on Screen
        float x = (width - activity.getWidth()) / 2;
        float y = (height - activity.getHeight()) / 2;
        activity.setPos(x, y);

        if (currentDialog != null) {
            currentDialog.onHide();
        }

        if (currentActivity != null && currentActivity != activity) {
            currentActivity.onHide();
            currentActivity.setEnabled(false);
            // am Anfang der Liste einfügen
            activityHistory.add(0, currentActivity);
            mActivity.removeChildDirect(currentActivity);
        }

        currentActivity = activity;

        mActivity.addChildDirect(activity);

        child.setClickable(false);
        currentActivityIsShown = true;
        child.onHide();
        currentActivity.onShow();

        PlatformUIBase.showForDialog();
    }

    public void closeActivity() {
        closeActivity(true);
    }

    public void closeActivity(boolean MsgToPlatformConector) {
        if (!currentActivityIsShown)
            return;

        //check if KeyboardFocus on this Activitiy
        if (focusedEditTextField != null && focusedEditTextField.getParent() == currentActivity) {
            setFocusedEditTextField(null);
        }

        if (activityHistory.size() > 0) {
            mActivity.removeChild(currentActivity);
            currentActivity.onHide();
            // letzten Dialog wiederherstellen
            currentActivity = activityHistory.get(0);
            currentActivity.onShow();
            currentActivity.setEnabled(true);
            activityHistory.remove(0);
            currentActivityIsShown = true;
            mActivity.addChildDirect(currentActivity);
            if (MsgToPlatformConector)
                PlatformUIBase.showForDialog();
        } else {
            currentActivity.onHide();

            Timer disposeTimer = new Timer();
            TimerTask disposeTask = new TimerTask() {
                @Override
                public void run() {
                    if (currentActivity != null)
                        currentActivity.dispose();
                    currentActivity = null;
                    System.gc();
                }
            };
            disposeTimer.schedule(disposeTask, 700);

            mActivity.removeChildsDirect();
            child.setClickable(true);
            // child.invalidate();
            currentActivityIsShown = false;
            darknessAlpha = 0f;
            if (MsgToPlatformConector)
                PlatformUIBase.hideForDialog();
            child.onShow();
        }

        clearRenderViews();
        renderOnce();
    }

    public Dialog getCurrentDialog() {
        return currentDialog;
    }

    boolean closeCurrentDialogOrActivity() {
        if (currentDialogIsShown) {
            closeDialog(currentDialog);
            return true;
        }

        if (currentActivityIsShown) {
            if (currentActivity != null) {
                if (!currentActivity.canCloseWithBackKey())
                    return true;
            }
            closeActivity();
            return true;
        }

        return false;
    }

    public void showMarker(SelectionMarker.Type type) {
        if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null)
            initMarkerOverlay();

        switch (type) {
            case Center:
                selectionMarkerCenter.setVisible();
                break;
            case Left:
                selectionMarkerLeft.setVisible();
                break;
            case Right:
                selectionMarkerRight.setVisible();
                break;
        }

        MarkerIsShown = true;
    }

    public void hideMarker() {
        if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null)
            initMarkerOverlay();
        selectionMarkerCenter.setInvisible();
        selectionMarkerLeft.setInvisible();
        selectionMarkerRight.setInvisible();

        MarkerIsShown = false;
    }

    public boolean isNotShownDialogOrActivity() {
        return !currentDialogIsShown && !currentActivityIsShown;
    }

    public boolean PopUpIsHidden() {
        return (aktPopUp == null);
    }

    private void disposeTexture() {
        if (mDarknessPixmap != null)
            mDarknessPixmap.dispose();
        if (mDarknessTexture != null)
            mDarknessTexture.dispose();
        mDarknessPixmap = null;
        mDarknessTexture = null;
        mDarknessSprite = null;
    }

    public boolean isGlThread() {
        return GL_ThreadId == Thread.currentThread().getId();
    }

    public void setBatchColor(HSV_Color color) {

        float gray = grayFader.getValue();

        if (gray < 1f) {
            float h = color.getSat() * gray;
            HSV_Color grayColor = new HSV_Color(color);
            grayColor.setSat(h);
            mPolygonSpriteBatch.setColor(grayColor);
        } else {
            mPolygonSpriteBatch.setColor(color);
        }
    }

    private void startTimer(long delay) {
        if (timerValue == delay)
            return;
        stopTimer();

        timerValue = delay;
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

            private void TimerMethod() {
                if (glListener != null)
                    glListener.requestRender();
            }

        }, 0, delay);

    }

    private void stopTimer() {
        // Log.debug(log, "Stop Timer");
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
        timerValue = 0;
    }

    /**
     * Run on GL-Thread!<br>
     * If this Thread the GL_thread, run direct!
     */
    public void RunOnGLWithThreadCheck(IRunOnGL run) {
        if (isGlThread()) {
            run.run();
        } else {
            RunOnGL(run);
        }
    }

    public void RunOnGL(IRunOnGL run) {
        // if in progress put into pool
        if (isWorkOnRunOnGL.get()) {
            synchronized (runOnGL_ListWaitPool) {
                runOnGL_ListWaitPool.add(run);
                renderOnce(true);
                return;
            }
        }
        synchronized (runOnGL_List) {
            runOnGL_List.add(run);
        }
    }

    public void RunIfInitial(IRunOnGL run) {
        synchronized (runIfInitial) {
            runIfInitial.add(run);
        }
        renderOnce(true);
    }

    private void setDefaultShader() {
        mPolygonSpriteBatch.setShader(SpriteBatch.createDefaultShader());
    }

    private void setGrayScaleShaderProgram(ShaderProgram grayScaleShaderProgram) {
        if (grayScaleShaderProgram == null) {
            setDefaultShader();
            return;
        }
        mPolygonSpriteBatch.setShader(grayScaleShaderProgram);
    }

    public float getStateTime() {
        return stateTime;
    }

    private void FBO_RunBegin() {
        FBO_RunBegin = System.currentTimeMillis();
        FBO_RunLapsed = false;
    }

    private boolean canFBO() {
        if (FBO_RunLapsed)
            return false;
        int MAX_FBO_RENDER_TIME = 200;
        if (FBO_RunBegin + MAX_FBO_RENDER_TIME >= System.currentTimeMillis())
            return true;
        FBO_RunLapsed = true;
        return false;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public MainViewBase getChild() {
        return child;
    }

    private void drawDarknessSprite() {
        if (mPolygonSpriteBatch == null)
            return;
        if (mDarknessSprite == null) {
            disposeTexture();
            mDarknessPixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
            mDarknessPixmap.setColor(COLOR.getDarknessColor());
            mDarknessPixmap.fillRectangle(0, 0, width, height);
            mDarknessTexture = new Texture(mDarknessPixmap, Pixmap.Format.RGBA8888, false);
            mDarknessSprite = new Sprite(mDarknessTexture, width, height);
        }

        mDarknessSprite.draw(mPolygonSpriteBatch, darknessAlpha);
        if (darknessAnimationRuns) {
            darknessAlpha += 0.1f;
            if (darknessAlpha > 1f) {
                darknessAlpha = 1f;
                darknessAnimationRuns = false;
            }
            renderOnce();
        }

    }

    private void initialize() {

        if (Gdx.graphics.getGL20() == null)
            return;// kann nicht initialisiert werden

        if (mPolygonSpriteBatch == null) {
            mPolygonSpriteBatch = new PolygonSpriteBatch(10920);// PolygonSpriteBatch(10920);
        }

        if (modelBatch == null) {
            try {
                modelBatch = new ModelBatch();
            } catch (java.lang.NoSuchFieldError ignored) {
            }
        }

        if (child == null) {
            child = mSplash;
            child.setClickable(true);
            child.setLongClickable(true);
        }

        if (mDialog == null) {
            mDialog = new MainViewBase(new CB_RectF(0, 0, width, height));
            mDialog.setClickable(true);
            mDialog.setLongClickable(true);
        }

        if (mActivity == null) {
            mActivity = new MainViewBase(new CB_RectF(0, 0, width, height));
            mActivity.setClickable(true);
            mActivity.setLongClickable(true);
        }

        // initialize GrayScale shader
        grayScaleShaderProgram = new GrayScaleShaderProgram();
        setGrayScaleShaderProgram(grayScaleShaderProgram);
        setGrayscale(0.5f);

    }

    public CB_View_Base getDialogLayer() {
        return mDialog;
    }

    private void initMarkerOverlay() {
        mMarkerOverlay = new Box(new CB_RectF(0, 0, width, height), "MarkerOverlay");
        selectionMarkerCenter = new SelectionMarker(SelectionMarker.Type.Center);
        selectionMarkerLeft = new SelectionMarker(SelectionMarker.Type.Left);
        selectionMarkerRight = new SelectionMarker(SelectionMarker.Type.Right);

        hideMarker();

        mMarkerOverlay.addChild(selectionMarkerCenter);
        mMarkerOverlay.addChild(selectionMarkerLeft);
        mMarkerOverlay.addChild(selectionMarkerRight);

    }

    public void setGLViewID() {
        if (child == null)
            initialize();
    }

    public void addRenderView(GL_View_Base view, int delay) {
        synchronized (renderViews) {
            if (!view.isVisible()) {
                if (renderViews.containsKey(view)) {
                    renderViews.remove(view);
                    calcNewRenderSpeed();
                    if (glListener != null)
                        glListener.requestRender();
                }
                return;
            }
            renderViews.remove(view);
            renderViews.put(view, delay);
            calcNewRenderSpeed();
            if (glListener != null)
                glListener.requestRender();
        }
    }

    public void removeRenderView(GL_View_Base view) {
        synchronized (renderViews) {
            if (renderViews.containsKey(view)) {
                renderViews.remove(view);
                calcNewRenderSpeed();
            }
        }
    }

    public void renderOnce() {
        requestRender(false);
    }

    public void renderOnce(boolean force) {
        requestRender(force);
    }

    /**
     * Führt EINEN Render Durchgang aus
     */
    private void requestRender(boolean force) {

        if (!force && lastRenderOnceTime == getStateTime())
            return;

        String name = Trace.getCallerName(1);
        if (caller.containsKey(name)) {
            float last = stateTime - caller.get(name);
            caller.put(name, stateTime);
            if (last < 0.008) {
                if (callerCount != null) {
                    int lastCount = callerCount.get(name) + 1;
                    if (lastCount > 50) {
                        lastCount = 0;
                        // Log.err(log, "to many calls from: " + name);
                    }
                    callerCount.put(name, lastCount);
                }
            }
        } else {
            callerCount.put(name, 0);
            caller.put(name, stateTime);
        }

        lastRenderOnceTime = getStateTime();

        if (glListener != null)
            glListener.requestRender();
    }

    private void calcNewRenderSpeed() {
        synchronized (renderViews) {
            int minDelay = 0;
            for (int delay : renderViews.values()) {
                if (delay > minDelay)
                    minDelay = delay;
            }
            if (minDelay == 0)
                stopTimer();
            else
                startTimer(minDelay);
        }

    }

    CB_View_Base getActiveView() {
        return currentDialogIsShown ? mDialog : currentActivityIsShown ? mActivity : child;
    }

    GL_View_Base touchActiveView(int x, int y, int pointer, int button) {
        GL_View_Base view = null;
        if (MarkerIsShown) {
            view = mMarkerOverlay.touchDown(x, (int) mMarkerOverlay.getHeight() - y, pointer, button);
        }
        if (view == null) {
            CB_View_Base activeView = getActiveView();
            return activeView.touchDown(x, (int) activeView.getHeight() - y, pointer, button);
        }
        return view;
    }

    public void showPopUp(PopUp_Base popUp, float x, float y) {
        popUp.setX(x);
        popUp.setY(y);

        CB_View_Base aktView = currentDialogIsShown ? mDialog : child;
        if (currentActivityIsShown && !currentDialogIsShown)
            aktView = mActivity;

        aktView.addChild(popUp);
        aktPopUp = popUp;
        aktPopUp.onShow();
        renderOnce();
    }

    public void closeAllDialogs() {
        for (CB_View_Base dialog : dialogHistory) {
            dialog.onHide();
        }
        dialogHistory.clear();

        if (currentDialog != null)
            closeDialog(currentDialog);

        for (CB_View_Base activity : activityHistory) {
            activity.onHide();
        }
        activityHistory.clear();
        if (currentActivity != null)
            closeActivity(true);

        currentActivityIsShown = false;
        currentDialogIsShown = false;
    }

    public void closeToast() {
        if (mToastOverlay != null) {
            toastIsShown = false;
            mToastOverlay.removeChilds();
            renderOnce();
        }
    }

    private void toast(CB_View_Base view) {
        if (mToastOverlay == null) {
            mToastOverlay = new Box(new CB_RectF(0, 0, width, height), "ToastView");
        }
        synchronized (mToastOverlay) {
            mToastOverlay.removeChilds();

            mToastOverlay.addChild(view);
            toastIsShown = true;

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    toastIsShown = false;
                    renderOnce();
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, 4000); // a constant delay for toasting of 4000
        }
    }

    public void toast(String string) {
        if (toastView == null) {
            toastView = new Toast(new CB_RectF(0, 0, 100, mainButtonSize.getHeight() / 1.5f), "StringToast");
        }
        toastView.setWrappedText(string);
        GlyphLayout bounds = null;
        try {
            bounds = Fonts.measureWrapped(string, UiSizes.getInstance().getWindowWidth());
        } catch (Exception e) {
            bounds = Fonts.measureWrapped("", UiSizes.getInstance().getWindowWidth());
        }
        float border = +(toastView.getLeftWidth() * 2) + (UiSizes.getInstance().getMargin() * 2);
        toastView.setWidth(bounds.width + border);
        toastView.setHeight(bounds.height + border);
        toastView.setPos((width >> 1) - (bounds.width / 2), mainButtonSize.getHeight() * 1.3f);
        toast(toastView);
    }

    /**
     * Stopt den Rendervorgang bis er durch RestartRender() wieder gestartet wird
     */
    public void stopRendering() {
        renderingIsStopped = true;
    }

    /**
     * Startet den Renderer, nicht nur wenn er durch StopRender() gestoppt wurde
     * auch MainViewInit:initialize()
     * und
     * ViewManager:reloadSprites(boolean switchDayNight)
     */
    public void restartRendering() {
        if (glListener != null) {
            glListener.renderContinous();
        }
        renderingIsStopped = false;
        renderOnce();
        setAllIsInitialized(true);
    }

    public EditTextField getFocusedEditTextField() {
        return focusedEditTextField;
    }

    public void setFocusedEditTextField(EditTextField editTextField) {
        if (editTextField == null && focusedEditTextField == null) {
            // neither to focus the editTextField nor to unfocus the focusedEditTextField
            return;
        }
        // inform the parent, perhaps to move the editTextField to the top of the screen
        KeyboardFocusChangedEventList.keyboardFocusChanged(editTextField);
        // inform the textfield, that it got the focus
        if (editTextField != null && editTextField != focusedEditTextField) {
            editTextField.becomesFocus();
        }

        hideMarker();

        // show or hide keyboard (what if the user has closed it? : it never comes up todo)
        boolean isAlreadyOpen = focusedEditTextField != null && focusedEditTextField.isKeyboardPopupEnabled();
        boolean shallBeOpened = editTextField != null && editTextField.isKeyboardPopupEnabled();
        if (isAlreadyOpen) {
            if (!shallBeOpened) {
                if (!useAndroidKeyboard.getValue() || Plattform.used != Plattform.Android)
                    Gdx.input.setOnscreenKeyboardVisible(false);
            }
        } else {
            if (shallBeOpened) {
                if (useAndroidKeyboard.getValue() && Plattform.used == Plattform.Android)
                    textInput.requestKeyboard(editTextField);
                else Gdx.input.setOnscreenKeyboardVisible(true);
            }
        }

        focusedEditTextField = editTextField;
    }

    public boolean hasFocus(EditTextField view) {
        return view == focusedEditTextField;
    }


    public void selectionMarkerCenterMoveTo(float f, float g) {
        selectionMarkerCenter.moveTo(f, g);
    }

    public void selectionMarkerLeftMoveTo(float f, float g) {
        selectionMarkerLeft.moveTo(f, g);
    }

    public void selectionMarkerRightMoveTo(float f, float g) {
        selectionMarkerRight.moveTo(f, g);
    }

    public boolean selectionMarkerCenterisShown() {
        return selectionMarkerCenter.isVisible();
    }

    public void selectionMarkerCenterMoveBy(float dx, float dy) {
        selectionMarkerCenter.moveBy(dx, dy);
    }

    public boolean selectionMarkerLeftisShown() {
        return selectionMarkerLeft.isVisible();
    }

    public void selectionMarkerLeftMoveBy(float dx, float dy) {
        selectionMarkerLeft.moveBy(dx, dy);
    }

    public boolean selectionMarkerRightisShown() {
        return selectionMarkerRight.isVisible();
    }

    public void selectionMarkerRightMoveBy(float dx, float dy) {
        selectionMarkerRight.moveBy(dx, dy);
    }

    public boolean getAllisInitialized() {
        return allIsInitialized;
    }

    public void setAllIsInitialized(boolean value) {
        allIsInitialized = value;
    }

    public PolygonSpriteBatch getPolygonSpriteBatch() {
        return mPolygonSpriteBatch;
    }

    public void switchToMainView() {
        child = mMainView;
        mSplash.dispose();
        mSplash = null;
        initMarkerOverlay();
        mMainView.onShow();
        if (glListener != null)
            glListener.renderDirty();
    }

    void resetAmbiantMode() {
        grayFader.resetFadeOut();
    }

    public GL_View_Base onTouchDown(int x, int y, int pointer, int button) {
        GL_View_Base view;

        CB_View_Base testingView = currentDialogIsShown ? mDialog : currentActivityIsShown ? mActivity : child;

        view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);

        return view;
    }

    public void postAsync(final Runnable runnable) {
        if (asyncExecutor == null) {
            asyncExecutor = new AsyncExecutor(8);
        }
        asyncExecutor.submit((AsyncTask<Void>) () -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                Log.err("GL", "postAsync ", e);
            }
            return null;
        });
    }

    public GL_Listener_Interface getGlListener() {
        return glListener;
    }

    public void setGlListener(GL_Listener_Interface _GL_Listener_Interface) {
        glListener = _GL_Listener_Interface;
    }

    public void setTextInput(TextInputInterface textInputInterface) {
        textInput = textInputInterface;
    }

}

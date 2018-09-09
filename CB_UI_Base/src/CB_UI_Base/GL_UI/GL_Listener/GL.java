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
package CB_UI_Base.GL_UI.GL_Listener;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.KeyCodes;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.*;
import CB_UI_Base.GL_UI.Controls.Animation.Fader;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.Controls.SelectionMarker.Type;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.Global;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Trace;
import CB_Utils.Math.Point;
import CB_Utils.Util.HSV_Color;
import CB_Utils.Util.IChanged;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GL implements ApplicationListener, InputProcessor {

    // Public Static Constants
    public static final int MAX_KINETIC_SCROLL_DISTANCE = 100;
    public static final int FRAME_RATE_IDLE = 200;
    public static final int FRAME_RATE_ACTION = 50;
    public static final int FRAME_RATE_FAST_ACTION = 40;

    private static final AtomicBoolean ambientMode = new AtomicBoolean(false);
    private static final boolean TOUCH_DEBUG = true;
    // Public Static Member
    public static GL_Listener_Interface listenerInterface;
    public static GL that;
    public static long GL_ThreadId;
    public static PolygonSpriteBatch batch;
    public static OrthographicCamera camera;
    // Private Static Member
    public static AtomicBoolean started = new AtomicBoolean(false);
    public static ArrayList<IRunOnGL> runIfInitial = new ArrayList<IRunOnGL>();
    public static boolean ifAllInitial = false;
    public static String MaptileLoaderDebugString = "";
    /**
     * Static for Debug
     */
    protected static HashMap<GL_View_Base, Integer> renderViews = new HashMap<GL_View_Base, Integer>();
    private static Timer myTimer;
    private static long timerValue;
    private static boolean isTouchDown = false;
    /**
     * See http://code.google.com/p/libgdx/wiki/SpriteBatch Performance tuning
     */
    protected final int SPRITE_BATCH_BUFFER = 150;
    private final int MAX_FBO_RENDER_TIME = 200;
    private final boolean FORCE = true;
    private final long mDoubleClickTime = 500;
    private final ArrayList<Dialog> dialogHistory = new ArrayList<Dialog>();
    private final ArrayList<CB_View_Base> activityHistory = new ArrayList<CB_View_Base>();
    private final MainViewBase mMainView;
    public Dialog actDialog;
    protected boolean ToastIsShown = false;
    protected boolean stopRender = false;
    protected boolean MarkerIsShown = false;
    protected int FpsInfoPos = 0;
    protected ParentInfo prjMatrix;
    protected Sprite FpsInfoSprite;
    protected ArrayList<IRunOnGL> runOnGL_List = new ArrayList<IRunOnGL>();
    protected ArrayList<IRunOnGL> runOnGL_ListWaitpool = new ArrayList<IRunOnGL>();
    protected AtomicBoolean isWorkOnRunOnGL = new AtomicBoolean(false);
    /**
     * Zwischenspeicher für die touchDown Positionen der einzelnen Finger
     */
    protected SortedMap<Integer, TouchDownPointer> touchDownPos = Collections.synchronizedSortedMap((new TreeMap<Integer, TouchDownPointer>()));
    // private Listener
    protected RenderStarted renderStartedListener = null;
    // Protected Member
    protected MainViewBase child;
    protected CB_View_Base mDialog, mActivity, mToastOverlay, mMarkerOverlay;
    protected SelectionMarker selectionMarkerCenter, selectionMarkerLeft, selectionMarkerRight;
    protected boolean DialogIsShown = false, ActivityIsShown = false;
    protected int width = 0, height = 0;
    protected boolean ShaderSetted = false;
    protected float stateTime = 0;
    protected int debugSpritebatchMaxCount = 0;
    protected long lastRenderBegin = 0;
    protected long renderTime = 0;
    // private Threads
    Thread threadDisposeDialog;
    long FBO_RunBegin = System.currentTimeMillis();
    boolean FBO_RunLapsed = false;
    Fader grayFader = null;
    int findCallerCount = 0;
    String lastCaller = "";
    HashMap<String, Float> caller = new HashMap<String, Float>();
    HashMap<String, Integer> callerCount = new HashMap<String, Integer>();
    // 3D Parts
    private render3D mAct3D_Render;
    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    // private Member
    private boolean touchDraggedActive = false;
    private Point touchDraggedCorrect = new Point(0, 0);
    private boolean darknessAnimationRuns = false;
    private float darknessAlpha = 0f;
    private long mLongClickTime = 0;
    private long lastClickTime = 0;
    private float lastRenderOnceTime = -1;
    private CB_View_Base disposeAcktivitie;
    private Point lastClickPoint = null;
    private CB_View_Base actActivity;

    // Overrides
    private PopUp_Base aktPopUp = null;
    private CB_UI_Base.GL_UI.Controls.Dialogs.Toast toast;
    private Timer longClickTimer;
    private Sprite mDarknesSprite;
    private Pixmap mDarknesPixmap;
    private Texture mDarknesTexture;
    private EditTextField focusedEditTextField;
    private MainViewBase mSplash;
    private float lastTouchX = 0;
    private float lastTouchY = 0;
    private GrayscalShaderProgram shader;
    private int MouseX = 0;
    private int MouseY = 0;

    /**
     * Constructor
     */
    public GL(int initalWidth, int initialHeight, MainViewBase splash, MainViewBase mainView) {
        mSplash = splash;
        mMainView = mainView;
        that = this;
        width = initalWidth;
        height = initialHeight;
        if (CB_UI_Base_Settings.LongClicktime == null) {
            mLongClickTime = 600;
        } else {
            mLongClickTime = CB_UI_Base_Settings.LongClicktime.getValue();
        }
    }

    public static void setAmbientMode(boolean value) {
        ambientMode.set(value);
    }

    public static void setIsInitial() {
        ifAllInitial = true;
    }

    public static void resetIsInitial() {
        ifAllInitial = false;
    }

    public static boolean isInitial() {
        return ifAllInitial;
    }

    public static boolean isGlThread() {
        return GL_ThreadId == Thread.currentThread().getId();
    }

    public static void setBatchColor(HSV_Color color) {

        float gray = that.grayFader.getValue();

        if (gray < 1f) {
            float h = color.getSat() * gray;
            HSV_Color grayColor = new HSV_Color(color);
            grayColor.setSat(h);
            GL.batch.setColor(grayColor);
        } else {
            GL.batch.setColor(color);
        }
    }

    public static boolean getIsTouchDown() {
        return isTouchDown;
    }

    public static void startTimer(long delay, final String Name) {
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
                if (listenerInterface != null)
                    listenerInterface.RequestRender();
            }

        }, 0, delay);

    }

    public static void stopTimer() {
        // Log.debug(log, "Stop Timer");
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
        timerValue = 0;
    }

    @Override
    public void create() {
        GL_UISizes.initial(width, height);

        Initialize();
        CB_UI_Base_Settings.nightMode.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                mDarknesSprite = null;// for new creation with changed color
            }
        });

        if (Gdx.input != null) {
            Gdx.input.setInputProcessor(this);
            Gdx.input.setCatchBackKey(true);
        }
    }

    /**
     * Run on GL-Thread!<br>
     * If this Thread the GL_thread, run direct!
     *
     * @param run
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
            synchronized (runOnGL_ListWaitpool) {
                runOnGL_ListWaitpool.add(run);
                renderOnce(FORCE);
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
        renderOnce(FORCE);
    }

    public void setDefaultShader() {
        batch.setShader(SpriteBatch.createDefaultShader());
        ShaderSetted = true;
    }

    public void setShader(ShaderProgram shader) {

        if (shader == null) {
            setDefaultShader();
            return;
        }
        batch.setShader(shader);
        ShaderSetted = true;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void register3D(final render3D renderView) {
        RunOnGL(new IRunOnGL() {
            @Override
            public void run() {
                mAct3D_Render = renderView;
            }
        });

    }

    public void unregister3D() {
        mAct3D_Render = null;
    }

    private void FBO_RunBegin() {
        FBO_RunBegin = System.currentTimeMillis();
        FBO_RunLapsed = false;
    }

    private boolean canFBO() {
        if (FBO_RunLapsed)
            return false;
        if (FBO_RunBegin + MAX_FBO_RENDER_TIME >= System.currentTimeMillis())
            return true;
        FBO_RunLapsed = true;
        return false;
    }

    public void resetAmbianeMode() {
        grayFader.resetFadeOut();
    }

    /**
     * render
     */
    @Override
    public void render() {

        if (Gdx.gl == null) {
            //	    Log.err(log, "GL.render() with not initial GDX.gl");
            Gdx.app.error("CB_UI GL", "GL.render() with not initial GDX.gl");
            return;
        }

        if (grayFader == null) {
            grayFader = new Fader();
            grayFader.setAlwaysOn(CB_UI_Base_Settings.dontUseAmbient.getValue());
            grayFader.setTimeToFadeOut(CB_UI_Base_Settings.ambientTime.getValue() * 1000);

            IChanged ce = new IChanged() {

                @Override
                public void isChanged() {
                    grayFader.setAlwaysOn(CB_UI_Base_Settings.dontUseAmbient.getValue());
                    grayFader.setTimeToFadeOut(CB_UI_Base_Settings.ambientTime.getValue() * 1000);
                    grayFader.resetFadeOut();
                }
            };

            CB_UI_Base_Settings.dontUseAmbient.addChangedEventListener(ce);
            CB_UI_Base_Settings.ambientTime.addChangedEventListener(ce);
        }

        setGrayscale(ambientMode.get() ? 0f : grayFader.getValue());

        GL_ThreadId = Thread.currentThread().getId();
        if (Energy.DisplayOff())
            return;

        if (!started.get() || stopRender)
            return;
        if (listenerInterface != null && listenerInterface.isContinous()) {
            // Log.debug(log, "Reset Continous rendering");
            listenerInterface.RenderDirty();
        }
        stateTime += Gdx.graphics.getDeltaTime();

        lastRenderBegin = System.currentTimeMillis();

        if (renderStartedListener != null) {
            renderStartedListener.renderIsStartet();
            renderStartedListener = null;
            removeRenderView(child);
        }

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
                                // Log.debug(log, "Max_FBO_Render_Calls" + run.toString());
                                runOnGL_ListWaitpool.add(run);
                            }
                        } else
                            run.run();
                    }
                }

                runOnGL_List.clear();
            }
        }
        isWorkOnRunOnGL.set(false);

        // work RunOnGlPool
        synchronized (runOnGL_ListWaitpool) {
            if (runOnGL_ListWaitpool != null && runOnGL_ListWaitpool.size() > 0) {
                if (runOnGL_ListWaitpool.size() > 0) {
                    for (IRunOnGL run : runOnGL_ListWaitpool) {
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

                    runOnGL_ListWaitpool.clear();
                }

            }
        }

        if (ifAllInitial) {
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

        if (CB_UI_Base_Settings.nightMode.getValue()) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        } else {

            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ?
        // GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        // reset BatchColor
        batch.setColor(Color.WHITE);

        {// Render 3D
            if (mAct3D_Render != null) {
                if (modelBatch == null) {
                    modelBatch = new ModelBatch();
                } else {
                    if (mAct3D_Render.is3D_Initial()) {
                        // modify 3Dcam
                        PerspectiveCamera retCam = mAct3D_Render.get3DCamera(cam);
                        if (retCam != null)
                            cam = retCam; // Cam modified
                        cam.update();
                        modelBatch.begin(cam);
                        mAct3D_Render.render3d(modelBatch);
                        modelBatch.end();
                    } else {
                        mAct3D_Render.Initial3D();
                    }

                }
            }
        }

        try {
            batch.begin();
        } catch (java.lang.IllegalStateException e) {
            // Log.err(log, "IllegalStateException", "batch.begin() without batch.end()", e);

            batch.flush();
            batch.end();
            batch.begin();
        }
        batch.setProjectionMatrix(prjMatrix.Matrix());

        if (ActivityIsShown && mActivity.getCildCount() <= 0) {
            ActivityIsShown = false;
            PlatformConnector.hideForDialog();
            renderOnce();
        }
        if (DialogIsShown && mDialog.getCildCount() <= 0) {
            DialogIsShown = false;
            PlatformConnector.hideForDialog();
            renderOnce();
        }

        if (actDialog != null && actDialog.isDisposed()) {
            closeDialog(actDialog);
        }

        if (actActivity != null && actActivity.isDisposed()) {
            closeActivity();
        }


        if (ActivityIsShown) {
            drawDarknessSprite();
            mActivity.renderChilds(batch, prjMatrix);
        }

        if (!ActivityIsShown) {
            child.renderChilds(batch, prjMatrix);
            // reset child Matrix
            batch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (DialogIsShown || ToastIsShown || MarkerIsShown)
            batch.setProjectionMatrix(prjMatrix.Matrix());

        if (DialogIsShown && mDialog.getCildCount() > 0) {
            // Zeichne Transparentes Rec um den Hintergrund abzudunkeln.

            drawDarknessSprite();
            mDialog.renderChilds(batch, prjMatrix);
            batch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (ToastIsShown) {
            mToastOverlay.renderChilds(batch, prjMatrix);
            batch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (MarkerIsShown) {
            mMarkerOverlay.renderChilds(batch, prjMatrix);
            batch.setProjectionMatrix(prjMatrix.Matrix());
        }

        if (GL_View_Base.debug && isTouchDown) {
            Sprite point = Sprites.LogIcons.get(14);
            TouchDownPointer first = touchDownPos.get(0);

            if (first != null) {
                int x = first.point.x;
                int y = this.height - first.point.y;
                int pointSize = 20;

                if (lastTouchX != x || lastTouchY != y) {
                    lastTouchX = x;
                    lastTouchY = y;
                    // Log.debug(log, "TOUCH on x/y" + x + "/" + y);
                }

                batch.draw(point, x - (pointSize / 2), y - (pointSize / 2), pointSize, pointSize);

            }

        }

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        batch.setProjectionMatrix(prjMatrix.Matrix());

        if (Global.isTestVersion()) {
            // TODO float FpsInfoSize = MapTileLoader.queueProcessorLifeCycle ? 4 : 8;
            float FpsInfoSize = 4 * UI_Size_Base.that.getScale();
            if (FpsInfoSprite != null) {
                batch.draw(FpsInfoSprite, FpsInfoPos, 2, FpsInfoSize, FpsInfoSize);
            } else {
                if (Sprites.Stars != null)// SpriteCache is initial
                {
                    FpsInfoSprite = new Sprite(Sprites.getSprite("pixel2x2"));
                    FpsInfoSprite.setColor(1.0f, 1.0f, 0.0f, 1.0f);
                    FpsInfoSprite.setSize(FpsInfoSize, FpsInfoSize);
                }
            }

            FpsInfoPos += UI_Size_Base.that.getScale();
            if (FpsInfoPos > 60 * UI_Size_Base.that.getScale()) {
                FpsInfoPos = 0;
            }

        }

        try {
            batch.end();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Gdx.gl.glFlush();
        Gdx.gl.glFinish();

    }

    @Override
    public void resize(int Width, int Height) {
        width = Width;
        height = Height;

        if (child != null)
            child.setSize(width, height);
        camera = new OrthographicCamera(width, height);
        // cam = new PerspectiveCamera(130f, width, height);
        // cam.position.set(10f, 10f, 10f);
        // cam.lookAt(0, 0, 0);
        // cam.near = 0.1f;
        // cam.far = 600;
        // cam.update();
        prjMatrix = new ParentInfo(new Matrix4().setToOrtho2D(0, 0, width, height), new Vector2(0, 0), new CB_RectF(0, 0, width, height));

    }

    @Override
    public void pause() {
        // wird aufgerufen beim Wechsel der aktiven App und beim Ausschalten des Geräts
        // Log.debug(log, "Pause");

        onStop();
    }

    @Override
    public void resume() {
        // Log.debug(log, "Resume");

        onStart();
    }

    @Override
    public void dispose() {
        disposeTexture();

        Sprites.destroyCache();
        try {
            Translation.writeMisingStringsFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onStart() {
        // App wird wiederhergestellt oder Gerät eingeschaltet
        started.set(true);
        if (listenerInterface != null)
            listenerInterface.RenderDirty();
        // startTimer(FRAME_RATE_ACTION, "GL_Listener onStart()");

        if (ActivityIsShown || DialogIsShown) {
            PlatformConnector.showForDialog();
        } else if (child != null) {
            child.onShow();
        }

        if (ActivityIsShown) {
            if (mActivity != null)
                mActivity.onShow();
        }
        if (DialogIsShown) {
            if (mDialog != null)
                mDialog.onShow();
        }

        renderOnce();

    }

    public void onStop() {
        // App wird verkleinert oder Gerät ausgeschaltet
        // Log.debug(log, "GL_Listener => onStop");
        stopTimer();
        if (listenerInterface != null)
            listenerInterface.RenderContinous();
        child.onStop();
        toast = null; // regenerate toast control
    }

    public int getFpsInfoPos() {
        return FpsInfoPos;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // TouchEreignisse die von der View gesendet werden
    // hier wird entschieden, wann TouchDonw, TouchDragged, TouchUp und Clicked, LongClicked Ereignisse gesendet werden müssen
    public boolean onTouchDownBase(int x, int y, int pointer, int button) {
        resetAmbianeMode();

        isTouchDown = true;
        touchDraggedActive = false;
        touchDraggedCorrect = new Point(0, 0);

        GL_View_Base view = null;

        if (MarkerIsShown)// zuerst Marker Testen
        {
            view = mMarkerOverlay.touchDown(x, (int) mMarkerOverlay.getHeight() - y, pointer, button);
        }

        // do that for round menu on the round menu
        //	// check to open popup menu and close if click outside
        //	if (aktPopUp != null) {
        //	    view = aktPopUp.touchDown(x, height - y, pointer, button);
        //	    if (view == null || view != aktPopUp) {
        //		//outside of popup menu => close and return
        //		aktPopUp.close();
        //		return false;
        //	    }
        //	}

        if (view == null) {
            CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;
            view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);
        }

        if (view == null)
            return false;

        // wenn dieser TouchDown ausserhalb einer TextView war, dann resete den TextField Focus
        if (focusedEditTextField != null) {
            if (!(view instanceof EditTextFieldBase) && !(view instanceof SelectionMarker) && !(view instanceof Button) && !this.PopUpIsShown()) {
                setFocusedEditTextField(null);
            }
        }

        if (touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist aktuell ein kinetisches Pan aktiv -> dieses abbrechen
            StopKinetic(x, y, pointer, false);
        }

        // down Position merken
        touchDownPos.put(pointer, new TouchDownPointer(pointer, new Point(x, y), view));

        // chk if LongClickable
        if (view.isLongClickable()) {
            startLongClickTimer(pointer, x, y);
        } else {
            cancelLongClickTimer();
        }

        renderOnce(FORCE);

        return true;
    }

    public boolean onTouchDraggedBase(int x, int y, int pointer) {

        CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

        if (!touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist kein touchDownPos gespeichert -> dürfte nicht passieren!!!
            return false;
        }

        TouchDownPointer first = touchDownPos.get(pointer);

        try {
            Point akt = new Point(x, y);
            if (touchDraggedActive || (distance(akt, first.point) > first.view.getClickTolerance())) {
                if (pointer != GL_View_Base.MOUSE_WHEEL_POINTER_UP && pointer != GL_View_Base.MOUSE_WHEEL_POINTER_DOWN) {
                    // Nachdem die ClickToleranz überschritten wurde
                    // wird jetzt hier die Verschiebung gemerkt.
                    // Diese wird dann immer von den Positionen abgezogen,
                    // damit der erste Sprung bei der Verschiebung
                    // nachem die Toleranz überschriten wurde
                    // nicht mehr auftritt.
                    if (!touchDraggedActive) {
                        touchDraggedCorrect = new Point(x - first.point.x, y - first.point.y);
                    }
                    x -= touchDraggedCorrect.x;
                    y -= touchDraggedCorrect.y;
                }

                // merken, dass das Dragging aktiviert wurde, bis der Finger wieder losgelassen wird
                touchDraggedActive = true;
                // zu weit verschoben -> Long-Click detection stoppen
                cancelLongClickTimer();
                // touchDragged Event an das View, das den onTouchDown bekommen hat
                boolean behandelt = first.view.touchDragged(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, false);
                if (TOUCH_DEBUG)
                    // Log.debug(log, "GL_Listener => onTouchDraggedBase : " + behandelt);
                    if (!behandelt && first.view.getParent() != null) {
                        // Wenn der Parent eine ScrollBox hat -> Scroll-Events dahin weiterleiten
                        first.view.getParent().touchDragged(x - (int) first.view.getParent().thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.getParent().thisWorldRec.getY(), pointer, false);
                    }
                if (touchDownPos.size() == 1) {
                    if (first.kineticPan == null)
                        first.kineticPan = new KineticPan();
                    first.kineticPan.setLast(System.currentTimeMillis(), x, y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean onTouchUpBase(int x, int y, int pointer, int button) {
        isTouchDown = false;
        cancelLongClickTimer();

        CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

        if (!touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist kein touchDownPos gespeichert -> dürfte nicht passieren!!!
            return false;
        }

        TouchDownPointer first = touchDownPos.get(pointer);

        try {
            Point akt = new Point(x, y);
            if (distance(akt, first.point) < first.view.getClickTolerance()) {
                // Finger wurde losgelassen ohne viel Bewegung
                if (first.view.isClickable()) {
                    // Testen, ob dies ein Doppelklick ist
                    if (first.view.isDoubleClickable() && (System.currentTimeMillis() < lastClickTime + mDoubleClickTime) && (lastClickPoint != null) && (distance(akt, lastClickPoint) < first.view.getClickTolerance())) {
                        boolean handled = first.view.doubleClick(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, button);
                        if (handled)
                            PlatformConnector.vibrate();

                        lastClickTime = 0;
                        lastClickPoint = null;
                    } else {
                        // normaler Click
                        boolean handled = first.view.click(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, button);
                        if (handled)
                            PlatformConnector.vibrate();

                        lastClickTime = System.currentTimeMillis();
                        lastClickPoint = akt;
                    }
                } else {
                    // onTouchUpBase: view is not clickable.
                }
            } else {
                x -= touchDraggedCorrect.x;
                y -= touchDraggedCorrect.y;
            }
        } catch (Exception e) {
        }

        try {
            if (first.kineticPan != null) {
                first.kineticPan.start();
                first.startKinetic(this, x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY());
            } else {
                // onTouchUp immer auslösen
                first.view.touchUp(x, (int) testingView.getHeight() - y, pointer, button);
                touchDownPos.remove(pointer);
            }
        } catch (Exception e) {
        }

        return true;
    }

    public GL_View_Base onTouchDown(int x, int y, int pointer, int button) {
        GL_View_Base view = null;

        CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

        view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);

        return view;
    }

    public boolean onTouchDragged(int x, int y, int pointer) {
        boolean behandelt = false;

        CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

        behandelt = testingView.touchDragged(x, (int) testingView.getHeight() - y, pointer, false);

        return behandelt;
    }

    public boolean onTouchUp(int x, int y, int pointer, int button) {
        boolean behandelt = false;

        CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

        behandelt = testingView.touchUp(x, (int) testingView.getHeight() - y, pointer, button);

        return behandelt;
    }

    public void registerRenderStartetListener(RenderStarted listener) {
        renderStartedListener = listener;

        // wenn kein Render Auftrag kommt, wird auch der waitDialog nicht ausgeblendet!
        addRenderView(child, FRAME_RATE_FAST_ACTION);
    }

    private void disposeTexture() {
        if (mDarknesPixmap != null)
            mDarknesPixmap.dispose();
        if (mDarknesTexture != null)
            mDarknesTexture.dispose();
        mDarknesPixmap = null;
        mDarknesTexture = null;
        mDarknesSprite = null;
    }

    protected void drawDarknessSprite() {
        if (batch == null)
            return;
        if (mDarknesSprite == null) {
            disposeTexture();
            mDarknesPixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
            mDarknesPixmap.setColor(COLOR.getDarknesColor());
            mDarknesPixmap.fillRectangle(0, 0, width, height);
            mDarknesTexture = new Texture(mDarknesPixmap, Pixmap.Format.RGBA8888, false);
            mDarknesSprite = new Sprite(mDarknesTexture, width, height);
        }

        if (mDarknesSprite != null)
            mDarknesSprite.draw(batch, darknessAlpha);
        if (darknessAnimationRuns) {
            darknessAlpha += 0.1f;
            if (darknessAlpha > 1f) {
                darknessAlpha = 1f;
                darknessAnimationRuns = false;
            }
            renderOnce();
        }

    }

    public void Initialize() {
        // Log.debug(log, "GL_Listener => Initialize");

        if (Gdx.graphics.getGL20() == null)
            return;// kann nicht initialisiert werden

        if (batch == null) {
            batch = new PolygonSpriteBatch(10920);// PolygonSpriteBatch(10920);
        }

        if (modelBatch == null) {
            try {
                modelBatch = new ModelBatch();
            } catch (java.lang.NoSuchFieldError e) {
                e.printStackTrace();
            }
        }

        if (child == null) {
            child = mSplash;
            child.setClickable(true);
            child.setLongClickable(true);
        }

        if (mDialog == null) {
            mDialog = new MainViewBase(0, 0, width, height, "Dialog");
            mDialog.setClickable(true);
            mDialog.setLongClickable(true);
        }

        if (mActivity == null) {
            mActivity = new MainViewBase(0, 0, width, height, "Dialog");
            mActivity.setClickable(true);
            mActivity.setLongClickable(true);
        }

        //initial GrayScale shader
        shader = new GrayscalShaderProgram();
        setShader(shader);

        setGrayscale(0.5f);

    }

    public void setGrayscale(float value) {
        if (shader != null) {
            shader.begin();
            shader.setUniformf("grayscale", value);
            shader.end();
        }
    }

    public CB_View_Base getDialogLayer() {
        return mDialog;
    }

    protected void initialMarkerOverlay() {
        mMarkerOverlay = new Box(new CB_RectF(0, 0, width, height), "MarkerOverlay");
        selectionMarkerCenter = new SelectionMarker(SelectionMarker.Type.Center);
        selectionMarkerLeft = new SelectionMarker(SelectionMarker.Type.Left);
        selectionMarkerRight = new SelectionMarker(SelectionMarker.Type.Right);

        hideMarker();

        mMarkerOverlay.addChild(selectionMarkerCenter);
        mMarkerOverlay.addChild(selectionMarkerLeft);
        mMarkerOverlay.addChild(selectionMarkerRight);

    }

    public void setGLViewID(ViewID id) {
        if (child == null)
            Initialize();
        child.setGLViewID(id);
    }

    public void addRenderView(GL_View_Base view, int delay) {
        synchronized (renderViews) {
            if (!view.isVisible()) {
                if (renderViews.containsKey(view)) {
                    renderViews.remove(view);
                    calcNewRenderSpeed();
                    if (listenerInterface != null)
                        listenerInterface.RequestRender();
                }
                return;
            }
            if (renderViews.containsKey(view)) {
                renderViews.remove(view);
            }
            renderViews.put(view, delay);
            calcNewRenderSpeed();
            if (listenerInterface != null)
                listenerInterface.RequestRender();
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

    /**
     * Führt EINEN Render Durchgang aus
     */
    public void renderOnce(boolean force) {
        requestRender(force);
    }

    /**
     * Führt EINEN Render Durchgang aus
     */
    private void requestRender(boolean force) {

        if (!force && lastRenderOnceTime == this.getStateTime())
            return;

        String name = Trace.getCallerName(1);
        if (caller.containsKey(name)) {
            float last = stateTime - caller.get(name);
            caller.put(name, stateTime);
            if (last < 0.008) {
                int lastCount = callerCount.get(name) + 1;
                if (lastCount > 50) {
                    lastCount = 0;
                    // Log.err(log, "to many calls from: " + name);
                }
                callerCount.put(name, lastCount);
            }
        } else {
            callerCount.put(name, 0);
            caller.put(name, stateTime);
        }

        lastRenderOnceTime = this.getStateTime();

        if (listenerInterface != null)
            listenerInterface.RequestRender();
    }

    /**
     * Führt EINEN Render Durchgang aus
     */
    public void renderOnce() {
        requestRender(false);
    }

    private void calcNewRenderSpeed() {
        synchronized (renderViews) {
            int minDelay = 0;
            Iterator<Integer> it = renderViews.values().iterator();
            while (it.hasNext()) {
                int delay = it.next();
                if (delay > minDelay)
                    minDelay = delay;
            }
            if (minDelay == 0)
                stopTimer();
            else
                startTimer(minDelay, "GL_Listener calcNewRenderSpeed()");
        }

    }

    private void startLongClickTimer(final int pointer, final int x, final int y) {
        cancelLongClickTimer();

        longClickTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!touchDownPos.containsKey(pointer))
                    return;
                // für diesen Pointer ist kein touchDownPos gespeichert ->
                // dürfte nicht passieren!!!
                TouchDownPointer first = touchDownPos.get(pointer);
                Point akt = new Point(x, y);
                if (distance(akt, first.point) < first.view.getClickTolerance()) {
                    if (first.view.isLongClickable()) {
                        boolean handled = first.view.longClick(x - (int) first.view.thisWorldRec.getX(), (int) child.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, 0);
                        // Log.debug(log, "GL_Listener => onLongClick : " + first.view.getName());
                        // für diesen TouchDownn darf kein normaler Click mehr ausgeführt werden
                        touchDownPos.remove(pointer);
                        // onTouchUp nach Long-Click direkt auslösen
                        first.view.touchUp(x, (int) child.getHeight() - y, pointer, 0);
                        // Log.debug(log, "GL_Listener => onTouchUpBase : " + first.view.getName());
                        if (handled)
                            PlatformConnector.vibrate();
                    }
                }
            }
        };
        longClickTimer.schedule(task, mLongClickTime);
    }

    private void cancelLongClickTimer() {
        if (longClickTimer != null) {
            longClickTimer.cancel();
            longClickTimer = null;
        }
    }

    public void StopKinetic(int x, int y, int pointer, boolean forceTouchUp) {
        TouchDownPointer first = touchDownPos.get(pointer);
        if (first != null) {
            first.stopKinetic();
            first.kineticPan = null;
            if (forceTouchUp)
                first.view.touchUp(x, y, pointer, 0);
        }
    }

    // Abstand zweier Punkte
    private int distance(Point p1, Point p2) {
        return (int) Math.round(Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)));
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.round(Math.sqrt(Math.pow(x1 - x1, 2) + Math.pow(y1 - y2, 2)));
    }

    public CB_View_Base getActDialog() {
        return actDialog;
    }

    public void showPopUp(PopUp_Base popUp, float x, float y) {
        popUp.setX(x);
        popUp.setY(y);

        CB_View_Base aktView = DialogIsShown ? mDialog : child;
        if (ActivityIsShown && !DialogIsShown)
            aktView = mActivity;

        aktView.addChild(popUp);
        aktPopUp = popUp;
        aktPopUp.onShow();
        renderOnce();
    }

    public void closePopUp(PopUp_Base popUp) {
        CB_View_Base aktView = DialogIsShown ? mDialog : child;
        if (ActivityIsShown)
            aktView = mActivity;

        aktView.removeChild(popUp);
        if (aktPopUp != null)
            aktPopUp.onHide();
        aktPopUp = null;
        if (popUp != null)
            popUp.dispose();
        renderOnce();
    }

    public boolean PopUpIsShown() {
        return (aktPopUp != null);
    }

    public void showDialog(final Dialog dialog) {
        if (dialog instanceof ActivityBase)
            throw new IllegalArgumentException("don't show an Activity as Dialog. Use \"GL_listener.showActivity()\"");

        showDialog(dialog, false);
    }

    public void showDialog(final Dialog dialog, boolean atTop) {

        setFocusedEditTextField(null);

        if (dialog instanceof ActivityBase)
            throw new IllegalArgumentException("don't show an Activity as Dialog. Use \"GL_listener.showActivity()\"");

        clearRenderViews();

        if (dialog.isDisposed())
            return;

        // Center Menu on Screen
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

        if (actDialog != null && actDialog != dialog) {
            actDialog.onHide();
            actDialog.setEnabled(false);
            // am Anfang der Liste einfügen
            dialogHistory.add(0, actDialog);
            mDialog.removeChildsDirekt(actDialog);
        }

        actDialog = dialog;

        mDialog.addChildDirekt(dialog);
        mDialog.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // Sollte bei einem Click neben dem Dialog ausgelöst werden.
                // Dann soll der Dialog geschlossen werden, wenn es sich um ein Menü handelt.
                if (DialogIsShown) {
                    GL_View_Base vDialog = mDialog.getChild(0);
                    if (vDialog instanceof Menu)
                        closeDialog(actDialog);
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
            }
        });

        child.setClickable(false);
        DialogIsShown = true;
        darknessAnimationRuns = true;
        actDialog.onShow();
        try {
            actDialog.setEnabled(true);
            actDialog.setVisible();
        } catch (Exception e) {

        }
        PlatformConnector.showForDialog();

        renderOnce();

    }

    public void showActivity(final ActivityBase activity) {
        setFocusedEditTextField(null);
        clearRenderViews();
        PlatformConnector.showForDialog();

        if (aktPopUp != null) {
            closePopUp(aktPopUp);
        }

        darknessAnimationRuns = true;

        // Center activity on Screen
        float x = (width - activity.getWidth()) / 2;
        float y = (height - activity.getHeight()) / 2;

        activity.setPos(x, y);

        if (actDialog != null) {
            actDialog.onHide();
        }

        if (actActivity != null && actActivity != activity) {
            actActivity.onHide();
            actActivity.setEnabled(false);
            // am Anfang der Liste einfügen
            activityHistory.add(0, actActivity);
            mActivity.removeChildsDirekt(actActivity);
        }

        actActivity = activity;

        mActivity.addChildDirekt(activity);

        child.setClickable(false);
        ActivityIsShown = true;
        child.onHide();
        actActivity.onShow();

        PlatformConnector.showForDialog();
    }

    public void closeActivity() {
        closeActivity(true);
    }

    public void closeActivity(boolean MsgToPlatformConector) {
        if (!ActivityIsShown)
            return;

        //check if KeyboardFocus on this Activitiy
        if (focusedEditTextField != null && focusedEditTextField.getParent() == actActivity) {
            setFocusedEditTextField(null);
        }

        if (activityHistory.size() > 0) {
            mActivity.removeChild(actActivity);
            actActivity.onHide();
            // letzten Dialog wiederherstellen
            actActivity = activityHistory.get(0);
            actActivity.onShow();
            actActivity.setEnabled(true);
            activityHistory.remove(0);
            ActivityIsShown = true;
            mActivity.addChildDirekt(actActivity);
            if (MsgToPlatformConector)
                PlatformConnector.showForDialog();
        } else {
            actActivity.onHide();

            disposeAcktivitie = actActivity;

            Timer disposeTimer = new Timer();
            TimerTask disposeTsak = new TimerTask() {
                @Override
                public void run() {
                    if (disposeAcktivitie != null)
                        disposeAcktivitie.dispose();
                    disposeAcktivitie = null;
                    System.gc();
                }
            };

            disposeTimer.schedule(disposeTsak, 700);

            actActivity = null;
            mActivity.removeChildsDirekt();
            child.setClickable(true);
            // child.invalidate();
            ActivityIsShown = false;
            darknessAlpha = 0f;
            if (MsgToPlatformConector)
                PlatformConnector.hideForDialog();
            child.onShow();
        }

        clearRenderViews();
        renderOnce();
    }

    public void closeAllDialogs() {
        for (Dialog view : dialogHistory) {
            view.onHide();
        }

        dialogHistory.clear();
        if (actDialog != null)
            closeDialog(actDialog);

        for (CB_View_Base view : activityHistory) {
            view.onHide();
        }

        activityHistory.clear();
        if (actActivity != null)
            closeActivity(true);

        ActivityIsShown = false;
        DialogIsShown = false;
    }

    public void closeDialog(CB_View_Base dialog) {
        if (dialog instanceof ActivityBase)
            throw new IllegalArgumentException("don't show an Activity as Dialog. Use \"GL_listener.showActivity()\"");
        closeDialog(dialog, true);
    }

    public void closeDialog(final CB_View_Base dialog, boolean MsgToPlatformConector) {

        if (!DialogIsShown || !mDialog.getchilds().contains((dialog))) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (dialog == null || dialog.isDisposed())
                        return;
                    if (dialog.equals(mDialog))
                        throw new IllegalStateException("mDialog can't disposed");
                    if (dialog != null)
                        dialog.dispose();
                }
            };
            timer.schedule(task, 50);
        }

        if (MsgToPlatformConector)
            PlatformConnector.hideForDialog();
        if (actDialog != null) {
            //check if KeyboardFocus on this Dialog
            if (focusedEditTextField != null && focusedEditTextField.getParent() == actDialog) {
                setFocusedEditTextField(null);
            }

            actDialog.onHide();
        }

        if (dialogHistory.size() > 0) {
            mDialog.removeChild(actDialog);
            // letzten Dialog wiederherstellen
            actDialog = dialogHistory.get(0);
            // actDialog.onShow();
            // actDialog.setEnabled(true);
            dialogHistory.remove(0);
            // DialogIsShown = true;
            // platformConector.showForDialog();
            showDialog(actDialog);
        } else {
            actDialog = null;
            mDialog.removeChildsDirekt();
            child.setClickable(true);
            // child.invalidate();
            DialogIsShown = false;
            darknessAlpha = 0f;
        }

        if (dialog != null) {
            if (!dialog.isDisposed()) {
                dialog.dispose();
            }
        }

        clearRenderViews();
        if (ActivityIsShown) {
            PlatformConnector.showForDialog();

        }
        renderOnce();
    }

    public void Toast(CB_View_Base view) {
        Toast(view, 4000);
    }

    public void closeToast() {
        if (mToastOverlay != null) {

            ToastIsShown = false;
            mToastOverlay.removeChilds();

            renderOnce();
        }
    }

    public void Toast(CB_View_Base view, int delay) {
        if (mToastOverlay == null) {
            mToastOverlay = new Box(new CB_RectF(0, 0, width, height), "ToastView");
        }
        synchronized (mToastOverlay) {
            mToastOverlay.removeChilds();

            mToastOverlay.addChild(view);
            ToastIsShown = true;

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    ToastIsShown = false;
                    renderOnce();
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, delay);
        }
    }

    public void Toast(String string) {
        Toast(string, 4000);
    }

    public void Toast(String string, int length) {
        if (toast == null) {
            toast = new CB_UI_Base.GL_UI.Controls.Dialogs.Toast(new CB_RectF(0, 0, 100, GL_UISizes.BottomButtonHeight / 1.5f), "StringToast");
        }
        toast.setWrappedText(string);

        GlyphLayout bounds = Fonts.MeasureWrapped(string, UiSizes.that.getWindowWidth());

        // float measuredWidth = Fonts.Measure(string).width + (toast.getLeftWidth() * 2) + (UI_Size_Base.that.getMargin() * 2);
        float border = +(toast.getLeftWidth() * 2) + (UI_Size_Base.that.getMargin() * 2);
        toast.setWidth(bounds.width + border);
        toast.setHeight(bounds.height + border);

        toast.setPos((width / 2) - (bounds.width / 2), GL_UISizes.BottomButtonHeight * 1.3f);

        Toast(toast, length);
    }

    /**
     * Stopt den Rendervorgang bis er durch RestartRender() wieder gestartet wird
     */
    public void StopRender() {
        stopRender = true;
    }

    /**
     * Startet den Renderer wenn er durch StopRender() gestoppt wurde
     */
    public void RestartRender() {

        // Log.debug(log, "restart render" + Trace.getCallerName());

        listenerInterface.RenderContinous();
        stopRender = false;
        renderOnce();
        setIsInitial();
    }

    public void clearRenderViews() {
        synchronized (renderViews) {
            stopTimer();
            renderViews.clear();
        }
    }

    /**
     * @return true wenn behandeld
     */
    public boolean keyBackClicked() {
        if (actDialog instanceof Menu) {
            closeDialog(actDialog);
            return true;
        }
        return false;
    }

    public EditTextField getFocusedEditTextField() {
        return focusedEditTextField;
    }

    public void setFocusedEditTextField(EditTextField editTextField) {
        if (editTextField == null && focusedEditTextField == null) {
            return;
        }

        // Don't open KeyBoard if Keybord is Showing
        boolean dontOpenKeybord = focusedEditTextField != null;

        if (editTextField != null && editTextField.isKeyboardPopupDisabled()) {
            dontOpenKeybord = true;
        }

        // inform the parent, perhaps to move the editTextField to the top of the screen
        KeyboardFocusChangedEventList.Call(editTextField);

        if (editTextField != null && editTextField != focusedEditTextField) {
            editTextField.becomesFocus();
        }

        focusedEditTextField = editTextField;

        hideMarker();

        if (focusedEditTextField != null) {
            if (!focusedEditTextField.isKeyboardPopupDisabled()) {
                if (!dontOpenKeybord) {
                    PlatformConnector.showVirtualKeyboard();
                }
            }
        } else {
            if (dontOpenKeybord) {
                PlatformConnector.hideVirtualKeyboard();
            }

        }
    }

    public boolean hasFocus(EditTextFieldBase view) {
        return view == focusedEditTextField;
    }

    public void hideMarker() {
        if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null)
            initialMarkerOverlay();
        selectionMarkerCenter.setInvisible();
        selectionMarkerLeft.setInvisible();
        selectionMarkerRight.setInvisible();

        MarkerIsShown = false;
    }

    public void showMarker(Type type) {
        if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null)
            initialMarkerOverlay();

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

    public boolean closeShownDialog() {
        if (DialogIsShown) {
            closeDialog(actDialog);
            return true;
        }

        if (ActivityIsShown) {
            closeActivity();
            return true;
        }

        return false;
    }

    public boolean isShownDialogActivity() {
        if (DialogIsShown) {
            return true;
        }

        if (ActivityIsShown) {

            return true;
        }
        return false;
    }

    public void switchToMainView() {
        MainViewBase altSplash = child;
        child = mMainView;
        altSplash.dispose();
        altSplash = null;
        mSplash.dispose();
        mSplash = null;
        initialMarkerOverlay();
        mMainView.onShow();
        if (listenerInterface != null)
            listenerInterface.RenderDirty();
    }

    /**
     * touchDown
     */
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return this.onTouchDownBase(x, y, pointer, button);

    }

    // ##########################################
    // Imput Listener
    // ##########################################

    /**
     * touchDragged
     */
    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return onTouchDraggedBase(x, y, pointer);
    }

    /**
     * mouseMoved
     */
    @Override
    public boolean mouseMoved(int x, int y) {
        MouseX = x;
        MouseY = y;
        return onTouchDraggedBase(x, y, -1);
    }

    /**
     * touchUp
     */
    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        boolean ret = onTouchUpBase(x, y, pointer, button);
        return ret;
    }

    @Override
    public boolean keyTyped(char character) {
        if (DialogIsShown && character == KeyCodes.KEYCODE_BACK) {
            if (DialogIsShown)
                closeDialog(actDialog);
            return true; // behandelt!
        }

        if (ActivityIsShown && character == KeyCodes.KEYCODE_BACK) {
            // chek closeable

            if (actActivity instanceof ActivityBase) {
                if (!((ActivityBase) actActivity).canCloseWithBackKey())
                    return true;
            }

            closeActivity();
            return true; // behandelt!
        }

        if (Character.getType(character) == 15) {
            //check if coursor up/down/left/rigt clicked
            // Log.debug(log, "value:" + Character.getNumericValue(character));
            if (Character.getNumericValue(character) == -1) {
                if (!(character == EditTextField.BACKSPACE || character == EditTextField.DELETE || character == EditTextField.ENTER_ANDROID || character == EditTextField.ENTER_DESKTOP || character == EditTextField.TAB)) {
                    return true;
                }
            }

        }
        if (focusedEditTextField != null) {
            focusedEditTextField.keyTyped(character);
            return true;
        }
        return false;

    }

    @Override
    public boolean keyUp(int value) {
        if (value == Input.Keys.BACK) {
            if (isShownDialogActivity()) {

                if (DialogIsShown) {
                    closeDialog(actDialog);
                    return true; // behandelt!
                }

                if (ActivityIsShown) {
                    closeActivity();
                    return true; // behandelt!
                }

                closeShownDialog();
            } else {
                MainViewBase.actionClose.Execute();
            }
            return true;
        }

        if (focusedEditTextField != null) {
            focusedEditTextField.keyUp(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int value) {
        if (focusedEditTextField != null) {
            focusedEditTextField.keyDown(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {

        int scrollSize = (UiSizes.that.getClickToleranz() + 10) * amount;

        int Pointer = (scrollSize > 0) ? GL_View_Base.MOUSE_WHEEL_POINTER_UP : GL_View_Base.MOUSE_WHEEL_POINTER_DOWN;

        this.onTouchDownBase(MouseX, MouseY, Pointer, -1);

        this.onTouchDraggedBase(MouseX - scrollSize, MouseY - scrollSize, Pointer);

        this.onTouchUpBase(MouseX - scrollSize, MouseY - scrollSize, Pointer, -1);

        return true;
    }

    public interface RenderStarted {
        public void renderIsStartet();
    }

    public class TouchDownPointer {
        private final int pointer;
        private final GL_View_Base view;
        public Point point;
        private KineticPan kineticPan;
        private Timer timer;

        public TouchDownPointer(int pointer, Point point, GL_View_Base view) {
            this.pointer = pointer;
            this.point = point;
            this.view = view;
            this.kineticPan = null;
        }

        public void startKinetic(final GL listener, final int x, final int y) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (kineticPan != null) {
                        Point pan = kineticPan.getAktPan();
                        try {
                            if (kineticPan.fertig) {
                                // Log.debug(log, "KineticPan fertig");
                                view.touchUp(x - pan.x, y - pan.y, pointer, 0);
                                touchDownPos.remove(pointer);
                                kineticPan = null;
                                this.cancel();
                                timer = null;
                            }
                        } catch (Exception e) {
                            touchDownPos.remove(pointer);
                            kineticPan = null;
                            this.cancel();
                            timer = null;
                        }
                        view.touchDragged(x - pan.x, y - pan.y, pointer, true);
                    }
                }
            }, 0, FRAME_RATE_FAST_ACTION);
        }

        public void stopKinetic() {
            if (timer != null) {
                timer.cancel();
                timer = null;
                kineticPan = null;
            }
        }
    }

    protected class KineticPan {
        // benutze den Abstand der letzten 5 Positionsänderungen
        final int anzPoints = 6;
        private final int[] x = new int[anzPoints];
        private final int[] y = new int[anzPoints];
        private final long[] ts = new long[anzPoints];
        int anzPointsUsed = 0;
        private boolean started;
        private boolean fertig;
        private int diffX;
        private int diffY;
        private long diffTs;
        private long startTs;
        private long endTs;
        private int lastX = 0;
        private int lastY = 0;

        public KineticPan() {
            fertig = false;
            started = false;
            diffX = 0;
            diffY = 0;
            for (int i = 0; i < anzPoints; i++) {
                x[i] = 0;
                y[i] = 0;
                ts[i] = 0;
            }
            anzPointsUsed = 0;
        }

        public void setLast(long aktTs, int aktX, int aktY) {
            if ((anzPointsUsed > 0) && (ts[0] < aktTs - 500)) {
                // wenn seit der letzten Verschiebung mehr Zeit Vergangen ist -> bisherige gemerkte Verschiebungen löschen
                anzPointsUsed = 0;
                started = false;
                return;
            }

            anzPointsUsed++;
            if (TOUCH_DEBUG)
                // Log.debug(log, "AnzUsedPoints: " + anzPointsUsed);
                if (anzPointsUsed > anzPoints)
                    anzPointsUsed = anzPoints;
            for (int i = anzPoints - 2; i >= 0; i--) {
                x[i + 1] = x[i];
                y[i + 1] = y[i];
                ts[i + 1] = ts[i];
            }
            x[0] = aktX;
            y[0] = aktY;
            ts[0] = aktTs;

            for (int i = 1; i < anzPoints; i++) {
                if (x[i] == 0)
                    x[i] = x[i - 1];
                if (y[i] == 0)
                    y[i] = y[i - 1];
                if (ts[i] == 0)
                    ts[i] = ts[i - 1];
            }
            diffX = x[anzPointsUsed - 1] - aktX;
            diffY = aktY - y[anzPointsUsed - 1];
            diffTs = aktTs - ts[anzPointsUsed - 1];

            if (diffTs > 0) {
                diffX = (int) ((float) diffX / FRAME_RATE_ACTION * diffTs);
                diffY = (int) ((float) diffY / FRAME_RATE_ACTION * diffTs);
            }
            // if (TOUCH_DEBUG)
            // Log.debug(log, "diffx = " + diffX + " - diffy = " + diffY);

            // debugString = x[2] + " - " + x[1] + " - " + x[0];
        }

        public boolean getFertig() {
            return fertig;
        }

        public boolean getStarted() {
            return started;
        }

        public void start() {
            anzPointsUsed = Math.max(anzPointsUsed, 1);
            if (ts[0] < System.currentTimeMillis() - 200) {
                // kinematisches Scrollen nur, wenn seit der letzten Verschiebung kaum Zeit vergangen ist
                fertig = true;
                return;
            }
            startTs = System.currentTimeMillis();
            int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

            endTs = startTs + 1000 + abstand * 15 / anzPointsUsed;
            // if (endTs > startTs + 6000) endTs = startTs + 6000; // max. Zeit festlegen
            if (TOUCH_DEBUG)
                // Log.debug(log, "endTs - startTs: " + String.valueOf(endTs - startTs));
                // endTs = startTs + 5000;
                started = true;
        }

        public Point getAktPan() {
            anzPointsUsed = Math.max(anzPointsUsed, 1);
            Point result = new Point(0, 0);

            long aktTs = System.currentTimeMillis();
            float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
            // Log.debug(log, "Faktor: " + faktor);
            faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
            // faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
            // Log.debug(log, "Faktor2: " + faktor);
            if (faktor >= 1) {
                fertig = true;
                faktor = 1;
            }

            result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
            result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;

            if ((result.x == lastX) && (result.y == lastY)) {
                // wenn keine Nennenswerten Änderungen mehr gemacht werden dann einfach auf fertig schalten
                fertig = true;
                faktor = 1;
                result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
                result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;
            }
            double abstand = distance(lastX, lastY, result.x, result.y);
            if (abstand > MAX_KINETIC_SCROLL_DISTANCE) {
                double fkt = MAX_KINETIC_SCROLL_DISTANCE / abstand;
                result.x = (int) ((result.x - lastX) * fkt + lastX);
                result.y = (int) ((result.y - lastY) * fkt + lastY);
            }

            lastX = result.x;
            lastY = result.y;
            return result;
        }
    }

}

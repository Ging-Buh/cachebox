package CB_Core.GL_UI.GL_Listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.Config;
import CB_Core.Energy;
import CB_Core.GlobalCore;
import CB_Core.GlobalLocationReceiver;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.SelectionMarker;
import CB_Core.GL_UI.Controls.SelectionMarker.Type;
import CB_Core.GL_UI.Controls.PopUps.PopUp_Base;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.Log.Logger;
import CB_Core.Map.MapTileLoader;
import CB_Core.Map.Point;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Settings.SettingBase.iChanged;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GL implements ApplicationListener
{
	// Public Static Constants
	public static final int MAX_KINETIC_SCROLL_DISTANCE = 100;
	public static final int FRAME_RATE_IDLE = 200;
	public static final int FRAME_RATE_ACTION = 50;
	public static final int FRAME_RATE_FAST_ACTION = 40;

	/**
	 * See http://code.google.com/p/libgdx/wiki/SpriteBatch Performance tuning
	 */
	protected final int SPRITE_BATCH_BUFFER = 150;

	// Public Static Member
	public static GL_Listener_Interface listenerInterface;
	public static GL that;
	public static SpriteBatch batch;
	public static OrthographicCamera camera;
	private static Timer myTimer;
	private static long timerValue;

	// Private Static Member
	public static AtomicBoolean started = new AtomicBoolean(false);
	public static boolean misTouchDown = false;

	// private Member
	private boolean touchDraggedActive = false;
	private Point touchDraggedCorrect = new Point(0, 0);
	protected boolean ToastIsShown = false;
	protected boolean stopRender = false;
	private boolean darknesAnimationRuns = false;
	protected boolean MarkerIsShown = false;
	protected int FpsInfoPos = 0;
	private float darknesAlpha = 0f;
	private long mLongClickTime = 0, mDoubleClickTime = 500, lastClickTime = 0;

	// private Threads
	Thread threadDisposeDialog;

	/**
	 * Static for Debug
	 */
	protected static HashMap<GL_View_Base, Integer> renderViews = new HashMap<GL_View_Base, Integer>();
	private Point lastClickPoint = null;
	protected ParentInfo prjMatrix;
	private CB_View_Base actActivity;
	public Dialog actDialog;
	private ArrayList<Dialog> dialogHistory = new ArrayList<Dialog>();
	private PopUp_Base aktPopUp = null;
	private CB_Core.GL_UI.Controls.Dialogs.Toast toast;
	private Stage mStage;
	private Timer longClickTimer;
	protected Sprite FpsInfoSprite;
	private Sprite mDarknesSprite;
	private Pixmap mDarknesPixmap;
	private Texture mDarknesTexture;
	protected EditWrapedTextField keyboardFocus;

	protected ArrayList<runOnGL> runOnGL_List = new ArrayList<runOnGL>();
	public static ArrayList<runOnGL> runIfInitial = new ArrayList<runOnGL>();

	public static boolean ifAllInitial = false;

	public static void setIsInitial()
	{
		ifAllInitial = true;
	}

	public static void resetIsInitial()
	{
		ifAllInitial = false;
	}

	public static boolean isInitial()
	{
		return ifAllInitial;
	}

	/**
	 * Zwischenspeicher für die touchDown Positionen der einzelnen Finger
	 */
	protected SortedMap<Integer, TouchDownPointer> touchDownPos = Collections
			.synchronizedSortedMap((new TreeMap<Integer, TouchDownPointer>()));

	// private Listner
	protected renderStartet renderStartetListner = null;

	// Protected Member
	protected MainViewBase child;
	protected CB_View_Base mDialog, mActivity, mToastOverlay, mMarkerOverlay;
	protected SelectionMarker selectionMarkerCenter, selectionMarkerLeft, selectionMarkerRight;
	protected boolean DialogIsShown = false, ActivityIsShown = false;
	protected int width = 0, height = 0;
	protected boolean debugWriteSpriteCount = false;

	/**
	 * Constructor
	 */
	public GL(int initalWidth, int initialHeight)
	{
		that = this;
		width = initalWidth;
		height = initialHeight;
		if (Config.settings == null || Config.settings.LongClicktime == null)
		{
			mLongClickTime = 600;
		}
		else
		{
			mLongClickTime = Config.settings.LongClicktime.getValue();
		}
	}

	// Overrides

	@Override
	public void create()
	{
		// Logger.LogCat("GL_Listner => Create");

		GL_UISizes.initial(width, height);

		Initialize();
		Config.settings.nightMode.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				mDarknesSprite = null;// for new creation with changed color
			}
		});
		GlobalCore.receiver = new GlobalLocationReceiver();
		debugWriteSpriteCount = Config.settings.DebugSpriteBatchCountBuffer.getValue();
	}

	public void RunOnGL(runOnGL run)
	{
		synchronized (runOnGL_List)
		{
			runOnGL_List.add(run);
		}

		renderOnce("RunOnGL called");
	}

	public void RunIfInitial(runOnGL run)
	{
		synchronized (runIfInitial)
		{
			runIfInitial.add(run);
		}

		renderOnce("runIfInitial called");
	}

	protected boolean ShaderSetted = false;

	protected void setShader()
	{
		if (Gdx.graphics.isGL20Available()) batch.setShader(SpriteBatch.createDefaultShader());
		ShaderSetted = true;
	}

	protected float stateTime = 0;

	public float getStateTime()
	{
		return stateTime;
	}

	@Override
	public void render()
	{

		if (Energy.DisplayOff()) return;

		if (!started.get() || stopRender) return;

		stateTime += Gdx.graphics.getDeltaTime();

		lastRenderBegin = System.currentTimeMillis();

		if (renderStartetListner != null)
		{
			renderStartetListner.renderIsStartet();
			renderStartetListner = null;
			removeRenderView(child);
		}

		if (!ShaderSetted) setShader();

		synchronized (runOnGL_List)
		{
			if (runOnGL_List.size() > 0)
			{
				for (runOnGL run : runOnGL_List)
				{
					if (run != null) run.run();
				}

				runOnGL_List.clear();
			}
		}

		if (ifAllInitial)
		{
			synchronized (runIfInitial)
			{
				if (runIfInitial.size() > 0)
				{
					for (runOnGL run : runIfInitial)
					{
						if (run != null) run.run();
					}

					runIfInitial.clear();
				}
			}
		}

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Config.settings.nightMode.getValue())
		{
			Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		}
		else
		{
			Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		}

		try
		{
			batch.begin();
		}
		catch (java.lang.IllegalStateException e)
		{
			batch.flush();
			batch.end();
			batch.begin();
		}
		batch.setProjectionMatrix(prjMatrix.Matrix());

		// if Tablet, so the Activity is smaller the screen size
		// render childs and darkness Sprite
		if (GlobalCore.isTab)
		{
			child.renderChilds(batch, prjMatrix);
			if (ActivityIsShown && mActivity.getCildCount() > 0)
			{
				// Zeichne Transparentes Rec um den Hintergrund abzudunkeln.
				drawDarknessSprite();
				mActivity.renderChilds(batch, prjMatrix);
			}
		}

		if (ActivityIsShown && !GlobalCore.isTab)
		{
			drawDarknessSprite();
			mActivity.renderChilds(batch, prjMatrix);
		}

		if (!ActivityIsShown)
		{
			child.renderChilds(batch, prjMatrix);
		}

		if (DialogIsShown && mDialog.getCildCount() > 0)
		{
			// Zeichne Transparentes Rec um den Hintergrund abzudunkeln.
			drawDarknessSprite();
			mDialog.renderChilds(batch, prjMatrix);
		}

		if (ToastIsShown)
		{
			mToastOverlay.renderChilds(batch, prjMatrix);
		}

		if (MarkerIsShown)
		{
			mMarkerOverlay.renderChilds(batch, prjMatrix);
		}

		GL_View_Base.debug = Config.settings.DebugMode.getValue();

		if (GL_View_Base.debug && misTouchDown)
		{
			Sprite point = SpriteCache.LogIcons.get(14);
			TouchDownPointer first = touchDownPos.get(0);

			if (first != null)
			{
				int x = first.point.x;
				int y = this.height - first.point.y;
				int pointSize = 20;

				batch.draw(point, x - (pointSize / 2), y - (pointSize / 2), pointSize, pointSize);

			}

		}

		if (Config.settings.StagingAPI.getValue())
		{
			if (StagingFont == null)
			{
				StagingFont = new BitmapFontCache(Fonts.getCompass());
				StagingFont.setColor(Color.RED);
				StagingFont.setPosition(100, 120);
				StagingFont.addText("Using Staging Server", 70, 50);
			}
			if (StagingFont != null) StagingFont.draw(batch);
		}

		if (GlobalCore.isTestVersion())
		{

			float FpsInfoSize = MapTileLoader.queueProcessorLifeCycle ? 4 : 8;

			if (FpsInfoSprite != null)
			{
				batch.draw(FpsInfoSprite, FpsInfoPos, 2, FpsInfoSize, FpsInfoSize);
			}
			else
			{
				if (SpriteCache.day_skin != null)// SpriteCache is initial
				{

					FpsInfoSprite = new Sprite(SpriteCache.getThemedSprite("pixel2x2"));
					FpsInfoSprite.setColor(1.0f, 1.0f, 0.0f, 1.0f);
					FpsInfoSprite.setSize(4, 4);
				}
			}

			FpsInfoPos++;
			if (FpsInfoPos > 60)
			{
				FpsInfoPos = 0;
			}

			if (debugWriteSpriteCount)
			{
				renderTime = ((System.currentTimeMillis() - lastRenderBegin) + renderTime) / 2;
				Fonts.getBubbleSmall().draw(batch,
						"Max Sprites on Batch:" + String.valueOf(debugSpritebatchMaxCount) + "/" + String.valueOf(renderTime), width / 4,
						20);
				debugSpritebatchMaxCount = Math.max(debugSpritebatchMaxCount, batch.maxSpritesInBatch);
			}
			batch.end();
		}

		Gdx.gl.glFlush();
		Gdx.gl.glFinish();

	}

	private BitmapFontCache StagingFont;

	protected int debugSpritebatchMaxCount = 0;
	protected long lastRenderBegin = 0;
	protected long renderTime = 0;

	@Override
	public void resize(int Width, int Height)
	{
		width = Width;
		height = Height;

		if (child != null) child.setSize(width, height);
		camera = new OrthographicCamera(width, height);
		prjMatrix = new ParentInfo(new Matrix4().setToOrtho2D(0, 0, width, height), new Vector2(0, 0), new CB_RectF(0, 0, width, height));

	}

	@Override
	public void pause()
	{
		// wird aufgerufen beim Wechsel der aktiven App und beim Ausschalten des Geräts
		// Logger.LogCat("Pause");

		onStop();
	}

	@Override
	public void resume()
	{
		// Logger.LogCat("Resume");

		onStart();
	}

	@Override
	public void dispose()
	{
		disposeTexture();

		SpriteCache.destroyCache();
		try
		{
			Translation.writeMisingStringsFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void onStart()
	{
		// App wird wiederhergestellt oder Gerät eingeschaltet
		// Logger.LogCat("GL_Listner => onStart");
		started.set(true);
		if (listenerInterface != null) listenerInterface.RenderDirty();
		// startTimer(FRAME_RATE_ACTION, "GL_Listner onStart()");

		if (ActivityIsShown || DialogIsShown)
		{
			platformConector.showForDialog();
		}
		else if (child != null)
		{
			child.onShow();
		}

		if (ActivityIsShown)
		{
			if (mActivity != null) mActivity.onShow();
		}
		if (DialogIsShown)
		{
			if (mDialog != null) mDialog.onShow();
		}

		renderOnce("Gl_Listner.onStart()");

	}

	public void onStop()
	{
		// App wird verkleinert oder Gerät ausgeschaltet
		// Logger.LogCat("GL_Listner => onStop");
		stopTimer();
		if (listenerInterface != null) listenerInterface.RenderContinous();
		child.onStop();
		toast = null; // regenerate toast control
	}

	public static boolean getIsTouchDown()
	{
		return misTouchDown;
	}

	public int getFpsInfoPos()
	{
		return FpsInfoPos;
	}

	public float getWidth()
	{
		return width;
	}

	public float getHeight()
	{
		return height;
	}

	// TouchEreignisse die von der View gesendet werden
	// hier wird entschieden, wann TouchDonw, TouchDragged, TouchUp und Clicked, LongClicked Ereignisse gesendet werden müssen
	public boolean onTouchDownBase(int x, int y, int pointer, int button)
	{
		misTouchDown = true;
		touchDraggedActive = false;
		touchDraggedCorrect = new Point(0, 0);

		GL_View_Base view = null;

		if (MarkerIsShown)// zuerst Marker Testen
		{
			view = mMarkerOverlay.touchDown(x, (int) mMarkerOverlay.getHeight() - y, pointer, button);
		}
		if (view == null)
		{
			CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;
			view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);
		}

		if (view == null) return false;

		// wenn dieser TouchDown ausserhalb einer TextView war, dann resete den TextField Focus
		if (GL.that.getKeyboardFocus() != null)
		{
			if (!(view instanceof EditTextFieldBase) && !(view instanceof SelectionMarker) && !(view instanceof Button)
					&& !GL.that.PopUpIsShown())
			{
				GL.that.setKeyboardFocus(null);
			}
		}

		if (touchDownPos.containsKey(pointer))
		{
			// für diesen Pointer ist aktuell ein kinetisches Pan aktiv -> dieses abbrechen
			StopKinetic(x, y, pointer, false);
		}

		// down Position merken
		touchDownPos.put(pointer, new TouchDownPointer(pointer, new Point(x, y), view));

		// chk if LongClickable
		if (view.isLongClickable())
		{
			startLongClickTimer(pointer, x, y);
		}
		else
		{
			cancelLongClickTimer();
		}

		return true;
	}

	public boolean onTouchDraggedBase(int x, int y, int pointer)
	{

		CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

		if (!touchDownPos.containsKey(pointer))
		{
			// für diesen Pointer ist kein touchDownPos gespeichert ->
			// dürfte nicht passieren!!!

			return false;
		}

		TouchDownPointer first = touchDownPos.get(pointer);

		try
		{
			Point akt = new Point(x, y);
			if (touchDraggedActive || (distance(akt, first.point) > first.view.getClickTolerance()))
			{
				// Nachdem die ClickToleranz überschritten wurde wird jetzt hier die Verschiebung gemerkt.
				// Diese wird dann immer von den Positionen abgezogen, damit der erste Sprung bei der Verschiebung nachem die Toleranz
				// überschriten wurde nicht mehr auftritt.
				if (!touchDraggedActive)
				{
					touchDraggedCorrect = new Point(x - first.point.x, y - first.point.y);
				}
				x -= touchDraggedCorrect.x;
				y -= touchDraggedCorrect.y;
				// merken, dass das Dragging aktiviert wurde, bis der Finger wieder losgelassen wird
				touchDraggedActive = true;
				// zu weit verschoben -> Long-Click detection stoppen
				cancelLongClickTimer();
				// touchDragged Event an das View, das den onTouchDown bekommen hat
				boolean behandelt = first.view.touchDragged(x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight() - y
						- (int) first.view.ThisWorldRec.getY(), pointer, false);
				Logger.LogCat("GL_Listner => onTouchDraggedBase : " + behandelt);
				if (!behandelt && first.view.getParent() != null)
				{
					// Wenn der Parent eine ScrollBox hat -> Scroll-Events dahin weiterleiten
					first.view.getParent().touchDragged(x - (int) first.view.getParent().ThisWorldRec.getX(),
							(int) testingView.getHeight() - y - (int) first.view.getParent().ThisWorldRec.getY(), pointer, false);
				}
				if (touchDownPos.size() == 1)
				{
					if (first.kineticPan == null) first.kineticPan = new KineticPan();
					first.kineticPan.setLast(System.currentTimeMillis(), x, y);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	public boolean onTouchUpBase(int x, int y, int pointer, int button)
	{
		misTouchDown = false;
		cancelLongClickTimer();

		CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

		if (!touchDownPos.containsKey(pointer))
		{
			// für diesen Pointer ist kein touchDownPos gespeichert ->
			// dürfte nicht passieren!!!

			return false;
		}

		TouchDownPointer first = touchDownPos.get(pointer);

		try
		{
			Point akt = new Point(x, y);
			if (distance(akt, first.point) < first.view.getClickTolerance())
			{
				// Finger wurde losgelassen ohne viel Bewegung -> onClick erzeugen
				// glListener.onClick(akt.x, akt.y, pointer, 0);
				if (first.view.isClickable())
				{
					// Testen, ob dies ein Doppelklick ist
					if (first.view.isDblClickable() && (System.currentTimeMillis() < lastClickTime + mDoubleClickTime)
							&& (lastClickPoint != null) && (distance(akt, lastClickPoint) < first.view.getClickTolerance()))
					{
						boolean handled = first.view.doubleClick(x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight()
								- y - (int) first.view.ThisWorldRec.getY(), pointer, button);
						if (handled) platformConector.vibrate();

						lastClickTime = 0;
						lastClickPoint = null;
					}
					else
					{
						// normaler Click
						boolean handled = first.view.click(x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight() - y
								- (int) first.view.ThisWorldRec.getY(), pointer, button);
						if (handled) platformConector.vibrate();
						// Logger.LogCat("GL_Listner => onTouchUpBase (Click) : " + first.view.getName());
						lastClickTime = System.currentTimeMillis();
						lastClickPoint = akt;
					}
				}
			}
			else
			{
				x -= touchDraggedCorrect.x;
				y -= touchDraggedCorrect.y;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			CB_Core.Log.Logger.Error("GL_Listner.onTouchUpBase()", "", e);
		}

		try
		{
			if (first.kineticPan != null)
			{
				first.kineticPan.start();
				first.startKinetic(this, x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight() - y
						- (int) first.view.ThisWorldRec.getY());
			}
			else
			{
				// onTouchUp immer auslösen
				first.view.touchUp(x, (int) testingView.getHeight() - y, pointer, button);
				touchDownPos.remove(pointer);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			CB_Core.Log.Logger.Error("GL_Listner.onTouchUpBase()", "", e);
		}
		// Logger.LogCat("GL_Listner => onTouchUpBase : " + first.view.getName());
		// glListener.onTouchUp(x, y, pointer, 0);

		return true;
	}

	public GL_View_Base onTouchDown(int x, int y, int pointer, int button)
	{
		GL_View_Base view = null;

		CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

		view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);

		return view;
	}

	public boolean onTouchDragged(int x, int y, int pointer)
	{
		boolean behandelt = false;

		CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

		behandelt = testingView.touchDragged(x, (int) testingView.getHeight() - y, pointer, false);

		return behandelt;
	}

	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		CB_View_Base testingView = DialogIsShown ? mDialog : ActivityIsShown ? mActivity : child;

		behandelt = testingView.touchUp(x, (int) testingView.getHeight() - y, pointer, button);

		return behandelt;
	}

	public static void startTimer(long delay, final String Name)
	{
		if (timerValue == delay) return;
		stopTimer();
		// Logger.LogCat("Start Timer: " + delay + " (" + Name + ")");

		timerValue = delay;
		myTimer = new Timer();
		myTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				TimerMethod();
			}

			private void TimerMethod()
			{
				if (listenerInterface != null) listenerInterface.RequestRender("Timer" + Name);

			}

		}, 0, delay);
		// if (listenerInterface != null) listenerInterface.RenderDirty();
	}

	public static void stopTimer()
	{
		// Logger.LogCat("Stop Timer");
		if (myTimer != null)
		{
			myTimer.cancel();
			myTimer = null;
		}
		timerValue = 0;
		// if (listenerInterface != null) listenerInterface.RenderContinous();
	}

	public interface renderStartet
	{
		public void renderIsStartet();
	}

	public void registerRenderStartetListner(renderStartet listner)
	{
		renderStartetListner = listner;

		// wenn kein Render Auftrag kommt, wird auch der waitDialog nicht ausgeblendet!
		addRenderView(child, FRAME_RATE_FAST_ACTION);
	}

	private void disposeTexture()
	{
		if (mDarknesPixmap != null) mDarknesPixmap.dispose();
		if (mDarknesTexture != null) mDarknesTexture.dispose();
		mDarknesPixmap = null;
		mDarknesTexture = null;
		mDarknesSprite = null;
	}

	protected void drawDarknessSprite()
	{
		if (batch == null) return;
		if (mDarknesSprite == null)
		{
			disposeTexture();
			mDarknesPixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
			mDarknesPixmap.setColor(Fonts.getDarknesColor());
			mDarknesPixmap.fillRectangle(0, 0, width, height);
			mDarknesTexture = new Texture(mDarknesPixmap, Pixmap.Format.RGBA8888, false);
			mDarknesSprite = new Sprite(mDarknesTexture, (int) width, (int) height);
		}

		if (mDarknesSprite != null) mDarknesSprite.draw(batch, darknesAlpha);
		if (darknesAnimationRuns)
		{
			darknesAlpha += 0.1f;
			if (darknesAlpha > 1f)
			{
				darknesAlpha = 1f;
				darknesAnimationRuns = false;
			}
			renderOnce("Darknes Animation");
		}

	}

	public void Initialize()
	{
		// Logger.LogCat("GL_Listner => Initialize");

		if (batch == null)
		{
			if (Config.settings.DebugSpriteBatchCountBuffer.getValue())
			{
				// for Debug set to max!
				batch = new SpriteBatch(10000);
			}
			else
			{
				batch = new SpriteBatch(SPRITE_BATCH_BUFFER);
			}
		}

		if (child == null)
		{
			child = new MainViewBase(0, 0, width, height, "MainView");
			child.setClickable(true);
		}

		if (mDialog == null)
		{
			mDialog = new MainViewBase(0, 0, width, height, "Dialog");
			mDialog.setClickable(true);
		}

		if (mActivity == null)
		{
			mActivity = new MainViewBase(0, 0, width, height, "Dialog");
			mActivity.setClickable(true);
		}

		initialMarkerOverlay();
	}

	public CB_View_Base getDialogLayer()
	{
		return mDialog;
	}

	protected void initialMarkerOverlay()
	{
		mMarkerOverlay = new Box(new CB_RectF(0, 0, width, height), "MarkerOverlay");
		selectionMarkerCenter = new SelectionMarker(SelectionMarker.Type.Center);
		selectionMarkerLeft = new SelectionMarker(SelectionMarker.Type.Left);
		selectionMarkerRight = new SelectionMarker(SelectionMarker.Type.Right);

		hideMarker();

		mMarkerOverlay.addChild(selectionMarkerCenter);
		mMarkerOverlay.addChild(selectionMarkerLeft);
		mMarkerOverlay.addChild(selectionMarkerRight);

	}

	public void setGLViewID(ViewID id)
	{
		if (child == null) Initialize();
		child.setGLViewID(id);
	}

	public void addRenderView(GL_View_Base view, int delay)
	{
		synchronized (renderViews)
		{
			if (!view.isVisible())
			{
				if (renderViews.containsKey(view))
				{
					renderViews.remove(view);
					calcNewRenderSpeed();
					if (listenerInterface != null) listenerInterface.RequestRender("");
				}
				return;
			}
			if (renderViews.containsKey(view))
			{
				renderViews.remove(view);
			}
			renderViews.put(view, delay);
			calcNewRenderSpeed();
			if (listenerInterface != null) listenerInterface.RequestRender("");
		}
	}

	public void removeRenderView(GL_View_Base view)
	{
		synchronized (renderViews)
		{
			if (renderViews.containsKey(view))
			{
				renderViews.remove(view);
				calcNewRenderSpeed();
			}
		}
	}

	/**
	 * Fürt EINEN Render Durchgang aus
	 * 
	 * @param view
	 *            Aufrufendes GL_View_Base für Debug zwecke. Kann auch null sein.
	 */
	public void renderOnce(String requestName)
	{

		if (requestName == null)
		{
			return;
		}

		if (listenerInterface != null) listenerInterface.RequestRender(requestName);
	}

	private void calcNewRenderSpeed()
	{
		synchronized (renderViews)
		{
			int minDelay = 0;
			Iterator<Integer> it = renderViews.values().iterator();
			while (it.hasNext())
			{
				int delay = it.next();
				if (delay > minDelay) minDelay = delay;
			}
			if (minDelay == 0) stopTimer();
			else
				startTimer(minDelay, "GL_Listner calcNewRenderSpeed()");
		}

	}

	private void startLongClickTimer(final int pointer, final int x, final int y)
	{
		cancelLongClickTimer();

		longClickTimer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				if (!touchDownPos.containsKey(pointer)) return;
				// für diesen Pointer ist kein touchDownPos gespeichert ->
				// dürfte nicht passieren!!!
				TouchDownPointer first = touchDownPos.get(pointer);
				Point akt = new Point(x, y);
				if (distance(akt, first.point) < first.view.getClickTolerance())
				{
					if (first.view.isClickable())
					{
						boolean handled = first.view.longClick(x - (int) first.view.ThisWorldRec.getX(), (int) child.getHeight() - y
								- (int) first.view.ThisWorldRec.getY(), pointer, 0);
						// Logger.LogCat("GL_Listner => onLongClick : " + first.view.getName());
						// für diesen TouchDownn darf kein normaler Click mehr ausgeführt werden
						touchDownPos.remove(pointer);
						// onTouchUp nach Long-Click direkt auslösen
						first.view.touchUp(x, (int) child.getHeight() - y, pointer, 0);
						// Logger.LogCat("GL_Listner => onTouchUpBase : " + first.view.getName());
						if (handled) platformConector.vibrate();
					}
				}
			}
		};
		longClickTimer.schedule(task, mLongClickTime);
	}

	private void cancelLongClickTimer()
	{
		if (longClickTimer != null)
		{
			longClickTimer.cancel();
			longClickTimer = null;
		}
	}

	public void StopKinetic(int x, int y, int pointer, boolean forceTouchUp)
	{
		TouchDownPointer first = touchDownPos.get(pointer);
		if (first != null)
		{
			first.stopKinetic();
			first.kineticPan = null;
			if (forceTouchUp) first.view.touchUp(x, y, pointer, 0);
		}
	}

	// Abstand zweier Punkte
	private int distance(Point p1, Point p2)
	{
		return (int) Math.round(Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)));
	}

	private int distance(int x1, int y1, int x2, int y2)
	{
		return (int) Math.round(Math.sqrt(Math.pow(x1 - x1, 2) + Math.pow(y1 - y2, 2)));
	}

	public class TouchDownPointer
	{
		public Point point;
		private int pointer;
		private GL_View_Base view;
		private KineticPan kineticPan;
		private Timer timer;

		public TouchDownPointer(int pointer, Point point, GL_View_Base view)
		{
			this.pointer = pointer;
			this.point = point;
			this.view = view;
			this.kineticPan = null;
		}

		public void startKinetic(final GL listener, final int x, final int y)
		{
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					Point pan = kineticPan.getAktPan();
					// Logger.LogCat("KinteicPan: " + pan.x + " - " + pan.y);

					try
					{
						if (kineticPan.fertig)
						{
							// Logger.LogCat("KineticPan fertig");
							view.touchUp(x - pan.x, y - pan.y, pointer, 0);
							touchDownPos.remove(pointer);
							kineticPan = null;
							this.cancel();
							timer = null;
						}
					}
					catch (Exception e)
					{
						touchDownPos.remove(pointer);
						kineticPan = null;
						this.cancel();
						timer = null;
					}

					view.touchDragged(x - pan.x, y - pan.y, pointer, true);
				}
			}, 0, FRAME_RATE_FAST_ACTION);
		}

		public void stopKinetic()
		{
			if (timer != null)
			{
				timer.cancel();
				timer = null;
				kineticPan = null;
			}
		}
	}

	protected class KineticPan
	{
		private boolean started;
		private boolean fertig;
		// benutze den Abstand der letzten 5 Positionsänderungen
		final int anzPoints = 6;
		int anzPointsUsed = 0;
		private int[] x = new int[anzPoints];
		private int[] y = new int[anzPoints];
		private long[] ts = new long[anzPoints];
		private int diffX;
		private int diffY;
		private long diffTs;
		private long startTs;
		private long endTs;

		public KineticPan()
		{
			fertig = false;
			started = false;
			diffX = 0;
			diffY = 0;
			for (int i = 0; i < anzPoints; i++)
			{
				x[i] = 0;
				y[i] = 0;
				ts[i] = 0;
			}
			anzPointsUsed = 0;
		}

		public void setLast(long aktTs, int aktX, int aktY)
		{
			if ((anzPointsUsed > 0) && (ts[0] < aktTs - 500))
			{
				// wenn seit der letzten Verschiebung mehr Zeit Vergangen ist -> bisherige gemerkte Verschiebungen löschen
				anzPointsUsed = 0;
				started = false;
				return;
			}

			anzPointsUsed++;
			Logger.LogCat("AnzUsedPoints: " + anzPointsUsed);
			if (anzPointsUsed > anzPoints) anzPointsUsed = anzPoints;
			for (int i = anzPoints - 2; i >= 0; i--)
			{
				x[i + 1] = x[i];
				y[i + 1] = y[i];
				ts[i + 1] = ts[i];
			}
			x[0] = aktX;
			y[0] = aktY;
			ts[0] = aktTs;

			for (int i = 1; i < anzPoints; i++)
			{
				if (x[i] == 0) x[i] = x[i - 1];
				if (y[i] == 0) y[i] = y[i - 1];
				if (ts[i] == 0) ts[i] = ts[i - 1];
			}
			diffX = x[anzPointsUsed - 1] - aktX;
			diffY = aktY - y[anzPointsUsed - 1];
			diffTs = aktTs - ts[anzPointsUsed - 1];

			if (diffTs > 0)
			{
				diffX = (int) ((float) diffX / FRAME_RATE_ACTION * diffTs);
				diffY = (int) ((float) diffY / FRAME_RATE_ACTION * diffTs);
			}
			Logger.LogCat("diffx = " + diffX + " - diffy = " + diffY);

			// debugString = x[2] + " - " + x[1] + " - " + x[0];
		}

		public boolean getFertig()
		{
			return fertig;
		}

		public boolean getStarted()
		{
			return started;
		}

		public void start()
		{
			anzPointsUsed = Math.max(anzPointsUsed, 1);
			if (ts[0] < System.currentTimeMillis() - 200)
			{
				// kinematisches Scrollen nur, wenn seit der letzten Verschiebung kaum Zeit vergangen ist
				fertig = true;
				return;
			}
			startTs = System.currentTimeMillis();
			int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

			endTs = startTs + 1000 + abstand * 15 / anzPointsUsed;
			// if (endTs > startTs + 6000) endTs = startTs + 6000; // max. Zeit festlegen
			Logger.LogCat("endTs - startTs: " + String.valueOf(endTs - startTs));
			// endTs = startTs + 5000;
			started = true;
		}

		private int lastX = 0;
		private int lastY = 0;

		public Point getAktPan()
		{
			anzPointsUsed = Math.max(anzPointsUsed, 1);
			Point result = new Point(0, 0);

			long aktTs = System.currentTimeMillis();
			float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
			// Logger.LogCat("Faktor: " + faktor);
			faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
			// faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
			// Logger.LogCat("Faktor2: " + faktor);
			if (faktor >= 1)
			{
				fertig = true;
				faktor = 1;
			}

			result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
			result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;

			if ((result.x == lastX) && (result.y == lastY))
			{
				// wenn keine Nennenswerten Änderungen mehr gemacht werden dann einfach auf fertig schalten
				fertig = true;
				faktor = 1;
				result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
				result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;
			}
			double abstand = distance(lastX, lastY, result.x, result.y);
			if (abstand > MAX_KINETIC_SCROLL_DISTANCE)
			{
				double fkt = MAX_KINETIC_SCROLL_DISTANCE / abstand;
				result.x = (int) ((result.x - lastX) * fkt + lastX);
				result.y = (int) ((result.y - lastY) * fkt + lastY);
			}

			lastX = result.x;
			lastY = result.y;
			return result;
		}
	}

	public CB_View_Base getActDialog()
	{
		return actDialog;
	}

	public void showPopUp(PopUp_Base popUp, float x, float y)
	{
		popUp.setX(x);
		popUp.setY(y);

		CB_View_Base aktView = DialogIsShown ? mDialog : child;
		if (ActivityIsShown) aktView = mActivity;

		aktView.addChild(popUp);
		aktPopUp = popUp;
		aktPopUp.onShow();
		renderOnce("Show PopUp");
	}

	public void closePopUp(PopUp_Base popUp)
	{
		CB_View_Base aktView = DialogIsShown ? mDialog : child;
		if (ActivityIsShown) aktView = mActivity;

		aktView.removeChild(popUp);
		if (aktPopUp != null) aktPopUp.onHide();
		aktPopUp = null;
		if (popUp != null) popUp.dispose();
		renderOnce("Close PopUp");
	}

	public boolean PopUpIsShown()
	{
		return (aktPopUp != null);
	}

	public void showDialog(final Dialog dialog)
	{
		if (dialog instanceof ActivityBase) throw new IllegalArgumentException(
				"don´t show an Activity as Dialog. Use \"GL_listner.showActivity()\"");

		showDialog(dialog, false);
	}

	public void showDialog(final Dialog dialog, boolean atTop)
	{
		setKeyboardFocus(null);

		if (dialog instanceof ActivityBase) throw new IllegalArgumentException(
				"don´t show an Activity as Dialog. Use \"GL_listner.showActivity()\"");

		clearRenderViews();

		// Center Menu on Screen
		float x = (width - dialog.getWidth()) / 2;
		float y = (height - dialog.getHeight()) / 2;

		if (atTop) y = height - dialog.getHeight() - (Dialog.getMargin() * 4);

		dialog.setPos(x, y);

		if (aktPopUp != null)
		{
			closePopUp(aktPopUp);
		}

		if (actDialog != null && actDialog != dialog)
		{
			actDialog.onHide();
			actDialog.setEnabled(false);
			// am Anfang der Liste einfügen
			dialogHistory.add(0, actDialog);
			// mDialog.removeChilds();
		}

		actDialog = dialog;

		mDialog.addChildDirekt(dialog);
		mDialog.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Sollte bei einem Click neben dem Dialog ausgelöst werden.
				// Dann soll der Dialog geschlossen werden, wenn es sich um ein Menü handelt.
				if (DialogIsShown)
				{
					GL_View_Base vDialog = mDialog.getChild(0);
					if (vDialog instanceof Menu) closeDialog(actDialog);
					if (aktPopUp != null)
					{
						closePopUp(aktPopUp);
					}
					return true;
				}

				if (aktPopUp != null)
				{
					closePopUp(aktPopUp);
					return true;
				}

				return false;
			}
		});

		child.setClickable(false);
		DialogIsShown = true;
		darknesAnimationRuns = true;
		actDialog.onShow();
		actDialog.setEnabled(true);
		actDialog.setVisible();

		platformConector.showForDialog();

		renderOnce("ShowDialog");
		Logger.LogCat("ShowDialog: " + actDialog.toString());
	}

	public void showActivity(final ActivityBase activity)
	{
		setKeyboardFocus(null);
		clearRenderViews();
		platformConector.showForDialog();

		if (aktPopUp != null)
		{
			closePopUp(aktPopUp);
		}

		darknesAnimationRuns = true;

		// Center activity on Screen
		float x = (width - activity.getWidth()) / 2;
		float y = (height - activity.getHeight()) / 2;

		activity.setPos(x, y);

		if (actDialog != null)
		{
			actDialog.onHide();
		}

		actActivity = activity;

		mActivity.addChildDirekt(activity);

		child.setClickable(false);
		ActivityIsShown = true;
		if (!GlobalCore.isTab) child.onHide();
		actActivity.onShow();

		platformConector.showForDialog();
	}

	public void closeActivity()
	{
		closeActivity(true);
	}

	public void closeActivity(boolean MsgToPlatformConector)
	{
		if (!ActivityIsShown) return;
		if (MsgToPlatformConector) platformConector.hideForDialog();
		if (actActivity != null) actActivity.onHide();
		actActivity = null;
		mActivity.removeChildsDirekt();
		child.setClickable(true);
		child.invalidate();

		if (!GlobalCore.isTab) child.onShow();

		ActivityIsShown = false;
		darknesAlpha = 0f;

		clearRenderViews();
		renderOnce("Close Activity");
	}

	public void closeAllDialogs()
	{
		dialogHistory.clear();
		if (actDialog != null) closeDialog(actDialog);
	}

	public void closeDialog(CB_View_Base dialog)
	{
		if (dialog instanceof ActivityBase) throw new IllegalArgumentException(
				"don´t show an Activity as Dialog. Use \"GL_listner.showActivity()\"");
		closeDialog(dialog, true);
	}

	public void closeDialog(final CB_View_Base dialog, boolean MsgToPlatformConector)
	{
		if (!DialogIsShown || !mDialog.getchilds().contains((dialog)))
		{
			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					if (dialog != null) dialog.dispose();
					System.gc();
				}
			};
			timer.schedule(task, 500);

			return;
		}

		if (MsgToPlatformConector) platformConector.hideForDialog();
		if (actDialog != null) actDialog.onHide();
		if (dialogHistory.size() > 0)
		{
			mDialog.removeChild(actDialog);
			// letzten Dialog wiederherstellen
			actDialog = dialogHistory.get(0);
			actDialog.onShow();
			actDialog.setEnabled(true);
			dialogHistory.remove(0);
			DialogIsShown = true;
			platformConector.showForDialog();
		}
		else
		{
			actDialog = null;
			mDialog.removeChildsDirekt();
			child.setClickable(true);
			child.invalidate();
			DialogIsShown = false;
			darknesAlpha = 0f;
		}

		// if (threadDisposeDialog == null) threadDisposeDialog = new Thread(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// if (dialog != null) dialog.dispose();
		// System.gc();
		// }
		// });
		// threadDisposeDialog.run();

		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				if (dialog != null) dialog.dispose();
				System.gc();
			}
		};
		timer.schedule(task, 500);

		clearRenderViews();
		if (ActivityIsShown) platformConector.showForDialog();
		renderOnce("Close Dialog");
	}

	public void Toast(CB_View_Base view)
	{
		Toast(view, 4000);
	}

	public void closeToast()
	{
		if (mToastOverlay != null)
		{

			ToastIsShown = false;
			mToastOverlay.removeChilds();

			renderOnce("ToastClosing");
		}
	}

	public void Toast(CB_View_Base view, int delay)
	{
		if (mToastOverlay == null)
		{
			mToastOverlay = new Box(new CB_RectF(0, 0, width, height), "ToastView");
		}
		synchronized (mToastOverlay)
		{
			mToastOverlay.removeChilds();

			mToastOverlay.addChild(view);
			ToastIsShown = true;

			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					ToastIsShown = false;
					renderOnce("ToastClosing");
				}
			};

			Timer timer = new Timer();
			timer.schedule(task, delay);
		}
	}

	public void Toast(String string)
	{
		Toast(string, 4000);
	}

	public void Toast(String string, int length)
	{
		if (toast == null)
		{
			toast = new CB_Core.GL_UI.Controls.Dialogs.Toast(new CB_RectF(0, 0, 100, GL_UISizes.BottomButtonHeight / 1.5f), "StringToast");
		}
		toast.setText(string);

		float measuredWidth = Fonts.Measure(string).width + (toast.getLeftWidth() * 2) + (Dialog.getMargin() * 2);
		toast.setWidth(measuredWidth);

		toast.setPos((width / 2) - (measuredWidth / 2), GL_UISizes.BottomButtonHeight * 1.3f);

		Toast(toast, length);
	}

	private void chkStageInitial()
	{
		if (mStage == null)
		{// initial a virtual stage
			mStage = new Stage(UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight(), false);
		}
	}

	public boolean scrolled(int amount)
	{
		chkStageInitial();
		return mStage.scrolled(amount);
	}

	/**
	 * Stopt den Rendervorgang bis er durch RestartRender() wieder gestartet wird
	 */
	public void StopRender()
	{
		stopRender = true;
	}

	/**
	 * Startet den Renderer wenn er durch StopRender() gestoppt wurde
	 */
	public void RestartRender()
	{
		stopRender = false;
		renderOnce("Restart Render");
	}

	public void clearRenderViews()
	{
		synchronized (renderViews)
		{
			stopTimer();
			renderViews.clear();
		}
	}

	/**
	 * @return true wenn behandeld
	 */
	public boolean keyBackCliced()
	{
		if (actDialog instanceof Menu)
		{
			closeDialog(actDialog);
			return true;
		}
		return false;
	}

	// TextFiled Methodes

	public boolean keyDown(int keycode)
	{
		chkStageInitial();
		return mStage.keyDown(keycode);
	}

	public boolean keyTyped(char character)
	{
		chkStageInitial();
		return mStage.keyTyped(character);
	}

	public boolean keyUp(int keycode)
	{
		chkStageInitial();
		return mStage.keyUp(keycode);
	}

	public void setKeyboardFocus(EditWrapedTextField view)
	{
		// don't set Focus to NULL?
		if (view == null && keyboardFocus == null) return;

		// Don't open KeyBoard if Keybord is Showing
		boolean dontOpenKeybord = keyboardFocus != null;

		String sView = "NULL";
		if (view != null) sView = view.toString();
		Logger.LogCat("GL => set KeyBoardFocus to " + sView);

		// fire event
		KeyboardFocusChangedEventList.Call(view);

		keyboardFocus = view;
		hideMarker();

		if (keyboardFocus != null)
		{
			if (!keyboardFocus.dontShowKeyBoard())
			{
				if (!dontOpenKeybord)
				{
					platformConector.callsetKeybordFocus(true);
				}
			}
		}
		else
		{
			if (dontOpenKeybord)
			{
				platformConector.callsetKeybordFocus(false);
			}

		}
	}

	public EditWrapedTextField getKeyboardFocus()
	{
		return keyboardFocus;
	}

	public boolean hasFocus(EditTextFieldBase view)
	{
		return view == keyboardFocus;
	}

	public void hideMarker()
	{
		if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null) initialMarkerOverlay();
		selectionMarkerCenter.setInvisible();
		selectionMarkerLeft.setInvisible();
		selectionMarkerRight.setInvisible();

		MarkerIsShown = false;
	}

	public void showMarker(Type type)
	{
		if (selectionMarkerCenter == null || selectionMarkerLeft == null || selectionMarkerRight == null) initialMarkerOverlay();

		switch (type)
		{
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

	public void selectionMarkerCenterMoveTo(float f, float g)
	{
		selectionMarkerCenter.moveTo(f, g);
	}

	public void selectionMarkerLeftMoveTo(float f, float g)
	{
		selectionMarkerLeft.moveTo(f, g);
	}

	public void selectionMarkerRightMoveTo(float f, float g)
	{
		selectionMarkerRight.moveTo(f, g);
	}

	public boolean selectionMarkerCenterisShown()
	{
		return selectionMarkerCenter.isVisible();
	}

	public void selectionMarkerCenterMoveBy(float dx, float dy)
	{
		selectionMarkerCenter.moveBy(dx, dy);
	}

	public boolean selectionMarkerLeftisShown()
	{
		return selectionMarkerLeft.isVisible();
	}

	public void selectionMarkerLeftMoveBy(float dx, float dy)
	{
		selectionMarkerLeft.moveBy(dx, dy);
	}

	public boolean selectionMarkerRightisShown()
	{
		return selectionMarkerRight.isVisible();
	}

	public void selectionMarkerRightMoveBy(float dx, float dy)
	{
		selectionMarkerRight.moveBy(dx, dy);
	}

	public boolean closeShownDialog()
	{
		if (DialogIsShown)
		{
			closeDialog(actDialog);
			return true;
		}

		if (ActivityIsShown)
		{
			closeActivity();
			return true;
		}

		return false;
	}

	public boolean isShownDialogActivity()
	{
		if (DialogIsShown)
		{
			return true;
		}

		if (ActivityIsShown)
		{

			return true;
		}
		return false;
	}

}

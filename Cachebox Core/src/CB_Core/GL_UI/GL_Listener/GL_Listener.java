package CB_Core.GL_UI.GL_Listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Main.MainView;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.Log.Logger;
import CB_Core.Map.Point;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class GL_Listener implements ApplicationListener // , InputProcessor
{
	public static GL_Listener_Interface listenerInterface;
	public static GL_Listener glListener;
	// # private Member
	private HashMap<GL_View_Base, Integer> renderViews = new HashMap<GL_View_Base, Integer>();
	protected MainViewBase child;
	protected CB_View_Base mDialog;
	private static AtomicBoolean started = new AtomicBoolean(false);
	static boolean useNewInput = true;

	private long mLongClickTime = 0;

	public static final int FRAME_RATE_IDLE = 500;
	public static final int FRAME_RATE_ACTION = 50;
	public static final int FRAME_RATE_FAST_ACTION = 15;

	// # public static member
	public static SpriteBatch batch;
	public static OrthographicCamera camera;
	private ParentInfo prjMatrix;

	protected int width = 0;
	protected int height = 0;

	/**
	 * Constructor
	 */
	public GL_Listener(int initalWidth, int initialHeight)
	{
		glListener = this;
		width = initalWidth;
		height = initialHeight;
		mLongClickTime = Config.settings.LongClicktime.getValue();
	}

	@Override
	public void create()
	{
		Logger.LogCat("GL_Listner => Create");

		GL_UISizes.initial(width, height);

		Initialize();
		startTime = System.currentTimeMillis();
	}

	@Override
	public void pause()
	{
		Logger.DEBUG("Pause");

		onStop();
	}

	@Override
	public void resume()
	{
		Logger.DEBUG("Resume");

		onStart();
	}

	@Override
	public void dispose()
	{

		SpriteCache.destroyCache();

	}

	public void onStart()
	{
		Logger.LogCat("GL_Listner => onStart");
		started.set(true);
		if (listenerInterface != null) listenerInterface.RenderDirty();
		// startTimer(FRAME_RATE_ACTION, "GL_Listner onStart()");
	}

	public void onStop()
	{
		Logger.LogCat("GL_Listner => onStop");
		stopTimer();
		if (listenerInterface != null) listenerInterface.RenderContinous();
		child.onStop();
	}

	// public boolean onClick(int x, int y, int pointer, int button)
	// {
	// boolean behandelt = false;
	//
	// CB_View_Base testingView = DialogIsShown ? mDialog : child;
	//
	// if (testingView.isClickable())
	// {
	// behandelt = testingView.click(x, (int) testingView.getHeight() - y, pointer, button);
	// }
	//
	// return behandelt;
	// }
	//
	// public boolean onLongClick(int x, int y, int pointer, int button)
	// {
	// boolean behandelt = false;
	//
	// CB_View_Base testingView = DialogIsShown ? mDialog : child;
	//
	// if (testingView.isClickable())
	// {
	// behandelt = testingView.longClick(x, (int) testingView.getHeight() - y, pointer, button);
	// }
	// return behandelt;
	// }

	public GL_View_Base onTouchDown(int x, int y, int pointer, int button)
	{
		GL_View_Base view = null;

		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);

		return view;
	}

	public boolean onTouchDragged(int x, int y, int pointer)
	{
		boolean behandelt = false;

		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		behandelt = testingView.touchDragged(x, (int) testingView.getHeight() - y, pointer);

		return behandelt;
	}

	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		behandelt = testingView.touchUp(x, (int) testingView.getHeight() - y, pointer, button);

		return behandelt;
	}

	@Override
	public void resize(int Width, int Height)
	{
		width = Width;
		height = Height;

		if (child != null) child.setSize(width, height);
		camera = new OrthographicCamera(width, height);
		prjMatrix = new ParentInfo(new Matrix4().setToOrtho2D(0, 0, width, height), new Vector2(0, 0), new CB_RectF(0, 0, width, height));

	}

	public static void startTimer(long delay, String Name)
	{
		if (timerValue == delay) return;
		stopTimer();
		Logger.LogCat("Start Timer: " + delay + " (" + Name + ")");

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
				if (listenerInterface != null) listenerInterface.RequestRender(null);

			}

		}, 0, delay);
		// if (listenerInterface != null) listenerInterface.RenderDirty();
	}

	public static long timerValue;
	long startTime;
	static Timer myTimer;

	public static void stopTimer()
	{
		Logger.LogCat("Stop Timer");
		if (myTimer != null)
		{
			myTimer.cancel();
			myTimer = null;
		}
		timerValue = 0;
		// if (listenerInterface != null) listenerInterface.RenderContinous();
	}

	private float darknesAlpha = 0f;
	private boolean darknesAnimationRuns = false;

	@Override
	public void render()
	{

		if (!started.get()) return;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

		batch.setProjectionMatrix(prjMatrix.Matrix());

		child.renderChilds(batch, prjMatrix);

		if (DialogIsShown && mDialog.getCildCount() > 0)
		{

			// Zeichne Transparentes Rec um den Hintergrund abzudunkeln.
			if (mDarknesSprite == null)
			{
				int w = CB_View_Base.getNextHighestPO2((int) width);
				int h = CB_View_Base.getNextHighestPO2((int) height);
				Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
				p.setColor(0f, 0.1f, 0f, 0.9f);
				// p.drawRectangle(1, 1, (int) width - 1, (int) height - 1);
				p.fillRectangle(0, 0, width, height);

				Texture tex = new Texture(p, Pixmap.Format.RGBA8888, false);

				mDarknesSprite = new Sprite(tex, (int) width, (int) height);
			}

			batch.begin();

			mDarknesSprite.draw(batch, darknesAlpha);
			if (darknesAnimationRuns)
			{
				darknesAlpha += 0.0185f;
				if (darknesAlpha > 1f)
				{
					darknesAlpha = 1f;
					darknesAnimationRuns = false;
					removeRenderView(mDialog);
				}
			}

			batch.end();

			mDialog.renderChilds(batch, prjMatrix);
		}

		Gdx.gl.glFlush();
		Gdx.gl.glFinish();

	}

	private Sprite mDarknesSprite;

	private void reduceFPS()
	{
		if (useNewInput) return;
		long endTime = System.currentTimeMillis();
		long dt = endTime - startTime;
		if (dt < 33)
		{
			try
			{
				if (20 - dt > 0) Thread.sleep(20 - dt);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		startTime = System.currentTimeMillis();
	}

	public void Initialize()
	{
		Logger.LogCat("GL_Listner => Initialize");

		if (batch == null)
		{
			batch = new SpriteBatch();
		}

		if (child == null)
		{
			child = new MainView(0, 0, width, height, "MainView");
			child.setClickable(true);
		}

		if (mDialog == null)
		{
			mDialog = new MainView(0, 0, width, height, "Dialog");
			mDialog.setClickable(true);
		}

	}

	public void setGLViewID(ViewID id)
	{
		if (child == null) Initialize();
		child.setGLViewID(id);
	}

	public void addRenderView(GL_View_Base view, int delay)
	{
		if (renderViews.containsKey(view))
		{
			renderViews.remove(view);
		}
		renderViews.put(view, delay);
		calcNewRenderSpeed();
		Logger.LogCat("addRenderView " + view.getName() + "/" + delay + " /registrierte RenderViews" + renderViews.size());
	}

	public void removeRenderView(GL_View_Base view)
	{
		if (renderViews.containsKey(view))
		{
			renderViews.remove(view);
			calcNewRenderSpeed();
			Logger.LogCat("removeRenderView " + view.getName() + "/verbleibende RenderViews" + renderViews.size());
		}
	}

	/**
	 * Fürt EINEN Render Durchgang aus
	 * 
	 * @param view
	 *            Aufrufendes GL_View_Base für Debug zwecke. Kann auch null sein.
	 */
	public void renderOnce(GL_View_Base view)
	{
		if (listenerInterface != null) listenerInterface.RequestRender(view);
	}

	private void calcNewRenderSpeed()
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

	// TouchEreignisse die von der View gesendet werden
	// hier wird entschieden, wann TouchDonw, TouchDragged, TouchUp und Clicked, LongClicked Ereignisse gesendet werden müssen

	// Timer für Long-Click
	private Timer longTimer;

	private void startLongClickTimer(final int pointer, final int x, final int y)
	{
		cancelLongClickTimer();

		longTimer = new Timer();
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
				if (distance(akt, first.point) < 15)
				{
					if (first.view.isClickable())
					{
						first.view.longClick(x - (int) first.view.ThisWorldRec.getX(), (int) child.getHeight() - y
								- (int) first.view.ThisWorldRec.getY(), pointer, 0);
						Logger.LogCat("GL_Listner => onLongClick : " + first.view.getName());
						// für diesen TouchDownn darf kein normaler Click mehr ausgeführt werden
						touchDownPos.remove(pointer);
						// onTouchUp nach Long-Click direkt auslösen
						first.view.touchUp(x, (int) child.getHeight() - y, pointer, 0);
						Logger.LogCat("GL_Listner => onTouchUpBase : " + first.view.getName());
					}
				}
			}
		};
		longTimer.schedule(task, mLongClickTime);
	}

	private void cancelLongClickTimer()
	{
		if (longTimer != null)
		{
			longTimer.cancel();
			longTimer = null;
		}
	}

	public boolean onTouchDownBase(int x, int y, int pointer, int button)
	{
		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		GL_View_Base view = testingView.touchDown(x, (int) testingView.getHeight() - y, pointer, button);
		if (view == null) return false;

		if (touchDownPos.containsKey(pointer))
		{
			// für diesen Pointer ist aktuell ein kinetisches Pan aktiv -> dieses abbrechen
			TouchDownPointer first = touchDownPos.get(pointer);
			first.stopKinetic();
			first.kineticPan = null;
		}

		// down Position merken
		touchDownPos.put(pointer, new TouchDownPointer(pointer, new Point(x, y), view));

		// Logger.LogCat("GL_Listner => onTouchDownBase : " + view.getName());
		startLongClickTimer(pointer, x, y);

		return true;
	}

	public boolean onTouchDraggedBase(int x, int y, int pointer)
	{
		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		if (!touchDownPos.containsKey(pointer)) return false; // für diesen Pointer ist kein touchDownPos gespeichert ->
																// dürfte nicht passieren!!!
		TouchDownPointer first = touchDownPos.get(pointer);

		Point akt = new Point(x, y);
		if (distance(akt, first.point) > 15)
		{
			// zu weit verschoben -> Long-Click detection stoppen
			cancelLongClickTimer();
			// touchDragged Event an das View, das den onTouchDown bekommen hat
			first.view.touchDragged(x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight() - y
					- (int) first.view.ThisWorldRec.getY(), pointer);
			// Logger.LogCat("GL_Listner => onTouchDraggedBase : " + first.view.getName());

			if (touchDownPos.size() == 1)
			{
				if (first.kineticPan == null) first.kineticPan = new KineticPan();
				first.kineticPan.setLast(System.currentTimeMillis(), x, y);
			}
		}

		return true;
	}

	public boolean onTouchUpBase(int x, int y, int pointer, int button)
	{
		cancelLongClickTimer();

		CB_View_Base testingView = DialogIsShown ? mDialog : child;

		if (!touchDownPos.containsKey(pointer)) return false; // für diesen Pointer ist kein touchDownPos gespeichert ->
																// dürfte nicht passieren!!!
		TouchDownPointer first = touchDownPos.get(pointer);
		Point akt = new Point(x, y);
		if (distance(akt, first.point) < 15)
		{
			// Finger wurde losgelassen ohne viel Bewegung -> onClick erzeugen
			// glListener.onClick(akt.x, akt.y, pointer, 0);
			if (first.view.isClickable())
			{
				first.view.click(x - (int) first.view.ThisWorldRec.getX(), (int) testingView.getHeight() - y
						- (int) first.view.ThisWorldRec.getY(), pointer, button);
				Logger.LogCat("GL_Listner => onTouchUpBase (Click) : " + first.view.getName());
			}
		}

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
		// Logger.LogCat("GL_Listner => onTouchUpBase : " + first.view.getName());
		// glListener.onTouchUp(x, y, pointer, 0);

		return true;
	}

	// Abstand zweier Punkte
	private int distance(Point p1, Point p2)
	{
		return (int) Math.round(Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)));
	}

	// Zwischenspeicher für die touchDown Positionen der einzelnen Finger
	private SortedMap<Integer, TouchDownPointer> touchDownPos = new TreeMap<Integer, TouchDownPointer>();

	class TouchDownPointer
	{
		private Point point;
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

		public void startKinetic(final GL_Listener listener, final int x, final int y)
		{
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					Point pan = kineticPan.getAktPan();
					Logger.LogCat("KinteicPan: " + pan.x + " - " + pan.y);
					if (kineticPan.fertig)
					{
						Logger.LogCat("KineticPan fertig");
						view.touchUp(x - pan.x, y - pan.y, pointer, 0);
						touchDownPos.remove(pointer);
						kineticPan = null;
						this.cancel();
						timer = null;

					}
					view.touchDragged(x - pan.x, y - pan.y, pointer);
				}
			}, 0, FRAME_RATE_ACTION);
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
		final int anzPoints = 3;
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
		}

		public void setLast(long aktTs, int aktX, int aktY)
		{
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
			diffX = x[anzPoints - 1] - aktX;
			diffY = aktY - y[anzPoints - 1];
			diffTs = aktTs - ts[anzPoints - 1];

			if (diffTs > 0)
			{
				diffX = (int) (diffX * FRAME_RATE_ACTION / diffTs);
				diffY = (int) (diffY * FRAME_RATE_ACTION / diffTs);
			}

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
			startTs = System.currentTimeMillis();
			int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

			endTs = startTs + 2000 + abstand * 50 / anzPoints;
			// endTs = startTs + 2000; // Povisorisch
			started = true;
		}

		private int lastX = 0;
		private int lastY = 0;

		public Point getAktPan()
		{
			Point result = new Point(0, 0);

			long aktTs = System.currentTimeMillis();
			float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
			Logger.LogCat("Faktor: " + faktor);
			faktor = com.badlogic.gdx.math.Interpolation.exp10Out.apply(faktor);
			Logger.LogCat("Faktor2: " + faktor);
			if (faktor >= 1)
			{
				fertig = true;
				faktor = 1;
			}

			result.x = (int) (diffX / anzPoints * (1 - faktor)) + lastX;
			result.y = (int) (diffY / anzPoints * (1 - faktor)) + lastY;
			lastX = result.x;
			lastY = result.y;
			return result;
		}
	}

	private boolean DialogIsShown = false;
	private Dialog actDialog;

	public Dialog getActDialog()
	{
		return actDialog;
	}

	public void showDialog(Dialog dialog)
	{

		// Center Menu on Screen
		float x = (width - dialog.getWidth()) / 2;
		float y = (height - dialog.getHeight()) / 2;
		dialog.setPos(x, y);
		actDialog = dialog;

		mDialog.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Sollte bei einem Click neben dem Dialog ausgelöst werden.
				// Dann soll der Dialog geschlossen werden.
				closeDialog();
				return true;
			}
		});

		mDialog.addChild(dialog);
		child.setClickable(false);
		DialogIsShown = true;
		darknesAnimationRuns = true;
		addRenderView(mDialog, FRAME_RATE_FAST_ACTION);
	}

	public void closeDialog()
	{
		actDialog = null;
		mDialog.removeChilds();
		child.setClickable(true);
		DialogIsShown = false;
		darknesAlpha = 0f;
		removeRenderView(mDialog);
		renderOnce(mDialog);
	}

}

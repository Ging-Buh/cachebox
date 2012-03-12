package CB_Core.GL_UI.GL_Listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.Controls.MainView;
import CB_Core.Log.Logger;
import CB_Core.Map.Point;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class GL_Listener implements ApplicationListener // , InputProcessor
{
	public static GL_Listener_Interface listenerInterface;
	public static GL_Listener glListener;
	// # private Member
	private HashMap<GL_View_Base, Integer> renderViews = new HashMap<GL_View_Base, Integer>();
	MainView child;
	private static AtomicBoolean started = new AtomicBoolean(false);
	static boolean useNewInput = true;

	public static final int FRAME_RATE_IDLE = 500;
	public static final int FRAME_RATE_ACTION = 50;
	public static final int FRAME_RATE_FAST_ACTION = 15;

	// # public static member
	public static SpriteBatch batch;
	public static OrthographicCamera camera;
	private ParentInfo prjMatrix;

	private int width = 0;
	private int height = 0;

	/**
	 * Constructor
	 */
	public GL_Listener(int initalWidth, int initialHeight)
	{
		glListener = this;
		width = initalWidth;
		height = initialHeight;
		// GL_View_Base.debug = true;
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

	public boolean onClick(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		if (child.isClickable())
		{
			behandelt = child.click(x, (int) child.getHeight() - y, pointer, button);
		}

		return behandelt;
	}

	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		if (child.isClickable())
		{
			behandelt = child.longClick(x, (int) child.getHeight() - y, pointer, button);
		}
		return behandelt;
	}

	public GL_View_Base onTouchDown(int x, int y, int pointer, int button)
	{
		GL_View_Base view = null;

		view = child.touchDown(x, (int) child.getHeight() - y, pointer, button);

		return view;
	}

	public boolean onTouchDragged(int x, int y, int pointer)
	{
		boolean behandelt = false;

		behandelt = child.touchDragged(x, (int) child.getHeight() - y, pointer);

		return behandelt;
	}

	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		behandelt = child.touchUp(x, (int) child.getHeight() - y, pointer, button);

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

	@Override
	public void render()
	{

		if (!started.get()) return;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

		batch.setProjectionMatrix(prjMatrix.Matrix());

		child.renderChilds(batch, prjMatrix);

		Gdx.gl.glFlush();
		Gdx.gl.glFinish();

	}

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
		longTimer.schedule(task, 2000);
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
		GL_View_Base view = child.touchDown(x, (int) child.getHeight() - y, pointer, button);
		if (view == null) return false;
		// down Position merken
		touchDownPos.put(pointer, new TouchDownPointer(pointer, new Point(x, y), view));

		Logger.LogCat("GL_Listner => onTouchDownBase : " + view.getName());
		startLongClickTimer(pointer, x, y);

		return true;
	}

	public boolean onTouchDraggedBase(int x, int y, int pointer)
	{
		if (!touchDownPos.containsKey(pointer)) return false; // für diesen Pointer ist kein touchDownPos gespeichert ->
																// dürfte nicht passieren!!!
		TouchDownPointer first = touchDownPos.get(pointer);

		Point akt = new Point(x, y);
		if (distance(akt, first.point) > 15)
		{
			// zu weit verschoben -> Long-Click detection stoppen
			cancelLongClickTimer();
			// touchDragged Event an das View, das den onTouchDown bekommen hat
			first.view.touchDragged(x, (int) child.getHeight() - y, pointer);
			Logger.LogCat("GL_Listner => onTouchDraggedBase : " + first.view.getName());
		}

		return true;
	}

	public boolean onTouchUpBase(int x, int y, int pointer, int button)
	{
		cancelLongClickTimer();

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
				first.view.click(x - (int) first.view.ThisWorldRec.getX(),
						(int) child.getHeight() - y - (int) first.view.ThisWorldRec.getY(), pointer, button);
				Logger.LogCat("GL_Listner => onTouchUpBase (Click) : " + first.view.getName());
			}
		}
		// onTouchUp immer auslösen
		first.view.touchUp(x, (int) child.getHeight() - y, pointer, button);
		Logger.LogCat("GL_Listner => onTouchUpBase : " + first.view.getName());
		// glListener.onTouchUp(x, y, pointer, 0);
		touchDownPos.remove(pointer);

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

		public TouchDownPointer(int pointer, Point point, GL_View_Base view)
		{
			this.pointer = pointer;
			this.point = point;
			this.view = view;
		}
	}

}

package CB_Core.GL_UI.GL_Listener;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;

public class GL_Listener implements ApplicationListener, InputProcessor
{

	// # private Member

	/**
	 * Enthält alle GL_Views
	 */
	private MoveableList<GL_View_Base> mChilds = new MoveableList<GL_View_Base>();
	private static AtomicBoolean started = new AtomicBoolean(false);
	private GestureDetector gestureDetector;
	private static int frameRateIdle = 200;
	private int frameRateAction = 30;
	static boolean useNewInput = true;

	// # public static member
	public static SpriteBatch batch;
	public static OrthographicCamera camera;

	// # View´s
	// private MapViewForGl mapView;

	/**
	 * Constructor
	 */
	public GL_Listener(int initalWidth, int initialHeight)
	{

	}

	@Override
	public void create()
	{
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (batch == null) batch = new SpriteBatch();

		// if (Gdx.input.getInputProcessor() != this) Gdx.input.setInputProcessor(this);

		startTime = System.currentTimeMillis();
	}

	@Override
	public void pause()
	{

		onStop();
	}

	@Override
	public void resume()
	{

		onStart();
	}

	@Override
	public void dispose()
	{

		SpriteCache.destroyCache();

	}

	public void onStart()
	{

		started.set(true);
		startTimer(frameRateIdle);
	}

	public void onStop()
	{

		stopTimer();

		// if (ScreenLock.isShown) return;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			view.onStop();

		}
	}

	// # ImputProzessor Implamantations

	@Override
	public boolean keyDown(int arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchDown(x, y, pointer, button))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		return behandelt;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchDragged(x, y, pointer))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		return behandelt;

	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchMoved(x, y))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		return behandelt;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchUp(x, y, pointer, button))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		return behandelt;

	}

	@Override
	public void resize(int width, int height)
	{
		camera = new OrthographicCamera(width, height);

	}

	public static void startTimer(long delay)
	{
		if (timerValue == delay) return;
		stopTimer();

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
				// ((GLSurfaceView) MapViewGL.ViewGl).requestRender();

			}

		}, 0, delay);
		// ((GLSurfaceView) MapViewGL.ViewGl).setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public static long timerValue;
	long startTime;
	static Timer myTimer;

	private static void stopTimer()
	{
		if (myTimer != null)
		{
			myTimer.cancel();
			myTimer = null;
		}
		// ((GLSurfaceView) MapViewGL.ViewGl).setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void render()
	{

		if (!started.get()) return;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			batch.begin();
			GL_View_Base view = iterator.next();
			if (view.getVisibility() == GL_View_Base.VISIBLE) view.renderChilds(batch);

			batch.end();
		}

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
		// TODO Auto-generated method stub

	}

	public void InitializeMap()
	{
		// TODO Auto-generated method stub

	}

	public void add(GL_View_Base view)
	{
		mChilds.add(view);

	}

}

package de.cachebox_test.Map;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Types.Cache;
import CB_Core.Types.MoveableList;
import CB_Core.Types.Waypoint;
import android.opengl.GLSurfaceView;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import de.cachebox_test.main;
import de.cachebox_test.Views.MapViewGL;
import de.cachebox_test.Views.Forms.ScreenLock;

public class GL_Listner implements ApplicationListener, InputProcessor
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
	public static CameraController controller;
	public static OrthographicCamera camera;

	// # View´s
	private MapViewForGl mapView;

	/**
	 * Constructor
	 */
	public GL_Listner(int initalWidth, int initialHeight)
	{
		mapView = new MapViewForGl(initalWidth, initialHeight);
		mChilds.add(mapView);

		create();
	}

	@Override
	public void create()
	{
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (batch == null) batch = new SpriteBatch();
		if (useNewInput)
		{
			if (Gdx.input.getInputProcessor() != this) Gdx.input.setInputProcessor(this);
		}
		else
		{
			if (Gdx.input.getInputProcessor() != gestureDetector) Gdx.input.setInputProcessor(gestureDetector);
		}

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

	public static void onStart()
	{

		started.set(true);
		startTimer(frameRateIdle);
	}

	public void onStop()
	{

		stopTimer();

		if (ScreenLock.isShown) return;

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

		controller = new CameraController();
		gestureDetector = new GestureDetector(20, 0.5f, 1, 0.15f, controller);
	}

	class CameraController implements GestureListener
	{

		@Override
		public boolean touchDown(int x, int y, int pointer)
		{
			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.touchDown(x, y, pointer))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

		@Override
		public boolean tap(int x, int y, int count)
		{
			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.tap(x, y, count))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

		/**
		 * Wählt Cache Thread sicher an.
		 * 
		 * @param cache
		 */
		private void ThreadSaveSetSelectedWP(final Cache cache)
		{
			ThreadSaveSetSelectedWP(cache, null);
		}

		/**
		 * Wählt Cache und Waypoint Thread sicher an.
		 * 
		 * @param cache
		 * @param waypoint
		 */
		private void ThreadSaveSetSelectedWP(final Cache cache, final Waypoint waypoint)
		{
			Thread t = new Thread()
			{
				public void run()
				{
					main.mainActivity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (waypoint == null)
							{
								GlobalCore.SelectedCache(cache);
							}
							else
							{
								GlobalCore.SelectedWaypoint(cache, waypoint);
							}
						}
					});
				}
			};

			t.start();
		}

		@Override
		public boolean longPress(int x, int y)
		{

			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.longPress(x, y))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

		@Override
		public boolean fling(float velocityX, float velocityY)
		{
			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.fling(velocityX, velocityY))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

		@Override
		public boolean pan(int x, int y, int deltaX, int deltaY)
		{

			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.pan(x, y, deltaX, deltaY))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

		@Override
		public boolean zoom(float originalDistance, float currentDistance)
		{
			boolean behandelt = false;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view.zoom(originalDistance, currentDistance))
				{
					// schon behandelt
					behandelt = true;
					break;
				}
			}

			return behandelt;

		}

	}

	static void startTimer(long delay)
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
				((GLSurfaceView) MapViewGL.ViewGl).requestRender();

			}

		}, 0, delay);
		((GLSurfaceView) MapViewGL.ViewGl).setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
		((GLSurfaceView) MapViewGL.ViewGl).setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void render()
	{

		if (!started.get()) return;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

		for (Iterator<GL_View_Base> iterator = mChilds.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.getVisibility() == GL_View_Base.VISIBLE) view.renderChilds(batch);
		}

		batch.end();

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

}

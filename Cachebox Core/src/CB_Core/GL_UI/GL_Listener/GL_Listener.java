package CB_Core.GL_UI.GL_Listener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.Controls.MainView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class GL_Listener implements ApplicationListener, InputProcessor
{

	// # private Member

	MainView child;
	private static AtomicBoolean started = new AtomicBoolean(false);
	static boolean useNewInput = true;

	public static final int FRAME_RATE_IDLE = 200;
	public static final int FRAME_RATE_ACTION = 30;

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
		Logger.LogCat("GL_Listner => onStart");
		started.set(true);
		startTimer(FRAME_RATE_IDLE);
	}

	public void onStop()
	{
		Logger.LogCat("GL_Listner => onStop");
		stopTimer();
		child.onStop();
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

		behandelt = child.longClick(x, (int) child.getHeight() - y, pointer, button);

		return behandelt;
	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		boolean behandelt = false;

		behandelt = child.touchDown(x, (int) child.getHeight() - y, pointer, button);

		return behandelt;
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
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		// Events vom Listener nicht behandeln, wir haben unsere eigenes Eventhandling
		return false;
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

}

package CB_Utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GdxTestRunner extends BlockJUnit4ClassRunner implements ApplicationListener
{
	private Map<FrameworkMethod, RunNotifier> invokeInRender = new HashMap<FrameworkMethod, RunNotifier>();

	public GdxTestRunner(Class<?> klass) throws InitializationError
	{
		super(klass);

		LwjglApplicationConfiguration lwjglAppCfg = new LwjglApplicationConfiguration();
		DisplayMode dispMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

		lwjglAppCfg.setFromDisplayMode(dispMode);
		lwjglAppCfg.fullscreen = false;
		lwjglAppCfg.resizable = false;
		lwjglAppCfg.width = 200;
		lwjglAppCfg.height = 100;
		lwjglAppCfg.title = "GDX Test Runner";
		lwjglAppCfg.samples = 16;
		LwjglApplicationConfiguration.disableAudio = true;
		new LwjglApplication(this, lwjglAppCfg);
	}

	@Override
	public void create()
	{
		// System.out.print("create");
	}

	@Override
	public void resume()
	{
		// System.out.print("resume");
	}

	@Override
	public void render()
	{
		synchronized (invokeInRender)
		{
			for (Map.Entry<FrameworkMethod, RunNotifier> each : invokeInRender.entrySet())
			{
				super.runChild(each.getKey(), each.getValue());
			}
			invokeInRender.clear();
		}
	}

	@Override
	public void resize(int width, int height)
	{
		// System.out.print("resize");
	}

	@Override
	public void pause()
	{
		// System.out.print("pause");
	}

	@Override
	public void dispose()
	{
		// System.out.print("dispose");
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier)
	{
		synchronized (invokeInRender)
		{
			// add for invoking in render phase, where gl context is available
			invokeInRender.put(method, notifier);
		}
		// wait until that test was invoked
		waitUntilInvokedInRenderMethod();
	}

	/**
	    * 
	    */
	private void waitUntilInvokedInRenderMethod()
	{
		try
		{
			while (true)
			{
				Thread.sleep(10);
				synchronized (invokeInRender)
				{
					if (invokeInRender.isEmpty()) break;
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}
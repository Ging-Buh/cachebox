package CB_Core.GL_UI.interfaces;

/**
 * Extends Runnable um eine Ready Meldung
 * 
 * @author Longri
 */
public abstract class RunnableReadyHandler implements Runnable
{

	Runnable mRunnable;
	Thread mRunThread;
	boolean isCanceld = false;

	public RunnableReadyHandler(Runnable runnable)
	{
		mRunnable = runnable;
	}

	public abstract void RunnableReady(boolean canceld);

	public void run()
	{
		mRunThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				mRunnable.run();
				RunnableReady(isCanceld);
			}
		});
		mRunThread.start();

	}

	/*
	 * Bricht den Thread, in dem das Runnable läuft ab!
	 */
	public void Cancel()
	{
		isCanceld = true;
		mRunThread.interrupt();
	}

}

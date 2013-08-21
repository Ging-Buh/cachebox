package CB_UI_Base.GL_UI.interfaces;

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
	boolean isRunning = false;

	public RunnableReadyHandler(Runnable runnable)
	{
		mRunnable = runnable;
	}

	public abstract void RunnableReady(boolean canceld);

	public void run()
	{
		if (!isRunning)
		{
			isRunning = true;
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

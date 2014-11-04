package CB_Utils.Util;

import CB_Utils.Tag;

import com.badlogic.gdx.Gdx;

public abstract class LoopThread
{

	private boolean isAlive;
	private Thread loop;
	private Thread lifeCycle;
	private final long sleepTime;

	public LoopThread(long LoopBreakTime)
	{
		super();
		sleepTime = LoopBreakTime;
	}

	public boolean Alive()
	{
		return this.isAlive;
	}

	protected abstract void Loop();

	protected abstract boolean LoopBraek();

	public void start()
	{
		if (isAlive) return;
		if (loop == null)
		{
			loop = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					do
					{
						isAlive = true;
						if (!LoopBraek()) Loop();
						try
						{
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException e)
						{
							Gdx.app.error(Tag.TAG, "", e);
						}
					}
					while (isAlive);
				}
			});
		}
		try
		{
			loop.start();
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "", e);
		}

		if (lifeCycle == null) lifeCycleStart();
	}

	private void lifeCycleStart()
	{
		if (lifeCycle == null)
		{
			lifeCycle = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					do
					{
						if (!loop.isAlive())
						{
							stop();
							start();
						}
						try
						{
							Thread.sleep(700);
						}
						catch (InterruptedException e)
						{
							Gdx.app.error(Tag.TAG, "", e);
						}
					}
					while (isAlive);
				}
			});
			lifeCycle.setPriority(Thread.MIN_PRIORITY);
			lifeCycle.start();
		}
	}

	public void stop()
	{
		isAlive = false;
		loop = null;
		lifeCycle = null;
	}

}
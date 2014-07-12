package CB_Core.Api;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class API_ErrorEventHandlerList
{
	private static ArrayList<API_ErrorEventHandler> list = new ArrayList<API_ErrorEventHandler>();

	public static void addHandler(API_ErrorEventHandler handler)
	{
		synchronized (list)
		{
			if (!list.contains(handler)) list.add(handler);
		}
	}

	private static Thread threadCall;
	private static long lastCall;
	private static final long MIN_CALL_TIME = 5000;

	static void callInvalidApiKey()
	{
		if (lastCall != 0 && lastCall > System.currentTimeMillis() - MIN_CALL_TIME) return;
		lastCall = System.currentTimeMillis();

		if (threadCall == null) threadCall = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					synchronized (list)
					{
						for (API_ErrorEventHandler handler : list)
						{
							handler.InvalidAPI_Key();
						}
					}
				}

			}
		});

		// Zeit verzögerter Fehler aufruf
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				threadCall.run();
			}
		};
		timer.schedule(task, 700);

	}
}

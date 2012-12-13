package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GL_UI.GL_Listener.GL;

public class GpsStateChangeEventList
{
	public static ArrayList<GpsStateChangeEvent> list = new ArrayList<GpsStateChangeEvent>();

	public static void Add(GpsStateChangeEvent event)
	{
		list.add(event);
	}

	public static void Remove(GpsStateChangeEvent event)
	{
		list.remove(event);
	}

	private static int count = 0;

	public static long minEventTime = Long.MAX_VALUE;

	public static long lastTime = 0;

	public static long maxEventListTime = 0;
	private static long lastChanged = 0;

	public static void Call()
	{

		minEventTime = Math.min(minEventTime, System.currentTimeMillis() - lastTime);
		lastTime = System.currentTimeMillis();

		if (lastChanged != 0 && lastChanged > System.currentTimeMillis() - Config.settings.gpsUpdateTime.getValue())
		{
			return;
		}
		lastChanged = System.currentTimeMillis();

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					long thradStart = System.currentTimeMillis();
					count++;
					for (GpsStateChangeEvent event : list)
					{

						FireEvent(event);

					}
					if (count > 10) count = 0;

					// alle events abgearbeitet, jetzt kann die GL_View einmal Rendern
					GL.that.renderOnce(null);

					maxEventListTime = Math.max(maxEventListTime, System.currentTimeMillis() - thradStart);
				}

			}
		});

		thread.run();

	}

	private static void FireEvent(GpsStateChangeEvent event)
	{
		event.GpsStateChanged();
		// Log.d("CACHEBOX", "GPS State Change called " + event.toString());
	}

}

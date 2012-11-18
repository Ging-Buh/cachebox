package CB_Core.Events;

import java.util.ArrayList;

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

	public static void Call()
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					count++;
					for (GpsStateChangeEvent event : list)
					{

						FireEvent(event);

					}
					if (count > 10) count = 0;
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

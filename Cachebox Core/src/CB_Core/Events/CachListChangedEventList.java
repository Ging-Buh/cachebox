package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.Energy;

public class CachListChangedEventList
{
	public static ArrayList<CacheListChangedEventListner> list = new ArrayList<CacheListChangedEventListner>();

	public static void Add(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	public static void Call()
	{

		if (Energy.DisplayOff()) return;

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					for (CacheListChangedEventListner event : list)
					{
						event.CacheListChangedEvent();
					}
				}

			}
		});

		thread.run();

	}

}

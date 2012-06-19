package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class SelectedCacheEventList
{
	public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();

	public static void Add(SelectedCacheEvent event)
	{
		synchronized (list)
		{
			list.add(event);
		}
	}

	public static void Remove(SelectedCacheEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	public static void Call(final Cache cache, final Waypoint waypoint)
	{
		// Aufruf aus in einen neuen Thread packen
		if (cache != null)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					synchronized (list)
					{
						for (SelectedCacheEvent event : list)
						{
							event.SelectedCacheChanged(cache, waypoint);
						}

						// save last selected Cache in to DB
						Config.settings.LastSelectedCache.setValue(cache.GcCode);
						Config.AcceptChanges();
					}
				}
			});

			thread.run();
		}

	}
}

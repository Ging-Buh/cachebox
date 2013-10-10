package CB_UI.Events;

import java.util.ArrayList;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalLocationReceiver;

public class SelectedCacheEventList
{
	public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();

	public static void Add(SelectedCacheEvent event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(SelectedCacheEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	private static Cache lastSelectedCache;
	private static Waypoint lastSelectedWayPoint;

	public static void Call(final Cache cache, final Waypoint waypoint)
	{
		boolean change = true;

		if (lastSelectedCache != null)
		{
			if (lastSelectedCache == cache)
			{
				if (lastSelectedWayPoint != null)
				{
					if (lastSelectedWayPoint == waypoint) change = false;
				}
				else
				{
					if (waypoint == null) change = false;
				}
			}
		}

		if (change) GlobalLocationReceiver.resetApprouch();

		if (selectChangeThread != null)
		{
			if (selectChangeThread.getState() != Thread.State.TERMINATED) return;
			else
				selectChangeThread = null;
		}

		if (cache != null)
		{
			selectChangeThread = new Thread(new Runnable()
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
						// nur beim Verlassen des Programms und DB-Wechsel
						// Config.settings.LastSelectedCache.setValue(cache.GcCode);
						// Config.AcceptChanges();
					}
				}
			});

			selectChangeThread.start();
		}

	}

	static Thread selectChangeThread;
}
